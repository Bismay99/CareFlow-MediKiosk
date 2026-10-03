package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppThemeMode
import com.example.ui.components.CareFlowEmergencyDialog
import com.example.ui.components.KioskBottomBar
import com.example.ui.components.KioskTopBar
import com.example.ui.components.LanguageSelectDialog
import com.example.ui.screens.*
import com.example.ui.theme.MediKioskTheme
import com.example.ui.viewmodel.KioskScreen
import com.example.ui.viewmodel.KioskViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: KioskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isAccessibilityMode by viewModel.isAccessibilityMode.collectAsStateWithLifecycle()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            val isDark = when (themeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            MediKioskTheme(
                darkTheme = isDark,
                isAccessibilityMode = isAccessibilityMode
            ) {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
                val patientProfile by viewModel.patientProfile.collectAsStateWithLifecycle()
                val socratesIntake by viewModel.socratesIntake.collectAsStateWithLifecycle()
                val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
                val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
                val quickReplies by viewModel.quickReplies.collectAsStateWithLifecycle()
                val scannedBitmap by viewModel.scannedBitmap.collectAsStateWithLifecycle()
                val isDocumentScanning by viewModel.isDocumentScanning.collectAsStateWithLifecycle()
                val extractedDocument by viewModel.extractedDocument.collectAsStateWithLifecycle()
                val clinicalSummary by viewModel.clinicalSummary.collectAsStateWithLifecycle()
                val isGeneratingSummary by viewModel.isGeneratingSummary.collectAsStateWithLifecycle()
                val isSubmitted by viewModel.isSubmitted.collectAsStateWithLifecycle()
                val homeSearchQuery by viewModel.homeSearchQuery.collectAsStateWithLifecycle()
                val isRedFlagDetected by viewModel.isRedFlagDetected.collectAsStateWithLifecycle()
                val redFlagMessage by viewModel.redFlagMessage.collectAsStateWithLifecycle()
                val isEmergencyAlertActive by viewModel.isEmergencyAlertActive.collectAsStateWithLifecycle()
                val activeEmergencyType by viewModel.activeEmergencyType.collectAsStateWithLifecycle()
                val emergencyEtaSeconds by viewModel.emergencyEtaSeconds.collectAsStateWithLifecycle()
                val showEmergencyCenterDialog by viewModel.showEmergencyCenterDialog.collectAsStateWithLifecycle()

                val authState by viewModel.authState.collectAsStateWithLifecycle()
                val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
                val staffRequests by viewModel.staffAssistanceRequests.collectAsStateWithLifecycle()
                val liveQueues by viewModel.liveDepartmentQueues.collectAsStateWithLifecycle()
                val patientHistories by viewModel.patientHistories.collectAsStateWithLifecycle()
                val appointments by viewModel.appointments.collectAsStateWithLifecycle()
                val telemedicineDoctors by viewModel.telemedicineDoctors.collectAsStateWithLifecycle()
                val activeTelemedicineSession by viewModel.activeTelemedicineSession.collectAsStateWithLifecycle()
                val healthDashboardVitals by viewModel.healthDashboardVitals.collectAsStateWithLifecycle()
                val isVoiceAssistantEnabled by viewModel.isVoiceAssistantEnabled.collectAsStateWithLifecycle()
                val isTtsEnabled by viewModel.isTtsEnabled.collectAsStateWithLifecycle()
                val isVoiceIntakeEnabled by viewModel.isVoiceIntakeEnabled.collectAsStateWithLifecycle()

                var showLanguageDialog by remember { mutableStateOf(false) }

                // System Back button navigation
                BackHandler(enabled = currentScreen != KioskScreen.ROLE_SELECTION) {
                    when (currentScreen) {
                        KioskScreen.USER_LOGIN -> viewModel.navigateTo(KioskScreen.ROLE_SELECTION)
                        KioskScreen.ADMIN_LOGIN -> viewModel.navigateTo(KioskScreen.ROLE_SELECTION)
                        KioskScreen.ADMIN_DASHBOARD -> viewModel.logout()
                        KioskScreen.DOCTOR_DASHBOARD -> viewModel.logout()
                        KioskScreen.WELCOME -> viewModel.navigateTo(KioskScreen.ROLE_SELECTION)
                        KioskScreen.PROFILE -> viewModel.navigateTo(KioskScreen.WELCOME)
                        KioskScreen.APPOINTMENTS -> viewModel.navigateTo(KioskScreen.WELCOME)
                        KioskScreen.TELEMEDICINE_DOCTORS -> viewModel.navigateTo(KioskScreen.WELCOME)
                        KioskScreen.TELEMEDICINE_CALL -> viewModel.navigateTo(KioskScreen.TELEMEDICINE_DOCTORS)
                        KioskScreen.HEALTH_MONITORING -> viewModel.navigateTo(KioskScreen.WELCOME)
                        KioskScreen.AI_HEALTH_ASSISTANT -> viewModel.navigateTo(KioskScreen.WELCOME)
                        KioskScreen.MEDICAL_RECORDS -> viewModel.navigateTo(KioskScreen.WELCOME)
                        KioskScreen.DOCTOR_VERIFICATION -> viewModel.navigateTo(KioskScreen.ADMIN_DASHBOARD)
                        KioskScreen.IDENTIFY -> viewModel.navigateTo(KioskScreen.WELCOME)
                        KioskScreen.CONVERSATIONAL_INTAKE -> viewModel.navigateTo(KioskScreen.IDENTIFY)
                        KioskScreen.DOCUMENT_SCAN -> viewModel.navigateTo(KioskScreen.CONVERSATIONAL_INTAKE)
                        KioskScreen.SUMMARY -> viewModel.navigateTo(KioskScreen.DOCUMENT_SCAN)
                        KioskScreen.ROLE_SELECTION -> { /* Root screen */ }
                        else -> viewModel.navigateTo(KioskScreen.WELCOME)
                    }
                }

                val showTopBar = currentScreen in listOf(
                    KioskScreen.IDENTIFY,
                    KioskScreen.CONVERSATIONAL_INTAKE,
                    KioskScreen.DOCUMENT_SCAN,
                    KioskScreen.SUMMARY
                )

                val showBottomBar = currentScreen in listOf(
                    KioskScreen.WELCOME,
                    KioskScreen.DOCUMENT_SCAN,
                    KioskScreen.APPOINTMENTS,
                    KioskScreen.PROFILE,
                    KioskScreen.HEALTH_MONITORING,
                    KioskScreen.TELEMEDICINE_DOCTORS,
                    KioskScreen.MEDICAL_RECORDS
                )

                Scaffold(
                    topBar = {
                        if (showTopBar) {
                            KioskTopBar(
                                currentScreen = currentScreen,
                                selectedLanguage = selectedLanguage,
                                isAccessibilityMode = isAccessibilityMode,
                                onToggleAccessibility = { viewModel.toggleAccessibilityMode() },
                                onBackClick = {
                                    when (currentScreen) {
                                        KioskScreen.IDENTIFY -> viewModel.navigateTo(KioskScreen.WELCOME)
                                        KioskScreen.CONVERSATIONAL_INTAKE -> viewModel.navigateTo(KioskScreen.IDENTIFY)
                                        KioskScreen.DOCUMENT_SCAN -> viewModel.navigateTo(KioskScreen.CONVERSATIONAL_INTAKE)
                                        KioskScreen.SUMMARY -> viewModel.navigateTo(KioskScreen.DOCUMENT_SCAN)
                                        else -> {}
                                    }
                                },
                                onLanguageClick = { showLanguageDialog = true },
                                onEmergencySosClick = { viewModel.openEmergencyCenterDialog() }
                            )
                        }
                    },
                    bottomBar = {
                        if (showBottomBar) {
                            KioskBottomBar(
                                currentScreen = currentScreen,
                                onScreenSelect = { viewModel.navigateTo(it) },
                                onAppointmentsClick = {
                                    viewModel.navigateTo(KioskScreen.APPOINTMENTS)
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false)
                                    viewModel.userActivityDetected()
                                }
                            }
                    ) {
                        when (currentScreen) {
                            KioskScreen.ROLE_SELECTION -> {
                                RoleSelectionScreen(
                                    selectedLanguage = selectedLanguage,
                                    onSelectPatientRole = { viewModel.navigateTo(KioskScreen.USER_LOGIN) },
                                    onSelectAdminRole = { viewModel.navigateTo(KioskScreen.ADMIN_LOGIN) },
                                    onQuickDemoLaunch = { viewModel.loginDemoPatient(0) },
                                    onLanguageClick = { showLanguageDialog = true }
                                )
                            }
                            KioskScreen.USER_LOGIN -> {
                                UserLoginScreen(
                                    authState = authState,
                                    demoPatients = viewModel.demoPatients,
                                    onLoginClick = { id, pass -> viewModel.loginPatient(id, pass) },
                                    onSelectDemoPatient = { index -> viewModel.loginDemoPatient(index) },
                                    onBackClick = { viewModel.navigateTo(KioskScreen.ROLE_SELECTION) }
                                )
                            }
                            KioskScreen.ADMIN_LOGIN -> {
                                AdminLoginScreen(
                                    authState = authState,
                                    onAdminLoginClick = { email, pass, pin -> viewModel.loginAdmin(email, pass, pin) },
                                    onQuickAdminFill = { viewModel.loginDemoAdmin() },
                                    onBackClick = { viewModel.navigateTo(KioskScreen.ROLE_SELECTION) }
                                )
                            }
                            KioskScreen.ADMIN_DASHBOARD -> {
                                AdminDashboardScreen(
                                    adminUser = currentUser,
                                    staffRequests = staffRequests,
                                    liveQueues = liveQueues,
                                    patientHistories = patientHistories,
                                    onCallNextPatient = { dept -> viewModel.callNextPatientInDepartment(dept) },
                                    onMarkInConsultation = { token -> viewModel.markPatientInConsultation(token) },
                                    onDispatchStaff = { reqId -> viewModel.dispatchStaffToRequest(reqId) },
                                    onResolveStaffRequest = { reqId -> viewModel.resolveStaffRequest(reqId) },
                                    onRecordConsultation = { pId, pName, age, gen, abha, ph, doc, spec, dept, dt, room, cc, diag, notes, vit, meds, labs, fup, adv, bg, allerg, conds ->
                                        viewModel.recordPatientConsultation(
                                            patientId = pId,
                                            patientName = pName,
                                            age = age,
                                            gender = gen,
                                            abhaId = abha,
                                            phone = ph,
                                            doctorName = doc,
                                            doctorSpecialty = spec,
                                            department = dept,
                                            consultationDateTime = dt,
                                            roomNumber = room,
                                            chiefComplaint = cc,
                                            diagnosis = diag,
                                            clinicalNotes = notes,
                                            vitals = vit,
                                            medicines = meds,
                                            labInvestigations = labs,
                                            followUpDate = fup,
                                            doctorAdvice = adv,
                                            bloodGroup = bg,
                                            allergies = allerg,
                                            chronicConditions = conds
                                        )
                                    },
                                    onAddMedicineToConsultation = { consultId, med ->
                                        viewModel.addMedicineToConsultation(consultId, med)
                                    },
                                    onUpdateMedicineStatus = { pId, medId, status ->
                                        viewModel.updateMedicineStatus(pId, medId, status)
                                    },
                                    onSpeakPrescription = { pName, meds ->
                                        viewModel.speakPrescriptionSchedule(pName, meds)
                                    },
                                    onNavigateToVerification = { viewModel.navigateTo(KioskScreen.DOCTOR_VERIFICATION) },
                                    onNavigateToTelemedicine = { viewModel.navigateTo(KioskScreen.TELEMEDICINE_DOCTORS) },
                                    onLogoutClick = { viewModel.logout() }
                                )
                            }
                            KioskScreen.TELEMEDICINE_DOCTORS -> {
                                TelemedicineDoctorsScreen(
                                    doctors = telemedicineDoctors,
                                    upcomingAppointments = appointments,
                                    onBackClick = { viewModel.navigateTo(KioskScreen.WELCOME) },
                                    onBookAppointment = { doc, slot, type ->
                                        viewModel.bookNewAppointment(doc.name, doc.department, "Today, $slot", "Virtual Telehealth Room")
                                    },
                                    onStartInstantConsultation = { doc, type ->
                                        viewModel.startTeleconsultationSession(doc, type)
                                    }
                                )
                            }
                            KioskScreen.TELEMEDICINE_CALL -> {
                                activeTelemedicineSession?.let { sess ->
                                    TelemedicineCallScreen(
                                        session = sess,
                                        onToggleAudio = { viewModel.toggleTelemedicineAudio() },
                                        onToggleVideo = { viewModel.toggleTelemedicineVideo() },
                                        onFlipCamera = { viewModel.flipTelemedicineCamera() },
                                        onSendMessage = { viewModel.sendTelemedicineChatMessage(it) },
                                        onEndCall = { viewModel.endTeleconsultationSession() },
                                        onSaveConsultationNotes = { notes, advice, meds ->
                                            viewModel.saveTeleconsultationNotes(notes, advice, meds)
                                        }
                                    )
                                } ?: run {
                                    viewModel.navigateTo(KioskScreen.TELEMEDICINE_DOCTORS)
                                }
                            }
                            KioskScreen.HEALTH_MONITORING -> {
                                HealthMonitoringScreen(
                                    healthConnectManager = viewModel.healthConnectManager,
                                    initialDashboard = healthDashboardVitals,
                                    onBackClick = { viewModel.navigateTo(KioskScreen.WELCOME) }
                                )
                            }
                            KioskScreen.AI_HEALTH_ASSISTANT -> {
                                AiHealthAssistantScreen(
                                    patientProfile = patientProfile,
                                    socratesIntake = socratesIntake,
                                    vitalsSummary = healthDashboardVitals,
                                    onBackClick = { viewModel.navigateTo(KioskScreen.WELCOME) },
                                    onEmergencyTrigger = { reason -> viewModel.triggerEmergencySos(reason) },
                                    onNavigateToTelemedicine = { viewModel.navigateTo(KioskScreen.TELEMEDICINE_DOCTORS) },
                                    onSpeakText = { viewModel.speakText(it) }
                                )
                            }
                            KioskScreen.DOCTOR_VERIFICATION -> {
                                DoctorVerificationScreen(
                                    verificationRepo = viewModel.doctorVerificationRepo,
                                    doctorName = currentUser?.name ?: "Dr. Specialist",
                                    onBackClick = { viewModel.navigateTo(KioskScreen.ADMIN_DASHBOARD) },
                                    onCommitToEhr = { viewModel.commitVerifiedEntitiesToEhr() }
                                )
                            }
                            KioskScreen.MEDICAL_RECORDS -> {
                                MedicalRecordsScreen(
                                    patientHistories = patientHistories,
                                    recentRecords = viewModel.recentRecords.collectAsStateWithLifecycle().value,
                                    patientName = patientProfile.name.ifBlank { "Rajesh Sharma" },
                                    onBackClick = { viewModel.navigateTo(KioskScreen.WELCOME) }
                                )
                            }
                            KioskScreen.DOCTOR_DASHBOARD -> {
                                val currentDoc = telemedicineDoctors.firstOrNull() ?: DoctorProfile(
                                    id = "doc-01", name = "Dr. Ananya Sharma", specialty = "General Medicine",
                                    department = "Internal Medicine", qualifications = "MBBS, MD", registrationNumber = "MCI-48192",
                                    experienceYears = 14, rating = 4.9f, reviewCount = 384, consultationFee = "₹ 500",
                                    availableSlots = listOf("10:00 AM", "11:30 AM")
                                )
                                DoctorDashboardScreen(
                                    doctor = currentDoc,
                                    appointments = appointments,
                                    liveQueues = liveQueues,
                                    verificationRepo = viewModel.doctorVerificationRepo,
                                    patientHistories = patientHistories,
                                    onStartConsultation = { doc -> viewModel.startTeleconsultationSession(doc) },
                                    onNavigateToVerification = { viewModel.navigateTo(KioskScreen.DOCTOR_VERIFICATION) },
                                    onNavigateToRecords = { viewModel.navigateTo(KioskScreen.MEDICAL_RECORDS) },
                                    onLogoutClick = { viewModel.logout() }
                                )
                            }
                            KioskScreen.PROFILE -> {
                                PatientProfileScreen(
                                    profile = patientProfile,
                                    user = currentUser,
                                    selectedLanguage = selectedLanguage,
                                    themeMode = themeMode,
                                    isAccessibilityMode = isAccessibilityMode,
                                    isVoiceAssistantEnabled = isVoiceAssistantEnabled,
                                    isTtsEnabled = isTtsEnabled,
                                    isVoiceIntakeEnabled = isVoiceIntakeEnabled,
                                    onBackClick = { viewModel.navigateTo(KioskScreen.WELCOME) },
                                    onLanguageClick = { showLanguageDialog = true },
                                    onThemeSelect = { viewModel.setThemeMode(it) },
                                    onToggleEasyMode = { viewModel.toggleAccessibilityMode() },
                                    onToggleVoiceAssistant = { viewModel.setVoiceAssistantEnabled(it) },
                                    onToggleTts = { viewModel.setTtsEnabled(it) },
                                    onToggleVoiceIntake = { viewModel.setVoiceIntakeEnabled(it) },
                                    onLogoutClick = { viewModel.logout() }
                                )
                            }
                            KioskScreen.APPOINTMENTS -> {
                                AppointmentsScreen(
                                    appointments = appointments,
                                    onBackClick = { viewModel.navigateTo(KioskScreen.WELCOME) },
                                    onBookNewAppointment = { doctor, dept, date, room ->
                                        viewModel.bookNewAppointment(doctor, dept, date, room)
                                    }
                                )
                            }
                            KioskScreen.WELCOME -> {
                                PatientHomeScreen(viewModel = viewModel)
                            }
                            KioskScreen.IDENTIFY -> {
                                PatientIdentifyScreen(
                                    profile = patientProfile,
                                    selectedLanguage = selectedLanguage,
                                    onNameChange = { viewModel.updatePatientName(it) },
                                    onAgeChange = { viewModel.updatePatientAge(it) },
                                    onGenderChange = { viewModel.updatePatientGender(it) },
                                    onAbhaChange = { viewModel.updateAbhaId(it) },
                                    onNewPatientToggle = { viewModel.toggleNewPatient(it) },
                                    onConsentToggle = { viewModel.toggleConsent(it) },
                                    onReadConsentAloud = { viewModel.speakConsent() },
                                    onProceedClick = { viewModel.navigateTo(KioskScreen.CONVERSATIONAL_INTAKE) }
                                )
                            }
                            KioskScreen.CONVERSATIONAL_INTAKE -> {
                                HistoryIntakeScreen(
                                    chatMessages = chatMessages,
                                    quickReplies = quickReplies,
                                    isAiThinking = isAiThinking,
                                    socratesIntake = socratesIntake,
                                    selectedLanguage = selectedLanguage,
                                    isRedFlagDetected = isRedFlagDetected,
                                    redFlagMessage = redFlagMessage,
                                    onAlertStaff = { viewModel.alertHospitalStaff() },
                                    onDismissRedFlag = { viewModel.dismissRedFlag() },
                                    onSendMessage = { viewModel.sendChatMessage(it) },
                                    onReadAloud = { viewModel.speakText(it) },
                                    onProceedToDocumentScan = { viewModel.navigateTo(KioskScreen.DOCUMENT_SCAN) }
                                )
                            }
                            KioskScreen.DOCUMENT_SCAN -> {
                                DocumentScanScreen(
                                    scannedBitmap = scannedBitmap,
                                    isScanning = isDocumentScanning,
                                    extractedDoc = extractedDocument,
                                    onBitmapCaptured = { viewModel.setDocumentBitmap(it) },
                                    onLoadSampleDocument = { viewModel.loadSampleMedicalDocument() },
                                    onUpdateDiagnosis = { viewModel.updateDiagnosis(it) },
                                    onAddMedication = { viewModel.addMedication(it) },
                                    onUpdateMedication = { index, med -> viewModel.updateMedication(index, med) },
                                    onDeleteMedication = { viewModel.removeMedication(it) },
                                    onAddLabValue = { viewModel.addLabValue(it) },
                                    onUpdateLabValue = { index, lab -> viewModel.updateLabValue(index, lab) },
                                    onDeleteLabValue = { viewModel.removeLabValue(it) },
                                    onProceedToSummary = { viewModel.navigateTo(KioskScreen.SUMMARY) }
                                )
                            }
                            KioskScreen.SUMMARY -> {
                                SummaryScreen(
                                    patient = patientProfile,
                                    summary = clinicalSummary,
                                    isGenerating = isGeneratingSummary,
                                    isSubmitted = isSubmitted,
                                    onEditSection = { section, content -> viewModel.updateSummarySection(section, content) },
                                    onSubmitClick = { viewModel.submitIntake() },
                                    onResetForNewPatient = { viewModel.resetForNewPatient() },
                                    onRegenerateSummary = { viewModel.generateSummary() }
                                )
                            }
                        }

                        if (showLanguageDialog) {
                            LanguageSelectDialog(
                                selectedLanguage = selectedLanguage,
                                onLanguageSelected = { 
                                    viewModel.setLanguage(it)
                                    showLanguageDialog = false
                                },
                                onDismiss = { showLanguageDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
