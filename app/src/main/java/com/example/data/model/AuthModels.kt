package com.example.data.model

enum class UserRole {
    PATIENT,
    STAFF,
    DOCTOR,
    ADMIN
}

data class AuthUser(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val phone: String = "",
    val abhaId: String = "",
    val profilePhotoUrl: String = "",
    val isDemoUser: Boolean = false
)

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: AuthUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

data class StaffAssistanceRequest(
    val id: String,
    val patientName: String,
    val location: String,
    val reason: String,
    val timestamp: String,
    val isUrgent: Boolean = true,
    val status: RequestStatus = RequestStatus.PENDING,
    val assignedStaff: String? = null
)

enum class RequestStatus {
    PENDING,
    DISPATCHED,
    RESOLVED
}

data class HospitalAppointment(
    val id: String,
    val patientId: String,
    val doctorName: String,
    val specialty: String,
    val department: String,
    val date: String,
    val time: String,
    val room: String,
    val status: String = "Confirmed", // Confirmed, Completed, Cancelled
    val visitType: String = "In-Person" // In-Person, Video Consult
)

data class QueueItem(
    val token: String,
    val patientName: String,
    val department: String,
    val room: String,
    val position: Int,
    val status: String, // Waiting, In Consultation, Completed
    val estimatedWaitMinutes: Int
)

data class DemoPatientData(
    val profile: PatientProfile,
    val user: AuthUser,
    val visit: ActiveVisit,
    val appointments: List<HospitalAppointment>,
    val records: List<RecentRecordItem>,
    val activities: List<RecentActivityItem>,
    val notifications: List<HospitalNotification>
)
