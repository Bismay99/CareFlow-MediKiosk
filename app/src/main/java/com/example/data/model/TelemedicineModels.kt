package com.example.data.model

enum class TeleconsultationStatus(val label: String) {
    SCHEDULED("Scheduled"),
    WAITING_ROOM("Waiting for Doctor"),
    IN_PROGRESS("Live Consultation"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled")
}

enum class ConsultationType(val label: String) {
    VIDEO_CALL("Video Consultation"),
    AUDIO_CALL("Audio Consultation"),
    IN_PERSON("In-Person OPD")
}

data class DoctorProfile(
    val id: String,
    val name: String,
    val specialty: String,
    val department: String,
    val qualifications: String,
    val registrationNumber: String,
    val experienceYears: Int,
    val rating: Float,
    val reviewCount: Int,
    val consultationFee: String,
    val availableSlots: List<String>,
    val hospitalAffiliation: String = "Sunrise Multispeciality Hospital",
    val bio: String = "",
    val profilePhotoUrl: String = "",
    val isOnlineNow: Boolean = true
)

data class TelemedicineChatMessage(
    val id: String = "msg-${System.currentTimeMillis()}-${(100..999).random()}",
    val sessionId: String,
    val senderId: String,
    val senderName: String,
    val messageText: String,
    val timestampFormatted: String,
    val isFromDoctor: Boolean,
    val attachedDocumentName: String? = null,
    val attachedDocumentType: String? = null
)

data class TeleconsultationSession(
    val sessionId: String = "session-${System.currentTimeMillis()}",
    val appointmentId: String,
    val patientId: String,
    val patientName: String,
    val doctorId: String,
    val doctorName: String,
    val doctorSpecialty: String,
    val callType: ConsultationType = ConsultationType.VIDEO_CALL,
    val status: TeleconsultationStatus = TeleconsultationStatus.WAITING_ROOM,
    val isAudioMuted: Boolean = false,
    val isVideoDisabled: Boolean = false,
    val isFrontCamera: Boolean = true,
    val durationSeconds: Long = 0L,
    val chatMessages: List<TelemedicineChatMessage> = emptyList(),
    val sharedDocumentIds: List<String> = emptyList(),
    val isDoctorConnected: Boolean = true,
    val isPatientConnected: Boolean = true
)
