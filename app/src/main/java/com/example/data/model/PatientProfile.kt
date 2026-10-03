package com.example.data.model

data class PatientProfile(
    val name: String = "",
    val age: String = "",
    val gender: String = "Male",
    val isNewPatient: Boolean = false,
    val abhaId: String = "",
    val consentGiven: Boolean = false
) {
    val isValid: Boolean
        get() = name.isNotBlank() && age.isNotBlank() && consentGiven && (isNewPatient || abhaId.isNotBlank())
}
