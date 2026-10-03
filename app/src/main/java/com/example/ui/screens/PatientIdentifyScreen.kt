package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Language
import com.example.data.model.PatientProfile
import com.example.ui.components.LargeActionButton
import com.example.ui.components.kioskTextFieldColors
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientIdentifyScreen(
    profile: PatientProfile,
    selectedLanguage: Language,
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit,
    onGenderChange: (String) -> Unit,
    onAbhaChange: (String) -> Unit,
    onNewPatientToggle: (Boolean) -> Unit,
    onConsentToggle: (Boolean) -> Unit,
    onReadConsentAloud: () -> Unit,
    onProceedClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val genders = listOf("Male", "Female", "Other")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SoftBgPale)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step 1 of 4 Progress Header
        com.example.ui.components.IntakeStepProgressHeader(
            step = 1,
            totalSteps = 4,
            stepTitle = "Patient Identification"
        )

        // Section Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Patient Identification",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            )
            Text(
                text = "Please enter your details to create or link your hospital visit record.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Slate600)
            )
        }

        // Card Container
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Field
                OutlinedTextField(
                    value = profile.name,
                    onValueChange = onNameChange,
                    label = { Text("Full Name / पूरा नाम", fontSize = 16.sp) },
                    placeholder = { Text("e.g. Ramesh Patel") },
                    textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 16.sp),
                    colors = kioskTextFieldColors(focusedBorderColor = MediBluePrimary),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MediBluePrimary
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("patient_name_input")
                )

                // Age & Quick Age Selectors
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = profile.age,
                        onValueChange = onAgeChange,
                        label = { Text("Age (Years) / उम्र", fontSize = 16.sp) },
                        placeholder = { Text("e.g. 52") },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 16.sp),
                        colors = kioskTextFieldColors(focusedBorderColor = MediBluePrimary),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MediBluePrimary
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("patient_age_input")
                    )

                    // Quick Age Selector Chips for elderly convenience
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("25", "45", "60", "72").forEach { quickAge ->
                            FilterChip(
                                selected = profile.age == quickAge,
                                onClick = { onAgeChange(quickAge) },
                                label = { Text("$quickAge yrs") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MediBlueLight,
                                    selectedLabelColor = MediBlueDark
                                ),
                                modifier = Modifier.testTag("quick_age_$quickAge")
                            )
                        }
                    }
                }

                // Gender Selection (Large touch target pills)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Gender / लिंग",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Slate800
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        genders.forEach { gender ->
                            val isSelected = profile.gender.equals(gender, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) MediBlueLight else Slate100,
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 2.dp else 1.dp,
                                    if (isSelected) MediBluePrimary else Slate300
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onGenderChange(gender) }
                                    .testTag("gender_option_$gender")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = gender,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MediBlueDark else Slate700
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Slate200, modifier = Modifier.padding(vertical = 4.dp))

                // New Patient Toggle vs Mock ABHA ID
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "First Time / New Patient",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = "Toggle ON to skip ABHA ID",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate500)
                        )
                    }

                    Switch(
                        checked = profile.isNewPatient,
                        onCheckedChange = onNewPatientToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MediTealSecondary
                        ),
                        modifier = Modifier.testTag("new_patient_toggle")
                    )
                }

                if (!profile.isNewPatient) {
                    OutlinedTextField(
                        value = profile.abhaId,
                        onValueChange = onAbhaChange,
                        label = { Text("ABHA ID (Health Account)", fontSize = 16.sp) },
                        placeholder = { Text("e.g. 91-8842-1092-3841") },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 16.sp),
                        colors = kioskTextFieldColors(focusedBorderColor = MediTealSecondary),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = MediTealSecondary
                            )
                        },
                        trailingIcon = {
                            TextButton(
                                onClick = { onAbhaChange("91-4521-8839-1029") },
                                modifier = Modifier.testTag("fill_mock_abha_button")
                            ) {
                                Text("Demo ID", color = MediTealDark, fontWeight = FontWeight.Bold)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("abha_id_input")
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MediGreenLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MediGreenSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "A temporary hospital kiosk token will be assigned.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF064E3B),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }
        }

        // Consent Card with TTS read-aloud button
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MediBlueSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MediBlueLight),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Data Consent / सहमति",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MediBlueDark
                        )
                    )

                    // Read aloud button
                    FilledTonalButton(
                        onClick = onReadConsentAloud,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MediBlueLight,
                            contentColor = MediBlueDark
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("read_consent_aloud_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Read Aloud",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Read Aloud", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onConsentToggle(!profile.consentGiven) }
                        .testTag("consent_checkbox_row")
                ) {
                    Checkbox(
                        checked = profile.consentGiven,
                        onCheckedChange = onConsentToggle,
                        colors = CheckboxDefaults.colors(
                            checkedColor = MediBluePrimary
                        ),
                        modifier = Modifier.testTag("consent_checkbox")
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedLanguage.consentText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Slate800,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }

        // Proceed Action Button
        val canProceed = profile.name.isNotBlank() && profile.age.isNotBlank() && profile.consentGiven
        LargeActionButton(
            text = "Proceed to Symptom Intake",
            onClick = onProceedClick,
            enabled = canProceed,
            testTag = "proceed_to_intake_button"
        )
    }
}
