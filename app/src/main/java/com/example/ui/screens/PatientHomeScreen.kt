package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.CareFlowEmergencyDialog
import com.example.ui.components.CriticalEmergencySection
import com.example.ui.components.UrgentRedFlagAlertBanner
import com.example.ui.theme.*
import com.example.ui.viewmodel.KioskScreen
import com.example.ui.viewmodel.KioskViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHomeScreen(
    viewModel: KioskViewModel,
    modifier: Modifier = Modifier
) {
    // ViewModel state collections
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val patientProfile by viewModel.patientProfile.collectAsState()
    val activeVisit by viewModel.activeVisit.collectAsState()
    val healthInsights by viewModel.healthInsights.collectAsState()
    val recentlyViewed by viewModel.recentlyViewedItems.collectAsState()
    val recentRecords by viewModel.recentRecords.collectAsState()
    val recentActivities by viewModel.recentActivities.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val isAccessibilityMode by viewModel.isAccessibilityMode.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val isRedFlagDetected by viewModel.isRedFlagDetected.collectAsState()
    val redFlagMessage by viewModel.redFlagMessage.collectAsState()
    val isEmergencyAlertActive by viewModel.isEmergencyAlertActive.collectAsState()
    val activeEmergencyType by viewModel.activeEmergencyType.collectAsState()
    val emergencyEtaSeconds by viewModel.emergencyEtaSeconds.collectAsState()
    val showEmergencyCenterDialog by viewModel.showEmergencyCenterDialog.collectAsState()
    val homeSearchQuery by viewModel.homeSearchQuery.collectAsState()
    val isVoiceAssistantOpen by viewModel.isVoiceAssistantOpen.collectAsState()
    val voiceAssistantState by viewModel.voiceAssistantState.collectAsState()
    val voiceAssistantTranscript by viewModel.voiceAssistantTranscript.collectAsState()
    val voiceAssistantResponse by viewModel.voiceAssistantResponse.collectAsState()
    val voiceAssistantStatusText by viewModel.voiceAssistantStatusText.collectAsState()

    // Local UI dialog states
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showQueueDetailsDialog by remember { mutableStateOf(false) }
    var showAppointmentsDialog by remember { mutableStateOf(false) }
    var activeServiceActionDialog by remember { mutableStateOf<String?>(null) }
    var selectedRecordForDetail by remember { mutableStateOf<RecentRecordItem?>(null) }

    val unreadNotificationsCount = notifications.count { !it.isRead }
    val strings = HomeStringProvider.getStrings(selectedLanguage, isAccessibilityMode)

    // Easy mode typography
    val titleFontSize = if (isAccessibilityMode) 28.sp else 22.sp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .widthIn(max = 760.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // 0. URGENT RED-FLAG ALERT (If detected)
            // ==========================================
            AnimatedVisibility(
                visible = isRedFlagDetected,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                UrgentRedFlagAlertBanner(
                    message = redFlagMessage,
                    onCallStaff = { viewModel.alertHospitalStaff() },
                    onDismiss = { viewModel.dismissRedFlag() }
                )
            }

            // ==========================================
            // 1. TOP HEADER
            // ==========================================
            HomeHeaderSection(
                hospitalName = strings.hospitalName,
                unreadCount = unreadNotificationsCount,
                isAccessibilityMode = isAccessibilityMode,
                themeMode = themeMode,
                selectedLanguage = selectedLanguage,
                onNotificationClick = { showNotificationsDialog = true },
                onProfileClick = { viewModel.navigateTo(KioskScreen.PROFILE) },
                onLanguageClick = { showLanguageDialog = true },
                onToggleEasyMode = { viewModel.toggleAccessibilityMode() },
                onThemeClick = { showThemeDialog = true },
                onEmergencySosClick = { viewModel.openEmergencyCenterDialog() }
            )

            // ==========================================
            // 2. CRITICAL CARE & EMERGENCY SOS ALERT SECTION
            // ==========================================
            CriticalEmergencySection(
                isEmergencyActive = isEmergencyAlertActive,
                activeEmergencyType = activeEmergencyType,
                onTriggerSos = { viewModel.triggerEmergencySos("1-Tap SOS Emergency Distress") },
                onOpenEmergencyCenter = { viewModel.openEmergencyCenterDialog() },
                onSelectCriticalSymptom = { viewModel.reportCriticalSymptom(it) },
                onCallEmergencyDesk = { viewModel.callEmergencyDesk() },
                onDispatchPorter = { viewModel.dispatchEmergencyPorter() },
                isAccessibilityMode = isAccessibilityMode
            )

            // ==========================================
            // 3. GREETING
            // ==========================================
            GreetingSection(
                patientName = patientProfile.name.ifBlank { "Rajesh Sharma" },
                welcomeText = strings.howCanWeHelp,
                titleFontSize = titleFontSize,
                onSpeakGreeting = {
                    viewModel.speakText("Hello, ${patientProfile.name.ifBlank { "Rajesh Sharma" }}. ${strings.howCanWeHelp}")
                }
            )

            // ==========================================
            // 3. SEARCH + AI BUTTON
            // ==========================================
            SearchAndAiSection(
                query = homeSearchQuery,
                onQueryChange = { viewModel.setHomeSearchQuery(it) },
                onSearchSubmit = { query ->
                    viewModel.openVoiceAssistant(query)
                },
                onAskAiClick = {
                    if (homeSearchQuery.isNotBlank()) {
                        viewModel.openVoiceAssistant(homeSearchQuery)
                    } else {
                        viewModel.openVoiceAssistant()
                        viewModel.startVoiceAssistantListening()
                    }
                },
                placeholder = strings.searchPlaceholder,
                isAccessibilityMode = isAccessibilityMode
            )

            // ==========================================
            // 4. CURRENT VISIT CARD
            // ==========================================
            CurrentVisitSection(
                activeVisit = activeVisit,
                onStartVisit = { viewModel.startVisit() },
                onViewDirections = { showQueueDetailsDialog = true },
                onToggleSimulatedVisit = { viewModel.toggleActiveVisit() },
                isAccessibilityMode = isAccessibilityMode
            )

            // ==========================================
            // 5. CONSULTATION SECTION (In-Person & Video)
            // ==========================================
            ConsultationSection(
                sectionTitle = strings.consultationTitle,
                onStartInPerson = { viewModel.navigateTo(KioskScreen.IDENTIFY) },
                onBookVideo = { viewModel.navigateTo(KioskScreen.TELEMEDICINE_DOCTORS) },
                isAccessibilityMode = isAccessibilityMode
            )

            // ==========================================
            // 5B. CONNECTED HEALTH & AI ASSISTANT SHORTCUTS
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.navigateTo(KioskScreen.HEALTH_MONITORING) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDCFCE7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Health & Vitals",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Wearables • 7d/30d Trends",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.navigateTo(KioskScreen.AI_HEALTH_ASSISTANT) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Health Triage",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Safety-Governed Triage",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            // ==========================================
            // 6. HOSPITAL SERVICES & RECORDS GRID
            // ==========================================
            HospitalServicesSection(
                sectionTitle = strings.servicesTitle,
                onServiceClick = { serviceKey ->
                    when (serviceKey) {
                        "lab_tests" -> activeServiceActionDialog = "Book Diagnostic Lab Tests"
                        "surgery" -> activeServiceActionDialog = "Plan Surgery & Counseling"
                        "medicines" -> activeServiceActionDialog = "Order Medicine & Refills"
                        "health_records" -> viewModel.navigateTo(KioskScreen.MEDICAL_RECORDS)
                        "appointments" -> viewModel.navigateTo(KioskScreen.APPOINTMENTS)
                        "prescriptions" -> viewModel.navigateTo(KioskScreen.DOCUMENT_SCAN)
                        "medical_reports" -> viewModel.navigateTo(KioskScreen.DOCUMENT_SCAN)
                    }
                },
                isAccessibilityMode = isAccessibilityMode
            )

            // ==========================================
            // 7. RECENTLY VIEWED CARDS
            // ==========================================
            RecentlyViewedSection(
                sectionTitle = strings.recentlyViewedTitle,
                items = recentlyViewed,
                emptyText = strings.nothingViewedRecently,
                onItemClick = { item ->
                    when (item.type) {
                        "Medical Report", "Document", "Prescription" -> viewModel.navigateTo(KioskScreen.DOCUMENT_SCAN)
                        "Appointment" -> viewModel.navigateTo(KioskScreen.APPOINTMENTS)
                        "Consultation" -> viewModel.navigateTo(KioskScreen.SUMMARY)
                    }
                },
                isAccessibilityMode = isAccessibilityMode
            )

            // ==========================================
            // 8. AI-POWERED HEALTH INSIGHTS
            // ==========================================
            AiHealthInsightsSection(
                sectionTitle = strings.aiInsightsTitle,
                insights = healthInsights,
                onInsightClick = { insight ->
                    viewModel.speakText(insight.description)
                },
                isAccessibilityMode = isAccessibilityMode
            )

            // ==========================================
            // 9. RECENT RECORDS
            // ==========================================
            RecentRecordsSection(
                sectionTitle = strings.recentRecordsTitle,
                records = recentRecords,
                onRecordClick = { record ->
                    selectedRecordForDetail = record
                },
                onViewAll = { viewModel.navigateTo(KioskScreen.DOCUMENT_SCAN) },
                isAccessibilityMode = isAccessibilityMode
            )

            // ==========================================
            // 10. RECENT ACTIVITY TIMELINE
            // ==========================================
            RecentActivityTimelineSection(
                sectionTitle = strings.recentActivityTitle,
                activities = recentActivities,
                isAccessibilityMode = isAccessibilityMode
            )

            // ==========================================
            // 11. NEED HELP? CARD
            // ==========================================
            NeedHelpSection(
                title = strings.needHelpTitle,
                subtitle = strings.needHelpDesc,
                buttonText = strings.requestStaffHelp,
                onRequestHelp = { viewModel.requestHospitalHelp() },
                isAccessibilityMode = isAccessibilityMode
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        // ==========================================
        // CAREFLOW EMERGENCY & CRITICAL RESPONSE DIALOG
        // ==========================================
        CareFlowEmergencyDialog(
            isOpen = showEmergencyCenterDialog,
            isEmergencyActive = isEmergencyAlertActive,
            activeEmergencyType = activeEmergencyType,
            emergencyEtaSeconds = emergencyEtaSeconds,
            onDismiss = { viewModel.closeEmergencyCenterDialog() },
            onTriggerSos = { viewModel.triggerEmergencySos(it) },
            onCancelSos = { viewModel.cancelEmergencySos() },
            onCallEmergencyDesk = { viewModel.callEmergencyDesk() },
            onDispatchPorter = { viewModel.dispatchEmergencyPorter() }
        )

        // ==========================================
        // REAL VOICE ASSISTANT BOTTOM SHEET / DIALOG
        // ==========================================
        if (isVoiceAssistantOpen) {
            MediKioskVoiceAssistantDialog(
                state = voiceAssistantState,
                statusText = voiceAssistantStatusText,
                transcript = voiceAssistantTranscript,
                response = voiceAssistantResponse,
                onStartListening = { viewModel.startVoiceAssistantListening() },
                onStopListening = { viewModel.stopVoiceAssistantListening() },
                onSendCommand = { viewModel.processVoiceAssistantCommand(it) },
                onClose = { viewModel.closeVoiceAssistant() },
                isAccessibilityMode = isAccessibilityMode
            )
        }

        // ==========================================
        // NOTIFICATIONS DIALOG
        // ==========================================
        if (showNotificationsDialog) {
            HospitalNotificationsDialog(
                notifications = notifications,
                onMarkRead = { viewModel.markNotificationAsRead(it) },
                onDismiss = { showNotificationsDialog = false }
            )
        }

        // ==========================================
        // QUEUE DETAILS / HOSPITAL DIRECTIONS DIALOG
        // ==========================================
        if (showQueueDetailsDialog && activeVisit != null) {
            QueueDetailsDialog(
                visit = activeVisit!!,
                onDismiss = { showQueueDetailsDialog = false }
            )
        }

        // ==========================================
        // UPCOMING APPOINTMENTS DIALOG
        // ==========================================
        if (showAppointmentsDialog) {
            UpcomingAppointmentsDialog(
                onDismiss = { showAppointmentsDialog = false },
                onBookNew = { viewModel.navigateTo(KioskScreen.IDENTIFY) }
            )
        }

        // ==========================================
        // LANGUAGE SELECTOR DIALOG
        // ==========================================
        if (showLanguageDialog) {
            LanguageSelectorDialog(
                currentLanguage = selectedLanguage,
                onSelect = {
                    viewModel.setLanguage(it)
                    showLanguageDialog = false
                },
                onDismiss = { showLanguageDialog = false }
            )
        }

        // ==========================================
        // THEME SELECTOR DIALOG
        // ==========================================
        if (showThemeDialog) {
            ThemeSelectorDialog(
                currentTheme = themeMode,
                onSelect = {
                    viewModel.setThemeMode(it)
                    showThemeDialog = false
                },
                onDismiss = { showThemeDialog = false }
            )
        }

        // ==========================================
        // SERVICE ACTION BOOKING DIALOG
        // ==========================================
        activeServiceActionDialog?.let { serviceTitle ->
            ServiceBookingDialog(
                serviceTitle = serviceTitle,
                onConfirm = { detail ->
                    when {
                        serviceTitle.contains("Video") -> viewModel.bookVideoConsultation(detail)
                        serviceTitle.contains("Lab") -> viewModel.bookLabTest(detail)
                        serviceTitle.contains("Surgery") -> viewModel.planSurgery(detail)
                        serviceTitle.contains("Medicine") -> viewModel.orderMedicines(detail)
                    }
                    activeServiceActionDialog = null
                },
                onDismiss = { activeServiceActionDialog = null }
            )
        }

        // ==========================================
        // RECORD DETAIL DIALOG
        // ==========================================
        selectedRecordForDetail?.let { record ->
            RecordDetailDialog(
                record = record,
                onDismiss = { selectedRecordForDetail = null },
                onOpenDocument = {
                    selectedRecordForDetail = null
                    viewModel.navigateTo(KioskScreen.DOCUMENT_SCAN)
                }
            )
        }
    }
}

// =========================================================================
// 1. TOP HEADER COMPONENT
// =========================================================================
@Composable
private fun HomeHeaderSection(
    hospitalName: String,
    unreadCount: Int,
    isAccessibilityMode: Boolean,
    themeMode: AppThemeMode,
    selectedLanguage: Language,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onToggleEasyMode: () -> Unit,
    onThemeClick: () -> Unit,
    onEmergencySosClick: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Logo & Hospital Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = "Hospital Logo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = "CareFlow Ai",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 17.sp
                        )
                    )
                    Text(
                        text = hospitalName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right: SOS, Easy Mode, Language, Theme, Notifications & Profile Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (onEmergencySosClick != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CoralError,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onEmergencySosClick() }
                            .testTag("header_sos_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Emergency,
                                contentDescription = "Emergency SOS",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "SOS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
                // Easy Mode Quick Toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAccessibilityMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggleEasyMode() }
                        .testTag("home_easy_mode_toggle")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessibilityNew,
                            contentDescription = "Easy Mode",
                            tint = if (isAccessibilityMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isAccessibilityMode) "Easy On" else "Easy",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isAccessibilityMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Language Selector Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onLanguageClick() }
                        .testTag("home_language_toggle")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Select Language",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = selectedLanguage.nativeName.take(4),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Theme Mode Selector
                IconButton(
                    onClick = onThemeClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("home_theme_toggle")
                ) {
                    Icon(
                        imageVector = when (themeMode) {
                            AppThemeMode.LIGHT -> Icons.Default.LightMode
                            AppThemeMode.DARK -> Icons.Default.DarkMode
                            AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                        },
                        contentDescription = "Change Theme",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Notification Bell with Unread Badge
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("home_notifications_button")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ) {
                                    Text("$unreadCount")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Patient Profile Avatar
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable { onProfileClick() }
                        .testTag("home_profile_avatar")
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// =========================================================================
// 2. GREETING COMPONENT
// =========================================================================
@Composable
private fun GreetingSection(
    patientName: String,
    welcomeText: String,
    titleFontSize: androidx.compose.ui.unit.TextUnit,
    onSpeakGreeting: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hello, $patientName",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = titleFontSize
                )
            )
            Text(
                text = welcomeText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp
                )
            )
        }

        IconButton(
            onClick = onSpeakGreeting,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                .testTag("greeting_audio_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Read Welcome Aloud",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// =========================================================================
// 3. SEARCH + AI BUTTON COMPONENT
// =========================================================================
@Composable
private fun SearchAndAiSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchSubmit: (String) -> Unit,
    onAskAiClick: () -> Unit,
    placeholder: String,
    isAccessibilityMode: Boolean
) {
    val focusManager = LocalFocusManager.current

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        text = if (isAccessibilityMode) "Search hospital..." else placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    if (query.isNotBlank()) onSearchSubmit(query)
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier
                    .weight(1f)
                    .testTag("home_search_field")
            )

            // Prominent "Ask MediKiosk AI" Button with Mic Icon
            Button(
                onClick = onAskAiClick,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                modifier = Modifier
                    .height(46.dp)
                    .testTag("home_ask_ai_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Assistant",
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Ask AI",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }
    }
}

// =========================================================================
// 4. CURRENT VISIT CARD COMPONENT
// =========================================================================
@Composable
private fun CurrentVisitSection(
    activeVisit: ActiveVisit?,
    onStartVisit: () -> Unit,
    onViewDirections: () -> Unit,
    onToggleSimulatedVisit: () -> Unit,
    isAccessibilityMode: Boolean
) {
    if (activeVisit != null && activeVisit.hasActiveVisit) {
        // Active Visit Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("current_visit_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header badge row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("visit_badge")
                        ) {
                            Text(
                                text = "CURRENT VISIT",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.6.sp,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.testTag("visit_status_pill")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.tertiary)
                                )
                                Text(
                                    text = activeVisit.status,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    // Token Number Callout
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("visit_token_pill")
                    ) {
                        Text(
                            text = "Token ${activeVisit.tokenNumber}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                // Department and Doctor info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeVisit.department,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 19.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = activeVisit.doctorName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = activeVisit.roomNumber,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        )
                        Text(
                            text = "~${activeVisit.estimatedWaitMinutes} min wait",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Queue Position Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "You are #${activeVisit.queuePosition} in queue",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "${activeVisit.queuePosition} of ${activeVisit.totalInQueue} waiting",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    LinearProgressIndicator(
                        progress = {
                            val ratio = 1f - (activeVisit.queuePosition.toFloat() / activeVisit.totalInQueue.coerceAtLeast(1).toFloat())
                            ratio.coerceIn(0.1f, 0.95f)
                        },
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }

                // Action buttons: Directions & Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onViewDirections,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("visit_directions_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Directions,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Queue & Directions",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onToggleSimulatedVisit,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("visit_dismiss_sim_button")
                    ) {
                        Text("Check Out")
                    }
                }
            }
        }
    } else {
        // No Active Visit Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("no_active_visit_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassEmpty,
                            contentDescription = "No active visit",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "No active visit",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Check in or queue up for a consultation.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Button(
                    onClick = onStartVisit,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("start_visit_button")
                ) {
                    Text("Start a Visit")
                }
            }
        }
    }
}

