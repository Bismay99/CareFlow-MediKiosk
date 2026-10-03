package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelemedicineCallScreen(
    session: TeleconsultationSession,
    onToggleAudio: () -> Unit,
    onToggleVideo: () -> Unit,
    onFlipCamera: () -> Unit,
    onSendMessage: (String) -> Unit,
    onEndCall: () -> Unit,
    onSaveConsultationNotes: (String, String, List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var callSeconds by remember { mutableLongStateOf(0L) }
    var showChatSheet by remember { mutableStateOf(false) }
    var chatInput by remember { mutableStateOf("") }
    var isCallCompleted by remember { mutableStateOf(session.status == TeleconsultationStatus.COMPLETED) }

    // Post-Consultation State
    var clinicianNotes by remember {
        mutableStateOf("Patient presented for follow-up evaluation. Vitals reviewed; blood pressure stable under current ACE inhibitor regimen. Recommended continuing low sodium diet and monitoring fasting glucose.")
    }
    var followUpAdvice by remember { mutableStateOf("Repeat lipid panel and fasting glucose in 4 weeks. Report if morning headaches persist.") }
    var newPrescriptionMed by remember { mutableStateOf("Tab. Metformin 500mg - 1 Tab BD after meals") }
    var prescribedMedsList by remember { mutableStateOf(listOf("Tab. Telmisartan 40mg - 1 Tab OD", "Tab. Atorvastatin 10mg - 1 Tab HS")) }

    // Live call duration timer
    LaunchedEffect(isCallCompleted) {
        while (!isCallCompleted) {
            delay(1000)
            callSeconds += 1
        }
    }

    val minutes = callSeconds / 60
    val seconds = callSeconds % 60
    val timerFormatted = String.format("%02d:%02d", minutes, seconds)

    if (isCallCompleted) {
        // ==========================================
        // POST-CONSULTATION SUMMARY & PRESCRIPTION SCREEN
        // ==========================================
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Consultation Completed", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onEndCall) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close")
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Success banner
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Teleconsultation Finished",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFF14532D))
                                )
                                Text(
                                    text = "Duration: $timerFormatted • Dr. ${session.doctorName}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF15803D))
                                )
                            }
                        }
                    }
                }

                // Doctor's Clinical Notes
                item {
                    Text(
                        text = "Physician Consultation Notes",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = clinicianNotes,
                        onValueChange = { clinicianNotes = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Follow-up Recommendations
                item {
                    Text(
                        text = "Follow-up & Lifestyle Advice",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = followUpAdvice,
                        onValueChange = { followUpAdvice = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Generated Digital Prescription
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Prescriptions Issued",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        prescribedMedsList.forEachIndexed { index, med ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Medication,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = med,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            prescribedMedsList = prescribedMedsList.filterIndexed { i, _ -> i != index }
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        // Add new medication row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newPrescriptionMed,
                                onValueChange = { newPrescriptionMed = it },
                                placeholder = { Text("Add medicine name & dosage...") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            Button(
                                onClick = {
                                    if (newPrescriptionMed.isNotBlank()) {
                                        prescribedMedsList = prescribedMedsList + newPrescriptionMed.trim()
                                        newPrescriptionMed = ""
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Add")
                            }
                        }
                    }
                }

                // Final Action Buttons
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onSaveConsultationNotes(clinicianNotes, followUpAdvice, prescribedMedsList)
                            onEndCall()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save to Patient EHR & Return Home", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
        // ==========================================
        // LIVE CALL ROOM INTERFACE
        // ==========================================
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
        ) {
            // 1. Doctor Main Video Feed Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 96.dp)
            ) {
                if (session.isVideoDisabled) {
                    // Audio-Only Avatar Display
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = session.doctorName.take(2).uppercase(),
                                style = MaterialTheme.typography.headlineLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = "Dr. ${session.doctorName}",
                            style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Audio Consultation Active",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF94A3B8))
                        )
                    }
                } else {
                    // Simulated Doctor Video View
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0369A1).copy(alpha = 0.4f))
                                    .border(2.dp, Color(0xFF38BDF8), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(72.dp)
                                )
                            }
                            Text(
                                text = "Dr. ${session.doctorName}",
                                style = MaterialTheme.typography.titleLarge.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = session.doctorSpecialty,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF38BDF8))
                            )
                            Badge(
                                containerColor = Color(0xFF1E293B).copy(alpha = 0.8f),
                                contentColor = Color(0xFF4ADE80)
                            ) {
                                Text(
                                    text = "● Connected (Live Video)",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }

                // DEMO TELEMEDICINE MODE Watermark
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.65f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "DEMO TELEMEDICINE MODE • SIMULATED WEBRTC FEED",
                        color = Color(0xFFFBBF24),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // 2. Front-Camera Self Preview (Picture-in-Picture)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 16.dp)
                        .size(width = 110.dp, height = 150.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF334155))
                        .border(1.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (session.isFrontCamera) "You (Front)" else "You (Back)",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                // Top Controls: Doctor Name & Timer
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 48.dp, start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = timerFormatted,
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // 3. Bottom Call Controls Bar
            Surface(
                color = Color(0xFF1E293B).copy(alpha = 0.95f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute / Unmute Button
                    IconButton(
                        onClick = onToggleAudio,
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                if (session.isAudioMuted) Color(0xFFEF4444) else Color(0xFF334155),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (session.isAudioMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Audio Mute",
                            tint = Color.White
                        )
                    }

                    // Video Camera On/Off
                    IconButton(
                        onClick = onToggleVideo,
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                if (session.isVideoDisabled) Color(0xFFEF4444) else Color(0xFF334155),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (session.isVideoDisabled) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            contentDescription = "Video Camera",
                            tint = Color.White
                        )
                    }

                    // Flip Camera
                    IconButton(
                        onClick = onFlipCamera,
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFF334155), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Flip Camera",
                            tint = Color.White
                        )
                    }

                    // Open Chat Pane
                    IconButton(
                        onClick = { showChatSheet = true },
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                if (session.chatMessages.isNotEmpty()) Color(0xFF0284C7) else Color(0xFF334155),
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Chat",
                            tint = Color.White
                        )
                    }

                    // End Call Button
                    IconButton(
                        onClick = { isCallCompleted = true },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFDC2626), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            // 4. In-Call Live Chat Bottom Sheet / Dialog
            if (showChatSheet) {
                Dialog(onDismissRequest = { showChatSheet = false }) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(480.dp)
                            .padding(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "In-Consultation Chat",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                IconButton(onClick = { showChatSheet = false }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            }

                            HorizontalDivider()

                            // Message List
                            val listState = rememberLazyListState()
                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(session.chatMessages) { msg ->
                                    val isMe = !msg.isFromDoctor
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                                    ) {
                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            modifier = Modifier.widthIn(max = 260.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = msg.senderName,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isMe) MaterialTheme.colorScheme.primary else Color(0xFF0369A1)
                                                    )
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = msg.messageText,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = msg.timestampFormatted,
                                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Chat Input Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = chatInput,
                                    onValueChange = { chatInput = it },
                                    placeholder = { Text("Message doctor...") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                IconButton(
                                    onClick = {
                                        if (chatInput.isNotBlank()) {
                                            onSendMessage(chatInput.trim())
                                            chatInput = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
