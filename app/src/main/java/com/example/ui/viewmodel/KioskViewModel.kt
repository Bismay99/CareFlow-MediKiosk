package com.example.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AssistantPipelineResult
import com.example.data.api.GeminiApiService
import com.example.data.model.*
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

import com.example.data.health.DemoHealthDataProvider
import com.example.data.health.HealthConnectManager
import com.example.data.repository.DoctorVerificationRepository
import com.example.data.repository.TelemedicineRepository
import com.example.data.api.ClinicalTriageEngine

enum class KioskScreen {
    ROLE_SELECTION,
    USER_LOGIN,
    USER_REGISTER,
    ADMIN_LOGIN,
    ADMIN_DASHBOARD,
    WELCOME,
    HEALTH_MONITORING,
    AI_HEALTH_ASSISTANT,
    TELEMEDICINE_DOCTORS,
    TELEMEDICINE_CALL,
    MEDICAL_RECORDS,
    DOCTOR_DASHBOARD,
    DOCTOR_CONSULTATION,
    DOCTOR_VERIFICATION,
    EMERGENCY_SOS,
    IDENTIFY,
    CONVERSATIONAL_INTAKE,
    DOCUMENT_SCAN,
    SUMMARY,
    PROFILE,
    APPOINTMENTS
}

class KioskViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = GeminiApiService()
    private val preferencesRepo = UserPreferencesRepository(application)
    private val authRepo = com.example.data.repository.AuthRepository()
    private val hospitalRepo = com.example.data.repository.HospitalDataRepository()
    private val patientHistoryRepo = com.example.data.repository.PatientHistoryRepository()
    private val toolRegistry = com.example.data.api.AssistantToolRegistry(hospitalRepo)
    private val voiceEngine = com.example.data.api.VoiceAssistantEngine(toolRegistry)
    private val voiceConversationHistory = mutableListOf<Pair<String, String>>()

    val healthConnectManager = HealthConnectManager(application)
    val doctorVerificationRepo = DoctorVerificationRepository()
    val telemedicineRepo = TelemedicineRepository()

    // Health Monitoring State
    private val _healthDashboardVitals = MutableStateFlow(DemoHealthDataProvider.getDashboardVitals())
    val healthDashboardVitals: StateFlow<PatientHealthDashboardSummary> = _healthDashboardVitals.asStateFlow()

    private val _activeMetricType = MutableStateFlow(HealthMetricType.HEART_RATE)
    val activeMetricType: StateFlow<HealthMetricType> = _activeMetricType.asStateFlow()

    private val _activeTimeRange = MutableStateFlow(TimeRangeFilter.TODAY)
    val activeTimeRange: StateFlow<TimeRangeFilter> = _activeTimeRange.asStateFlow()

    private val _selectedMetricTrend = MutableStateFlow(DemoHealthDataProvider.getTrendSummary(HealthMetricType.HEART_RATE, TimeRangeFilter.TODAY))
    val selectedMetricTrend: StateFlow<HealthTrendSummary> = _selectedMetricTrend.asStateFlow()

    // Telemedicine State
    val telemedicineDoctors: StateFlow<List<DoctorProfile>> = telemedicineRepo.doctors
    val activeTelemedicineSession: StateFlow<TeleconsultationSession?> = telemedicineRepo.activeSession

    // AI Triage & Intake Summary State
    private val _aiTriageResult = MutableStateFlow<AiTriageResult?>(null)
    val aiTriageResult: StateFlow<AiTriageResult?> = _aiTriageResult.asStateFlow()

    private val _aiClinicianDraftSummary = MutableStateFlow("")
    val aiClinicianDraftSummary: StateFlow<String> = _aiClinicianDraftSummary.asStateFlow()

    // Doctor Verification State
    val pendingVerifications: StateFlow<List<VerifiableClinicalEntity>> = doctorVerificationRepo.pendingEntities
    val verificationAuditLog: StateFlow<List<String>> = doctorVerificationRepo.verifiedAuditLog

    // Navigation State (Starts at ROLE_SELECTION as required)
    private val _currentScreen = MutableStateFlow(KioskScreen.ROLE_SELECTION)
    val currentScreen: StateFlow<KioskScreen> = _currentScreen.asStateFlow()

    // Authentication & Roles
    val currentUser: StateFlow<AuthUser?> = authRepo.currentUser
    val authState: StateFlow<AuthState> = authRepo.authState
    val demoPatients = authRepo.demoPatients
    val demoAdmin = authRepo.demoAdmin

    // Patient History Management (Admin Tool & Master EHR)
    val patientHistories: StateFlow<List<PatientMasterHistory>> = patientHistoryRepo.patientHistories

    // Live Hospital Operations & Staff Requests
    val staffAssistanceRequests: StateFlow<List<StaffAssistanceRequest>> = hospitalRepo.staffAssistanceRequests
    val liveDepartmentQueues: StateFlow<List<QueueItem>> = hospitalRepo.liveDepartmentQueues

    // Hospital Appointments
    private val _appointments = MutableStateFlow<List<HospitalAppointment>>(authRepo.demoPatients[0].appointments)
    val appointments: StateFlow<List<HospitalAppointment>> = _appointments.asStateFlow()

    // Voice & Accessibility Preferences
    private val _isVoiceAssistantEnabled = MutableStateFlow(true)
    val isVoiceAssistantEnabled: StateFlow<Boolean> = _isVoiceAssistantEnabled.asStateFlow()

    private val _isTtsEnabled = MutableStateFlow(true)
    val isTtsEnabled: StateFlow<Boolean> = _isTtsEnabled.asStateFlow()

    private val _isVoiceIntakeEnabled = MutableStateFlow(true)
    val isVoiceIntakeEnabled: StateFlow<Boolean> = _isVoiceIntakeEnabled.asStateFlow()

    // Screen 1: Language
    private val _selectedLanguage = MutableStateFlow(Language.ENGLISH)
    val selectedLanguage: StateFlow<Language> = _selectedLanguage.asStateFlow()

    // Screen 2: Patient Profile (default demo patient Rajesh Sharma, 54, Male)
    private val _patientProfile = MutableStateFlow(
        PatientProfile(
            name = "Rajesh Sharma",
            age = "54",
            gender = "Male",
            isNewPatient = false,
            abhaId = "14-2951-8402-1920",
            consentGiven = true
        )
    )
    val patientProfile: StateFlow<PatientProfile> = _patientProfile.asStateFlow()

    // Theme Mode (Persisted via DataStore: SYSTEM, LIGHT, DARK)
    private val _themeMode = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    // Accessibility / Easy Mode Toggle State (Larger font, high contrast, enlarged buttons)
    private val _isAccessibilityMode = MutableStateFlow(false)
    val isAccessibilityMode: StateFlow<Boolean> = _isAccessibilityMode.asStateFlow()

    // Hospital Dashboard: Current Visit state
    private val _activeVisit = MutableStateFlow<ActiveVisit?>(
        ActiveVisit(
            hasActiveVisit = true,
            department = "General Medicine",
            doctorName = "Dr. Rajesh Mehta",
            tokenNumber = "#24",
            queuePosition = 6,
            totalInQueue = 11,
            roomNumber = "Room 104 - 1st Floor",
            status = "Waiting",
            estimatedWaitMinutes = 18
        )
    )
    val activeVisit: StateFlow<ActiveVisit?> = _activeVisit.asStateFlow()

    // Hospital Dashboard: Recently Viewed items
    private val _recentlyViewedItems = MutableStateFlow<List<RecentlyViewedItem>>(
        listOf(
            RecentlyViewedItem(
                id = "rv-1",
                type = "Medical Report",
                title = "Lipid Panel & Fasting Glucose",
                dateOrStatus = "02 Sep 2026 • Verified",
                iconType = "report"
            ),
            RecentlyViewedItem(
                id = "rv-2",
                type = "Appointment",
                title = "Follow-up Cardiology with Dr. V. Rao",
                dateOrStatus = "Tomorrow 10:30 AM • Confirmed",
                iconType = "appointment"
            ),
            RecentlyViewedItem(
                id = "rv-3",
                type = "Prescription",
                title = "Outpatient Prescription #KMC-49210",
                dateOrStatus = "12 Aug 2026 • Active Rx",
                iconType = "prescription"
            ),
            RecentlyViewedItem(
                id = "rv-4",
                type = "Consultation",
                title = "General Medicine Intake Summary",
                dateOrStatus = "02 Sep 2026 • Completed",
                iconType = "consultation"
            )
        )
    )
    val recentlyViewedItems: StateFlow<List<RecentlyViewedItem>> = _recentlyViewedItems.asStateFlow()

    // Hospital Dashboard: AI-Powered Health Insights (Strictly non-diagnostic)
    private val _healthInsights = MutableStateFlow<List<HealthInsight>>(
        listOf(
            HealthInsight(
                id = "hi-1",
                title = "Upcoming Cardiology Appointment",
                description = "Your scheduled consultation with Dr. V. Rao is tomorrow at 10:30 AM in Room 204.",
                tag = "AI-assisted",
                iconType = "calendar"
            ),
            HealthInsight(
                id = "hi-2",
                title = "Diagnostic Lab Report Available",
                description = "Your latest Apollo fasting glucose and lipid profile results are ready for physician review.",
                tag = "AI-assisted",
                iconType = "document"
            ),
            HealthInsight(
                id = "hi-3",
                title = "Check-in Advisory",
                description = "Remember to carry your previous medical prescriptions and government ABHA health ID card.",
                tag = "AI-assisted",
                iconType = "info"
            )
        )
    )
    val healthInsights: StateFlow<List<HealthInsight>> = _healthInsights.asStateFlow()

    // Hospital Dashboard: Recent Medical Records
    private val _recentRecords = MutableStateFlow<List<RecentRecordItem>>(
        listOf(
            RecentRecordItem(
                id = "rec-1",
                department = "General Medicine",
                recordType = "Consultation",
                doctorOrFacility = "Dr. Rajesh Mehta, MD",
                date = "02 Sep 2026",
                summary = "Routine outpatient clinical intake, Stage 1 hypertension check and vitals assessment."
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
            ),
            RecentRecordItem(
                id = "rec-4",
                department = "Internal Medicine",
                recordType = "Medical Record",
                doctorOrFacility = "City General Hospital",
                date = "15 Jun 2026",
                summary = "Annual health check-up, normal ECG, baseline metabolic assessment completed."
            )
        )
    )
    val recentRecords: StateFlow<List<RecentRecordItem>> = _recentRecords.asStateFlow()

    // Hospital Dashboard: Recent Activity Timeline
    private val _recentActivities = MutableStateFlow<List<RecentActivityItem>>(
        listOf(
            RecentActivityItem(
                id = "act-1",
                title = "Queue updated",
                subtitle = "Token #24 assigned in General Medicine queue (Room 104)",
                timestamp = "Today, 10:00 AM",
                isCompleted = true
            ),
            RecentActivityItem(
                id = "act-2",
                title = "Document uploaded",
                subtitle = "Apollo Fasting Lipid & Blood Glucose Report scanned and parsed",
                timestamp = "Yesterday, 04:30 PM",
                isCompleted = true
            ),
            RecentActivityItem(
                id = "act-3",
                title = "Appointment booked",
                subtitle = "Follow-up consultation confirmed with Dr. V. Rao in Cardiology",
                timestamp = "03 Sep 2026",
                isCompleted = true
            ),
            RecentActivityItem(
                id = "act-4",
                title = "Consultation completed",
                subtitle = "General Medicine SOAP clinical note signed and archived",
                timestamp = "12 Aug 2026",
                isCompleted = true
            ),
            RecentActivityItem(
                id = "act-5",
                title = "Prescription dispensed",
                subtitle = "Hospital pharmacy fulfilled 3 outpatient medications",
                timestamp = "12 Aug 2026",
                isCompleted = true
            )
        )
    )
    val recentActivities: StateFlow<List<RecentActivityItem>> = _recentActivities.asStateFlow()

    // Hospital Dashboard: Notifications
    private val _notifications = MutableStateFlow<List<HospitalNotification>>(
        listOf(
            HospitalNotification(
                id = "notif-1",
                title = "Token #24 Approaching",
                message = "You are #6 in queue for Room 104. Please remain seated in Waiting Area B.",
                timestamp = "5 mins ago",
                isUrgent = true,
                isRead = false
            ),
            HospitalNotification(
                id = "notif-2",
                title = "Lab Results Verified",
                message = "Your blood test profile has been uploaded and verified by City General Pathology.",
                timestamp = "1 hour ago",
                isUrgent = false,
                isRead = false
            ),
            HospitalNotification(
                id = "notif-3",
                title = "Welcome to City General",
                message = "Need physical or wheelchair assistance? Tap Request Staff Help on your screen.",
                timestamp = "Today, 08:30 AM",
                isUrgent = false,
                isRead = true
            )
        )
    )
    val notifications: StateFlow<List<HospitalNotification>> = _notifications.asStateFlow()

    // Real Home AI Voice Assistant State (Separate from medical intake)
    private val _voiceAssistantState = MutableStateFlow(VoiceAssistantState.IDLE)
    val voiceAssistantState: StateFlow<VoiceAssistantState> = _voiceAssistantState.asStateFlow()

    private val _isVoiceAssistantOpen = MutableStateFlow(false)
    val isVoiceAssistantOpen: StateFlow<Boolean> = _isVoiceAssistantOpen.asStateFlow()

    private val _voiceAssistantTranscript = MutableStateFlow("")
    val voiceAssistantTranscript: StateFlow<String> = _voiceAssistantTranscript.asStateFlow()

    private val _voiceAssistantResponse = MutableStateFlow("")
    val voiceAssistantResponse: StateFlow<String> = _voiceAssistantResponse.asStateFlow()

    private val _voiceAssistantStatusText = MutableStateFlow("Tap the microphone or type below")
    val voiceAssistantStatusText: StateFlow<String> = _voiceAssistantStatusText.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    // Screen 3: Conversational Intake
    private val _socratesIntake = MutableStateFlow(SocratesIntake())
    val socratesIntake: StateFlow<SocratesIntake> = _socratesIntake.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _quickReplies = MutableStateFlow<List<String>>(emptyList())
    val quickReplies: StateFlow<List<String>> = _quickReplies.asStateFlow()

    // Screen 4: Document Scan
    private val _scannedBitmap = MutableStateFlow<Bitmap?>(null)
    val scannedBitmap: StateFlow<Bitmap?> = _scannedBitmap.asStateFlow()

    private val _isDocumentScanning = MutableStateFlow(false)
    val isDocumentScanning: StateFlow<Boolean> = _isDocumentScanning.asStateFlow()

    private val _extractedDocument = MutableStateFlow(ExtractedDocument())
    val extractedDocument: StateFlow<ExtractedDocument> = _extractedDocument.asStateFlow()

    // Screen 5: Summary
    private val _clinicalSummary = MutableStateFlow<ClinicalSummary?>(null)
    val clinicalSummary: StateFlow<ClinicalSummary?> = _clinicalSummary.asStateFlow()

    private val _isGeneratingSummary = MutableStateFlow(false)
    val isGeneratingSummary: StateFlow<Boolean> = _isGeneratingSummary.asStateFlow()

    private val _isSubmitted = MutableStateFlow(false)
    val isSubmitted: StateFlow<Boolean> = _isSubmitted.asStateFlow()

    // TTS state
    private var tts: TextToSpeech? = null
    private val _isTtsSpeaking = MutableStateFlow(false)
    val isTtsSpeaking: StateFlow<Boolean> = _isTtsSpeaking.asStateFlow()

    // Red-Flag Detection State
    private val _isRedFlagDetected = MutableStateFlow(false)
    val isRedFlagDetected: StateFlow<Boolean> = _isRedFlagDetected.asStateFlow()

    private val _redFlagMessage = MutableStateFlow("")
    val redFlagMessage: StateFlow<String> = _redFlagMessage.asStateFlow()

    // Dedicated Critical & Emergency System
    private val _isEmergencyAlertActive = MutableStateFlow(false)
    val isEmergencyAlertActive: StateFlow<Boolean> = _isEmergencyAlertActive.asStateFlow()

    private val _activeEmergencyType = MutableStateFlow("Code Red: Rapid Response Dispatched")
    val activeEmergencyType: StateFlow<String> = _activeEmergencyType.asStateFlow()

    private val _showEmergencyCenterDialog = MutableStateFlow(false)
    val showEmergencyCenterDialog: StateFlow<Boolean> = _showEmergencyCenterDialog.asStateFlow()

    private val _emergencyEtaSeconds = MutableStateFlow(45)
    val emergencyEtaSeconds: StateFlow<Int> = _emergencyEtaSeconds.asStateFlow()

    // Home Search Query State
    private val _homeSearchQuery = MutableStateFlow("")
    val homeSearchQuery: StateFlow<String> = _homeSearchQuery.asStateFlow()

    init {
        initTts()
        viewModelScope.launch {
            preferencesRepo.themeModeFlow.collect { _themeMode.value = it }
        }
        viewModelScope.launch {
            preferencesRepo.easyModeFlow.collect { _isAccessibilityMode.value = it }
        }
        viewModelScope.launch {
            preferencesRepo.languageFlow.collect {
                _selectedLanguage.value = it
                setTtsLanguage(it)
            }
        }
        viewModelScope.launch {
            preferencesRepo.voiceAssistantFlow.collect { _isVoiceAssistantEnabled.value = it }
        }
        viewModelScope.launch {
            preferencesRepo.ttsEnabledFlow.collect { _isTtsEnabled.value = it }
        }
        viewModelScope.launch {
            preferencesRepo.voiceIntakeFlow.collect { _isVoiceIntakeEnabled.value = it }
        }
        startKioskSessionWatchdog()
    }

    // Kiosk Session Privacy Timeout (HIPAA / Patient Data Protection)
    private val _sessionSecondsRemaining = MutableStateFlow(180)
    val sessionSecondsRemaining: StateFlow<Int> = _sessionSecondsRemaining.asStateFlow()

    fun userActivityDetected() {
        _sessionSecondsRemaining.value = 180
    }

    private fun startKioskSessionWatchdog() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val screen = _currentScreen.value
                val isAuthenticated = screen != KioskScreen.ROLE_SELECTION &&
                        screen != KioskScreen.USER_LOGIN &&
                        screen != KioskScreen.ADMIN_LOGIN
                if (isAuthenticated) {
                    val rem = _sessionSecondsRemaining.value - 1
                    if (rem <= 0) {
                        _sessionSecondsRemaining.value = 180
                        logout()
                    } else {
                        _sessionSecondsRemaining.value = rem
                    }
                } else {
                    _sessionSecondsRemaining.value = 180
                }
            }
        }
    }

    // Role-Based Auth Actions
    fun loginPatient(identifier: String, pass: String) {
        viewModelScope.launch {
            val result = authRepo.loginPatient(identifier, pass)
            result.onSuccess { user ->
                val matched = authRepo.demoPatients.find { it.user.id == user.id }
                if (matched != null) {
                    applyDemoPatient(matched)
                } else {
                    _patientProfile.value = _patientProfile.value.copy(name = user.name, abhaId = user.abhaId)
                }
                _currentScreen.value = KioskScreen.WELCOME
            }
        }
    }

    fun loginAdmin(email: String, pass: String, pin: String) {
        viewModelScope.launch {
            val result = authRepo.loginAdmin(email, pass, pin)
            result.onSuccess {
                _currentScreen.value = KioskScreen.ADMIN_DASHBOARD
            }
        }
    }

    fun loginDemoPatient(index: Int) {
        val demo = authRepo.loginDemoPatient(index)
        applyDemoPatient(demo)
        _currentScreen.value = KioskScreen.WELCOME
    }

    fun loginDemoAdmin() {
        authRepo.loginDemoAdmin()
        _currentScreen.value = KioskScreen.ADMIN_DASHBOARD
    }

    fun logout() {
        stopSpeaking()
        authRepo.logout()
        _currentScreen.value = KioskScreen.ROLE_SELECTION
    }

    private fun applyDemoPatient(demo: DemoPatientData) {
        _patientProfile.value = demo.profile
        _activeVisit.value = demo.visit
        _appointments.value = demo.appointments
        _recentRecords.value = demo.records
        _recentActivities.value = demo.activities
        _notifications.value = demo.notifications
    }

    // User Preferences Settings
    fun setVoiceAssistantEnabled(enabled: Boolean) {
        _isVoiceAssistantEnabled.value = enabled
        viewModelScope.launch { preferencesRepo.setVoiceAssistantEnabled(enabled) }
    }

    fun setTtsEnabled(enabled: Boolean) {
        _isTtsEnabled.value = enabled
        viewModelScope.launch { preferencesRepo.setTtsEnabled(enabled) }
    }

    fun setVoiceIntakeEnabled(enabled: Boolean) {
        _isVoiceIntakeEnabled.value = enabled
        viewModelScope.launch { preferencesRepo.setVoiceIntakeEnabled(enabled) }
    }

    // Appointments Management
    fun bookNewAppointment(doctorName: String, dept: String, date: String, room: String) {
        val newApt = HospitalAppointment(
            id = "apt-${System.currentTimeMillis()}",
            patientId = currentUser.value?.id ?: "patient-001",
            doctorName = doctorName,
            specialty = dept,
            department = dept,
            date = date,
            time = date.substringAfter(", ").ifBlank { "10:30 AM" },
            room = room,
            status = "Confirmed",
            visitType = "In-Person"
        )
        _appointments.value = listOf(newApt) + _appointments.value
        speakText("Appointment with $doctorName confirmed for $date in $room.")
    }

    // Admin Operations
    fun callNextPatientInDepartment(dept: String) {
        hospitalRepo.callNextPatientInDepartment(dept)
    }

    fun markPatientInConsultation(token: String) {
        hospitalRepo.markPatientInConsultation(token)
    }

    fun dispatchStaffToRequest(requestId: String) {
        hospitalRepo.dispatchStaffToRequest(requestId)
    }

    fun resolveStaffRequest(requestId: String) {
        hospitalRepo.resolveStaffRequest(requestId)
    }

    fun toggleAccessibilityMode() {
        val newMode = !_isAccessibilityMode.value
        _isAccessibilityMode.value = newMode
        viewModelScope.launch {
            preferencesRepo.setEasyMode(newMode)
        }
    }

    fun setHomeSearchQuery(query: String) {
        _homeSearchQuery.value = query
    }

    fun dismissRedFlag() {
        _isRedFlagDetected.value = false
    }

    fun openEmergencyCenterDialog() {
        _showEmergencyCenterDialog.value = true
    }

    fun closeEmergencyCenterDialog() {
        _showEmergencyCenterDialog.value = false
    }

    fun triggerEmergencySos(emergencyType: String = "Code Red: Rapid Response Dispatched") {
        _isEmergencyAlertActive.value = true
        _activeEmergencyType.value = emergencyType
        _emergencyEtaSeconds.value = 45

        val newNotif = HospitalNotification(
            id = "sos-${System.currentTimeMillis()}",
            title = "🚨 EMERGENCY CODE ACTIVATED",
            message = "$emergencyType at Kiosk 01 (Main Atrium). Rapid Response Team dispatched.",
            timestamp = "Just now",
            isUrgent = true,
            isRead = false
        )
        _notifications.value = listOf(newNotif) + _notifications.value

        hospitalRepo.requestStaffHelp(
            patientName = _patientProfile.value.name.ifBlank { "Patient in Distress" },
            location = "Kiosk 01 (Main Atrium)",
            reason = "CRITICAL EMERGENCY SOS: $emergencyType",
            isUrgent = true
        )

        speakText("Emergency alert activated. The hospital rapid response medical team is en route to your location immediately.")
    }

    fun cancelEmergencySos() {
        _isEmergencyAlertActive.value = false
        speakText("Emergency alert has been cancelled.")
    }

    fun dispatchEmergencyPorter() {
        triggerEmergencySos("Emergency Wheelchair & Stretcher Dispatch")
        speakText("Emergency wheelchair and porter assistance has been dispatched to Kiosk 01.")
    }

    fun callEmergencyDesk() {
        speakText("Connecting to Hospital Emergency Triage Desk at extension 108. Please stay near the kiosk.")
    }

    fun reportCriticalSymptom(symptom: String) {
        _isRedFlagDetected.value = true
        _redFlagMessage.value = "CRITICAL ALERT: $symptom reported! Triage staff and priority medical team alerted. Please proceed to the Emergency Desk or wait for the assigned responder."
        triggerEmergencySos("Critical Symptom: $symptom")
    }

    fun alertHospitalStaff() {
        speakText("Urgent assistance requested. Hospital triage staff have been notified for priority assessment.")
    }

    fun checkForRedFlags(text: String) {
        val lower = text.lowercase()

        // Cardiac Emergency Check: Chest pain combined with breathlessness, shortness of breath, or sweating
        val hasChestPain = lower.contains("chest pain") || lower.contains("chest pressure") ||
            lower.contains("chest tightness") || lower.contains("pain in chest") || lower.contains("angina") ||
            lower.contains("heart pain") || lower.contains("सीने में दर्द") || lower.contains("छाती में दर्द")

        val hasBreathlessness = lower.contains("breathless") || lower.contains("short of breath") ||
            lower.contains("shortness of breath") || lower.contains("hard to breathe") ||
            lower.contains("difficulty breathing") || lower.contains("can't breathe") ||
            lower.contains("sweat") || lower.contains("dyspnea") || lower.contains("सांस फूलना") ||
            lower.contains("सांस लेने में तकलीफ")

        // Stroke-like symptoms: Facial droop, arm/leg weakness, slurred speech, sudden numbness
        val hasStrokeSymptoms = lower.contains("stroke") || lower.contains("paralysis") ||
            lower.contains("facial droop") || lower.contains("face drooping") ||
            (lower.contains("face") && lower.contains("numb")) ||
            lower.contains("slurred speech") || lower.contains("trouble speaking") ||
            lower.contains("cannot speak") || lower.contains("arm weakness") ||
            (lower.contains("weakness") && lower.contains("arm")) ||
            lower.contains("sudden numbness") || lower.contains("one side") ||
            lower.contains("लकवा") || lower.contains("बोलने में परेशानी")

        // Acute emergency (syncope, collapse, severe bleeding)
        val hasAcuteCollapse = lower.contains("passed out") || lower.contains("unconscious") ||
            lower.contains("collapsed") || lower.contains("vomiting blood") || lower.contains("severe bleeding")

        if (hasChestPain && hasBreathlessness) {
            _isRedFlagDetected.value = true
            _redFlagMessage.value = "CRITICAL RED-FLAG ALERT: Combination of Chest Pain and Breathlessness detected! Immediate hospital staff attention required. Do NOT wait in the routine queue."
        } else if (hasStrokeSymptoms) {
            _isRedFlagDetected.value = true
            _redFlagMessage.value = "CRITICAL RED-FLAG ALERT: Potential stroke-like symptoms detected (facial droop, speech difficulty, or weakness)! Please notify triage or emergency nurses immediately."
        } else if (hasAcuteCollapse) {
            _isRedFlagDetected.value = true
            _redFlagMessage.value = "URGENT CLINICAL ALERT: Acute high-risk symptoms reported. Immediate emergency staff attention required."
        }
    }

    private fun initTts() {
        try {
            tts = TextToSpeech(getApplication()) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    setTtsLanguage(_selectedLanguage.value)
                }
            }
        } catch (e: Exception) {
            Log.e("KioskViewModel", "TTS initialization failed", e)
        }
    }

    private fun setTtsLanguage(language: Language) {
        val loc = when (language) {
            Language.HINDI -> Locale.forLanguageTag("hi-IN")
            Language.BENGALI -> Locale.forLanguageTag("bn-IN")
            Language.TAMIL -> Locale.forLanguageTag("ta-IN")
            Language.TELUGU -> Locale.forLanguageTag("te-IN")
            Language.MARATHI -> Locale.forLanguageTag("mr-IN")
            else -> Locale.ENGLISH
        }
        try {
            tts?.language = loc
        } catch (e: Exception) {
            tts?.language = Locale.ENGLISH
        }
    }

    fun speakText(text: String) {
        if (text.isBlank()) return
        stopSpeaking()
        _isTtsSpeaking.value = true
        setTtsLanguage(_selectedLanguage.value)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kiosk_tts")
    }

    fun stopSpeaking() {
        _isTtsSpeaking.value = false
        tts?.stop()
    }

    fun speakConsent() {
        speakText(_selectedLanguage.value.ttsPrompt)
    }

    // Navigation methods
    fun navigateTo(screen: KioskScreen) {
        stopSpeaking()
        _currentScreen.value = screen
        if (screen == KioskScreen.CONVERSATIONAL_INTAKE && _chatMessages.value.isEmpty()) {
            startInitialChat()
        } else if (screen == KioskScreen.SUMMARY && _clinicalSummary.value == null) {
            generateSummary()
        }
    }

    fun setLanguage(language: Language) {
        _selectedLanguage.value = language
        setTtsLanguage(language)
        viewModelScope.launch {
            preferencesRepo.setLanguage(language)
        }
    }

    // Patient Profile Form updates
    fun updatePatientName(name: String) {
        _patientProfile.value = _patientProfile.value.copy(name = name)
    }

    fun updatePatientAge(age: String) {
        val filtered = age.filter { it.isDigit() }
        _patientProfile.value = _patientProfile.value.copy(age = filtered)
    }

    fun updatePatientGender(gender: String) {
        _patientProfile.value = _patientProfile.value.copy(gender = gender)
    }

    fun updateAbhaId(id: String) {
        _patientProfile.value = _patientProfile.value.copy(abhaId = id)
    }

    fun toggleNewPatient(isNew: Boolean) {
        _patientProfile.value = _patientProfile.value.copy(isNewPatient = isNew)
    }

    fun toggleConsent(consent: Boolean) {
        _patientProfile.value = _patientProfile.value.copy(consentGiven = consent)
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun bookVideoConsultation(specialty: String = "General Physician") {
        speakText("Video consultation with $specialty has been queued. You will receive an SMS and screen notification with the secure link.")
        _notifications.value = listOf(
            HospitalNotification(
                id = "video-${System.currentTimeMillis()}",
                title = "Video Consultation Booked",
                message = "Your remote consult with $specialty is scheduled. Link sent to registered phone.",
                timestamp = "Just now",
                isUrgent = false,
                isRead = false
            )
        ) + _notifications.value
    }

    fun bookLabTest(testName: String = "Diagnostic Profile") {
        speakText("Lab test slot for $testName booked. Please report to the Ground Floor Diagnostic Laboratory.")
        _notifications.value = listOf(
            HospitalNotification(
                id = "lab-${System.currentTimeMillis()}",
                title = "Lab Test Slot Reserved",
                message = "$testName slot booked for today. Fasting token issued.",
                timestamp = "Just now",
                isUrgent = false,
                isRead = false
            )
        ) + _notifications.value
    }

    fun planSurgery(procedureName: String = "Elective Procedure") {
        speakText("Surgical pre-admission counseling request created. An anesthesiology coordinator will consult you.")
        _notifications.value = listOf(
            HospitalNotification(
                id = "surgery-${System.currentTimeMillis()}",
                title = "Surgery Pre-Planning Logged",
                message = "Clinical pre-assessment coordinator notified for $procedureName.",
                timestamp = "Just now",
                isUrgent = false,
                isRead = false
            )
        ) + _notifications.value
    }

    fun orderMedicines(refillText: String = "Prescription Refill") {
        speakText("Medicine request sent to the hospital central dispensary for packaging.")
        _notifications.value = listOf(
            HospitalNotification(
                id = "pharmacy-${System.currentTimeMillis()}",
                title = "Pharmacy Order Processing",
                message = "Refill request received for dispensing at Hospital Pharmacy Counter 3.",
                timestamp = "Just now",
                isUrgent = false,
                isRead = false
            )
        ) + _notifications.value
    }

    // Telemedicine Actions
    fun startTeleconsultationSession(doctor: DoctorProfile, type: ConsultationType = ConsultationType.VIDEO_CALL) {
        telemedicineRepo.startSession(
            appointmentId = "apt-live-${System.currentTimeMillis()}",
            patientId = currentUser.value?.id ?: "patient-001",
            patientName = _patientProfile.value.name.ifBlank { "Rajesh Sharma" },
            doctor = doctor,
            type = type
        )
        _currentScreen.value = KioskScreen.TELEMEDICINE_CALL
        speakText("Connecting to live teleconsultation with Dr. ${doctor.name}.")
    }

    fun toggleTelemedicineAudio() {
        telemedicineRepo.toggleAudio()
    }

    fun toggleTelemedicineVideo() {
        telemedicineRepo.toggleVideo()
    }

    fun flipTelemedicineCamera() {
        telemedicineRepo.flipCamera()
    }

    fun sendTelemedicineChatMessage(text: String) {
        telemedicineRepo.sendChatMessage(
            text = text,
            isDoctor = currentUser.value?.role == com.example.data.model.UserRole.ADMIN,
            senderName = _patientProfile.value.name.ifBlank { "Patient" }
        )
    }

    fun endTeleconsultationSession() {
        telemedicineRepo.endSession()
        _currentScreen.value = KioskScreen.WELCOME
    }

    fun saveTeleconsultationNotes(notes: String, advice: String, medicines: List<String>) {
        val session = activeTelemedicineSession.value
        val doctorName = session?.doctorName ?: "Dr. Specialist"
        val doctorSpec = session?.doctorSpecialty ?: "Internal Medicine"
        val presMeds = medicines.map { medName ->
            PrescriptionMedicine(
                medicineName = medName,
                dosage = "As Directed",
                frequency = MedicineFrequency.ONCE_DAILY,
                foodTiming = FoodTiming.AFTER_FOOD,
                durationDays = 5,
                specialInstructions = advice
            )
        }

        recordPatientConsultation(
            patientId = currentUser.value?.id ?: "patient-001",
            patientName = _patientProfile.value.name.ifBlank { "Rajesh Sharma" },
            age = _patientProfile.value.age.ifBlank { "54" },
            gender = _patientProfile.value.gender,
            abhaId = _patientProfile.value.abhaId,
            phone = "+91 98000 12345",
            doctorName = doctorName,
            doctorSpecialty = doctorSpec,
            department = "Telemedicine Outpatient",
            consultationDateTime = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date()),
            roomNumber = "Virtual Telehealth Room",
            chiefComplaint = _socratesIntake.value.chiefComplaint.ifBlank { "Remote Teleconsultation Intake" },
            diagnosis = "Teleconsultation Assessment Completed",
            clinicalNotes = notes,
            vitals = ConsultationVitals(),
            medicines = presMeds,
            labInvestigations = emptyList(),
            followUpDate = "In 2 weeks",
            doctorAdvice = advice
        )

        speakText("Teleconsultation notes and digital prescriptions have been committed to your permanent medical record.")
    }

    fun commitVerifiedEntitiesToEhr() {
        val verified = pendingVerifications.value.filter {
            it.status == ClinicalVerificationStatus.DOCTOR_VERIFIED || it.status == ClinicalVerificationStatus.DOCTOR_EDITED
        }
        val pName = _patientProfile.value.name.ifBlank { "Rajesh Sharma" }
        val now = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())

        val rxMeds = verified.filter { it.fieldCategory.contains("Medication", ignoreCase = true) || it.fieldCategory.contains("Prescription", ignoreCase = true) }.map {
            PrescriptionMedicine(
                medicineName = it.fieldName,
                dosage = it.verifiedValue ?: it.extractedValue,
                frequency = MedicineFrequency.ONCE_DAILY,
                foodTiming = FoodTiming.AFTER_FOOD,
                durationDays = 5,
                specialInstructions = it.doctorRemarks
            )
        }

        recordPatientConsultation(
            patientId = currentUser.value?.id ?: "patient-001",
            patientName = pName,
            age = _patientProfile.value.age.ifBlank { "54" },
            gender = _patientProfile.value.gender,
            abhaId = _patientProfile.value.abhaId,
            phone = "+91 98000 12345",
            doctorName = "Dr. Ananya Sharma",
            doctorSpecialty = "General Medicine",
            department = "Verified Clinical Intake",
            consultationDateTime = now,
            roomNumber = "OPD Desk 104",
            chiefComplaint = "Verified OCR Document Import",
            diagnosis = verified.find { it.fieldCategory.contains("Diagnosis", ignoreCase = true) }?.let { it.verifiedValue ?: it.extractedValue } ?: "Essential Hypertension Stage 1",
            clinicalNotes = "Doctor verified and signed off on ${verified.size} clinical entities extracted from prior hospital documentation.",
            vitals = ConsultationVitals(),
            medicines = rxMeds,
            labInvestigations = verified.filter { it.fieldCategory.contains("Lab", ignoreCase = true) }.map { "${it.fieldName}: ${it.verifiedValue ?: it.extractedValue}" },
            followUpDate = "In 1 month",
            doctorAdvice = "Continue prescribed therapy. All findings doctor-verified."
        )

        speakText("Doctor-verified records promoted to patient master medical file.")
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        viewModelScope.launch {
            preferencesRepo.setThemeMode(mode)
        }
    }

    fun startVisit() {
        _activeVisit.value = ActiveVisit(
            hasActiveVisit = true,
            department = "General Medicine",
            doctorName = "Dr. Rajesh Mehta",
            tokenNumber = "#24",
            queuePosition = 6,
            totalInQueue = 11,
            roomNumber = "Room 104 - 1st Floor",
            status = "Waiting",
            estimatedWaitMinutes = 18
        )
        speakText("Visit started. Your token number is 24 in General Medicine, Room 104.")
    }

    fun toggleActiveVisit() {
        val current = _activeVisit.value
        if (current == null || !current.hasActiveVisit) {
            startVisit()
        } else {
            _activeVisit.value = current.copy(hasActiveVisit = false)
            speakText("You have checked out of your active visit.")
        }
    }

    fun openVoiceAssistant(initialQuery: String? = null) {
        _isVoiceAssistantOpen.value = true
        if (!initialQuery.isNullOrBlank()) {
            _voiceAssistantTranscript.value = initialQuery
            _voiceAssistantState.value = VoiceAssistantState.PROCESSING
            _voiceAssistantStatusText.value = "Thinking..."
            _voiceAssistantResponse.value = ""
            processVoiceAssistantCommand(initialQuery)
        } else {
            _voiceAssistantState.value = VoiceAssistantState.IDLE
            _voiceAssistantStatusText.value = "Tap the microphone or type below"
            _voiceAssistantTranscript.value = ""
            _voiceAssistantResponse.value = ""
        }
    }

    fun closeVoiceAssistant() {
        _isVoiceAssistantOpen.value = false
        _voiceAssistantState.value = VoiceAssistantState.IDLE
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.w("KioskViewModel", "Error cleaning SpeechRecognizer", e)
        }
        stopSpeaking()
    }

    fun startVoiceAssistantListening() {
        _voiceAssistantState.value = VoiceAssistantState.LISTENING
        _voiceAssistantStatusText.value = "🎙 Listening... Speak your question clearly"
        _voiceAssistantTranscript.value = ""
        _voiceAssistantResponse.value = ""

        viewModelScope.launch(Dispatchers.Main) {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(getApplication())) {
                    speechRecognizer?.destroy()
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplication()).apply {
                        setRecognitionListener(object : RecognitionListener {
                            override fun onReadyForSpeech(params: Bundle?) {}
                            override fun onBeginningOfSpeech() {}
                            override fun onRmsChanged(rmsdB: Float) {}
                            override fun onBufferReceived(buffer: ByteArray?) {}
                            override fun onEndOfSpeech() {
                                _voiceAssistantState.value = VoiceAssistantState.PROCESSING
                                _voiceAssistantStatusText.value = "Thinking..."
                            }
                            override fun onError(error: Int) {
                                Log.w("KioskViewModel", "SpeechRecognizer error: $error")
                                if (_voiceAssistantTranscript.value.isBlank()) {
                                    _voiceAssistantState.value = VoiceAssistantState.IDLE
                                    _voiceAssistantStatusText.value = "Tap the microphone or type below"
                                }
                            }
                            override fun onResults(results: Bundle?) {
                                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                val text = matches?.firstOrNull()?.trim() ?: ""
                                if (text.isNotBlank()) {
                                    processVoiceAssistantCommand(text)
                                } else {
                                    _voiceAssistantState.value = VoiceAssistantState.IDLE
                                    _voiceAssistantStatusText.value = "Tap the microphone or type below"
                                }
                            }
                            override fun onPartialResults(partialResults: Bundle?) {
                                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                val text = matches?.firstOrNull()?.trim() ?: ""
                                if (text.isNotBlank()) {
                                    _voiceAssistantTranscript.value = text
                                }
                            }
                            override fun onEvent(eventType: Int, params: Bundle?) {}
                        })

                        val langTag = when (_selectedLanguage.value) {
                            Language.HINDI -> "hi-IN"
                            Language.TAMIL -> "ta-IN"
                            Language.TELUGU -> "te-IN"
                            Language.BENGALI -> "bn-IN"
                            Language.MARATHI -> "mr-IN"
                            else -> "en-IN"
                        }
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                        }
                        startListening(intent)
                    }
                }
            } catch (e: Exception) {
                Log.w("KioskViewModel", "SpeechRecognizer init failed", e)
            }
        }
    }

    fun stopVoiceAssistantListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.w("KioskViewModel", "Error stopping SpeechRecognizer", e)
        }
        if (_voiceAssistantTranscript.value.isNotBlank()) {
            processVoiceAssistantCommand(_voiceAssistantTranscript.value)
        } else {
            _voiceAssistantState.value = VoiceAssistantState.IDLE
            _voiceAssistantStatusText.value = "Tap the microphone or type below"
        }
    }

    fun processVoiceAssistantCommand(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            _voiceAssistantState.value = VoiceAssistantState.ERROR
            _voiceAssistantStatusText.value = "I couldn't hear that clearly. Please try again."
            _voiceAssistantResponse.value = "I couldn't hear that clearly. Please try again."
            return
        }

        _voiceAssistantTranscript.value = trimmed
        _voiceAssistantState.value = VoiceAssistantState.PROCESSING
        _voiceAssistantStatusText.value = "Thinking..."

        viewModelScope.launch {
            val result = voiceEngine.processQuery(
                rawQuery = trimmed,
                patientProfile = _patientProfile.value,
                currentUser = currentUser.value,
                activeVisit = _activeVisit.value,
                appointments = _appointments.value,
                recentRecords = _recentRecords.value,
                recentActivities = _recentActivities.value,
                extractedDoc = _extractedDocument.value,
                language = _selectedLanguage.value,
                conversationHistory = voiceConversationHistory,
                onStatusUpdate = { status ->
                    _voiceAssistantState.value = VoiceAssistantState.EXECUTING
                    _voiceAssistantStatusText.value = status
                }
            )

            // Maintain short conversation history
            voiceConversationHistory.add("user" to trimmed)
            voiceConversationHistory.add("model" to result.spokenResponse)
            if (voiceConversationHistory.size > 6) {
                voiceConversationHistory.removeAt(0)
                voiceConversationHistory.removeAt(0)
            }

            // Execute application actions
            if (result.isEmergency) {
                _isRedFlagDetected.value = true
                _redFlagMessage.value = "CRITICAL MEDICAL ALERT: Urgent distress detected via voice assistant. Emergency team dispatched."
                hospitalRepo.requestStaffHelp(
                    patientName = _patientProfile.value.name.ifBlank { "Patient at Kiosk" },
                    location = "Kiosk 01 (Main Atrium)",
                    reason = "Critical Voice Alert: $trimmed",
                    isUrgent = true
                )
            }

            when (result.intent) {
                AssistantIntent.REQUEST_STAFF_HELP -> {
                    val newNotif = HospitalNotification(
                        id = "help-${System.currentTimeMillis()}",
                        title = "Staff Assistance Dispatched",
                        message = "A member of our patient services team is on the way to help you at this kiosk.",
                        timestamp = "Just now",
                        isUrgent = true,
                        isRead = false
                    )
                    _notifications.value = listOf(newNotif) + _notifications.value
                }
                AssistantIntent.START_VISIT -> startVisit()
                AssistantIntent.CHANGE_LANGUAGE -> {
                    when (result.extraData?.lowercase()) {
                        "hi", "hindi" -> setLanguage(Language.HINDI)
                        "ta", "tamil" -> setLanguage(Language.TAMIL)
                        "te", "telugu" -> setLanguage(Language.TELUGU)
                        "bn", "bengali" -> setLanguage(Language.BENGALI)
                        "mr", "marathi" -> setLanguage(Language.MARATHI)
                        else -> setLanguage(Language.ENGLISH)
                    }
                }
                AssistantIntent.TOGGLE_EASY_MODE -> toggleAccessibilityMode()
                else -> { /* Other intents handled via tool data or navigation */ }
            }

            // Execute Navigation
            result.navigationTarget?.let { target ->
                when (target) {
                    "DOCUMENTS" -> navigateTo(KioskScreen.DOCUMENT_SCAN)
                    "APPOINTMENTS" -> navigateTo(KioskScreen.APPOINTMENTS)
                    "PROFILE" -> navigateTo(KioskScreen.PROFILE)
                    "HISTORY" -> navigateTo(KioskScreen.CONVERSATIONAL_INTAKE)
                    "HOME" -> navigateTo(KioskScreen.WELCOME)
                    else -> {}
                }
            }

            _voiceAssistantResponse.value = result.spokenResponse
            _voiceAssistantState.value = VoiceAssistantState.SPEAKING
            _voiceAssistantStatusText.value = "Speaking response..."

            if (_isTtsEnabled.value) {
                speakText(result.spokenResponse)
            }
        }
    }

    fun requestHospitalHelp() {
        val newNotif = HospitalNotification(
            id = "help-${System.currentTimeMillis()}",
            title = "Staff Assistance Dispatched",
            message = "A member of our patient services team is on the way to help you at this kiosk.",
            timestamp = "Just now",
            isUrgent = true,
            isRead = false
        )
        _notifications.value = listOf(newNotif) + _notifications.value

        hospitalRepo.requestStaffHelp(
            patientName = _patientProfile.value.name.ifBlank { "Patient at Kiosk" },
            location = "Kiosk 01 (Main Atrium)",
            reason = "In-person assistance requested from kiosk home screen.",
            isUrgent = false
        )

        speakText("Hospital staff have been notified. A patient care representative will assist you shortly.")
    }

    // Conversational Intake
    private fun startInitialChat() {
        viewModelScope.launch {
            _isAiThinking.value = true
            val patient = _patientProfile.value
            val result = apiService.generateNextChatTurn(
                patientName = patient.name.ifBlank { "Patient" },
                patientAge = patient.age.ifBlank { "Adult" },
                patientGender = patient.gender,
                intake = _socratesIntake.value,
                language = _selectedLanguage.value,
                chatHistory = emptyList(),
                latestUserReply = ""
            )

            val initialAiMsg = ChatMessage(
                sender = MessageSender.AI,
                text = result.aiText,
                quickReplies = result.quickReplies
            )
            _chatMessages.value = listOf(initialAiMsg)
            _quickReplies.value = result.quickReplies
            _isAiThinking.value = false
        }
    }

    fun sendChatMessage(userText: String) {
        if (userText.isBlank() || _isAiThinking.value) return

        // Evaluate user text for urgent red flags
        checkForRedFlags(userText)

        val userMsg = ChatMessage(
            sender = MessageSender.PATIENT,
            text = userText
        )
        val updatedHistory = _chatMessages.value + userMsg
        _chatMessages.value = updatedHistory
        _isAiThinking.value = true
        _quickReplies.value = emptyList()

        viewModelScope.launch {
            val patient = _patientProfile.value
            val currentIntake = _socratesIntake.value

            // Also check composite text of intake & history
            val compositeSymptoms = "${currentIntake.chiefComplaint} ${currentIntake.associations} $userText"
            checkForRedFlags(compositeSymptoms)

            val result = apiService.generateNextChatTurn(
                patientName = patient.name.ifBlank { "Patient" },
                patientAge = patient.age.ifBlank { "Adult" },
                patientGender = patient.gender,
                intake = currentIntake,
                language = _selectedLanguage.value,
                chatHistory = updatedHistory,
                latestUserReply = userText
            )

            // Update structured local model if a field was identified
            result.extractedField?.let { field ->
                result.extractedValue?.let { value ->
                    updateSocratesField(field, value)
                    checkForRedFlags(value)
                }
            }

            val aiMsg = ChatMessage(
                sender = MessageSender.AI,
                text = result.aiText,
                quickReplies = result.quickReplies
            )
            _chatMessages.value = updatedHistory + aiMsg
            _quickReplies.value = result.quickReplies
            _isAiThinking.value = false
        }
    }

    private fun updateSocratesField(field: String, value: String) {
        val cur = _socratesIntake.value
        val updated = when (field.lowercase()) {
            "chiefcomplaint", "chief_complaint" -> cur.copy(chiefComplaint = value)
            "site" -> cur.copy(site = value)
            "onset" -> cur.copy(onset = value)
            "character" -> cur.copy(character = value)
            "radiation" -> cur.copy(radiation = value)
            "associations" -> cur.copy(associations = value)
            "timing" -> cur.copy(timing = value)
            "exacerbatingrelieving", "exacerbating_relieving" -> cur.copy(exacerbatingRelieving = value)
            "severity" -> cur.copy(severity = value)
            "pastmedicalhistory", "past_medical_history" -> cur.copy(pastMedicalHistory = value)
            "currentmedications", "current_medications" -> cur.copy(currentMedications = value)
            "allergies" -> cur.copy(allergies = value)
            "familyhistory", "family_history" -> cur.copy(familyHistory = value)
            else -> cur
        }
        _socratesIntake.value = updated
    }

    // Document Scanning
    fun setDocumentBitmap(bitmap: Bitmap) {
        _scannedBitmap.value = bitmap
        analyzeScannedDocument(bitmap)
    }

    private fun analyzeScannedDocument(bitmap: Bitmap) {
        viewModelScope.launch {
            _isDocumentScanning.value = true
            val extracted = apiService.analyzeDocumentImage(bitmap)
            _extractedDocument.value = extracted
            _isDocumentScanning.value = false
        }
    }

    fun loadSampleMedicalDocument() {
        val sampleBitmap = createSamplePrescriptionBitmap()
        _scannedBitmap.value = sampleBitmap
        analyzeScannedDocument(sampleBitmap)
    }

    private fun createSamplePrescriptionBitmap(): Bitmap {
        val width = 800
        val height = 1000
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(AndroidColor.WHITE)

        val headerPaint = Paint().apply {
            color = AndroidColor.parseColor("#0369A1")
            textSize = 34f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = AndroidColor.parseColor("#1E293B")
            textSize = 22f
            isAntiAlias = true
        }

        val subPaint = Paint().apply {
            color = AndroidColor.parseColor("#64748B")
            textSize = 18f
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = AndroidColor.parseColor("#CBD5E1")
            strokeWidth = 2f
        }

        canvas.drawText("CITY GENERAL HOSPITAL - OUTPATIENT Rx", 40f, 60f, headerPaint)
        canvas.drawText("Department of Cardiology & Internal Medicine", 40f, 95f, subPaint)
        canvas.drawLine(40f, 115f, 760f, 115f, linePaint)

        canvas.drawText("Patient: ${_patientProfile.value.name.ifBlank { "Rajesh Sharma" }}  |  Age: ${_patientProfile.value.age.ifBlank { "54" }}  |  Date: 12/08/2026", 40f, 150f, textPaint)
        canvas.drawText("Diagnosis: Essential Hypertension Stage 1, Hyperlipidemia", 40f, 190f, textPaint)
        canvas.drawLine(40f, 215f, 760f, 215f, linePaint)

        canvas.drawText("Rx (Prescription):", 40f, 255f, headerPaint)
        canvas.drawText("1. Tab. Telmisartan 40 mg - 1 Tab OD (Morning, after food)", 50f, 295f, textPaint)
        canvas.drawText("2. Tab. Atorvastatin 10 mg - 1 Tab HS (At bedtime)", 50f, 335f, textPaint)
        canvas.drawText("3. Tab. Ecosprin 75 mg - 1 Tab OD (Post lunch)", 50f, 375f, textPaint)
        canvas.drawLine(40f, 410f, 760f, 410f, linePaint)

        canvas.drawText("Recent Fasting Lab Profile (Apollo Diagnostics):", 40f, 450f, headerPaint)
        canvas.drawText("• Total Cholesterol: 228 mg/dL [High] (Ref: < 200)", 50f, 490f, textPaint)
        canvas.drawText("• LDL Cholesterol: 142 mg/dL [High] (Ref: < 100)", 50f, 525f, textPaint)
        canvas.drawText("• HDL Cholesterol: 46 mg/dL [Normal] (Ref: > 40)", 50f, 560f, textPaint)
        canvas.drawText("• Fasting Glucose: 104 mg/dL [High] (Ref: 70-99)", 50f, 595f, textPaint)
        canvas.drawText("• Serum Creatinine: 0.92 mg/dL [Normal] (Ref: 0.7-1.2)", 50f, 630f, textPaint)

        canvas.drawLine(40f, 670f, 760f, 670f, linePaint)
        canvas.drawText("Advice: Low sodium diet, 30 min daily brisk walking. Follow up 3 months.", 40f, 710f, subPaint)
        canvas.drawText("Dr. V. Rao, MD (Cardiology) - Reg #KMC-49210", 420f, 780f, textPaint)

        return bitmap
    }

    // Editable Document Table actions
    fun updateDiagnosis(newDiagnosis: String) {
        val current = _extractedDocument.value
        _extractedDocument.value = current.copy(diagnosis = newDiagnosis)
    }

    fun updateMedication(index: Int, updated: MedicationItem) {
        val current = _extractedDocument.value
        if (index in current.medications.indices) {
            val newList = current.medications.toMutableList()
            newList[index] = updated
            _extractedDocument.value = current.copy(medications = newList)
        }
    }

    fun addMedication(item: MedicationItem) {
        val current = _extractedDocument.value
        val newList = current.medications.toMutableList().apply { add(item) }
        _extractedDocument.value = current.copy(medications = newList)
    }

    fun removeMedication(index: Int) {
        val current = _extractedDocument.value
        if (index in current.medications.indices) {
            val newList = current.medications.toMutableList().apply { removeAt(index) }
            _extractedDocument.value = current.copy(medications = newList)
        }
    }

    fun updateLabValue(index: Int, updated: LabValueItem) {
        val current = _extractedDocument.value
        if (index in current.labValues.indices) {
            val newList = current.labValues.toMutableList()
            newList[index] = updated
            _extractedDocument.value = current.copy(labValues = newList)
        }
    }

    fun addLabValue(item: LabValueItem) {
        val current = _extractedDocument.value
        val newList = current.labValues.toMutableList().apply { add(item) }
        _extractedDocument.value = current.copy(labValues = newList)
    }

    fun removeLabValue(index: Int) {
        val current = _extractedDocument.value
        if (index in current.labValues.indices) {
            val newList = current.labValues.toMutableList().apply { removeAt(index) }
            _extractedDocument.value = current.copy(labValues = newList)
        }
    }

    // Summary Generation & Section Editing
    fun generateSummary() {
        viewModelScope.launch {
            _isGeneratingSummary.value = true
            val summary = apiService.generateClinicalSummary(
                patient = _patientProfile.value,
                intake = _socratesIntake.value,
                doc = _extractedDocument.value
            )
            _clinicalSummary.value = summary
            _isGeneratingSummary.value = false
        }
    }

    fun updateSummarySection(section: String, newContent: String) {
        val cur = _clinicalSummary.value ?: return
        val updated = when (section) {
            "Chief Complaint" -> cur.copy(chiefComplaint = newContent)
            "History of Present Illness" -> cur.copy(historyOfPresentIllness = newContent)
            "Past Medical/Surgical History" -> cur.copy(pastMedicalHistory = newContent)
            "Drug & Allergy History" -> cur.copy(drugAndAllergyHistory = newContent)
            "Family History" -> cur.copy(familyHistory = newContent)
            "Review of Systems" -> cur.copy(reviewOfSystems = newContent)
            "Prior Investigations" -> cur.copy(priorInvestigations = newContent)
            else -> cur
        }
        _clinicalSummary.value = updated
    }

    fun submitIntake() {
        _isSubmitted.value = true
    }

    // ==========================================
    // ADMIN PATIENT HISTORY MANAGEMENT TOOL
    // ==========================================
    fun recordPatientConsultation(
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
        val entry = patientHistoryRepo.recordConsultation(
            patientId = patientId,
            patientName = patientName,
            age = age,
            gender = gender,
            abhaId = abhaId,
            phone = phone,
            doctorName = doctorName,
            doctorSpecialty = doctorSpecialty,
            department = department,
            consultationDateTime = consultationDateTime,
            roomNumber = roomNumber,
            chiefComplaint = chiefComplaint,
            diagnosis = diagnosis,
            clinicalNotes = clinicalNotes,
            vitals = vitals,
            medicines = medicines,
            labInvestigations = labInvestigations,
            followUpDate = followUpDate,
            doctorAdvice = doctorAdvice,
            bloodGroup = bloodGroup,
            allergies = allergies,
            chronicConditions = chronicConditions
        )

        _notifications.value = listOf(
            HospitalNotification(
                id = "notif-hist-${System.currentTimeMillis()}",
                title = "EHR Updated: $patientName",
                message = "New clinical consultation and ${medicines.size} prescriptions recorded by Admin/Doctor.",
                timestamp = "Just now",
                isUrgent = false,
                isRead = false
            )
        ) + _notifications.value

        return entry
    }

    fun addMedicineToConsultation(consultationId: String, medicine: PrescriptionMedicine) {
        patientHistoryRepo.addMedicineToConsultation(consultationId, medicine)
    }

    fun updateMedicineStatus(patientId: String, medicineId: String, newStatus: String) {
        patientHistoryRepo.updateMedicineStatus(patientId, medicineId, newStatus)
    }

    fun speakPrescriptionSchedule(patientName: String, medicines: List<PrescriptionMedicine>) {
        if (medicines.isEmpty()) {
            speakText("No active medicines on record for $patientName.")
            return
        }
        val sb = StringBuilder()
        sb.append("Medication schedule for $patientName: ")
        medicines.filter { it.status == "Active" }.forEachIndexed { idx, med ->
            sb.append("${idx + 1}. ${med.medicineName}, ${med.dosage}. Frequency: ${med.frequency.label}. When to take: ${med.foodTiming.label}. ")
            if (med.specialInstructions.isNotBlank()) {
                sb.append("Instructions: ${med.specialInstructions}. ")
            }
        }
        speakText(sb.toString())
    }

    fun resetForNewPatient() {
        stopSpeaking()
        _currentScreen.value = KioskScreen.WELCOME
        _patientProfile.value = PatientProfile()
        _socratesIntake.value = SocratesIntake()
        _chatMessages.value = emptyList()
        _quickReplies.value = emptyList()
        _scannedBitmap.value = null
        _extractedDocument.value = ExtractedDocument()
        _clinicalSummary.value = null
        _isSubmitted.value = false
        _isRedFlagDetected.value = false
        _redFlagMessage.value = ""
        _homeSearchQuery.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            Log.w("KioskViewModel", "Error destroying SpeechRecognizer", e)
        }
    }
}
