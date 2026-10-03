package com.example.ui.components

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.*

/**
 * High-visibility Critical Care & Emergency Alert Section for the Home Screen.
 */
@Composable
fun CriticalEmergencySection(
    isEmergencyActive: Boolean,
    activeEmergencyType: String?,
    onTriggerSos: () -> Unit,
    onOpenEmergencyCenter: () -> Unit,
    onSelectCriticalSymptom: (String) -> Unit,
    onCallEmergencyDesk: () -> Unit,
    onDispatchPorter: () -> Unit,
    isAccessibilityMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isEmergencyActive) CoralError.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isEmergencyActive) 2.dp else 1.5.dp,
            color = if (isEmergencyActive) CoralError else CoralError.copy(alpha = 0.4f)
        ),
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Alert Beacon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CoralError)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Emergency,
                            contentDescription = "Emergency SOS",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "CRITICAL & EMERGENCY SOS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = CoralError,
                                letterSpacing = 0.5.sp,
                                fontSize = if (isAccessibilityMode) 18.sp else 15.sp
                            )
                        )
                        Text(
                            text = if (isEmergencyActive) "🚨 Active Emergency Response En Route" else "Immediate hospital medical response & code dispatch",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isEmergencyActive) CoralError else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isEmergencyActive) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    }
                }

                // Info / Open Center Button
                FilledTonalButton(
                    onClick = onOpenEmergencyCenter,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = CoralError.copy(alpha = 0.15f),
                        contentColor = CoralError
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("open_emergency_center_button")
                ) {
                    Text(
                        text = "Hub & Guides",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Main SOS 1-Tap Broadcast Button
            Button(
                onClick = onTriggerSos,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CoralError,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isAccessibilityMode) 58.dp else 52.dp)
                    .scale(if (isEmergencyActive) pulseScale else 1f)
                    .testTag("primary_sos_emergency_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "SOS Alert",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isEmergencyActive) "🚨 MEDICAL TEAM DISPATCHED (ETA <45s)" else "1-TAP EMERGENCY SOS BROADCAST",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = if (isAccessibilityMode) 17.sp else 15.sp
                        )
                    )
                }
            }

            // Fast-Action Emergency Option Chips
            Text(
                text = "Rapid Critical Selectors (Priority Response):",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    EmergencyQuickChip(
                        icon = Icons.Default.Favorite,
                        title = "Chest Pain / Cardiac",
                        color = Color(0xFFD32F2F),
                        onClick = { onSelectCriticalSymptom("Severe Chest Pain / Potential Cardiac Event") },
                        testTag = "sos_cardiac_chip"
                    )
                }
                item {
                    EmergencyQuickChip(
                        icon = Icons.Default.Air,
                        title = "Severe Breathlessness",
                        color = Color(0xFFC2185B),
                        onClick = { onSelectCriticalSymptom("Severe Breathlessness / Respiratory Distress") },
                        testTag = "sos_respiratory_chip"
                    )
                }
                item {
                    EmergencyQuickChip(
                        icon = Icons.Default.Psychology,
                        title = "Stroke Signs (FAST)",
                        color = Color(0xFF7B1FA2),
                        onClick = { onSelectCriticalSymptom("Stroke Signs (Facial droop / Arm weakness / Slurred speech)") },
                        testTag = "sos_stroke_chip"
                    )
                }
                item {
                    EmergencyQuickChip(
                        icon = Icons.Default.Accessible,
                        title = "Wheelchair & Stretcher",
                        color = NavyPrimary,
                        onClick = onDispatchPorter,
                        testTag = "sos_porter_chip"
                    )
                }
                item {
                    EmergencyQuickChip(
                        icon = Icons.Default.PhoneInTalk,
                        title = "Call ER Desk (108)",
                        color = TealSecondary,
                        onClick = onCallEmergencyDesk,
                        testTag = "sos_call_er_chip"
                    )
                }
            }
        }
    }
}

@Composable
private fun EmergencyQuickChip(
    icon: ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
    }
}

/**
 * Comprehensive CareFlow AI Emergency & Critical Care Center Dialog.
 */
