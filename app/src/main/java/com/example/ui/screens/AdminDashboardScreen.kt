package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.PatientHistoryAdminView
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminUser: AuthUser?,
    staffRequests: List<StaffAssistanceRequest>,
    liveQueues: List<QueueItem>,
    patientHistories: List<PatientMasterHistory> = emptyList(),
    onCallNextPatient: (String) -> Unit,
    onMarkInConsultation: (String) -> Unit,
    onDispatchStaff: (String) -> Unit,
    onResolveStaffRequest: (String) -> Unit,
    onRecordConsultation: (
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
        bloodGroup: String,
        allergies: List<String>,
        chronicConditions: List<String>
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onAddMedicineToConsultation: (String, PrescriptionMedicine) -> Unit = { _, _ -> },
    onUpdateMedicineStatus: (String, String, String) -> Unit = { _, _, _ -> },
    onSpeakPrescription: (String, List<PrescriptionMedicine>) -> Unit = { _, _ -> },
    onNavigateToVerification: () -> Unit = {},
    onNavigateToTelemedicine: () -> Unit = {},
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Live Queue, 1: Patient History, 2: Staff Help, 3: System Status
    var actionNotification by remember { mutableStateOf<String?>(null) }

    val waitingCount = liveQueues.count { it.status == "Waiting" || it.status == "Near Turn" }
    val inConsultCount = liveQueues.count { it.status == "In Consultation" }
    val pendingHelpCount = staffRequests.count { it.status == RequestStatus.PENDING }
    val patientCount = patientHistories.size

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Admin Command Center Top Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = TealSecondary.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = TealSecondary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Hospital Admin Console",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = adminUser?.name ?: "Dr. Alok Verma (Administrator)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateToVerification,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7))
                        ) {
                            Icon(Icons.Default.FactCheck, contentDescription = "Verification Queue", tint = Color(0xFFD97706))
                        }

                        IconButton(
                            onClick = onNavigateToTelemedicine,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(Icons.Default.Videocam, contentDescription = "Telemedicine Call", tint = MaterialTheme.colorScheme.primary)
                        }

                        // Logout / Switch Role
                        IconButton(
                            onClick = onLogoutClick,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("admin_logout_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Sign out",
                                tint = CoralError
                            )
                        }
                    }
                }
            }

            // Real-time Action Snackbar / Feedback
            AnimatedVisibility(visible = actionNotification != null) {
                if (actionNotification != null) {
                    Surface(
                        color = Emerald500.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Emerald500.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Emerald700,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = actionNotification ?: "",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Emerald700
                                    )
                                )
                            }
                            IconButton(
                                onClick = { actionNotification = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // Stat Cards Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "In Queue",
                    value = waitingCount.toString(),
                    color = NavyPrimary,
                    icon = Icons.Default.Groups,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Consulting",
                    value = inConsultCount.toString(),
                    color = Emerald500,
                    icon = Icons.Default.PersonSearch,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Patient EHR",
                    value = patientCount.toString(),
                    color = TealSecondary,
                    icon = Icons.Default.MedicalInformation,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Staff Alerts",
                    value = pendingHelpCount.toString(),
                    color = if (pendingHelpCount > 0) CoralError else Slate600,
                    icon = Icons.Default.SupportAgent,
                    modifier = Modifier.weight(1f)
                )
            }

            // Navigation Tabs - Featuring Patient History Tool
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Live Queue ($waitingCount)", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("admin_tab_queue")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Patient History ($patientCount)", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.MedicalInformation, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("admin_tab_patient_history")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Staff Help ($pendingHelpCount)", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("admin_tab_staff_help")
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = { Text("System Status", fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("admin_tab_system")
                )
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(14.dp)
            ) {
                when (selectedTab) {
                    0 -> LiveQueueManagementTab(
                        queues = liveQueues,
                        onCallNext = { dept ->
                            onCallNextPatient(dept)
                            actionNotification = "Called next patient for $dept"
                        },
                        onConsult = { token ->
                            onMarkInConsultation(token)
                            actionNotification = "Patient $token marked In-Consultation"
                        }
                    )
                    1 -> PatientHistoryAdminView(
                        patientHistories = patientHistories,
                        onRecordConsultation = { pId, pName, age, gen, abha, ph, doc, spec, dept, dt, room, cc, diag, notes, vit, meds, labs, fup, adv, bg, allerg, conds ->
                            onRecordConsultation(pId, pName, age, gen, abha, ph, doc, spec, dept, dt, room, cc, diag, notes, vit, meds, labs, fup, adv, bg, allerg, conds)
                            actionNotification = "Recorded new clinical consultation & Rx for $pName"
                        },
                        onAddMedicineToConsultation = { consultId, med ->
                            onAddMedicineToConsultation(consultId, med)
                            actionNotification = "Added ${med.medicineName} to prescription"
                        },
                        onUpdateMedicineStatus = { pId, medId, status ->
                            onUpdateMedicineStatus(pId, medId, status)
                            actionNotification = "Updated medicine status to $status"
                        },
                        onSpeakPrescription = { pName, meds ->
                            onSpeakPrescription(pName, meds)
                        }
                    )
                    2 -> StaffAssistanceTab(
                        requests = staffRequests,
                        onDispatch = { id ->
                            onDispatchStaff(id)
                            actionNotification = "Staff care escort dispatched to kiosk"
                        },
                        onResolve = { id ->
                            onResolveStaffRequest(id)
                            actionNotification = "Assistance request marked resolved"
                        }
                    )
                    3 -> SystemStatusTab()
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
    }
}

