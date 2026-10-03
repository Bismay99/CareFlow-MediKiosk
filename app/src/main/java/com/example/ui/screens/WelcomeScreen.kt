package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.ui.components.LargeActionButton
import com.example.ui.components.PastelShortcutItem
import com.example.ui.components.UrgentRedFlagAlertBanner
import com.example.ui.components.kioskTextFieldColors
import com.example.ui.theme.*
import com.example.ui.viewmodel.KioskScreen

@Composable
fun WelcomeScreen(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    onGetStartedClick: () -> Unit,
    onStartHistoryIntake: () -> Unit,
    onScanDocuments: () -> Unit,
    onShortcutClick: (KioskScreen) -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isAccessibilityMode: Boolean,
    onToggleAccessibility: () -> Unit,
    isRedFlagDetected: Boolean,
    redFlagMessage: String,
    onAlertStaff: () -> Unit,
    onDismissRedFlag: () -> Unit,
    onPlayAudioWelcome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val languages = listOf(
        Language.ENGLISH,
        Language.HINDI,
        Language.BENGALI,
        Language.TAMIL,
        Language.TELUGU,
        Language.MARATHI
    )

    var showAccessibilityDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SoftBgPale)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Red-Flag Urgent Alert Banner (shown at top if detected)
        AnimatedVisibility(
            visible = isRedFlagDetected,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            UrgentRedFlagAlertBanner(
                message = redFlagMessage,
                onCallStaff = onAlertStaff,
                onDismiss = onDismissRedFlag
            )
        }

        // Top Header Row with Title, Audio assist, and Accessibility Settings Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(NavyPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = "Hospital Cross Logo",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column {
                    Text(
                        text = "CareFlow Ai",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            fontSize = 22.sp
                        )
                    )
                    Text(
                        text = "AI Patient Intake & Critical Care",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate600,
                            fontSize = 13.sp
                        )
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Audio assist button
                IconButton(
                    onClick = onPlayAudioWelcome,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MediTealLight)
                        .testTag("audio_welcome_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Listen to Instructions",
                        tint = MediTealDark,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Settings icon for High-Contrast / Large-Font accessibility toggle
                IconButton(
                    onClick = { showAccessibilityDialog = true },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isAccessibilityMode) NavyPrimary else Color.White)
                        .border(1.dp, if (isAccessibilityMode) NavyPrimary else Slate200, CircleShape)
                        .testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Accessibility & Display Settings",
                        tint = if (isAccessibilityMode) Color.White else NavyPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Search Bar at Top
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = {
                    Text(
                        text = "Search symptoms, medications, services...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Slate400)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = NavyPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Search",
                                tint = Slate400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = kioskTextFieldColors(
                    focusedBorderColor = NavyPrimary,
                    unfocusedBorderColor = Color.Transparent,
                    containerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_search_bar")
            )
        }

        // Dynamic search results banner if query is entered
        if (searchQuery.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Quick Actions for \"$searchQuery\"",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SuggestionChip(
                            onClick = onStartHistoryIntake,
                            label = { Text("Report Symptoms") },
                            icon = {
                                Icon(Icons.Default.ChatBubble, null, modifier = Modifier.size(16.dp))
                            }
                        )
                        SuggestionChip(
                            onClick = onScanDocuments,
                            label = { Text("Scan Rx / Labs") },
                            icon = {
                                Icon(Icons.Default.DocumentScanner, null, modifier = Modifier.size(16.dp))
                            }
                        )
                    }
                }
            }
        }

        // Two Large Side-by-Side Feature Cards with Icon Illustrations
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Feature Card 1: Start History Intake
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onStartHistoryIntake() }
                    .testTag("feature_card_history_intake")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(NavyLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Start History Intake",
                            tint = NavyPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Start History\nIntake",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                fontSize = 16.sp,
                                lineHeight = 20.sp
                            )
                        )
                        Text(
                            text = "AI-guided conversational symptom intake",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate600,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Begin Intake",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = NavyPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Feature Card 2: Scan Documents
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onScanDocuments() }
                    .testTag("feature_card_scan_documents")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MediTealLight)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = "Scan Documents",
                            tint = MediTealDark,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Scan\nDocuments",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                fontSize = 16.sp,
                                lineHeight = 20.sp
                            )
                        )
                        Text(
                            text = "Extract prescriptions & lab tests instantly",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Slate600,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Scan Now",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MediTealDark,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MediTealDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Row of 4 Icon-in-Circle Shortcuts with Pastel Backgrounds
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 3.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Quick Navigation",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate800,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Top
                ) {
                    // 1. Chief Complaint (Pastel Light Blue)
                    PastelShortcutItem(
                        title = "Chief\nComplaint",
                        icon = Icons.Default.MedicalServices,
                        bgColor = PastelBlueBg,
                        iconColor = PastelBlueIcon,
                        onClick = { onShortcutClick(KioskScreen.CONVERSATIONAL_INTAKE) },
                        testTag = "shortcut_chief_complaint"
                    )

                    // 2. Medications (Pastel Peach)
                    PastelShortcutItem(
                        title = "Medications",
                        icon = Icons.Default.Medication,
                        bgColor = PastelPeachBg,
                        iconColor = PastelPeachIcon,
                        onClick = { onShortcutClick(KioskScreen.DOCUMENT_SCAN) },
                        testTag = "shortcut_medications"
                    )

                    // 3. Lab Reports (Pastel Lavender)
                    PastelShortcutItem(
                        title = "Lab\nReports",
                        icon = Icons.Default.Biotech,
                        bgColor = PastelLavenderBg,
                        iconColor = PastelLavenderIcon,
                        onClick = { onShortcutClick(KioskScreen.DOCUMENT_SCAN) },
                        testTag = "shortcut_lab_reports"
                    )

                    // 4. Summary (Pastel Mint)
                    PastelShortcutItem(
                        title = "Summary",
                        icon = Icons.Default.AssignmentTurnedIn,
                        bgColor = PastelMintBg,
                        iconColor = PastelMintIcon,
                        onClick = { onShortcutClick(KioskScreen.SUMMARY) },
                        testTag = "shortcut_summary"
                    )
                }
            }
        }

        // Language Select Section (Floating White Rounded Card)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 3.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Preferred Language / भाषा",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            fontSize = 15.sp
                        )
                    )
                    Text(
                        text = selectedLanguage.nativeName,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MediTealDark,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                // Language Grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    languages.chunked(3).forEach { rowLangs ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowLangs.forEach { lang ->
                                val isSelected = lang == selectedLanguage
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) NavyPrimary else SoftBgPale,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) NavyPrimary else Slate200
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onLanguageSelected(lang) }
                                        .testTag("language_chip_${lang.code}")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = lang.nativeName,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else Slate800,
                                                fontSize = 13.sp
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Primary Call to Action Button
        LargeActionButton(
            text = when (selectedLanguage) {
                Language.HINDI -> "शुरू करें (Get Started)"
                Language.BENGALI -> "শুরু করুন (Get Started)"
                Language.TAMIL -> "தொடங்குங்கள் (Get Started)"
                Language.TELUGU -> "ప్రారంభించండి (Get Started)"
                Language.MARATHI -> "सुरू करा (Get Started)"
                else -> "Get Started • Check-in"
            },
            onClick = onGetStartedClick,
            containerColor = NavyPrimary,
            testTag = "welcome_get_started_button"
        )

        Spacer(modifier = Modifier.height(10.dp))
    }

    // Accessibility Settings Dialog
    if (showAccessibilityDialog) {
        AlertDialog(
            onDismissRequest = { showAccessibilityDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessibilityNew,
                        contentDescription = null,
                        tint = NavyPrimary
                    )
                    Text("Display & Accessibility", fontWeight = FontWeight.Bold, color = NavyPrimary)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Customize text size and visual contrast for maximum clarity in hospital waiting rooms.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Slate700)
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SoftBgPale,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleAccessibility() }
                            .padding(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "High Contrast & Large Font",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Slate900
                                )
                                Text(
                                    text = "Increases body & heading size for easier reading",
                                    fontSize = 12.sp,
                                    color = Slate600
                                )
                            }
                            Switch(
                                checked = isAccessibilityMode,
                                onCheckedChange = { onToggleAccessibility() }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showAccessibilityDialog = false }
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, color = NavyPrimary)
                }
            }
        )
    }
}