// =========================================================================
// 5. CONSULTATION SECTION (In-Person & Video)
// =========================================================================
@Composable
private fun ConsultationSection(
    sectionTitle: String,
    onStartInPerson: () -> Unit,
    onBookVideo: () -> Unit,
    isAccessibilityMode: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = if (isAccessibilityMode) 20.sp else 17.sp
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // CARD 1: In-Person Consultation
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onStartInPerson() }
                    .testTag("consultation_in_person_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = "In-Person Consultation",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "In-Person",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Visit the hospital and consult with a doctor.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = onStartInPerson,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("start_in_person_button")
                    ) {
                        Text(
                            text = "Start",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            // CARD 2: Video Consultation
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .weight(1f)
                    .clickable { onBookVideo() }
                    .testTag("consultation_video_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoCall,
                            contentDescription = "Video Consultation",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Video Consult",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = "Consult a doctor remotely from your device.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    FilledTonalButton(
                        onClick = onBookVideo,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("book_video_button")
                    ) {
                        Text(
                            text = "Book",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 6. HOSPITAL SERVICES & RECORDS GRID
// =========================================================================
@Composable
private fun HospitalServicesSection(
    sectionTitle: String,
    onServiceClick: (String) -> Unit,
    isAccessibilityMode: Boolean
) {
    data class ServiceItem(
        val key: String,
        val title: String,
        val subtitle: String,
        val icon: ImageVector,
        val containerColor: Color,
        val iconColor: Color
    )

    val services = listOf(
        ServiceItem(
            key = "lab_tests",
            title = "Book Lab Tests",
            subtitle = "Pathology & Scans",
            icon = Icons.Default.Biotech,
            containerColor = PastelMintBg,
            iconColor = PastelMintIcon
        ),
        ServiceItem(
            key = "surgery",
            title = "Plan Surgery",
            subtitle = "Pre-admission counseling",
            icon = Icons.Default.MedicalInformation,
            containerColor = PastelLavenderBg,
            iconColor = PastelLavenderIcon
        ),
        ServiceItem(
            key = "medicines",
            title = "Order Medicines",
            subtitle = "Dispensary & Refill",
            icon = Icons.Default.Medication,
            containerColor = PastelPeachBg,
            iconColor = PastelPeachIcon
        ),
        ServiceItem(
            key = "health_records",
            title = "Health Records",
            subtitle = "Intake summaries",
            icon = Icons.Default.FolderShared,
            containerColor = PastelBlueBg,
            iconColor = PastelBlueIcon
        ),
        ServiceItem(
            key = "appointments",
            title = "Appointments",
            subtitle = "Follow-ups & slots",
            icon = Icons.Default.CalendarMonth,
            containerColor = PastelLavenderBg,
            iconColor = PastelLavenderIcon
        ),
        ServiceItem(
            key = "prescriptions",
            title = "Prescriptions",
            subtitle = "Active doctor Rx",
            icon = Icons.Default.ReceiptLong,
            containerColor = PastelMintBg,
            iconColor = PastelMintIcon
        ),
        ServiceItem(
            key = "medical_reports",
            title = "Medical Reports",
            subtitle = "Verified PDF results",
            icon = Icons.Default.Description,
            containerColor = PastelBlueBg,
            iconColor = PastelBlueIcon
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = if (isAccessibilityMode) 20.sp else 17.sp
            )
        )

        // 2-Column Responsive Grid Layout
        val chunked = services.chunked(2)
        chunked.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { item ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onServiceClick(item.key) }
                            .testTag("service_card_${item.key}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(item.containerColor)
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = item.iconColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 11.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

// =========================================================================
// 7. RECENTLY VIEWED SECTION
// =========================================================================
@Composable
private fun RecentlyViewedSection(
    sectionTitle: String,
    items: List<RecentlyViewedItem>,
    emptyText: String,
    onItemClick: (RecentlyViewedItem) -> Unit,
    isAccessibilityMode: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = if (isAccessibilityMode) 20.sp else 17.sp
            )
        )

        if (items.isEmpty()) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(items) { item ->
                    val icon = when (item.iconType) {
                        "report" -> Icons.Default.Description
                        "appointment" -> Icons.Default.Event
                        "prescription" -> Icons.Default.Receipt
                        else -> Icons.Default.Assignment
                    }

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .width(220.dp)
                            .clickable { onItemClick(item) }
                            .testTag("recently_viewed_${item.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = item.type.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                )
                            }

                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = item.dateOrStatus,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 8. AI-POWERED HEALTH INSIGHTS SECTION
// =========================================================================
@Composable
private fun AiHealthInsightsSection(
    sectionTitle: String,
    insights: List<HealthInsight>,
    onInsightClick: (HealthInsight) -> Unit,
    isAccessibilityMode: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = sectionTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = if (isAccessibilityMode) 20.sp else 17.sp
                )
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.testTag("ai_insights_badge")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "AI-assisted",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }

        insights.forEach { insight ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onInsightClick(insight) }
                    .testTag("health_insight_${insight.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = when (insight.iconType) {
                                "calendar" -> Icons.Default.CalendarToday
                                "document" -> Icons.Default.FactCheck
                                else -> Icons.Default.TipsAndUpdates
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = insight.title,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = insight.description,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }
            }
        }

        // Mandatory Non-Diagnostic Disclaimer Note
        Text(
            text = "Notice: AI suggestions are for clinical workflow assistance and scheduling reminders only. They do not constitute medical diagnosis or prescription.",
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                fontSize = 11.sp,
                lineHeight = 15.sp
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

// =========================================================================
// 9. RECENT RECORDS SECTION
// =========================================================================
@Composable
private fun RecentRecordsSection(
    sectionTitle: String,
    records: List<RecentRecordItem>,
    onRecordClick: (RecentRecordItem) -> Unit,
    onViewAll: () -> Unit,
    isAccessibilityMode: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sectionTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = if (isAccessibilityMode) 20.sp else 17.sp
                )
            )

            TextButton(
                onClick = onViewAll,
                modifier = Modifier.testTag("view_all_records_button")
            ) {
                Text(
                    text = "View All →",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        records.forEach { record ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRecordClick(record) }
                    .testTag("recent_record_${record.id}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = when (record.recordType) {
                                    "Consultation" -> Icons.Default.MedicalServices
                                    "Lab Report" -> Icons.Default.Biotech
                                    "Prescription" -> Icons.Default.ReceiptLong
                                    else -> Icons.Default.FolderOpen
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            Text(
                                text = record.department,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${record.date} • ${record.doctorOrFacility}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "View",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View Record",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 10. RECENT ACTIVITY TIMELINE SECTION
// =========================================================================
@Composable
private fun RecentActivityTimelineSection(
    sectionTitle: String,
    activities: List<RecentActivityItem>,
    isAccessibilityMode: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = if (isAccessibilityMode) 20.sp else 17.sp
            )
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                activities.forEachIndexed { index, activity ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Timeline node
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(24.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            if (index < activities.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(28.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                )
                            }
                        }

                        // Content
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activity.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = activity.subtitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = activity.timestamp,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 11. NEED HELP SECTION
// =========================================================================
@Composable
private fun NeedHelpSection(
    title: String,
    subtitle: String,
    buttonText: String,
    onRequestHelp: () -> Unit,
    isAccessibilityMode: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("need_help_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.SupportAgent,
                        contentDescription = "Staff Assistance",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Button(
                onClick = onRequestHelp,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.testTag("request_staff_help_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = buttonText,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

// =========================================================================
// 12. REAL VOICE ASSISTANT MODAL DIALOG
// =========================================================================
@Composable
private fun MediKioskVoiceAssistantDialog(
    state: VoiceAssistantState,
    statusText: String,
    transcript: String,
    response: String,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    onSendCommand: (String) -> Unit,
    onClose: () -> Unit,
    isAccessibilityMode: Boolean
) {
    var textFallbackInput by remember { mutableStateOf("") }

    // Pulsating animation for mic when listening
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (state == VoiceAssistantState.LISTENING) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.5.dp,
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("voice_assistant_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "CareFlow AI Voice Assistant",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("voice_assistant_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Pulsing Mic Button & Visual Status
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(90.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            when (state) {
                                VoiceAssistantState.LISTENING -> MaterialTheme.colorScheme.error
                                VoiceAssistantState.PROCESSING -> MaterialTheme.colorScheme.tertiary
                                VoiceAssistantState.EXECUTING -> MaterialTheme.colorScheme.secondary
                                VoiceAssistantState.SPEAKING -> MaterialTheme.colorScheme.primary
                                VoiceAssistantState.ERROR -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.primaryContainer
                            }
                        )
                        .clickable {
                            if (state == VoiceAssistantState.LISTENING) {
                                onStopListening()
                            } else {
                                onStartListening()
                            }
                        }
                        .testTag("voice_assistant_mic_button")
                ) {
                    Icon(
                        imageVector = when (state) {
                            VoiceAssistantState.LISTENING -> Icons.Default.Mic
                            VoiceAssistantState.PROCESSING -> Icons.Default.HourglassTop
                            VoiceAssistantState.EXECUTING -> Icons.Default.Sync
                            VoiceAssistantState.SPEAKING -> Icons.AutoMirrored.Filled.VolumeUp
                            VoiceAssistantState.ERROR -> Icons.Default.Warning
                            else -> Icons.Default.MicNone
                        },
                        contentDescription = "Mic",
                        tint = when (state) {
                            VoiceAssistantState.IDLE -> MaterialTheme.colorScheme.onPrimaryContainer
                            VoiceAssistantState.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                            else -> Color.White
                        },
                        modifier = Modifier.size(40.dp)
                    )
                }

                // State description
                Text(
                    text = statusText.ifBlank {
                        when (state) {
                            VoiceAssistantState.IDLE -> "Tap the microphone or type below"
                            VoiceAssistantState.LISTENING -> "Listening... Speak your question clearly"
                            VoiceAssistantState.PROCESSING -> "Thinking..."
                            VoiceAssistantState.EXECUTING -> "Processing request..."
                            VoiceAssistantState.SPEAKING -> "Speaking response..."
                            VoiceAssistantState.ERROR -> "Could not process. Tap to retry."
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                )

                // Live Transcript / Response display
                if (transcript.isNotBlank() || response.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (transcript.isNotBlank()) {
                                Text(
                                    text = "You said: \"$transcript\"",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                )
                            }
                            if (response.isNotBlank()) {
                                Text(
                                    text = response,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                // Quick Suggestion Chips
                Text(
                    text = "Quick Inquiries:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )

                val quickChips = listOf(
                    "What is my token number?",
                    "Where is the laboratory?",
                    "Show my doctor appointments",
                    "Can you give me medical advice?",
                    "Request staff help",
                    "Switch to Hindi"
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickChips) { chip ->
                        SuggestionChip(
                            onClick = { onSendCommand(chip) },
                            label = { Text(chip, fontSize = 12.sp) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // Text Fallback Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = textFallbackInput,
                        onValueChange = { textFallbackInput = it },
                        placeholder = { Text("Or type your command...", fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("voice_assistant_fallback_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    IconButton(
                        onClick = {
                            if (textFallbackInput.isNotBlank()) {
                                onSendCommand(textFallbackInput)
                                textFallbackInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .testTag("voice_assistant_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// 13. HOSPITAL NOTIFICATIONS DIALOG
// =========================================================================
@Composable
private fun HospitalNotificationsDialog(
    notifications: List<HospitalNotification>,
    onMarkRead: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("hospital_notifications_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hospital Alerts & Updates",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (notifications.isEmpty()) {
                    Text(
                        text = "No notifications right now.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        notifications.forEach { notif ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (notif.isUrgent) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                                else if (!notif.isRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onMarkRead(notif.id) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (notif.isUrgent) Icons.Default.PriorityHigh else Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = if (notif.isUrgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notif.title,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = notif.message,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                        Text(
                                            text = notif.timestamp,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close")
                }
            }
        }
    }
}

// =========================================================================
// 14. QUEUE & DIRECTIONS DIALOG
// =========================================================================
@Composable
private fun QueueDetailsDialog(
    visit: ActiveVisit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("queue_details_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live Queue & Directions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Room & Token Summary
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Token ${visit.tokenNumber} • Position #${visit.queuePosition}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "Doctor: ${visit.doctorName} (${visit.department})",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                        )
                        Text(
                            text = "Location: ${visit.roomNumber}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }

                Text(
                    text = "Turn-by-Turn Wayfinding:",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                val steps = listOf(
                    "Proceed straight from the Kiosk lobby toward Elevator Bank A.",
                    "Take the elevator to the 1st Floor (or use the central stairs on your right).",
                    "Exit right and follow the Blue Corridor toward Outpatient Wing B.",
                    "Room 104 is on your left, across from the Nursing Station."
                )

                steps.forEachIndexed { i, step ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${i + 1}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        Text(
                            text = step,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        )
                    }
                }

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got It")
                }
            }
        }
    }
}

// =========================================================================
// 15. UPCOMING APPOINTMENTS DIALOG
// =========================================================================
@Composable
private fun UpcomingAppointmentsDialog(
    onDismiss: () -> Unit,
    onBookNew: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("upcoming_appointments_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hospital Appointments",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Appointment card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Cardiology Follow-up",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Confirmed",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Doctor: Dr. V. Rao, MD, DM",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                        )
                        Text(
                            text = "Date & Time: Tomorrow, 10:30 AM (Room 204)",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onBookNew()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Book New")
                    }
                }
            }
        }
    }
}

// =========================================================================
// 16. LANGUAGE SELECTOR DIALOG
// =========================================================================
@Composable
private fun LanguageSelectorDialog(
    currentLanguage: Language,
    onSelect: (Language) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("language_selector_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select Preferred Language",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Language.values().forEach { lang ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (lang == currentLanguage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (lang == currentLanguage) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(lang) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = lang.nativeName,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = lang.displayName,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            if (lang == currentLanguage) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 17. THEME SELECTOR DIALOG
// =========================================================================
@Composable
private fun ThemeSelectorDialog(
    currentTheme: AppThemeMode,
    onSelect: (AppThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("theme_selector_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Display Theme",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                val modes = listOf(
                    Triple(AppThemeMode.LIGHT, "Light Theme", Icons.Default.LightMode),
                    Triple(AppThemeMode.DARK, "Dark Theme", Icons.Default.DarkMode),
                    Triple(AppThemeMode.SYSTEM, "System Default", Icons.Default.BrightnessAuto)
                )

                modes.forEach { (mode, label, icon) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (mode == currentTheme) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (mode == currentTheme) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(mode) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                            if (mode == currentTheme) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 18. SERVICE BOOKING DIALOG (Real Interactive Dialog)
// =========================================================================
@Composable
private fun ServiceBookingDialog(
    serviceTitle: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var detailText by remember { mutableStateOf("") }

    val defaultOptions = when {
        serviceTitle.contains("Lab") -> listOf("Complete Blood Count (CBC)", "Fasting Lipid Panel", "HbA1c Glycated Hemoglobin", "Liver Function Panel")
        serviceTitle.contains("Video") -> listOf("General Physician", "Cardiology Specialist", "Orthopedics", "Pediatrics")
        serviceTitle.contains("Surgery") -> listOf("Pre-Operative Assessment", "Elective Day Surgery", "Anesthesia Consultation", "Surgical Second Opinion")
        else -> listOf("Current Prescription Refill", "Chronic Medication Supply (30 days)", "Hospital Pharmacy Pickup Counter 3")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = serviceTitle,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )

                Text(
                    text = "Select specific service or enter notes:",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )

                defaultOptions.forEach { opt ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (detailText == opt) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = if (detailText == opt) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { detailText = opt }
                    ) {
                        Text(
                            text = opt,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            onConfirm(detailText.ifBlank { defaultOptions.first() })
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

// =========================================================================
// 19. RECORD DETAIL DIALOG
// =========================================================================
@Composable
private fun RecordDetailDialog(
    record: RecentRecordItem,
    onDismiss: () -> Unit,
    onOpenDocument: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = record.recordType,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = record.department,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 18.sp
                    )
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Date: ${record.date}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                        )
                        Text(
                            text = "Provider / Facility: ${record.doctorOrFacility}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                        )
                        Text(
                            text = "Summary: ${record.summary}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Text(
                            text = "Verified Medical Record • Signed Digitally",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close")
                    }

                    Button(
                        onClick = onOpenDocument,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Open Document")
                    }
                }
            }
        }
    }
}