@Composable
fun CareFlowEmergencyDialog(
    isOpen: Boolean,
    isEmergencyActive: Boolean,
    activeEmergencyType: String?,
    emergencyEtaSeconds: Int,
    onDismiss: () -> Unit,
    onTriggerSos: (String) -> Unit,
    onCancelSos: () -> Unit,
    onCallEmergencyDesk: () -> Unit,
    onDispatchPorter: () -> Unit
) {
    if (!isOpen) return

    var selectedFirstAidTab by remember { mutableStateOf(0) }
    val firstAidTopics = listOf("CPR Lifesaving", "Stroke (F.A.S.T.)", "Choking (Heimlich)", "Severe Bleeding", "Recovery Position")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CoralError)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Emergency,
                                contentDescription = "Emergency",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "CareFlow Emergency Hub",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = CoralError
                                )
                            )
                            Text(
                                text = "Hospital Code Dispatch & Lifesaving First Aid",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("emergency_dialog_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate600)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Active Emergency Monitor (if active)
                    if (isEmergencyActive) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CoralError.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(2.dp, CoralError),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Alert Active",
                                        tint = CoralError,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "🚨 LIVE EMERGENCY CODE ACTIVE",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = CoralError
                                        )
                                    )
                                }

                                Text(
                                    text = "Type: ${activeEmergencyType ?: "General Distress"}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "📍 Location: Kiosk 01 (Main Atrium)",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                    Text(
                                        text = "⏱ ETA: < $emergencyEtaSeconds sec",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CoralError
                                        )
                                    )
                                }

                                Text(
                                    text = "Assigned Team: Rapid Response Trauma Unit #4 (Dr. Arvind & Triage Nurse)",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Slate700)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    OutlinedButton(
                                        onClick = onCancelSos,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralError),
                                        modifier = Modifier.testTag("cancel_sos_button")
                                    ) {
                                        Text("Cancel Alert", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Emergency 1-Tap Trigger Buttons
                    Text(
                        text = "1. Instant Medical Code Dispatch",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onTriggerSos("Code Blue: Cardiac Arrest / Severe Chest Pain") },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("code_blue_button")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🫀 CODE BLUE", fontWeight = FontWeight.Black, fontSize = 13.sp)
                                Text("Cardiac Arrest", fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = { onTriggerSos("Code Red: Trauma & Acute Distress") },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CoralError),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("code_red_button")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🚨 CODE RED", fontWeight = FontWeight.Black, fontSize = 13.sp)
                                Text("Trauma / Collapse", fontSize = 11.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onDispatchPorter,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("porter_stretcher_button")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("♿ STRETCHER / PORTER", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Wheelchair Transport", fontSize = 10.sp)
                            }
                        }

                        Button(
                            onClick = onCallEmergencyDesk,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealSecondary),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("call_er_hotline_button")
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📞 CALL ER DESK", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Ext: 108 / 112", fontSize = 10.sp)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Hospital Emergency Hotlines Direct Reference
                    Text(
                        text = "2. Critical Hospital Emergency Hotlines",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EmergencyHotlineRow(label = "Emergency Trauma Triage", number = "108 / 112 (Toll Free)")
                            EmergencyHotlineRow(label = "Cardiac Resuscitation Desk", number = "+91 (0) 800-CARDIAC")
                            EmergencyHotlineRow(label = "Poison & Toxicology Control", number = "+91 (0) 800-POISON")
                            EmergencyHotlineRow(label = "Emergency Blood Bank", number = "+91 (0) 800-BLOOD (24/7)")
                            EmergencyHotlineRow(label = "Security & Porter Dispatch", number = "Extension 04 / Gate A")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // First Aid Lifesaving Directives
                    Text(
                        text = "3. Emergency First-Aid Lifesaving Guide",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )

                    ScrollableTabRow(
                        selectedTabIndex = selectedFirstAidTab,
                        edgePadding = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        firstAidTopics.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedFirstAidTab == index,
                                onClick = { selectedFirstAidTab = index },
                                text = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) }
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            when (selectedFirstAidTab) {
                                0 -> {
                                    Text("🫀 CPR (Cardiopulmonary Resuscitation)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("1. Check responsiveness & breathing. Call Code Blue / SOS immediately.", fontSize = 13.sp)
                                    Text("2. Place heels of interlocking hands on center of chest.", fontSize = 13.sp)
                                    Text("3. Push hard & fast at 100-120 beats per minute (to the beat of 'Stayin' Alive').", fontSize = 13.sp)
                                    Text("4. Perform 30 chest compressions followed by 2 gentle rescue breaths.", fontSize = 13.sp)
                                    Text("5. Continue without stopping until the rapid response team arrives.", fontSize = 13.sp)
                                }
                                1 -> {
                                    Text("🧠 Stroke Assessment (F.A.S.T.)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("• F - FACE: Ask person to smile. Does one side of the face droop?", fontSize = 13.sp)
                                    Text("• A - ARMS: Ask person to raise both arms. Does one arm drift downward?", fontSize = 13.sp)
                                    Text("• S - SPEECH: Ask person to repeat a simple phrase. Is speech slurred or strange?", fontSize = 13.sp)
                                    Text("• T - TIME: If any sign is present, time is critical! Tap SOS immediately.", fontSize = 13.sp)
                                }
                                2 -> {
                                    Text("🗣 Choking (Heimlich Maneuver)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("1. Stand behind the choking person and wrap arms around waist.", fontSize = 13.sp)
                                    Text("2. Make a fist just above the person's navel.", fontSize = 13.sp)
                                    Text("3. Grasp fist with other hand and give quick upward & inward thrusts.", fontSize = 13.sp)
                                    Text("4. Repeat until the foreign object is dislodged or help arrives.", fontSize = 13.sp)
                                }
                                3 -> {
                                    Text("🩸 Severe Bleeding Control", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("1. Apply firm, continuous direct pressure with a clean cloth or sterile gauze.", fontSize = 13.sp)
                                    Text("2. Keep firm pressure for at least 5-10 minutes without lifting.", fontSize = 13.sp)
                                    Text("3. If bleeding on an arm or leg, elevate above heart level if possible.", fontSize = 13.sp)
                                    Text("4. Do not remove saturated bandages; add additional layers on top.", fontSize = 13.sp)
                                }
                                4 -> {
                                    Text("🛏 Recovery Position (Unconscious but Breathing)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text("1. Place person on their side to keep airway open and prevent choking.", fontSize = 13.sp)
                                    Text("2. Bend top leg at knee to prop the body stably.", fontSize = 13.sp)
                                    Text("3. Tilt head gently backward to keep airway clear.", fontSize = 13.sp)
                                    Text("4. Monitor breathing continuously until the medical team arrives.", fontSize = 13.sp)
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
private fun EmergencyHotlineRow(label: String, number: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
        )
        Text(
            text = number,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = CoralError
            )
        )
    }
}
