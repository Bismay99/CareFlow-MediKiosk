package com.example.data.model

data class MedicationItem(
    var name: String = "",
    var dosage: String = "",
    var frequency: String = "",
    var instructions: String = ""
)

data class LabValueItem(
    var testName: String = "",
    var value: String = "",
    var unit: String = "",
    var referenceRange: String = "",
    var status: String = "Normal" // Normal, High, Low
)

data class ExtractedDocument(
    var documentType: String = "Prescription & Lab Report",
    var diagnosis: String = "",
    var medications: MutableList<MedicationItem> = mutableListOf(),
    var labValues: MutableList<LabValueItem> = mutableListOf(),
    var rawNotes: String = ""
)
