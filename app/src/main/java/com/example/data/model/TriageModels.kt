package com.example.data.model

enum class TriageUrgency(
    val title: String,
    val recommendedTimeframe: String,
    val hexColorString: String,
    val requiresEmergencyEscalation: Boolean
) {
    ROUTINE(
        "Routine Care",
        "Within 24–48 hours or scheduled OPD visit",
        "#10B981", // Emerald green
        false
    ),
    SOON(
        "Priority Evaluation",
        "Within 4–12 hours at urgent care or OPD",
        "#F59E0B", // Amber
        false
    ),
    URGENT(
        "Urgent Medical Attention",
        "Within 1–2 hours at emergency / urgent care",
        "#EF4444", // Red-orange
        false
    ),
    EMERGENCY(
        "Immediate Critical Emergency",
        "Immediate emergency medical evaluation (Dial 108 / 112 / 911)",
        "#DC2626", // Deep crimson
        true
    )
}

data class AiTriageResult(
    val urgency: TriageUrgency,
    val clinicalReasoning: String,
    val primarySymptomSummary: String,
    val concerningVitalsIdentified: List<String> = emptyList(),
    val recommendedNextSteps: List<String> = emptyList(),
    val confidenceLabel: String = "High Rule-Governed Confidence",
    val safetyNotice: String = "AI-assisted urgency assessment. Not a medical diagnosis. Clinical judgment takes precedence.",
    val evaluatedAtMillis: Long = System.currentTimeMillis()
)

enum class ClinicalVerificationStatus(val label: String, val badgeColor: String) {
    AI_EXTRACTED_PENDING("AI Extracted — Pending Verification", "#6B7280"),
    DOCTOR_VERIFIED("Doctor Verified", "#0D9488"),
    DOCTOR_EDITED("Doctor Edited & Verified", "#2563EB"),
    DOCTOR_REJECTED("Doctor Rejected", "#DC2626")
}

data class VerifiableClinicalEntity(
    val id: String = "ent-${System.currentTimeMillis()}-${(100..999).random()}",
    val fieldCategory: String, // Medication, Lab Value, Diagnosis, Symptom, Advice
    val fieldName: String,
    val extractedValue: String,
    var verifiedValue: String = extractedValue,
    var status: ClinicalVerificationStatus = ClinicalVerificationStatus.AI_EXTRACTED_PENDING,
    val confidenceScore: Float = 0.95f,
    val sourceDocumentName: String = "Prescription Scan",
    val sourceLocation: String = "Page 1, Rx Table",
    var doctorRemarks: String = "",
    var reviewedByDoctorName: String? = null,
    var reviewedAtTimestamp: String? = null
)
