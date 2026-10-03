package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.Language
import com.example.data.model.MessageSender
import com.example.data.model.SocratesIntake
import com.example.ui.components.LargeActionButton
import com.example.ui.components.QuickReplyPill
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun HistoryIntakeScreen(
    chatMessages: List<ChatMessage>,
    quickReplies: List<String>,
    isAiThinking: Boolean,
    socratesIntake: SocratesIntake,
    selectedLanguage: Language,
    isRedFlagDetected: Boolean = false,
    redFlagMessage: String = "",
    onAlertStaff: () -> Unit = {},
    onDismissRedFlag: () -> Unit = {},
    onSendMessage: (String) -> Unit,
    onReadAloud: (String) -> Unit,
    onProceedToDocumentScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Speech recognition launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                onSendMessage(spoken)
            }
        }
    }

    // Auto-scroll to latest message
    LaunchedEffect(chatMessages.size, isAiThinking) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SoftBgPale)
            .navigationBarsPadding()
    ) {
        // Step 2 of 4 Progress Header
        com.example.ui.components.IntakeStepProgressHeader(
            step = 2,
            totalSteps = 4,
            stepTitle = "Conversational History Intake",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Red-Flag Urgent Alert Banner if acute emergency symptoms are detected
        AnimatedVisibility(
            visible = isRedFlagDetected,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            com.example.ui.components.UrgentRedFlagAlertBanner(
                message = redFlagMessage,
                onCallStaff = onAlertStaff,
                onDismiss = onDismissRedFlag,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Clinical Intake Header with SOCRATES tracker badge
        Surface(
            color = Color.White,
            tonalElevation = 1.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NavyLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MedicalServices,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "SOCRATES History Intake",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                fontSize = 15.sp
                            )
                        )
                        Text(
                            text = "Chief Complaint & Medical Background",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                        )
                    }
                }

                // Progress Badge
                val completed = socratesIntake.getCompletedFieldsCount()
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (completed >= 6) MediGreenLight else NavyLight
                ) {
                    Text(
                        text = "$completed/11 recorded",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (completed >= 6) Color(0xFF064E3B) else NavyDark
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Chat Message Stream
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(chatMessages, key = { it.id }) { message ->
                ChatBubble(
                    message = message,
                    onReadAloud = { onReadAloud(message.text) }
                )
            }

            if (isAiThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, top = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.5.dp,
                            color = MediBluePrimary
                        )
                        Text(
                            text = "MediKiosk AI is listening & thinking...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Slate500,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                    }
                }
            }
        }

        // Quick Reply Buttons (2 to 4 tappable pills)
        if (quickReplies.isNotEmpty() && !isAiThinking) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Suggested Quick Responses (Tap any):",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Slate600,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickReplies) { reply ->
                        QuickReplyPill(
                            text = reply,
                            onClick = {
                                onSendMessage(reply)
                            },
                            testTag = "quick_reply_${reply.take(10).replace(" ", "_")}"
                        )
                    }
                }
            }
        }

        // Bottom Input Bar & Document Scan Button
        Surface(
            color = Color.White,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Voice Input Button (Built-in Android Speech Recognition)
                    IconButton(
                        onClick = {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                )
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguage.code)
                                putExtra(
                                    RecognizerIntent.EXTRA_PROMPT,
                                    "Speak your symptoms clearly..."
                                )
                            }
                            try {
                                speechLauncher.launch(intent)
                            } catch (e: Exception) {
                                // Ignore or fallback
                            }
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MediTealLight)
                            .testTag("voice_input_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = MediTealDark,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Text Input Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Type symptom or reply...", fontSize = 15.sp, color = Slate500) },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 16.sp),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            cursorColor = Color.Black,
                            focusedBorderColor = MediBluePrimary,
                            unfocusedBorderColor = Slate300
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 50.dp)
                            .testTag("chat_text_input")
                    )

                    // Send Button
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !isAiThinking,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank() && !isAiThinking) NavyPrimary else Slate200)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isAiThinking) Color.White else Slate500,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Proceed Button
                val hasSomeData = socratesIntake.chiefComplaint.isNotBlank() || chatMessages.size >= 4
                LargeActionButton(
                    text = "Done / Proceed to Document Scan",
                    onClick = onProceedToDocumentScan,
                    enabled = hasSomeData,
                    icon = Icons.Default.DocumentScanner,
                    containerColor = NavyPrimary,
                    testTag = "proceed_to_doc_scan_button"
                )
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onReadAloud: () -> Unit
) {
    val isPatient = message.sender == MessageSender.PATIENT

    Row(
        horizontalArrangement = if (isPatient) Arrangement.End else Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!isPatient) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NavyPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "AI",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isPatient) 18.dp else 4.dp,
                bottomEnd = if (isPatient) 4.dp else 18.dp
            ),
            color = if (isPatient) NavyPrimary else Color.White,
            border = if (isPatient) null else androidx.compose.foundation.BorderStroke(1.dp, Slate200),
            shadowElevation = 2.dp,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (isPatient) Color.White else Slate900,
                        fontSize = 16.sp,
                        lineHeight = 23.sp
                    )
                )

                if (!isPatient) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = NavyLight,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onReadAloud() }
                                .testTag("chat_read_aloud_button_${message.id.take(8)}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Read aloud question",
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Listen / सुनें",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = NavyPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isPatient) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Slate300)
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Patient",
                    tint = Slate700,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
