package com.example.data.api

import com.example.data.model.*
import java.util.Locale

object ClinicalTriageEngine {

    /**
     * Evaluates symptoms, patient context, and reported vitals against clinical safety rules.
     * Guaranteed non-diagnostic: outputs an urgency assessment and recommended care timeframe.
     */
    fun evaluateTriage(
        symptomsText: String,
        duration: String = "",
        severityScale1To10: Int = 5,
        reportedVitals: Map<String, String> = emptyMap(),
        intake: SocratesIntake? = null
    ): AiTriageResult {
        val normalizedText = (symptomsText + " " + (intake?.chiefComplaint ?: "") + " " + (intake?.associations ?: "")).lowercase(Locale.ROOT)
        val concerningVitals = mutableListOf<String>()

        // 1. Vital Sign Red-Flags Check
        val spo2Str = reportedVitals["spo2"] ?: reportedVitals["SpO2"] ?: ""
        val spo2Val = spo2Str.filter { it.isDigit() }.toIntOrNull()
        if (spo2Val != null && spo2Val < 92) {
            concerningVitals.add("Severe Hypoxemia: SpO2 $spo2Val% is below critical threshold (<92%)")
        }

        val hrStr = reportedVitals["heartRate"] ?: reportedVitals["Heart Rate"] ?: ""
        val hrVal = hrStr.filter { it.isDigit() }.toIntOrNull()
        if (hrVal != null && (hrVal > 130 || hrVal < 42)) {
            concerningVitals.add("Cardiac Rate Anomaly: Pulse $hrVal bpm (critical tachycardia/bradycardia)")
        }

        val bpStr = reportedVitals["bp"] ?: reportedVitals["Blood Pressure"] ?: ""
        val sysBp = bpStr.split("/").firstOrNull()?.filter { it.isDigit() }?.toIntOrNull()
        if (sysBp != null && (sysBp > 180 || sysBp < 85)) {
            concerningVitals.add("Critical Blood Pressure: Systolic $sysBp mmHg requires emergent stabilization")
        }

        // 2. Critical Emergency Symptom Checks
        val isChestPainEmergency = normalizedText.contains("chest pain") ||
                normalizedText.contains("pressure in chest") ||
                normalizedText.contains("crushing pain") ||
                (normalizedText.contains("chest") && (normalizedText.contains("radiation") || normalizedText.contains("arm") || normalizedText.contains("sweat")))

        val isStrokeEmergency = normalizedText.contains("face droop") ||
                normalizedText.contains("facial droop") ||
                normalizedText.contains("arm weakness") ||
                normalizedText.contains("slurred speech") ||
                normalizedText.contains("cannot speak") ||
                normalizedText.contains("thunderclap headache") ||
                normalizedText.contains("sudden numbness")

        val isSevereRespiratoryEmergency = normalizedText.contains("cannot breathe") ||
                normalizedText.contains("struggling to breathe") ||
                normalizedText.contains("blue lips") ||
                normalizedText.contains("gasping") ||
                normalizedText.contains("severe shortness of breath")

        val isAnaphylaxisEmergency = (normalizedText.contains("swelling") && (normalizedText.contains("throat") || normalizedText.contains("tongue") || normalizedText.contains("lips"))) ||
                (normalizedText.contains("allergic reaction") && normalizedText.contains("breath"))

        // EMERGENCY DECISION
        if (isChestPainEmergency || isStrokeEmergency || isSevereRespiratoryEmergency || isAnaphylaxisEmergency || concerningVitals.isNotEmpty() || severityScale1To10 >= 9) {
            val reason = when {
                isChestPainEmergency -> "Reported acute chest discomfort with potential ischemic or cardiopulmonary indicators."
                isStrokeEmergency -> "Reported acute neurological symptoms (FAST indicators) requiring immediate stroke protocol activation."
                isSevereRespiratoryEmergency -> "Reported severe acute dyspnea with respiratory compromise."
                isAnaphylaxisEmergency -> "Signs of acute airway compromise or systemic hypersensitivity."
                concerningVitals.isNotEmpty() -> "Physiological vital measurements indicate critical acute instability."
                else -> "Severe pain score ($severityScale1To10/10) with systemic distress reported."
            }

            return AiTriageResult(
                urgency = TriageUrgency.EMERGENCY,
                clinicalReasoning = reason,
                primarySymptomSummary = symptomsText.ifBlank { "Acute severe symptomatic presentation" },
                concerningVitalsIdentified = concerningVitals,
                recommendedNextSteps = listOf(
                    "Activate Emergency SOS or dial emergency medical services (108 / 112 / 911) immediately",
                    "Do NOT drive yourself to the hospital; await medical transport",
                    "Rest in a comfortable, upright or semi-reclined position",
                    "Notify staff or family members in your immediate vicinity"
                ),
                confidenceLabel = "Rule-Governed Safety Trigger",
                evaluatedAtMillis = System.currentTimeMillis()
            )
        }

        // 3. URGENT Check (Within 1-2 hours)
        val isUrgentPresentation = severityScale1To10 >= 7 ||
                normalizedText.contains("high fever") ||
                normalizedText.contains("vomiting blood") ||
                normalizedText.contains("severe abdominal pain") ||
                normalizedText.contains("fainting") ||
                normalizedText.contains("syncope") ||
                (normalizedText.contains("fever") && normalizedText.contains("stiff neck"))

        if (isUrgentPresentation) {
            return AiTriageResult(
                urgency = TriageUrgency.URGENT,
                clinicalReasoning = "Significant acute symptoms with elevated severity score ($severityScale1To10/10) requiring expedited clinical assessment.",
                primarySymptomSummary = symptomsText,
                concerningVitalsIdentified = concerningVitals,
                recommendedNextSteps = listOf(
                    "Report directly to an Urgent Care or Emergency OPD triage desk",
                    "Bring active prescription containers and prior investigation records",
                    "Avoid eating or drinking until examined if severe abdominal pain is present"
                ),
                confidenceLabel = "High Priority Clinical Threshold",
                evaluatedAtMillis = System.currentTimeMillis()
            )
        }

        // 4. SOON Check (Within 4-12 hours)
        val isSoonPresentation = severityScale1To10 >= 4 ||
                normalizedText.contains("fever") ||
                normalizedText.contains("moderate pain") ||
                normalizedText.contains("persistent cough") ||
                normalizedText.contains("urinary burning") ||
                duration.contains("3 days") || duration.contains("4 days") || duration.contains("5 days")

        if (isSoonPresentation) {
            return AiTriageResult(
                urgency = TriageUrgency.SOON,
                clinicalReasoning = "Subacute symptomatic episode without acute physiological instability. Evaluation within 4–12 hours is recommended.",
                primarySymptomSummary = symptomsText,
                concerningVitalsIdentified = concerningVitals,
                recommendedNextSteps = listOf(
                    "Schedule a same-day OPD or Telemedicine consultation",
                    "Maintain hydration and log temperature readings",
                    "Seek immediate re-evaluation if fever spikes or pain rapidly worsens"
                ),
                confidenceLabel = "Standard Clinical Intake Assessment",
                evaluatedAtMillis = System.currentTimeMillis()
            )
        }

        // 5. ROUTINE
        return AiTriageResult(
            urgency = TriageUrgency.ROUTINE,
            clinicalReasoning = "Mild, non-emergent constitutional presentation suitable for standard outpatient or teleconsultation review.",
            primarySymptomSummary = symptomsText.ifBlank { "Routine health consultation request" },
            concerningVitalsIdentified = concerningVitals,
            recommendedNextSteps = listOf(
                "Book a scheduled teleconsultation or in-person visit with your physician",
                "Continue standard restorative rest and hydration",
                "Prepare prior medical records and current medication list for the doctor"
            ),
            confidenceLabel = "Standard Outpatient Care Guideline",
            evaluatedAtMillis = System.currentTimeMillis()
        )
    }

