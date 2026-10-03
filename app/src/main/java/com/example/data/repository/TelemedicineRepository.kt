package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class TelemedicineRepository {

    private val _doctors = MutableStateFlow<List<DoctorProfile>>(createDemoDoctors())
    val doctors: StateFlow<List<DoctorProfile>> = _doctors.asStateFlow()

    private val _activeSession = MutableStateFlow<TeleconsultationSession?>(null)
    val activeSession: StateFlow<TeleconsultationSession?> = _activeSession.asStateFlow()

    fun startSession(
        appointmentId: String,
        patientId: String,
        patientName: String,
        doctor: DoctorProfile,
        type: ConsultationType = ConsultationType.VIDEO_CALL
    ): TeleconsultationSession {
        val nowFormatted = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val session = TeleconsultationSession(
            appointmentId = appointmentId,
            patientId = patientId,
            patientName = patientName,
            doctorId = doctor.id,
            doctorName = doctor.name,
            doctorSpecialty = doctor.specialty,
            callType = type,
            status = TeleconsultationStatus.IN_PROGRESS,
            chatMessages = listOf(
                TelemedicineChatMessage(
                    sessionId = "init",
                    senderId = doctor.id,
                    senderName = doctor.name,
                    messageText = "Hello $patientName. I am Dr. ${doctor.name}. I have your preliminary intake notes and recent vital readings on screen. How can I assist you today?",
                    timestampFormatted = nowFormatted,
                    isFromDoctor = true
                )
            )
        )
        _activeSession.value = session
        return session
    }

    fun toggleAudio() {
        val current = _activeSession.value ?: return
        _activeSession.value = current.copy(isAudioMuted = !current.isAudioMuted)
    }

    fun toggleVideo() {
        val current = _activeSession.value ?: return
        _activeSession.value = current.copy(isVideoDisabled = !current.isVideoDisabled)
    }

    fun flipCamera() {
        val current = _activeSession.value ?: return
        _activeSession.value = current.copy(isFrontCamera = !current.isFrontCamera)
    }

    fun sendChatMessage(text: String, isDoctor: Boolean, senderName: String) {
        val current = _activeSession.value ?: return
        val now = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val msg = TelemedicineChatMessage(
            sessionId = current.sessionId,
            senderId = if (isDoctor) current.doctorId else current.patientId,
            senderName = senderName,
            messageText = text,
            timestampFormatted = now,
            isFromDoctor = isDoctor
        )
        _activeSession.value = current.copy(chatMessages = current.chatMessages + msg)
    }

    fun endSession() {
        val current = _activeSession.value ?: return
        _activeSession.value = current.copy(status = TeleconsultationStatus.COMPLETED)
    }

    fun closeSession() {
        _activeSession.value = null
    }

    private fun createDemoDoctors(): List<DoctorProfile> {
        return listOf(
            DoctorProfile(
                id = "doc-001",
                name = "Dr. Ananya Sharma",
                specialty = "General Medicine & Diabetology",
                department = "Internal Medicine",
                qualifications = "MBBS, MD (Medicine)",
                registrationNumber = "MCI-48192",
                experienceYears = 14,
                rating = 4.9f,
                reviewCount = 384,
                consultationFee = "₹ 500",
                availableSlots = listOf("10:00 AM", "11:30 AM", "02:00 PM", "04:30 PM", "06:00 PM"),
                bio = "Experienced internal medicine specialist focusing on cardiovascular risk mitigation, diabetic management, and acute febrile presentations.",
                isOnlineNow = true
            ),
            DoctorProfile(
                id = "doc-002",
                name = "Dr. Vikram Sethi",
                specialty = "Cardiology & Preventive Care",
                department = "Cardiology",
                qualifications = "MBBS, MD, DM (Cardiology)",
                registrationNumber = "MCI-61029",
                experienceYears = 18,
                rating = 4.95f,
                reviewCount = 512,
                consultationFee = "₹ 800",
                availableSlots = listOf("09:30 AM", "12:00 PM", "03:30 PM", "05:00 PM"),
                bio = "Senior Interventional Cardiologist specializing in hypertension, arrhythmias, coronary artery disease, and wearable remote cardiac telemetry.",
                isOnlineNow = true
            ),
            DoctorProfile(
                id = "doc-003",
                name = "Dr. Priya Deshmukh",
                specialty = "Pediatrics & Child Health",
                department = "Pediatrics",
                qualifications = "MBBS, DCH, DNB (Pediatrics)",
                registrationNumber = "MCI-73204",
                experienceYears = 11,
                rating = 4.85f,
                reviewCount = 290,
                consultationFee = "₹ 450",
                availableSlots = listOf("10:30 AM", "01:00 PM", "04:00 PM"),
                bio = "Compassionate pediatrician with expertise in childhood respiratory infections, immunization scheduling, and adolescent wellness.",
                isOnlineNow = false
            ),
            DoctorProfile(
                id = "doc-004",
                name = "Dr. Rajiv Menon",
                specialty = "Pulmonology & Respiratory Care",
                department = "Pulmonology",
                qualifications = "MBBS, MD (Pulmonary Medicine)",
                registrationNumber = "MCI-55912",
                experienceYears = 16,
                rating = 4.9f,
                reviewCount = 420,
                consultationFee = "₹ 600",
                availableSlots = listOf("11:00 AM", "02:30 PM", "05:30 PM"),
                bio = "Expert in acute respiratory infections, asthma management, COPD, and oxygen desaturation monitoring.",
                isOnlineNow = true
            )
        )
    }
}
