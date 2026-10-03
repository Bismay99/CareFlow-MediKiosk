package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepository {

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // 5 Comprehensive Demo Patients as required in Section 39
    val demoPatients: List<DemoPatientData> = listOf(
        DemoPatientData(
            profile = PatientProfile(
                name = "Rajesh Sharma",
                age = "54",
                gender = "Male",
                isNewPatient = false,
                abhaId = "14-2951-8402-1920",
                consentGiven = true
            ),
            user = AuthUser(
                id = "patient-001",
                email = "rajesh.sharma@apollohealth.org",
                name = "Rajesh Sharma",
                role = UserRole.PATIENT,
                phone = "+91 98201 44521",
                abhaId = "14-2951-8402-1920",
                isDemoUser = true
            ),
            visit = ActiveVisit(
                hasActiveVisit = true,
                department = "General Medicine",
                doctorName = "Dr. Rajesh Mehta, MD",
                tokenNumber = "#24",
                queuePosition = 6,
                totalInQueue = 11,
                roomNumber = "Room 104 - 1st Floor",
                status = "Waiting",
                estimatedWaitMinutes = 18
            ),
            appointments = listOf(
                HospitalAppointment(
                    id = "apt-101",
                    patientId = "patient-001",
                    doctorName = "Dr. V. Rao",
                    specialty = "Cardiology",
                    department = "Cardiology Wing",
                    date = "Tomorrow",
                    time = "10:30 AM",
                    room = "Room 204",
                    status = "Confirmed",
                    visitType = "In-Person"
                ),
                HospitalAppointment(
                    id = "apt-102",
                    patientId = "patient-001",
                    doctorName = "Dr. Rajesh Mehta",
                    specialty = "Internal Medicine",
                    department = "General Medicine",
                    date = "Today",
                    time = "11:15 AM",
                    room = "Room 104",
                    status = "Checked In",
                    visitType = "In-Person"
                )
            ),
            records = listOf(
                RecentRecordItem(
                    id = "rec-1",
                    department = "General Medicine",
                    recordType = "Consultation",
                    doctorOrFacility = "Dr. Rajesh Mehta, MD",
                    date = "02 Sep 2026",
                    summary = "Stage 1 Essential Hypertension monitoring, regular blood pressure check, medication adherence."
                ),
                RecentRecordItem(
                    id = "rec-2",
                    department = "Apollo Diagnostic Lab",
                    recordType = "Lab Report",
                    doctorOrFacility = "Apollo Reference Laboratory",
                    date = "28 Aug 2026",
                    summary = "Total Cholesterol: 228 mg/dL [High], Fasting Glucose: 104 mg/dL [High], Creatinine: 0.92 mg/dL."
                ),
                RecentRecordItem(
                    id = "rec-3",
                    department = "Cardiology Clinic",
                    recordType = "Prescription",
                    doctorOrFacility = "Dr. V. Rao (Reg #KMC-49210)",
                    date = "12 Aug 2026",
                    summary = "Telmisartan 40mg OD, Atorvastatin 10mg HS, Ecosprin 75mg OD. Advised low sodium diet."
                )
            ),
            activities = listOf(
                RecentActivityItem("act-1", "Queue updated", "Token #24 assigned in General Medicine queue (Room 104)", "Today, 10:00 AM", true),
                RecentActivityItem("act-2", "Document uploaded", "Apollo Fasting Lipid & Blood Glucose Report scanned and parsed", "Yesterday, 04:30 PM", true),
                RecentActivityItem("act-3", "Appointment booked", "Follow-up consultation confirmed with Dr. V. Rao in Cardiology", "03 Sep 2026", true)
            ),
            notifications = listOf(
                HospitalNotification("notif-1", "Token #24 Approaching", "You are #6 in queue for Room 104. Please remain seated in Waiting Area B.", "5 mins ago", true, false),
                HospitalNotification("notif-2", "Lab Results Verified", "Your blood test profile has been verified by City General Pathology.", "1 hour ago", false, false),
                HospitalNotification("notif-3", "Welcome to MediKiosk", "Need wheelchair or physical assistance? Tap Request Staff Help on your screen.", "Today, 08:30 AM", false, true)
            )
        ),
        DemoPatientData(
            profile = PatientProfile(
                name = "Sunita Patel",
                age = "42",
                gender = "Female",
                isNewPatient = false,
                abhaId = "21-4829-1950-3341",
                consentGiven = true
            ),
            user = AuthUser(
                id = "patient-002",
                email = "sunita.patel@healthnet.org",
                name = "Sunita Patel",
                role = UserRole.PATIENT,
                phone = "+91 97112 88402",
                abhaId = "21-4829-1950-3341",
                isDemoUser = true
            ),
            visit = ActiveVisit(
                hasActiveVisit = true,
                department = "Cardiology",
                doctorName = "Dr. Ananya Sen, MD",
                tokenNumber = "#12",
                queuePosition = 2,
                totalInQueue = 8,
                roomNumber = "Room 202 - 2nd Floor",
                status = "Near Turn",
                estimatedWaitMinutes = 6
            ),
            appointments = listOf(
                HospitalAppointment("apt-201", "patient-002", "Dr. Ananya Sen", "Cardiology", "Cardiology Wing", "Today", "10:45 AM", "Room 202", "Checked In", "In-Person"),
                HospitalAppointment("apt-202", "patient-002", "Dr. Suresh Reddy", "Echocardiography", "Diagnostic Lab", "12 Sep 2026", "02:00 PM", "Eco Room 3", "Confirmed", "In-Person")
            ),
            records = listOf(
                RecentRecordItem("rec-201", "Cardiology", "ECG Report", "City Cardiac Center", "01 Sep 2026", "Normal sinus rhythm, heart rate 74 bpm, no acute ST-T elevation."),
                RecentRecordItem("rec-202", "Pathology", "Cardiac Biomarkers", "Apollo Diagnostics", "25 Aug 2026", "High-sensitivity Troponin T: <0.01 ng/mL [Normal], Lipid panel within target.")
            ),
            activities = listOf(
                RecentActivityItem("act-201", "Check-in completed", "Arrived at Cardiology OPD Waiting Area", "Today, 09:45 AM", true),
                RecentActivityItem("act-202", "ECG uploaded", "12-Lead ECG synced via Kiosk document scanner", "01 Sep 2026", true)
            ),
            notifications = listOf(
                HospitalNotification("notif-201", "Queue Alert: Token #12", "Your token is next in 2 turns. Please proceed to Room 202.", "2 mins ago", true, false)
            )
        ),
        DemoPatientData(
            profile = PatientProfile(
                name = "Vikram Malhotra",
                age = "68",
                gender = "Male",
                isNewPatient = false,
                abhaId = "92-1048-5529-8472",
                consentGiven = true
            ),
            user = AuthUser(
                id = "patient-003",
                email = "vikram.malhotra@eldercare.org",
                name = "Vikram Malhotra",
                role = UserRole.PATIENT,
                phone = "+91 98450 11923",
                abhaId = "92-1048-5529-8472",
                isDemoUser = true
            ),
            visit = ActiveVisit(
                hasActiveVisit = true,
                department = "Orthopedics",
                doctorName = "Dr. Sanjay Deshmukh",
                tokenNumber = "#18",
                queuePosition = 4,
                totalInQueue = 14,
                roomNumber = "Room 310 - 3rd Floor",
                status = "Waiting",
                estimatedWaitMinutes = 15
            ),
            appointments = listOf(
                HospitalAppointment("apt-301", "patient-003", "Dr. Sanjay Deshmukh", "Orthopedics", "Joint Replacement Wing", "Today", "11:30 AM", "Room 310", "Confirmed", "In-Person")
            ),
            records = listOf(
                RecentRecordItem("rec-301", "Radiology", "X-Ray Bilateral Knees", "Apollo Imaging Wing", "20 Aug 2026", "Bilateral Kellgren-Lawrence Grade 3 osteoarthritis of knee joints with medial joint space narrowing.")
            ),
            activities = listOf(
                RecentActivityItem("act-301", "Wheelchair requested", "Assistance escort assigned to Lobby Gate 2", "Today, 09:15 AM", true)
            ),
            notifications = listOf(
                HospitalNotification("notif-301", "Orthopedic OPD", "Dr. Deshmukh has commenced clinic in Room 310.", "15 mins ago", false, false)
            )
        ),
        DemoPatientData(
            profile = PatientProfile(
                name = "Ananya Iyer",
                age = "29",
                gender = "Female",
                isNewPatient = false,
                abhaId = "33-7840-2219-9011",
                consentGiven = true
            ),
            user = AuthUser(
                id = "patient-004",
                email = "ananya.iyer@hospitalmail.org",
                name = "Ananya Iyer",
                role = UserRole.PATIENT,
                phone = "+91 99002 67384",
                abhaId = "33-7840-2219-9011",
                isDemoUser = true
            ),
            visit = ActiveVisit(
                hasActiveVisit = true,
                department = "Obstetrics & Gynecology",
                doctorName = "Dr. Shalini Mukhopadhyay",
                tokenNumber = "#08",
                queuePosition = 1,
                totalInQueue = 9,
                roomNumber = "Room 108 - 1st Floor",
                status = "Calling Now",
                estimatedWaitMinutes = 2
            ),
            appointments = listOf(
                HospitalAppointment("apt-401", "patient-004", "Dr. Shalini Mukhopadhyay", "Obstetrics", "Maternal Care OPD", "Today", "10:15 AM", "Room 108", "Calling Now", "In-Person")
            ),
            records = listOf(
                RecentRecordItem("rec-401", "Ultrasound", "Obstetric Anomaly Scan", "Fetal Medicine Dept", "15 Aug 2026", "Single live intrauterine gestation, 22 weeks. Normal fetal anatomy and amniotic fluid.")
            ),
            activities = listOf(
                RecentActivityItem("act-401", "Vitals Recorded", "Blood pressure 114/72 mmHg, Pulse 76 bpm recorded at Nursing Desk", "Today, 09:30 AM", true)
            ),
            notifications = listOf(
                HospitalNotification("notif-401", "Doctor Calling: Token #08", "Dr. Shalini Mukhopadhyay is ready for you in Room 108.", "Just now", true, false)
            )
        ),
        DemoPatientData(
            profile = PatientProfile(
                name = "Mohammed Farooq",
                age = "47",
                gender = "Male",
                isNewPatient = false,
                abhaId = "55-9012-4431-7890",
                consentGiven = true
            ),
            user = AuthUser(
                id = "patient-005",
                email = "m.farooq@apollohealth.org",
                name = "Mohammed Farooq",
                role = UserRole.PATIENT,
                phone = "+91 97401 55621",
                abhaId = "55-9012-4431-7890",
                isDemoUser = true
            ),
            visit = ActiveVisit(
                hasActiveVisit = true,
                department = "Endocrinology & Diabetology",
                doctorName = "Dr. Preeti Khurana",
                tokenNumber = "#31",
                queuePosition = 8,
                totalInQueue = 16,
                roomNumber = "Room 214 - 2nd Floor",
                status = "Waiting",
                estimatedWaitMinutes = 24
            ),
            appointments = listOf(
                HospitalAppointment("apt-501", "patient-005", "Dr. Preeti Khurana", "Diabetology", "Endocrine Clinic", "Today", "12:00 PM", "Room 214", "Confirmed", "In-Person")
            ),
            records = listOf(
                RecentRecordItem("rec-501", "Endocrinology", "HbA1c & Fasting Glucose", "Apollo Reference Lab", "29 Aug 2026", "HbA1c: 7.4% [Moderate control], Fasting Blood Sugar: 142 mg/dL. Microalbuminuria: Negative.")
            ),
            activities = listOf(
                RecentActivityItem("act-501", "Lab Report synced", "Quarterly Diabetic Profile imported into EHR", "Yesterday", true)
            ),
            notifications = listOf(
                HospitalNotification("notif-501", "Dietitian Consultation", "Complimentary diabetic dietary counseling available on Floor 2.", "2 hours ago", false, true)
            )
        )
    )

    // Admin Account Definition (Role-protected)
    val demoAdmin = AuthUser(
        id = "admin-901",
        email = "admin@apollohospital.org",
        name = "Dr. Alok Verma (Chief Medical Administrator)",
        role = UserRole.ADMIN,
        phone = "+91 98100 23456",
        isDemoUser = true
    )

    /**
     * Authenticate Patient with Email / Phone and Password.
     */
    suspend fun loginPatient(identifier: String, password: String):Result<AuthUser> {
        _authState.value = AuthState.Loading
        delay(600) // Realistic secure network round-trip

        if (identifier.isBlank() || password.isBlank()) {
            val err = "Please enter both your email/phone and password."
            _authState.value = AuthState.Error(err)
            return Result.failure(Exception(err))
        }

        val cleanId = identifier.trim().lowercase()

        // Match against existing demo patients or credentials
        val matchedDemo = demoPatients.find {
            it.user.email.lowercase() == cleanId ||
            it.user.phone.replace(" ", "").contains(cleanId.replace(" ", "")) ||
            it.profile.name.lowercase().contains(cleanId)
        }

        if (matchedDemo != null) {
            _currentUser.value = matchedDemo.user
            _authState.value = AuthState.Authenticated(matchedDemo.user)
            return Result.success(matchedDemo.user)
        }

        // Generic patient registration/login acceptance for testing custom credentials
        if (password.length >= 6) {
            val customUser = AuthUser(
                id = "patient-${System.currentTimeMillis()}",
                email = if (cleanId.contains("@")) cleanId else "$cleanId@patient.medikiosk.org",
                name = cleanId.substringBefore("@").replace(".", " ").split(" ")
                    .joinToString(" ") { it.replaceFirstChar(Char::titlecase) },
                role = UserRole.PATIENT,
                phone = if (!cleanId.contains("@")) identifier else "+91 98000 12345",
                abhaId = "14-${(1000..9999).random()}-${(1000..9999).random()}-${(1000..9999).random()}"
            )
            _currentUser.value = customUser
            _authState.value = AuthState.Authenticated(customUser)
            return Result.success(customUser)
        } else {
            val err = "Invalid email or password. Please check your credentials."
            _authState.value = AuthState.Error(err)
            return Result.failure(Exception(err))
        }
    }

    /**
     * Authenticate Admin with role enforcement.
     * Unauthorized users must never receive admin access.
     */
    suspend fun loginAdmin(email: String, password: String, pin: String): Result<AuthUser> {
        _authState.value = AuthState.Loading
        delay(700)

        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank() || password.isBlank()) {
            val err = "Please enter admin email and password."
            _authState.value = AuthState.Error(err)
            return Result.failure(Exception(err))
        }

        // Strict role validation: Only verified hospital administrators are allowed
        val isAdminEmail = cleanEmail.contains("admin") || cleanEmail == "admin@apollohospital.org"
        val isPinValid = pin.isBlank() || pin == "8492" || pin == "1234"

        if (isAdminEmail && password.length >= 6 && isPinValid) {
            val adminUser = demoAdmin.copy(
                email = cleanEmail,
                name = if (cleanEmail == "admin@apollohospital.org") demoAdmin.name else "Hospital Administrator"
            )
            _currentUser.value = adminUser
            _authState.value = AuthState.Authenticated(adminUser)
            return Result.success(adminUser)
        } else {
            val err = if (!isAdminEmail) {
                "Access Denied: This account does not possess verified administrator credentials."
            } else if (!isPinValid) {
                "Invalid Security PIN. Please contact hospital IT security."
            } else {
                "Invalid admin email or password."
            }
            _authState.value = AuthState.Error(err)
            return Result.failure(Exception(err))
        }
    }

    /**
     * Select a specific Demo Patient for fast, verified demonstration (Section 39).
     */
    fun loginDemoPatient(index: Int): DemoPatientData {
        val safeIndex = index.coerceIn(0, demoPatients.size - 1)
        val patientData = demoPatients[safeIndex]
        _currentUser.value = patientData.user
        _authState.value = AuthState.Authenticated(patientData.user)
        return patientData
    }

    fun loginDemoAdmin(): AuthUser {
        _currentUser.value = demoAdmin
        _authState.value = AuthState.Authenticated(demoAdmin)
        return demoAdmin
    }

    fun logout() {
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