    /**
     * Synthesizes an evidence-linked intake summary formatted strictly as a provisional clinician draft.
     */
    fun formatClinicianIntakeDraft(
        patient: PatientProfile,
        symptoms: String,
        intake: SocratesIntake,
        triageResult: AiTriageResult,
        vitals: PatientHealthDashboardSummary?
    ): String {
        return buildString {
            appendLine("=== AI-ASSISTED INTAKE SUMMARY — DRAFT FOR CLINICIAN REVIEW ===")
            appendLine("Patient: ${patient.name}, ${patient.age} y/o, ${patient.gender} | ABHA: ${patient.abhaId}")
            appendLine("Triage Urgency: ${triageResult.urgency.title} [${triageResult.confidenceLabel}]")
            appendLine("Clinical Rationale: ${triageResult.clinicalReasoning}")
            appendLine()
            appendLine("--- SUBJECTIVE HISTORY (SOCRATES MAPPING) ---")
            appendLine("Chief Complaint: ${intake.chiefComplaint.ifBlank { symptoms }}")
            if (intake.site.isNotBlank()) appendLine("Site: ${intake.site}")
            if (intake.onset.isNotBlank()) appendLine("Onset: ${intake.onset}")
            if (intake.character.isNotBlank()) appendLine("Character: ${intake.character}")
            if (intake.radiation.isNotBlank()) appendLine("Radiation: ${intake.radiation}")
            if (intake.associations.isNotBlank()) appendLine("Associated Symptoms: ${intake.associations}")
            if (intake.timing.isNotBlank()) appendLine("Timing / Course: ${intake.timing}")
            if (intake.exacerbatingRelieving.isNotBlank()) appendLine("Exacerbating/Relieving: ${intake.exacerbatingRelieving}")
            if (intake.severity.isNotBlank()) appendLine("Severity: ${intake.severity} / 10")
            appendLine()
            appendLine("--- REPORTED BACKGROUND & ALLERGIES ---")
            appendLine("Known Allergies: ${patient.allergies.joinToString().ifBlank { "None Reported" }}")
            appendLine("Chronic Conditions: ${patient.chronicConditions.joinToString().ifBlank { "None Reported" }}")
            appendLine()
            if (vitals != null) {
                appendLine("--- RECENT BIOMETRIC OBSERVATIONS (WEARABLE / DEMO DATA) ---")
                appendLine("Heart Rate: ${vitals.heartRateLatest.displayValueString}")
                appendLine("SpO2: ${vitals.spO2Latest.displayValueString}")
                appendLine("Blood Pressure: ${vitals.bloodPressureLatest.displayValueString}")
                appendLine("Temperature: ${vitals.temperatureLatest.displayValueString}")
                appendLine("Status: ${vitals.dataSourceBadge}")
            }
            appendLine()
            appendLine("--- PROPOSED CLINICAL FOCUS QUESTIONS FOR DOCTOR ---")
            appendLine("1. Corroborate onset timeline against physical auscultation / abdominal examination.")
            appendLine("2. Verify medication adherence and check for drug-drug interactions with newly prescribed agents.")
            appendLine()
            appendLine("NOTICE: Generated as preliminary triage intake draft. Diagnosis and therapeutic plan remain the sole responsibility of the attending physician.")
        }
    }
}
