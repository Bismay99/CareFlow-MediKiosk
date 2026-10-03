package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppThemeMode
import com.example.data.model.AuthUser
import com.example.data.model.Language
import com.example.data.model.PatientProfile
import com.example.ui.components.LanguageSelectDialog
import com.example.ui.theme.*

@Composable
fun PatientProfileScreen(
    profile: PatientProfile,
    user: AuthUser?,
    selectedLanguage: Language,
    themeMode: AppThemeMode,
    isAccessibilityMode: Boolean,
    isVoiceAssistantEnabled: Boolean,
    isTtsEnabled: Boolean,
    isVoiceIntakeEnabled: Boolean,
    onBackClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onThemeSelect: (AppThemeMode) -> Unit,
    onToggleEasyMode: () -> Unit,
    onToggleVoiceAssistant: (Boolean) -> Unit,
    onToggleTts: (Boolean) -> Unit,
    onToggleVoiceIntake: (Boolean) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showThemeDialog by remember { mutableStateOf(false) }

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .widthIn(max = 680.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Back Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .testTag("profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "Patient Profile & Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                // Logout Button
                OutlinedButton(
                    onClick = onLogoutClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralError),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CoralError.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("profile_logout_button")
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Logout", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Patient Identity Card
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 3.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(18.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary.copy(alpha = 0.15f))
                    ) {
                        Text(
                            text = profile.name.take(1).ifBlank { "P" },
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp,
                            color = NavyPrimary
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = profile.name.ifBlank { user?.name ?: "Patient" },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )

                        Text(
                            text = "ABHA: ${profile.abhaId.ifBlank { "14-2951-8402-1920" }}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Text(
                            text = "${profile.age} Years • ${profile.gender} • Phone: ${user?.phone ?: "+91 98201 44521"}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // ==========================================
            // ACCESSIBILITY & DISPLAY SETTINGS (Section 26 & 30)
            // ==========================================
            Text(
                text = "ACCESSIBILITY & APPEARANCE",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // Easy Mode Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.AccessibilityNew, contentDescription = null, tint = NavyPrimary)
                            Column {
                                Text("Easy Mode (Senior & High Contrast)", fontWeight = FontWeight.SemiBold)
                                Text("Enlarged text and buttons for easy readability", style = MaterialTheme.typography.bodySmall, color = Slate600)
                            }
                        }
                        Switch(
                            checked = isAccessibilityMode,
                            onCheckedChange = { onToggleEasyMode() },
                            modifier = Modifier.testTag("toggle_easy_mode_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Language Selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageClick() }
                            .padding(vertical = 12.dp)
                            .testTag("profile_language_select_row")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = NavyPrimary)
                            Column {
                                Text("Language / भाषा", fontWeight = FontWeight.SemiBold)
                                Text(selectedLanguage.nativeName, style = MaterialTheme.typography.bodySmall, color = Slate600)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate400)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Theme Selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showThemeDialog = true }
                            .padding(vertical = 12.dp)
                            .testTag("profile_theme_select_row")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = NavyPrimary)
                            Column {
                                Text("Theme / Appearance", fontWeight = FontWeight.SemiBold)
                                Text(themeMode.name, style = MaterialTheme.typography.bodySmall, color = Slate600)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate400)
                    }
                }
            }

            // ==========================================
            // AI VOICE ASSISTANT SETTINGS (Section 26)
            // ==========================================
            Text(
                text = "AI VOICE & ASSISTIVE SERVICES",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // Voice Assistant ON/OFF
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Mic, contentDescription = null, tint = TealSecondary)
                            Column {
                                Text("MediKiosk AI Voice Assistant", fontWeight = FontWeight.SemiBold)
                                Text("Conversational hospital navigation & queries", style = MaterialTheme.typography.bodySmall, color = Slate600)
                            }
                        }
                        Switch(
                            checked = isVoiceAssistantEnabled,
                            onCheckedChange = { onToggleVoiceAssistant(it) },
                            modifier = Modifier.testTag("toggle_voice_assistant_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Text-To-Speech ON/OFF
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, tint = TealSecondary)
                            Column {
                                Text("Text-to-Speech (Spoken Audio)", fontWeight = FontWeight.SemiBold)
                                Text("Read directions and responses aloud", style = MaterialTheme.typography.bodySmall, color = Slate600)
                            }
                        }
                        Switch(
                            checked = isTtsEnabled,
                            onCheckedChange = { onToggleTts(it) },
                            modifier = Modifier.testTag("toggle_tts_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    // Voice Intake ON/OFF
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = TealSecondary)
                            Column {
                                Text("Voice Symptom Intake", fontWeight = FontWeight.SemiBold)
                                Text("Speak clinical complaints in your language", style = MaterialTheme.typography.bodySmall, color = Slate600)
                            }
                        }
                        Switch(
                            checked = isVoiceIntakeEnabled,
                            onCheckedChange = { onToggleVoiceIntake(it) },
                            modifier = Modifier.testTag("toggle_voice_intake_switch")
                        )
                    }
                }
            }

            // Kiosk Privacy & Session Timeout Notice
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Emerald500.copy(alpha = 0.08f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Emerald500.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(14.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Emerald700)
                    Text(
                        text = "For your privacy, this public kiosk terminal automatically wipes session tokens and patient data upon logout or 3 minutes of inactivity.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Emerald700, fontSize = 12.sp)
                    )
                }
            }
        }
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Appearance") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppThemeMode.entries.forEach { mode ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (themeMode == mode) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onThemeSelect(mode)
                                    showThemeDialog = false
                                }
                        ) {
                            Text(
                                text = when (mode) {
                                    AppThemeMode.SYSTEM -> "System Default"
                                    AppThemeMode.LIGHT -> "Light Mode (Hospital Blue)"
                                    AppThemeMode.DARK -> "Dark Mode (Deep Navy)"
                                },
                                modifier = Modifier.padding(14.dp),
                                fontWeight = if (themeMode == mode) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Done") }
            }
        )
    }
}
