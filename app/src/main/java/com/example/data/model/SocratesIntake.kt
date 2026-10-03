package com.example.data.model

data class SocratesIntake(
    var chiefComplaint: String = "",
    var site: String = "",
    var onset: String = "",
    var character: String = "",
    var radiation: String = "",
    var associations: String = "",
    var timing: String = "",
    var exacerbatingRelieving: String = "",
    var severity: String = "",
    var pastMedicalHistory: String = "",
    var currentMedications: String = "",
    var allergies: String = "",
    var familyHistory: String = ""
) {
    fun getCompletedFieldsCount(): Int {
        var count = 0
        if (chiefComplaint.isNotBlank()) count++
        if (site.isNotBlank()) count++
        if (onset.isNotBlank()) count++
        if (character.isNotBlank()) count++
        if (radiation.isNotBlank()) count++
        if (associations.isNotBlank()) count++
        if (timing.isNotBlank()) count++
        if (exacerbatingRelieving.isNotBlank()) count++
        if (severity.isNotBlank()) count++
        if (pastMedicalHistory.isNotBlank()) count++
        if (currentMedications.isNotBlank() || allergies.isNotBlank()) count++
        if (familyHistory.isNotBlank()) count++
        return count
    }

    val totalKeyStages: Int = 11
}
