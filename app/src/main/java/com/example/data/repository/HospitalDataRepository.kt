package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HospitalDataRepository {

    // Live Staff Assistance Requests (Received by Admin/Staff triage desk)
    private val _staffAssistanceRequests = MutableStateFlow<List<StaffAssistanceRequest>>(
        listOf(
            StaffAssistanceRequest(
                id = "req-101",
                patientName = "Vikram Malhotra",
                location = "Kiosk 04 (Lobby Floor)",
                reason = "Wheelchair & Escort Required for Orthopedics 3rd Floor",
                timestamp = "3 mins ago",
                isUrgent = false,
                status = RequestStatus.DISPATCHED,
                assignedStaff = "Nurse Priya (Care Escort)"
            ),
            StaffAssistanceRequest(
                id = "req-102",
                patientName = "Suresh Nambiar",
                location = "Kiosk 02 (Ground Floor Diagnostics)",
                reason = "Language Translation Assistance (Malayalam to English)",
                timestamp = "7 mins ago",
                isUrgent = false,
                status = RequestStatus.PENDING
            )
        )
    )
    val staffAssistanceRequests: StateFlow<List<StaffAssistanceRequest>> = _staffAssistanceRequests.asStateFlow()

    // Live Department Queue List
    private val _liveDepartmentQueues = MutableStateFlow<List<QueueItem>>(
        listOf(
            QueueItem("Token #21", "Anil Kulkarni", "General Medicine", "Room 104", 1, "In Consultation", 0),
            QueueItem("Token #22", "Meenakshi Sundaram", "General Medicine", "Room 104", 2, "Waiting", 4),
            QueueItem("Token #23", "Harish Chandra", "General Medicine", "Room 104", 3, "Waiting", 9),
            QueueItem("Token #24", "Rajesh Sharma", "General Medicine", "Room 104", 4, "Waiting", 14),
            QueueItem("Token #25", "Fatima Begum", "General Medicine", "Room 104", 5, "Waiting", 20),
            QueueItem("Token #11", "Kavita Rao", "Cardiology", "Room 202", 1, "In Consultation", 0),
            QueueItem("Token #12", "Sunita Patel", "Cardiology", "Room 202", 2, "Near Turn", 5),
            QueueItem("Token #08", "Ananya Iyer", "Obstetrics", "Room 108", 1, "Calling Now", 2),
            QueueItem("Token #18", "Vikram Malhotra", "Orthopedics", "Room 310", 4, "Waiting", 16)
        )
    )
    val liveDepartmentQueues: StateFlow<List<QueueItem>> = _liveDepartmentQueues.asStateFlow()

    // Hospital Departments Directory
    val departments = listOf(
        "General Medicine (Room 101-105, 1st Floor)",
        "Cardiology & Heart Center (Room 201-206, 2nd Floor)",
        "Orthopedics & Joint Clinic (Room 308-314, 3rd Floor)",
        "Obstetrics & Gynecology (Room 107-112, 1st Floor)",
        "Endocrinology & Diabetology (Room 212-218, 2nd Floor)",
        "Pediatrics & Child Health (Room 115-120, 1st Floor)",
        "Diagnostic Pathology & Blood Bank (Ground Floor East Wing)",
        "Radiology, CT & MRI Center (Basement 1, Wing C)",
        "Hospital Pharmacy & Dispensary (Lobby Level Counter 1-4)"
    )

    fun requestStaffHelp(patientName: String, location: String, reason: String, isUrgent: Boolean = true): StaffAssistanceRequest {
        val request = StaffAssistanceRequest(
            id = "req-${System.currentTimeMillis()}",
            patientName = patientName.ifBlank { "Kiosk Patient" },
            location = location.ifBlank { "Kiosk 01 (Main Atrium)" },
            reason = reason.ifBlank { "Assistance requested at kiosk terminal" },
            timestamp = "Just now",
            isUrgent = isUrgent,
            status = RequestStatus.PENDING
        )
        _staffAssistanceRequests.value = listOf(request) + _staffAssistanceRequests.value
        return request
    }

    fun dispatchStaffToRequest(requestId: String, staffName: String = "Staff On-Duty") {
        _staffAssistanceRequests.value = _staffAssistanceRequests.value.map {
            if (it.id == requestId) {
                it.copy(status = RequestStatus.DISPATCHED, assignedStaff = staffName)
            } else it
        }
    }

    fun resolveStaffRequest(requestId: String) {
        _staffAssistanceRequests.value = _staffAssistanceRequests.value.map {
            if (it.id == requestId) {
                it.copy(status = RequestStatus.RESOLVED)
            } else it
        }
    }

    fun callNextPatientInDepartment(dept: String): QueueItem? {
        val current = _liveDepartmentQueues.value.toMutableList()
        val inConsultIndex = current.indexOfFirst { it.department.contains(dept, ignoreCase = true) && it.status == "In Consultation" }
        if (inConsultIndex >= 0) {
            current[inConsultIndex] = current[inConsultIndex].copy(status = "Completed")
        }

        val nextIndex = current.indexOfFirst { it.department.contains(dept, ignoreCase = true) && (it.status == "Waiting" || it.status == "Near Turn" || it.status == "Calling Now") }
        if (nextIndex >= 0) {
            val called = current[nextIndex].copy(status = "Calling Now")
            current[nextIndex] = called
            _liveDepartmentQueues.value = current
            return called
        }
        _liveDepartmentQueues.value = current
        return null
    }

    fun markPatientInConsultation(token: String) {
        _liveDepartmentQueues.value = _liveDepartmentQueues.value.map {
            if (it.token == token) it.copy(status = "In Consultation") else it
        }
    }
}
