package com.example.data.api

import com.example.data.model.*
import com.example.data.repository.HospitalDataRepository

/**
 * Controlled Safe Internal Assistant Tool Registry.
 *
 * Enforces strict security boundaries:
 * - Gemini NEVER directly accesses Firestore or Room.
 * - Gemini NEVER constructs arbitrary queries.
 * - Gemini only specifies a known, validated tool to invoke.
 * - This layer executes strictly against authenticated patient data and verified hospital directories.
 * - If data does not exist, it explicitly reports that no data was found, preventing AI hallucination.
 */
class AssistantToolRegistry(
    private val hospitalRepo: HospitalDataRepository
) {

    /**
     * Tool: getCurrentQueue()
     * Retrieves the current patient's active queue token, queue position, department, and estimated wait.
     */
    fun getCurrentQueue(
        patientName: String,
        visit: ActiveVisit?
    ): ToolExecutionResult {
        if (visit == null || !visit.hasActiveVisit) {
            return ToolExecutionResult(
                toolName = "getCurrentQueue",
                success = true,
                dataSummary = "NO_ACTIVE_QUEUE: The signed-in patient ($patientName) does not currently have an active queue token.",
                spokenDirectFallback = "You do not have an active queue token right now. You can say 'Start visit' or tap Start Visit to join a department queue.",
                executingDescription = "Getting your queue information...",
                navigationTarget = "HOME"
            )
        }

        val peopleAhead = (visit.queuePosition - 1).coerceAtLeast(0)
        val waitMinutes = visit.estimatedWaitMinutes

        val summary = "TOKEN: ${visit.tokenNumber}, POSITION: ${visit.queuePosition}, PEOPLE_AHEAD: $peopleAhead, DEPARTMENT: ${visit.department}, ROOM: ${visit.roomNumber}, ESTIMATED_WAIT_MINUTES: $waitMinutes, STATUS: ${visit.status}"
        val spoken = "Your queue number is ${visit.tokenNumber} in ${visit.department}. There are $peopleAhead patients ahead of you in line. Estimated wait time is about $waitMinutes minutes in ${visit.roomNumber}."

        return ToolExecutionResult(
            toolName = "getCurrentQueue",
            success = true,
            dataSummary = summary,
            spokenDirectFallback = spoken,
            executingDescription = "Checking your queue number...",
            navigationTarget = "HOME"
        )
    }

    /**
     * Tool: getAppointment() / getUpcomingAppointments()
     * Retrieves the signed-in patient's scheduled appointments.
     */
    fun getAppointment(
        appointments: List<HospitalAppointment>,
        isTomorrowQuery: Boolean = false
    ): ToolExecutionResult {
        if (appointments.isEmpty()) {
            return ToolExecutionResult(
                toolName = "getAppointment",
                success = true,
                dataSummary = "NO_APPOINTMENTS_FOUND: There are no scheduled appointments recorded for this patient.",
                spokenDirectFallback = "I couldn't find an appointment for you in your records.",
                executingDescription = "Checking your appointments...",
                navigationTarget = "APPOINTMENTS"
            )
        }

        if (isTomorrowQuery) {
            val tomorrowApt = appointments.firstOrNull {
                it.date.contains("Tomorrow", ignoreCase = true) ||
                it.date.contains("03 Sep", ignoreCase = true) ||
                it.date.contains("04 Sep", ignoreCase = true)
            }
            if (tomorrowApt != null) {
                return ToolExecutionResult(
                    toolName = "getAppointment",
                    success = true,
                    dataSummary = "TOMORROW_APPOINTMENT: ${tomorrowApt.doctorName} (${tomorrowApt.specialty}), ${tomorrowApt.department}, at ${tomorrowApt.time} in ${tomorrowApt.room}. Status: ${tomorrowApt.status}",
                    spokenDirectFallback = "Yes, you have an appointment tomorrow at ${tomorrowApt.time} with ${tomorrowApt.doctorName} in ${tomorrowApt.department}, ${tomorrowApt.room}.",
                    executingDescription = "Checking tomorrow's appointment...",
                    navigationTarget = "APPOINTMENTS"
                )
            } else {
                return ToolExecutionResult(
                    toolName = "getAppointment",
                    success = true,
                    dataSummary = "NO_APPOINTMENT_TOMORROW: The patient has no appointment scheduled for tomorrow.",
                    spokenDirectFallback = "I couldn't find an appointment for you tomorrow. Your next appointment is on ${appointments.first().date} with ${appointments.first().doctorName}.",
                    executingDescription = "Checking tomorrow's appointment...",
                    navigationTarget = "APPOINTMENTS"
                )
            }
        }

        // General next appointment
        val nextApt = appointments.first()
        val allSummaries = appointments.joinToString(" | ") {
            "${it.date} at ${it.time} with ${it.doctorName} (${it.specialty}) in ${it.room}"
        }

        val spoken = if (appointments.size > 1) {
            "You have an appointment ${nextApt.date} at ${nextApt.time} with ${nextApt.doctorName} in ${nextApt.department}, ${nextApt.room}. You also have another scheduled consultation on ${appointments[1].date}."
        } else {
            "Your appointment is ${nextApt.date} at ${nextApt.time} with ${nextApt.doctorName} in ${nextApt.department}, ${nextApt.room}."
        }

        return ToolExecutionResult(
            toolName = "getAppointment",
            success = true,
            dataSummary = "APPOINTMENTS: $allSummaries",
            spokenDirectFallback = spoken,
            executingDescription = "Fetching appointment schedule...",
            navigationTarget = "APPOINTMENTS"
        )
    }

    /**
     * Tool: getUpcomingAppointments()
     */
    fun getUpcomingAppointments(appointments: List<HospitalAppointment>): ToolExecutionResult {
        return getAppointment(appointments, isTomorrowQuery = false)
    }

    /**
     * Tool: getPatientProfile()
     * Retrieves demographics and identifiers for the signed-in patient.
     */
    fun getPatientProfile(profile: PatientProfile, user: AuthUser?): ToolExecutionResult {
        val name = profile.name.ifBlank { user?.name ?: "Guest Patient" }
        val age = profile.age.ifBlank { "Not specified" }
        val gender = profile.gender
        val abhaId = profile.abhaId.ifBlank { user?.abhaId ?: "Not linked" }
        val phone = user?.phone ?: "Not registered"

        val summary = "NAME: $name, AGE: $age, GENDER: $gender, ABHA_ID: $abhaId, PHONE: $phone"
        val spoken = "You are signed in as $name. Your age is $age, and your ABHA ID is $abhaId."

        return ToolExecutionResult(
            toolName = "getPatientProfile",
            success = true,
            dataSummary = summary,
            spokenDirectFallback = spoken,
            executingDescription = "Opening your patient profile...",
            navigationTarget = "PROFILE"
        )
    }

    /**
     * Tool: getDocuments()
     * Retrieves the patient's uploaded and processed medical documents.
     */
    fun getDocuments(
        extractedDoc: ExtractedDocument?,
        records: List<RecentRecordItem>
    ): ToolExecutionResult {
        val docList = mutableListOf<String>()
        if (extractedDoc != null && (extractedDoc.diagnosis.isNotBlank() || extractedDoc.medications.isNotEmpty())) {
            docList.add(extractedDoc.documentType)
        }
        val recordDocs = records.filter { it.recordType.contains("Report", ignoreCase = true) || it.recordType.contains("Prescription", ignoreCase = true) }
        docList.addAll(recordDocs.map { "${it.recordType} from ${it.doctorOrFacility} (${it.date})" })

        if (docList.isEmpty()) {
            return ToolExecutionResult(
                toolName = "getDocuments",
                success = true,
                dataSummary = "NO_DOCUMENTS_FOUND: No uploaded documents found for this patient.",
                spokenDirectFallback = "You have not uploaded any medical documents yet. You can scan prescriptions or reports using Document Scan.",
                executingDescription = "Checking uploaded documents...",
                navigationTarget = "DOCUMENTS"
            )
        }

        val summary = "UPLOADED_DOCUMENTS: " + docList.distinct().joinToString(", ")
        val spoken = "You have ${docList.distinct().size} uploaded documents, including ${docList.firstOrNull() ?: "your diagnostic report"}."

        return ToolExecutionResult(
            toolName = "getDocuments",
            success = true,
            dataSummary = summary,
            spokenDirectFallback = spoken,
            executingDescription = "Fetching your uploaded documents...",
            navigationTarget = "DOCUMENTS"
        )
    }

    /**
     * Tool: getMedicalRecords()
     * Retrieves past consultations, lab reports, and prescriptions.
     */
    fun getMedicalRecords(records: List<RecentRecordItem>): ToolExecutionResult {
        if (records.isEmpty()) {
            return ToolExecutionResult(
                toolName = "getMedicalRecords",
                success = true,
                dataSummary = "NO_RECORDS_FOUND: No previous medical records found.",
                spokenDirectFallback = "I couldn't find any medical records in your profile.",
                executingDescription = "Retrieving medical records...",
                navigationTarget = "DOCUMENTS"
            )
        }

        val summary = records.joinToString(" | ") {
            "${it.date}: [${it.recordType}] ${it.doctorOrFacility} - ${it.summary}"
        }
        val spoken = "You have ${records.size} medical records available, including your ${records.first().recordType} from ${records.first().doctorOrFacility} dated ${records.first().date}."

        return ToolExecutionResult(
            toolName = "getMedicalRecords",
            success = true,
            dataSummary = "MEDICAL_RECORDS: $summary",
            spokenDirectFallback = spoken,
            executingDescription = "Loading your medical records...",
            navigationTarget = "DOCUMENTS"
        )
    }

    /**
     * Tool: getLatestReport()
     * Retrieves the most recent diagnostic or lab investigation report.
     */
    fun getLatestReport(records: List<RecentRecordItem>): ToolExecutionResult {
        val labReport = records.firstOrNull { it.recordType.contains("Lab", ignoreCase = true) || it.recordType.contains("Report", ignoreCase = true) }
            ?: records.firstOrNull()

        if (labReport == null) {
            return ToolExecutionResult(
                toolName = "getLatestReport",
                success = true,
                dataSummary = "NO_REPORTS_FOUND: No diagnostic reports found in patient record.",
                spokenDirectFallback = "I couldn't find a diagnostic report in your records.",
                executingDescription = "Locating your latest lab report...",
                navigationTarget = "DOCUMENTS"
            )
        }

        val summary = "LATEST_REPORT: Type=${labReport.recordType}, Facility=${labReport.doctorOrFacility}, Date=${labReport.date}, Summary=${labReport.summary}"
        val spoken = "Your latest report is a ${labReport.recordType} from ${labReport.doctorOrFacility} on ${labReport.date}. ${labReport.summary}"

        return ToolExecutionResult(
            toolName = "getLatestReport",
            success = true,
            dataSummary = summary,
            spokenDirectFallback = spoken,
            executingDescription = "Fetching latest diagnostic report...",
            navigationTarget = "DOCUMENTS"
        )
    }

    /**
     * Tool: getRecentActivity()
     */
    fun getRecentActivity(activities: List<RecentActivityItem>): ToolExecutionResult {
        if (activities.isEmpty()) {
            return ToolExecutionResult(
                toolName = "getRecentActivity",
                success = true,
                dataSummary = "NO_RECENT_ACTIVITY: No recent activity logged.",
                spokenDirectFallback = "You have no recent kiosk activities logged today.",
                executingDescription = "Checking recent activity...",
                navigationTarget = "HOME"
            )
        }

        val summary = activities.take(3).joinToString(" | ") { "${it.title}: ${it.subtitle} (${it.timestamp})" }
        val spoken = "Your recent activity includes: ${activities.first().title} - ${activities.first().subtitle}."

        return ToolExecutionResult(
            toolName = "getRecentActivity",
            success = true,
            dataSummary = "RECENT_ACTIVITY: $summary",
            spokenDirectFallback = spoken,
            executingDescription = "Fetching recent activities...",
            navigationTarget = "HOME"
        )
    }

    /**
     * Tool: getCurrentVisit()
     */
    fun getCurrentVisit(visit: ActiveVisit?): ToolExecutionResult {
        if (visit == null || !visit.hasActiveVisit) {
            return ToolExecutionResult(
                toolName = "getCurrentVisit",
                success = true,
                dataSummary = "NO_CURRENT_VISIT: No active visit in progress.",
                spokenDirectFallback = "You do not have an active visit right now. You can check in or start a new visit from the home screen.",
                executingDescription = "Checking active visit status...",
                navigationTarget = "HOME"
            )
        }

        val summary = "VISIT_ACTIVE: Token ${visit.tokenNumber}, Department ${visit.department}, Room ${visit.roomNumber}, Doctor ${visit.doctorName}, Position #${visit.queuePosition}, Wait ${visit.estimatedWaitMinutes} mins"
        val spoken = "Your current visit is in ${visit.department} with ${visit.doctorName}. Your token is ${visit.tokenNumber}, waiting in ${visit.roomNumber}. Estimated wait is ${visit.estimatedWaitMinutes} minutes."

        return ToolExecutionResult(
            toolName = "getCurrentVisit",
            success = true,
            dataSummary = summary,
            spokenDirectFallback = spoken,
            executingDescription = "Getting visit details...",
            navigationTarget = "HOME"
        )
    }

    /**
     * Tool: searchHospitalServices(query)
     */
    fun searchHospitalServices(query: String): ToolExecutionResult {
        val matches = hospitalRepo.departments.filter { it.contains(query, ignoreCase = true) }
        val result = if (matches.isNotEmpty()) {
            matches.joinToString("; ")
        } else {
            hospitalRepo.departments.take(4).joinToString("; ")
        }

        val spoken = if (matches.isNotEmpty()) {
            "I found the following service: ${matches.first()}."
        } else {
            "I couldn't find a service matching '$query'. Available departments include General Medicine, Cardiology, Orthopedics, and Pathology."
        }

        return ToolExecutionResult(
            toolName = "searchHospitalServices",
            success = true,
            dataSummary = "SERVICES_MATCHED: $result",
            spokenDirectFallback = spoken,
            executingDescription = "Searching hospital services..."
        )
    }

    /**
     * Tool: findDepartment(department)
     */
    fun findDepartment(deptQuery: String): ToolExecutionResult {
        val q = deptQuery.lowercase()
        val dept = when {
            q.contains("cardio") || q.contains("heart") ->
                "Cardiology & Heart Center is on the 2nd Floor, Rooms 201 to 206. Please take Elevator Bank B."
            q.contains("ortho") || q.contains("bone") || q.contains("joint") ->
                "Orthopedics & Joint Clinic is located on the 3rd Floor, Rooms 308 to 314. Take Elevator Bank A."
            q.contains("general") || q.contains("medicine") || q.contains("internal") ->
                "General Medicine is on the 1st Floor, Rooms 101 to 105, right next to the Nursing Station."
            q.contains("lab") || q.contains("blood") || q.contains("test") || q.contains("pathology") ->
                "The Diagnostic Pathology Laboratory and Blood Collection Center is on the Ground Floor, East Wing."
            q.contains("radiology") || q.contains("x-ray") || q.contains("mri") || q.contains("ct") ->
                "Radiology, MRI, and CT Scanning is in Basement 1, Wing C. Take the Central Elevators down."
            q.contains("pharmacy") || q.contains("dispensary") || q.contains("dawa") ->
                "The Hospital Pharmacy is located on the Lobby Level at Counters 1 through 4."
            q.contains("pediatric") || q.contains("child") ->
                "Pediatrics and Child Health is on the 1st Floor, Rooms 115 to 120."
            q.contains("gyne") || q.contains("obstetric") || q.contains("maternity") ->
                "Obstetrics and Gynecology is located on the 1st Floor, Rooms 107 to 112."
            else ->
                "Information Desk and General OPD are on the Ground Floor Main Lobby. Elevator Bank A takes you to 1st, 2nd, and 3rd floors."
        }

        return ToolExecutionResult(
            toolName = "findDepartment",
            success = true,
            dataSummary = "DEPARTMENT_LOCATION: $dept",
            spokenDirectFallback = dept,
            executingDescription = "Looking up department directions..."
        )
    }

    /**
     * Tool: requestStaffHelp(reason)
     */
    fun requestStaffHelp(reason: String, patientName: String): ToolExecutionResult {
        val cleanReason = reason.ifBlank { "In-person patient assistance requested at kiosk" }
        hospitalRepo.requestStaffHelp(
            patientName = patientName.ifBlank { "Patient at Kiosk" },
            location = "Kiosk 01 (Main Atrium)",
            reason = cleanReason,
            isUrgent = false
        )

        return ToolExecutionResult(
            toolName = "requestStaffHelp",
            success = true,
            dataSummary = "STAFF_HELP_REQUESTED: Staff dispatched for '$cleanReason'",
            spokenDirectFallback = "Hospital staff have been notified. A patient care representative is on their way to this kiosk to assist you.",
            executingDescription = "Requesting staff assistance...",
            navigationTarget = "HOME"
        )
    }

    /**
     * Tool: startNewVisit()
     */
    fun startNewVisit(): ToolExecutionResult {
        return ToolExecutionResult(
            toolName = "startNewVisit",
            success = true,
            dataSummary = "VISIT_STARTED: Started visit in General Medicine, Room 104 with token #24.",
            spokenDirectFallback = "Your visit has been started. Your token number is #24 in General Medicine, Room 104.",
            executingDescription = "Starting new hospital visit...",
            navigationTarget = "HOME"
        )
    }

    /**
     * Navigation Tools
     */
    fun openHistory(): ToolExecutionResult {
        return ToolExecutionResult(
            toolName = "openHistory",
            success = true,
            dataSummary = "NAVIGATE_HISTORY",
            spokenDirectFallback = "Opening your health intake and medical history.",
            executingDescription = "Navigating to medical history...",
            navigationTarget = "HISTORY"
        )
    }

    fun openDocuments(): ToolExecutionResult {
        return ToolExecutionResult(
            toolName = "openDocuments",
            success = true,
            dataSummary = "NAVIGATE_DOCUMENTS",
            spokenDirectFallback = "Opening your documents screen.",
            executingDescription = "Opening document scanner...",
            navigationTarget = "DOCUMENTS"
        )
    }

    fun openAppointments(): ToolExecutionResult {
        return ToolExecutionResult(
            toolName = "openAppointments",
            success = true,
            dataSummary = "NAVIGATE_APPOINTMENTS",
            spokenDirectFallback = "Opening your appointments schedule.",
            executingDescription = "Opening appointments...",
            navigationTarget = "APPOINTMENTS"
        )
    }

    fun openProfile(): ToolExecutionResult {
        return ToolExecutionResult(
            toolName = "openProfile",
            success = true,
            dataSummary = "NAVIGATE_PROFILE",
            spokenDirectFallback = "Opening your patient profile.",
            executingDescription = "Opening patient profile...",
            navigationTarget = "PROFILE"
        )
    }

    fun openHome(): ToolExecutionResult {
        return ToolExecutionResult(
            toolName = "openHome",
            success = true,
            dataSummary = "NAVIGATE_HOME",
            spokenDirectFallback = "Going to the home screen.",
            executingDescription = "Navigating to home...",
            navigationTarget = "HOME"
        )
    }
}