@Composable
fun LiveQueueManagementTab(
    queues: List<QueueItem>,
    onCallNext: (String) -> Unit,
    onConsult: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick Queue Action Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onCallNext("General Medicine") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier
                    .weight(1f)
                    .testTag("call_next_medicine_button")
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Call Next (Med)", fontSize = 12.sp)
            }

            Button(
                onClick = { onCallNext("Cardiology") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = TealSecondary),
                modifier = Modifier
                    .weight(1f)
                    .testTag("call_next_cardiology_button")
            ) {
                Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Call Next (Cardio)", fontSize = 12.sp)
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(queues) { item ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (item.status == "In Consultation") Emerald500.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = item.token,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = when (item.status) {
                                        "In Consultation" -> Emerald500.copy(alpha = 0.15f)
                                        "Calling Now" -> AmberWarning.copy(alpha = 0.2f)
                                        else -> Slate200
                                    }
                                ) {
                                    Text(
                                        text = item.status,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = when (item.status) {
                                                "In Consultation" -> Emerald700
                                                "Calling Now" -> Amber700
                                                else -> Slate700
                                            },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = item.patientName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )

                            Text(
                                text = "${item.department} • ${item.room}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        if (item.status != "In Consultation" && item.status != "Completed") {
                            OutlinedButton(
                                onClick = { onConsult(item.token) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Mark In", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StaffAssistanceTab(
    requests: List<StaffAssistanceRequest>,
    onDispatch: (String) -> Unit,
    onResolve: (String) -> Unit
) {
    if (requests.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "No pending assistance requests. All patient kiosks clear.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(requests) { req ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (req.status == RequestStatus.PENDING) CoralError.copy(alpha = 0.4f)
                        else Emerald500.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (req.status == RequestStatus.PENDING) CoralError else Emerald500)
                                )
                                Text(
                                    text = req.patientName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }

                            Text(
                                text = req.timestamp,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.outline
                                )
                            )
                        }

                        Text(
                            text = "Location: ${req.location}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        Text(
                            text = "Reason: ${req.reason}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )

                        if (req.assignedStaff != null) {
                            Text(
                                text = "Assigned: ${req.assignedStaff}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Emerald700,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (req.status == RequestStatus.PENDING) {
                                Button(
                                    onClick = { onDispatch(req.id) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Dispatch Staff", fontSize = 12.sp)
                                }
                            } else if (req.status == RequestStatus.DISPATCHED) {
                                OutlinedButton(
                                    onClick = { onResolve(req.id) },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Mark Resolved", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SystemStatusTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Terminal & Services Telemetry",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                TelemetryRow("Kiosk Terminal Hardware", "Online (Kiosk #01 - Atrium)", Emerald700)
                TelemetryRow("Firebase Authentication", "Connected (IAM Role Rules Active)", Emerald700)
                TelemetryRow("Cloud Firestore Sync", "Real-Time Listeners Connected", Emerald700)
                TelemetryRow("Gemini API (Flash 2.5)", "Operational (Triage & Voice Active)", Emerald700)
                TelemetryRow("Android Speech & TTS Engine", "Hardware Ready (Multi-Language)", Emerald700)
                TelemetryRow("Camera & OCR Pipeline", "Optical Character Recognition Ready", Emerald700)
                TelemetryRow("Security Audit", "HIPAA/NDHM Compliant Session Timeout Active", Emerald700)
            }
        }
    }
}

@Composable
fun TelemetryRow(label: String, status: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = color
            ),
            textAlign = TextAlign.End
        )
    }
}
