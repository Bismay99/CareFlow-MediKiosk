package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PatientHistoryRepository {

    private val _patientHistories = MutableStateFlow<List<PatientMasterHistory>>(createInitialPatientHistories())
    val patientHistories: StateFlow<List<PatientMasterHistory>> = _patientHistories.asStateFlow()

    fun recordConsultation(
        patientId: String,
        patientName: String,
        age: String,
        gender: String,
        abhaId: String,
        phone: String,
        doctorName: String,
        doctorSpecialty: String,
        department: String,
        consultationDateTime: String,
        roomNumber: String,
        chiefComplaint: String,
        diagnosis: String,
        clinicalNotes: String,
        vitals: ConsultationVitals,
        medicines: List<PrescriptionMedicine>,
        labInvestigations: List<String>,
        followUpDate: String,
        doctorAdvice: String,
        bloodGroup: String = "B+",
        allergies: List<String> = listOf("None Known"),
        chronicConditions: List<String> = emptyList()
    ): ConsultationHistoryEntry {
        val entry = ConsultationHistoryEntry(
            patientId = patientId,
            patientName = patientName,
            doctorName = doctorName,
            doctorSpecialty = doctorSpecialty,
            department = department,
            consultationDateTime = consultationDateTime.ifBlank {
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
            },
            roomNumber = roomNumber,
            chiefComplaint = chiefComplaint,
            diagnosis = diagnosis,
            clinicalNotes = clinicalNotes,
            vitals = vitals,
            medicines = medicines,
            labInvestigations = labInvestigations,
            followUpDate = followUpDate,
            doctorAdvice = doctorAdvice,
            isRecordedByAdmin = true
        )

        val currentList = _patientHistories.value.toMutableList()
        val existingIndex = currentList.indexOfFirst { it.patientId == patientId || it.abhaId == abhaId || it.patientName.equals(patientName, ignoreCase = true) }

        if (existingIndex >= 0) {
            val existingPatient = currentList[existingIndex]
            val updatedConsultations = listOf(entry) + existingPatient.consultations
            val updatedConditions = (existingPatient.chronicConditions + chronicConditions).distinct()
            currentList[existingIndex] = existingPatient.copy(
                consultations = updatedConsultations,
                chronicConditions = updatedConditions,
                bloodGroup = if (existingPatient.bloodGroup.isNotBlank()) existingPatient.bloodGroup else bloodGroup
            )
        } else {
            val newMaster = PatientMasterHistory(
                patientId = patientId.ifBlank { "pt-${System.currentTimeMillis()}" },
                patientName = patientName,
                age = age,
                gender = gender,
                abhaId = abhaId,
                phone = phone,
                bloodGroup = bloodGroup,
                allergies = allergies,
                chronicConditions = chronicConditions,
                consultations = listOf(entry)
            )
            currentList.add(0, newMaster)
        }

        _patientHistories.value = currentList
        return entry
    }

    fun addMedicineToConsultation(consultationId: String, medicine: PrescriptionMedicine) {
        val updated = _patientHistories.value.map { patient ->
            val updatedConsultations = patient.consultations.map { consult ->
                if (consult.id == consultationId) {
                    consult.copy(medicines = consult.medicines + medicine)
                } else consult
            }
            patient.copy(consultations = updatedConsultations)
        }
        _patientHistories.value = updated
    }

    fun updateMedicineStatus(patientId: String, medicineId: String, newStatus: String) {
        val updated = _patientHistories.value.map { patient ->
            if (patient.patientId == patientId) {
                val updatedConsultations = patient.consultations.map { consult ->
                    val updatedMeds = consult.medicines.map { med ->
                        if (med.id == medicineId) med.copy(status = newStatus) else med
                    }
                    consult.copy(medicines = updatedMeds)
                }
                patient.copy(consultations = updatedConsultations)
            } else patient
        }
        _patientHistories.value = updated
    }

    private fun createInitialPatientHistories(): List<PatientMasterHistory> {
        return listOf(
            // 1. Rajesh Sharma (54, Male) - Hypertension & Dyslipidemia
            PatientMasterHistory(
                patientId = "patient-001",
                patientName = "Rajesh Sharma",
                age = "54",
                gender = "Male",
                abhaId = "14-2951-8402-1920",
                phone = "+91 98201 44521",
                bloodGroup = "B+",
                allergies = listOf("Penicillin (Mild rash)"),
                chronicConditions = listOf("Essential Hypertension", "Hyperlipidemia", "Borderline Pre-Diabetes"),
                consultations = listOf(
                    ConsultationHistoryEntry(
                        id = "consult-001",
                        patientId = "patient-001",
                        patientName = "Rajesh Sharma",
                        doctorName = "Dr. Rajesh Mehta, MD",
                        doctorSpecialty = "Internal Medicine & Hypertension",
                        department = "General Medicine",
                        consultationDateTime = "02 Sep 2026, 11:15 AM",
                        roomNumber = "Room 104 - 1st Floor",
                        chiefComplaint = "Occasional morning occipital headache and mild exertional fatigue for 3 weeks.",
                        diagnosis = "Stage 1 Essential Hypertension (Moderate Control) & Mixed Dyslipidemia",
                        clinicalNotes = "Patient shows steady blood pressure improvement. Advised strict restriction of dietary sodium (<2g/day) and 30 mins brisk morning walk.",
                        vitals = ConsultationVitals(
                            bloodPressure = "138/88 mmHg",
                            pulseRate = "74 bpm",
                            oxygenSaturationSpO2 = "98%",
                            temperature = "98.4 °F",
                            bloodSugarFasting = "104 mg/dL",
                            weightKg = "78 kg",
                            bmi = "26.2"
                        ),
                        medicines = listOf(
                            PrescriptionMedicine(
                                id = "med-001",
                                medicineName = "Telmisartan 40mg",
                                genericName = "Telmisartan IP 40mg",
                                dosage = "1 Tablet (40mg)",
                                form = "Tablet",
                                frequency = MedicineFrequency.ONCE_DAILY_MORNING,
                                foodTiming = MedicineFoodTiming.AFTER_BREAKFAST,
                                duration = "90 Days (Long-term)",
                                specialInstructions = "Take daily at 8:00 AM with water after morning breakfast. Do not skip.",
                                isMorning = true,
                                isAfternoon = false,
                                isEvening = false,
                                isNight = false,
                                status = "Active"
                            ),
                            PrescriptionMedicine(
                                id = "med-002",
                                medicineName = "Atorvastatin 10mg",
                                genericName = "Atorvastatin Calcium IP 10mg",
                                dosage = "1 Tablet (10mg)",
                                form = "Tablet",
                                frequency = MedicineFrequency.ONCE_DAILY_NIGHT,
                                foodTiming = MedicineFoodTiming.BEDTIME,
                                duration = "90 Days",
                                specialInstructions = "Take at bedtime after dinner. Avoid grapefruit juice.",
                                isMorning = false,
                                isAfternoon = false,
                                isEvening = false,
                                isNight = true,
                                status = "Active"
                            ),
                            PrescriptionMedicine(
                                id = "med-003",
                                medicineName = "Ecosprin 75mg",
                                genericName = "Aspirin Gastro-resistant IP 75mg",
                                dosage = "1 Tablet (75mg)",
                                form = "Tablet",
                                frequency = MedicineFrequency.ONCE_DAILY_MORNING,
                                foodTiming = MedicineFoodTiming.AFTER_LUNCH,
                                duration = "90 Days",
                                specialInstructions = "Take strictly AFTER lunch with full glass of water to avoid gastric irritation.",
                                isMorning = false,
                                isAfternoon = true,
                                isEvening = false,
                                isNight = false,
                                status = "Active"
                            ),
                            PrescriptionMedicine(
                                id = "med-004",
                                medicineName = "Pantoprazole 40mg",
                                genericName = "Pantoprazole Sodium Gastro-resistant IP 40mg",
                                dosage = "1 Tablet (40mg)",
                                form = "Tablet",
                                frequency = MedicineFrequency.ONCE_DAILY_MORNING,
                                foodTiming = MedicineFoodTiming.BEFORE_BREAKFAST_EMPTY_STOMACH,
                                duration = "14 Days",
                                specialInstructions = "Take in the morning 30 minutes BEFORE breakfast on an empty stomach.",
                                isMorning = true,
                                isAfternoon = false,
                                isEvening = false,
                                isNight = false,
                                status = "Active"
                            )
                        ),
                        labInvestigations = listOf("Fasting Lipid Profile", "Fasting Blood Sugar (FBS)", "Serum Creatinine & eGFR", "Urine Routine & Microalbumin"),
                        followUpDate = "28 Sep 2026",
                        doctorAdvice = "Maintain BP log chart twice weekly. Limit salt intake to under 1 teaspoon daily. Brisk walking 30 mins."
                    ),
                    ConsultationHistoryEntry(
                        id = "consult-002",
                        patientId = "patient-001",
                        patientName = "Rajesh Sharma",
                        doctorName = "Dr. V. Rao, DM",
                        doctorSpecialty = "Senior Interventional Cardiologist",
                        department = "Cardiology & Heart Center",
                        consultationDateTime = "12 Aug 2026, 03:30 PM",
                        roomNumber = "Room 204 - 2nd Floor",
                        chiefComplaint = "Routine annual cardiac wellness evaluation.",
                        diagnosis = "Cardiovascular Risk Stratification - Low to Moderate Risk",
                        clinicalNotes = "Echocardiography showed normal LV systolic function, EF 62%, no regional wall motion abnormalities. Normal resting ECG.",
                        vitals = ConsultationVitals(
                            bloodPressure = "142/90 mmHg",
                            pulseRate = "78 bpm",
                            oxygenSaturationSpO2 = "99%",
                            temperature = "98.6 °F",
                            bloodSugarFasting = "110 mg/dL",
                            weightKg = "79 kg",
                            bmi = "26.5"
                        ),
                        medicines = listOf(
                            PrescriptionMedicine(
                                id = "med-005",
                                medicineName = "Metoprolol Succinate 25mg ER",
                                genericName = "Metoprolol Tartrate / Succinate Extended Release",
                                dosage = "1 Tablet (25mg)",
                                form = "Tablet",
                                frequency = MedicineFrequency.ONCE_DAILY_MORNING,
                                foodTiming = MedicineFoodTiming.AFTER_BREAKFAST,
                                duration = "Discontinued",
                                specialInstructions = "Tapered and replaced by Telmisartan 40mg.",
                                isMorning = true,
                                isAfternoon = false,
                                isEvening = false,
                                isNight = false,
                                status = "Discontinued"
                            )
                        ),
                        labInvestigations = listOf("12-Lead ECG", "2D Color Doppler Echocardiogram", "TMT Treadmill Test"),
                        followUpDate = "August 2027 (Annual)",
                        doctorAdvice = "Continue current antihypertensive therapy. Avoid smoking and alcohol."
                    )
                )
            ),

            // 2. Sunita Patel (42, Female) - Palpitations & Thyroid Evaluation
            PatientMasterHistory(
                patientId = "patient-002",
                patientName = "Sunita Patel",
                age = "42",
                gender = "Female",
                abhaId = "21-4829-1950-3341",
                phone = "+91 97112 88402",
                bloodGroup = "A+",
                allergies = listOf("Sulfa Drugs"),
                chronicConditions = listOf("Hypothyroidism", "Occasional Sinus Tachycardia"),
                consultations = listOf(
                    ConsultationHistoryEntry(
                        id = "consult-003",
                        patientId = "patient-002",
                        patientName = "Sunita Patel",
                        doctorName = "Dr. Ananya Sen, MD",
                        doctorSpecialty = "Consultant Cardiologist",
                        department = "Cardiology Wing",
                        consultationDateTime = "01 Sep 2026, 10:45 AM",
                        roomNumber = "Room 202 - 2nd Floor",
                        chiefComplaint = "Intermittent palpitations during work stress and feeling exhausted.",
                        diagnosis = "Sinus Tachycardia secondary to Thyroxine dose adjustment & Anxiety",
                        clinicalNotes = "Cardiac auscultation normal, ECG demonstrates sinus tachycardia at 94 bpm. Advised thyroid panel re-check and adequate hydration.",
                        vitals = ConsultationVitals(
                            bloodPressure = "118/76 mmHg",
                            pulseRate = "92 bpm",
                            oxygenSaturationSpO2 = "99%",
                            temperature = "98.2 °F",
                            bloodSugarFasting = "92 mg/dL",
                            weightKg = "62 kg",
                            bmi = "24.1"
                        ),
                        medicines = listOf(
                            PrescriptionMedicine(
                                id = "med-006",
                                medicineName = "Thyronorm 50mcg",
                                genericName = "Levothyroxine Sodium IP 50mcg",
                                dosage = "1 Tablet (50mcg)",
                                form = "Tablet",
                                frequency = MedicineFrequency.ONCE_DAILY_MORNING,
                                foodTiming = MedicineFoodTiming.BEFORE_BREAKFAST_EMPTY_STOMACH,
                                duration = "Ongoing (Long-term)",
                                specialInstructions = "Strictly take FIRST THING in morning on empty stomach with plain water. Wait 45 mins before tea/coffee/milk.",
                                isMorning = true,
                                isAfternoon = false,
                                isEvening = false,
                                isNight = false,
                                status = "Active"
                            ),
                            PrescriptionMedicine(
                                id = "med-007",
                                medicineName = "Propranolol 10mg",
                                genericName = "Propranolol Hydrochloride IP 10mg",
                                dosage = "1 Tablet (10mg)",
                                form = "Tablet",
                                frequency = MedicineFrequency.AS_NEEDED,
                                foodTiming = MedicineFoodTiming.AFTER_BREAKFAST,
                                duration = "10 Days SOS",
                                specialInstructions = "Take SOS only when heart rate exceeds 95 bpm or experiencing acute palpitations. Do not take with asthma.",
                                isMorning = true,
                                isAfternoon = false,
                                isEvening = false,
                                isNight = false,
                                status = "Active"
                            ),
                            PrescriptionMedicine(
                                id = "med-008",
                                medicineName = "Calcitriol & Calcium Citrate",
                                genericName = "Calcium Citrate 1000mg + Vitamin D3 0.25mcg",
                                dosage = "1 Tablet",
                                form = "Tablet",
                                frequency = MedicineFrequency.ONCE_DAILY_MORNING,
                                foodTiming = MedicineFoodTiming.AFTER_LUNCH,
                                duration = "60 Days",
                                specialInstructions = "Take after lunch with water. Keep 4 hours gap from Thyronorm.",
                                isMorning = false,
                                isAfternoon = true,
                                isEvening = false,
                                isNight = false,
                                status = "Active"
                            )
                        ),
                        labInvestigations = listOf("Serum T3, T4, TSH Ultra-sensitive", "24-Hour Holter Monitor", "Serum Ferritin & Vitamin B12"),
                        followUpDate = "15 Sep 2026",
                        doctorAdvice = "Maintain consistent sleep cycle. Avoid excess caffeine (maximum 1 cup/day)."
                    )
                )
            ),

            // 3. Vikram Malhotra (68, Male) - Bilateral Knee Osteoarthritis
            PatientMasterHistory(
                patientId = "patient-003",
                patientName = "Vikram Malhotra",
                age = "68",
                gender = "Male",
                abhaId = "92-1048-5529-8472",
                phone = "+91 98450 11923",
                bloodGroup = "O+",
                allergies = listOf("None Known"),
                chronicConditions = listOf("Bilateral Knee Osteoarthritis Grade 3", "Benign Prostatic Hyperplasia (BPH)"),
                consultations = listOf(
                    ConsultationHistoryEntry(
                        id = "consult-004",
                        patientId = "patient-003",
                        patientName = "Vikram Malhotra",
                        doctorName = "Dr. Sanjay Deshmukh, MS",
                        doctorSpecialty = "Senior Joint Replacement Surgeon",
                        department = "Orthopedics & Joint Clinic",
                        consultationDateTime = "20 Aug 2026, 11:30 AM",
                        roomNumber = "Room 310 - 3rd Floor",
                        chiefComplaint = "Severe bilateral knee joint pain and morning stiffness while climbing stairs or standing.",
                        diagnosis = "Advanced Bilateral Primary Osteoarthritis of Knees (Kellgren-Lawrence Grade III)",
                        clinicalNotes = "Joint crepitus present, flexion restricted to 105 degrees bilaterally. Advised quadriceps strengthening exercises, knee brace during walking.",
                        vitals = ConsultationVitals(
                            bloodPressure = "132/82 mmHg",
                            pulseRate = "70 bpm",
                            oxygenSaturationSpO2 = "97%",
                            temperature = "98.4 °F",
                            bloodSugarFasting = "98 mg/dL",
                            weightKg = "82 kg",
                            bmi = "27.8"
                        ),
                        medicines = listOf(
                            PrescriptionMedicine(
                                id = "med-009",
                                medicineName = "Etoricoxib 90mg",
                                genericName = "Etoricoxib IP 90mg",
                                dosage = "1 Tablet (90mg)",
                                form = "Tablet",
                                frequency = MedicineFrequency.ONCE_DAILY_MORNING,
                                foodTiming = MedicineFoodTiming.AFTER_BREAKFAST,
                                duration = "10 Days",
                                specialInstructions = "Take strictly AFTER breakfast. Take with antacid. Stop if experiencing acidity or black stools.",
                                isMorning = true,
                                isAfternoon = false,
                                isEvening = false,
                                isNight = false,
                                status = "Active"
                            ),
                            PrescriptionMedicine(
                                id = "med-010",
                                medicineName = "Glucosamine + Diacerein + MSM",
                                genericName = "Glucosamine Sulphate 750mg + Diacerein 50mg",
                                dosage = "1 Tablet",
                                form = "Tablet",
                                frequency = MedicineFrequency.TWICE_DAILY,
                                foodTiming = MedicineFoodTiming.AFTER_DINNER,
                                duration = "60 Days",
                                specialInstructions = "Take 1 tablet after breakfast and 1 tablet after dinner with warm water.",
                                isMorning = true,
                                isAfternoon = false,
                                isEvening = false,
                                isNight = true,
                                status = "Active"
                            ),
                            PrescriptionMedicine(
                                id = "med-011",
                                medicineName = "Silodosin 8mg",
                                genericName = "Silodosin IP 8mg",
                                dosage = "1 Capsule (8mg)",
                                form = "Capsule",
                                frequency = MedicineFrequency.ONCE_DAILY_NIGHT,
                                foodTiming = MedicineFoodTiming.AFTER_DINNER,
                                duration = "Ongoing",
                                specialInstructions = "Take with dinner at the same time each day for BPH symptom relief.",
                                isMorning = false,
                                isAfternoon = false,
                                isEvening = false,
                                isNight = true,
                                status = "Active"
                            )
                        ),
                        labInvestigations = listOf("X-Ray Both Knees (Standing AP & Lateral)", "Serum Uric Acid", "Serum Creatinine"),
                        followUpDate = "20 Sep 2026",
                        doctorAdvice = "Avoid squatting and sitting on the floor. Daily physiotherapy for quadriceps. Hot water fomentation."
                    )
                )
            ),

            // 4. Ananya Iyer (29, Female) - Antenatal Care
            PatientMasterHistory(
                patientId = "patient-004",
                patientName = "Ananya Iyer",
                age = "29",
                gender = "Female",
                abhaId = "33-7840-2219-9011",
                phone = "+91 99002 67384",
                bloodGroup = "O+",
                allergies = listOf("None Known"),
                chronicConditions = listOf("Primigravida (22 Weeks Gestation)"),
                consultations = listOf(
                    ConsultationHistoryEntry(
                        id = "consult-005",
                        patientId = "patient-004",
                        patientName = "Ananya Iyer",
                        doctorName = "Dr. Shalini Mukhopadhyay, MD",
                        doctorSpecialty = "Consultant Obstetrician & Gynecologist",
                        department = "Obstetrics & Gynecology",
                        consultationDateTime = "15 Aug 2026, 10:15 AM",
                        roomNumber = "Room 108 - 1st Floor",
                        chiefComplaint = "Routine second-trimester antenatal checkup (22 weeks).",
                        diagnosis = "Single Live Intrauterine Gestation at 22 Weeks - Normal Course",
                        clinicalNotes = "Fetal heart rate 146 bpm, fundal height corresponds to dates, anomaly scan normal, maternal weight gain optimal.",
                        vitals = ConsultationVitals(
                            bloodPressure = "114/72 mmHg",
                            pulseRate = "76 bpm",
                            oxygenSaturationSpO2 = "99%",
                            temperature = "98.4 °F",
                            bloodSugarFasting = "84 mg/dL",
                            weightKg = "58 kg",
                            bmi = "22.6"
                        ),
                        medicines = listOf(
                            PrescriptionMedicine(
                                id = "med-012",
                                medicineName = "Ferrous Ascorbate & Folic Acid",
                                genericName = "Elemental Iron 100mg + Folic Acid 1.5mg",
                                dosage = "1 Tablet",
                                form = "Tablet",
                                frequency = MedicineFrequency.ONCE_DAILY_NIGHT,
                                foodTiming = MedicineFoodTiming.AFTER_DINNER,
                                duration = "Throughout Pregnancy",
                                specialInstructions = "Take at night after dinner with lemon water/vitamin C. Avoid taking with milk or tea.",
                                isMorning = false,
                                isAfternoon = false,
                                isEvening = false,
                                isNight = true,
                                status = "Active"
                            ),
                            PrescriptionMedicine(
                                id = "med-013",
                                medicineName = "Calcium Carbonate + Vit D3 500mg",
                                genericName = "Calcium Carbonate 1250mg eq. to 500mg Elemental Calcium",
                                dosage = "1 Tablet",
                                form = "Tablet",
                                frequency = MedicineFrequency.TWICE_DAILY,
                                foodTiming = MedicineFoodTiming.AFTER_BREAKFAST,
                                duration = "Throughout Pregnancy",
                                specialInstructions = "Take 1 after breakfast and 1 after lunch. Keep 2 hours interval from Iron tablet.",
                                isMorning = true,
                                isAfternoon = true,
                                isEvening = false,
                                isNight = false,
                                status = "Active"
                            ),
                            PrescriptionMedicine(
                                id = "med-014",
                                medicineName = "DHA & Omega-3 Softgels 200mg",
                                genericName = "Docosahexaenoic Acid (DHA) 200mg",
                                dosage = "1 Softgel Capsule",
                                form = "Capsule",
                                frequency = MedicineFrequency.ONCE_DAILY_MORNING,
                                foodTiming = MedicineFoodTiming.AFTER_LUNCH,
                                duration = "Throughout Pregnancy",
                                specialInstructions = "Take after lunch with water for fetal neurodevelopment.",
                                isMorning = false,
                                isAfternoon = true,
                                isEvening = false,
                                isNight = false,
                                status = "Active"
                            )
                        ),
                        labInvestigations = listOf("Level-II Obstetric Anomaly Ultrasound Scan", "Complete Blood Count & Hemoglobin (Hb 11.8 g/dL)", "75g Oral Glucose Tolerance Test (OGTT)"),
                        followUpDate = "15 Sep 2026 (4-Week ANC Review)",
                        doctorAdvice = "Adequate hydration (2.5 - 3 liters daily), balanced diet rich in leafy greens, light maternal walking."
                    )
                )
            ),

            // 5. Mohammed Farooq (47, Male) - Type 2 Diabetes Mellitus
            PatientMasterHistory(
                patientId = "patient-005",
                patientName = "Mohammed Farooq",
                age = "47",
                gender = "Male",
                abhaId = "55-9012-4431-7890",
                phone = "+91 97401 55621",
                bloodGroup = "AB+",
                allergies = listOf("Aspirin (Gastric pain)"),
                chronicConditions = listOf("Type 2 Diabetes Mellitus (8 Years)", "Diabetic Peripheral Neuropathy"),
                consultations = listOf(
                    ConsultationHistoryEntry(
                        id = "consult-006",
                        patientId = "patient-005",
                        patientName = "Mohammed Farooq",
                        doctorName = "Dr. Preeti Khurana, MD, DM",
                        doctorSpecialty = "Senior Diabetologist & Endocrinologist",
                        department = "Endocrinology & Diabetology",
                        consultationDateTime = "29 Aug 2026, 12:00 PM",
                        roomNumber = "Room 214 - 2nd Floor",
                        chiefComplaint = "Tingling sensation in both feet at night and postprandial lethargy.",
                        diagnosis = "Type 2 Diabetes Mellitus (HbA1c 7.4%) with Early Diabetic Neuropathy",
                        clinicalNotes = "Monofilament test shows mildly reduced sensation in distal bilateral great toes. Peripheral pulses palpable. Foot care counseling provided.",
                        vitals = ConsultationVitals(
                            bloodPressure = "126/82 mmHg",
                            pulseRate = "76 bpm",
                            oxygenSaturationSpO2 = "98%",
                            temperature = "98.6 °F",
                            bloodSugarFasting = "142 mg/dL",
                            weightKg = "76 kg",
                            bmi = "25.8"
                        ),
                        medicines = listOf(
                            PrescriptionMedicine(
                                id = "med-015",
                                medicineName = "Metformin 500mg SR",
                                genericName = "Metformin Hydrochloride Sustained Release IP 500mg",
                                dosage = "1 Tablet (500mg)",
                                form = "Tablet",
                                frequency = MedicineFrequency.TWICE_DAILY,
                                foodTiming = MedicineFoodTiming.WITH_MEALS,
                                duration = "Ongoing (Long-term)",
                                specialInstructions = "Take during or immediately after meals (Breakfast and Dinner) to prevent gastric intolerance.",
                                isMorning = true,
                                isAfternoon = false,
                                isEvening = false,
                                isNight = true,
                                status = "Active"
                            ),
                            PrescriptionMedicine(
                                id = "med-016",
                                medicineName = "Teneligliptin 20mg",
                                genericName = "Teneligliptin Hydrobromide Hydrate IP 20mg",
                                dosage = "1 Tablet (20mg)",
                                form = "Tablet",
                                frequency = MedicineFrequency.ONCE_DAILY_MORNING,
                                foodTiming = MedicineFoodTiming.BEFORE_BREAKFAST_EMPTY_STOMACH,
                                duration = "Ongoing",
                                specialInstructions = "Take in the morning 30 minutes before breakfast with a glass of water.",
                                isMorning = true,
                                isAfternoon = false,
                                isEvening = false,
                                isNight = false,
                                status = "Active"
                            ),
                            PrescriptionMedicine(
                                id = "med-017",
                                medicineName = "Pregabalin 75mg + Methylcobalamin 1500mcg",
                                genericName = "Pregabalin IP 75mg + Methylcobalamin IP 1500mcg",
                                dosage = "1 Capsule",
                                form = "Capsule",
                                frequency = MedicineFrequency.ONCE_DAILY_NIGHT,
                                foodTiming = MedicineFoodTiming.BEDTIME,
                                duration = "60 Days",
                                specialInstructions = "Take at bedtime after dinner. May cause mild drowsiness. Relieves burning feet sensation.",
                                isMorning = false,
                                isAfternoon = false,
                                isEvening = false,
                                isNight = true,
                                status = "Active"
                            )
                        ),
                        labInvestigations = listOf("Glycated Hemoglobin (HbA1c: 7.4%)", "Fasting & Post-Prandial Blood Sugar", "Urine Microalbumin/Creatinine Ratio", "Dilated Fundus Eye Examination"),
                        followUpDate = "29 Nov 2026 (Quarterly Review)",
                        doctorAdvice = "Strict diabetic dietary control. Avoid refined sugars and white rice. Inspect feet daily for cuts or blisters. Wear seamless diabetic footwear."
                    )
                )
            )
        )
    }
}
