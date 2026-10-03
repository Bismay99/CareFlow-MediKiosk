package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class DoctorVerificationRepository {

    private val _pendingEntities = MutableStateFlow<List<VerifiableClinicalEntity>>(createInitialPendingEntities())
    val pendingEntities: StateFlow<List<VerifiableClinicalEntity>> = _pendingEntities.asStateFlow()

    private val _verifiedAuditLog = MutableStateFlow<List<String>>(
        listOf(
            "System Audit initialized. 3 prescription fields parsed with high OCR confidence (97.4%).",
            "Awaiting clinician sign-off before committing to patient master EHR."
        )
    )
    val verifiedAuditLog: StateFlow<List<String>> = _verifiedAuditLog.asStateFlow()

    fun acceptField(entityId: String, doctorName: String) {
        val now = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        _pendingEntities.value = _pendingEntities.value.map { entity ->
            if (entity.id == entityId) {
                entity.copy(
                    status = ClinicalVerificationStatus.DOCTOR_VERIFIED,
                    reviewedByDoctorName = doctorName,
                    reviewedAtTimestamp = now
                )
            } else entity
        }
        addAuditEntry("Field '${_pendingEntities.value.find { it.id == entityId }?.fieldName}' ACCEPTED by Dr. $doctorName at $now")
    }

    fun editField(entityId: String, newValue: String, remarks: String, doctorName: String) {
        val now = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        _pendingEntities.value = _pendingEntities.value.map { entity ->
            if (entity.id == entityId) {
                entity.copy(
                    verifiedValue = newValue,
                    status = ClinicalVerificationStatus.DOCTOR_EDITED,
                    doctorRemarks = remarks,
                    reviewedByDoctorName = doctorName,
                    reviewedAtTimestamp = now
                )
            } else entity
        }
        addAuditEntry("Field '${_pendingEntities.value.find { it.id == entityId }?.fieldName}' EDITED to '$newValue' (Remarks: $remarks) by Dr. $doctorName at $now")
    }

    fun rejectField(entityId: String, reason: String, doctorName: String) {
        val now = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        _pendingEntities.value = _pendingEntities.value.map { entity ->
            if (entity.id == entityId) {
                entity.copy(
                    status = ClinicalVerificationStatus.DOCTOR_REJECTED,
                    doctorRemarks = reason,
                    reviewedByDoctorName = doctorName,
                    reviewedAtTimestamp = now
                )
            } else entity
        }
        addAuditEntry("Field '${_pendingEntities.value.find { it.id == entityId }?.fieldName}' REJECTED (Reason: $reason) by Dr. $doctorName at $now")
    }

    fun acceptAllPending(doctorName: String) {
        val now = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        _pendingEntities.value = _pendingEntities.value.map { entity ->
            if (entity.status == ClinicalVerificationStatus.AI_EXTRACTED_PENDING) {
                entity.copy(
                    status = ClinicalVerificationStatus.DOCTOR_VERIFIED,
                    reviewedByDoctorName = doctorName,
                    reviewedAtTimestamp = now
                )
            } else entity
        }
        addAuditEntry("All pending clinical entities verified and signed off by Dr. $doctorName at $now")
    }

    fun ingestExtractedDocument(doc: ExtractedDocument, sourceFileName: String) {
        val newEntities = mutableListOf<VerifiableClinicalEntity>()

        if (doc.diagnosis.isNotBlank()) {
            newEntities.add(
                VerifiableClinicalEntity(
                    fieldCategory = "Preliminary Impression",
                    fieldName = "Extracted Diagnosis",
                    extractedValue = doc.diagnosis,
                    sourceDocumentName = sourceFileName,
                    sourceLocation = "Header / Clinical Notes"
                )
            )
        }

        doc.medications.forEach { med ->
            newEntities.add(
                VerifiableClinicalEntity(
                    fieldCategory = "Medication",
                    fieldName = med.name,
                    extractedValue = "${med.dosage} | ${med.frequency} | ${med.instructions}",
                    sourceDocumentName = sourceFileName,
                    sourceLocation = "Rx Table"
                )
            )
        }

        doc.labValues.forEach { lab ->
            newEntities.add(
                VerifiableClinicalEntity(
                    fieldCategory = "Lab Investigation",
                    fieldName = lab.testName,
                    extractedValue = "${lab.value} ${lab.unit} (Ref: ${lab.referenceRange})",
                    sourceDocumentName = sourceFileName,
                    sourceLocation = "Lab Findings"
                )
            )
        }

        _pendingEntities.value = newEntities + _pendingEntities.value
        addAuditEntry("Ingested ${newEntities.size} extracted clinical fields from '$sourceFileName' awaiting verification.")
    }

    private fun addAuditEntry(entry: String) {
        _verifiedAuditLog.value = listOf(entry) + _verifiedAuditLog.value
    }

    private fun createInitialPendingEntities(): List<VerifiableClinicalEntity> {
        return listOf(
            VerifiableClinicalEntity(
                id = "ent-rx-1",
                fieldCategory = "Prescription Medicine",
                fieldName = "Paracetamol 500 mg",
                extractedValue = "1 tablet | Three times daily after food | 3 days",
                confidenceScore = 0.98f,
                sourceDocumentName = "PS47_test_doctor_prescription.pdf",
                sourceLocation = "Page 1, Row 1 (y: 61pt)"
            ),
            VerifiableClinicalEntity(
                id = "ent-rx-2",
                fieldCategory = "Prescription Medicine",
                fieldName = "Cetirizine 10 mg",
                extractedValue = "1 tablet | Once at night | 5 days",
                confidenceScore = 0.97f,
                sourceDocumentName = "PS47_test_doctor_prescription.pdf",
                sourceLocation = "Page 1, Row 2 (y: 35pt)"
            ),
            VerifiableClinicalEntity(
                id = "ent-rx-3",
                fieldCategory = "Prescription Medicine",
                fieldName = "Pantoprazole 40 mg",
                extractedValue = "1 tablet | Once daily before breakfast | 5 days",
                confidenceScore = 0.96f,
                sourceDocumentName = "PS47_test_doctor_prescription.pdf",
                sourceLocation = "Page 1, Row 3 (y: 9pt)"
            ),
            VerifiableClinicalEntity(
                id = "ent-cc-1",
                fieldCategory = "Chief Complaint",
                fieldName = "Reported Symptoms",
                extractedValue = "Fever, headache and body ache for 3 days",
                confidenceScore = 0.99f,
                sourceDocumentName = "PS47_test_doctor_prescription.pdf",
                sourceLocation = "Page 1, Chief Complaint Section"
            )
        )
    }
}
