package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * When to eat medicine timing directives
 */
enum class MedicineFoodTiming(val label: String, val detailedInstruction: String) {
    BEFORE_BREAKFAST_EMPTY_STOMACH(
        "Before Breakfast (Empty Stomach)",
        "Take first thing in the morning with a full glass of water, at least 30-45 minutes before breakfast"
    ),
    AFTER_BREAKFAST(
        "After Breakfast",
        "Take within 15-30 minutes after morning breakfast"
    ),
    BEFORE_LUNCH(
        "Before Lunch",
        "Take 30 minutes before afternoon lunch"
    ),
    AFTER_LUNCH(
        "After Lunch",
        "Take within 15-30 minutes after afternoon lunch"
    ),
    BEFORE_DINNER(
        "Before Dinner",
        "Take 30 minutes before evening dinner"
    ),
    AFTER_DINNER(
        "After Dinner",
        "Take within 15-30 minutes after evening dinner"
    ),
    WITH_MEALS(
        "With Meals",
        "Take during the meal with your first or middle bites of food"
    ),
    BEDTIME(
        "At Bedtime (HS)",
        "Take 15-30 minutes before sleeping at night"
    ),
    AS_NEEDED_PRN(
        "As Needed (SOS / PRN)",
        "Take only when experiencing acute symptoms, maintain minimum 6 hours between doses"
    )
}

/**
 * Frequency of medicine intake
 */
enum class MedicineFrequency(val code: String, val label: String, val timesPerDay: String) {
    ONCE_DAILY_MORNING("OD (Morning)", "Once Daily in Morning", "1 - 0 - 0"),
    ONCE_DAILY_NIGHT("OD (Night / HS)", "Once Daily at Bedtime", "0 - 0 - 1"),
    TWICE_DAILY("BD / BID", "Twice Daily (Morning & Night)", "1 - 0 - 1"),
    THRICE_DAILY("TDS / TID", "Three Times Daily", "1 - 1 - 1"),
    FOUR_TIMES_DAILY("QID", "Four Times Daily", "1 - 1 - 1 - 1"),
    EVERY_ALTERNATE_DAY("QOD", "Every Alternate Day", "Alt Days"),
    ONCE_WEEKLY("QW", "Once a Week", "Weekly"),
    AS_NEEDED("PRN / SOS", "As Needed for Pain/Fever", "SOS")
}

/**
 * Detailed prescription item with medicine name, doctor, timing, food guidelines
 */
data class PrescriptionMedicine(
    val id: String = "med-${System.currentTimeMillis()}-${(100..999).random()}",
    val medicineName: String,
    val genericName: String = "",
    val dosage: String, // e.g. "40 mg", "500 mg", "5 ml"
    val form: String = "Tablet", // Tablet, Capsule, Syrup, Injection, Inhaler, Drops
    val frequency: MedicineFrequency = MedicineFrequency.ONCE_DAILY_MORNING,
    val foodTiming: MedicineFoodTiming = MedicineFoodTiming.AFTER_BREAKFAST,
    val duration: String = "30 Days", // e.g. "5 Days", "14 Days", "30 Days", "Ongoing"
    val specialInstructions: String = "", // e.g. "Avoid alcohol, monitor BP weekly, do not skip doses"
    val isMorning: Boolean = true,
    val isAfternoon: Boolean = false,
    val isEvening: Boolean = false,
    val isNight: Boolean = false,
    val status: String = "Active" // Active, Completed, Discontinued
)

/**
 * Vital signs captured during consultation
 */
data class ConsultationVitals(
    val bloodPressure: String = "120/80 mmHg",
    val pulseRate: String = "72 bpm",
    val oxygenSaturationSpO2: String = "98%",
    val temperature: String = "98.4 °F",
    val bloodSugarFasting: String = "",
    val weightKg: String = "68 kg",
    val bmi: String = "23.5"
)

/**
 * Consultation history entry for a patient
 */
data class ConsultationHistoryEntry(
    val id: String = "consult-${System.currentTimeMillis()}-${(100..999).random()}",
    val patientId: String,
    val patientName: String,
    val doctorName: String,
    val doctorSpecialty: String,
    val department: String,
    val consultationDateTime: String, // e.g. "06 Sep 2026, 10:30 AM"
    val roomNumber: String = "Room 104",
    val chiefComplaint: String,
    val diagnosis: String,
    val clinicalNotes: String = "",
    val vitals: ConsultationVitals = ConsultationVitals(),
    val medicines: List<PrescriptionMedicine> = emptyList(),
    val labInvestigations: List<String> = emptyList(),
    val followUpDate: String = "In 2 Weeks",
    val doctorAdvice: String = "",
    val isRecordedByAdmin: Boolean = true
)

/**
 * Master Patient History Record containing all consultation events and prescriptions
 */
data class PatientMasterHistory(
    val patientId: String,
    val patientName: String,
    val age: String,
    val gender: String,
    val abhaId: String,
    val phone: String,
    val bloodGroup: String = "O+",
    val allergies: List<String> = listOf("None Known"),
    val chronicConditions: List<String> = emptyList(),
    val emergencyContact: String = "+91 98201 44521",
    val consultations: List<ConsultationHistoryEntry> = emptyList()
) {
    val totalConsultations: Int get() = consultations.size
    val activeMedicines: List<PrescriptionMedicine>
        get() = consultations.flatMap { it.medicines }.filter { it.status == "Active" }.distinctBy { it.medicineName }
    val latestConsultation: ConsultationHistoryEntry?
        get() = consultations.firstOrNull()
}
