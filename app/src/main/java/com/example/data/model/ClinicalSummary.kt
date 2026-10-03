package com.example.data.model

data class ClinicalSummary(
    var chiefComplaint: String = "",
    var historyOfPresentIllness: String = "",
    var pastMedicalHistory: String = "",
    var drugAndAllergyHistory: String = "",
    var familyHistory: String = "",
    var reviewOfSystems: String = "",
    var priorInvestigations: String = "",
    var triagePriority: String = "Routine", // Urgent, Moderate, Routine
    var triageReason: String = "",
    var recommendedDepartment: String = "General Medicine",
    var tokenNumber: String = "MK-104",
    var estimatedWaitMinutes: Int = 15
)
