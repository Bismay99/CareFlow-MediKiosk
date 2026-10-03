package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClinicalSummary
import com.example.data.model.PatientProfile
import com.example.ui.components.LargeActionButton
import com.example.ui.components.kioskTextFieldColors
import com.example.ui.theme.*

@Composable
fun SummaryScreen(
    patient: PatientProfile,
    summary: ClinicalSummary?,
    isGenerating: Boolean,
    isSubmitted: Boolean,
    onEditSection: (String, String) -> Unit,
    onSubmitClick: () -> Unit,
    onResetForNewPatient: () -> Unit,
    onRegenerateSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editingSectionTitle by remember { mutableStateOf<String?>(null) }
    var editingSectionContent by remember { mutableStateOf("") }

    if (isGenerating || summary == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .background(Slate50)
                .padding(32.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = MediBluePrimary,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(54.dp)
                )
                Text(
                    text = "Synthesizing Clinical Intake Summary...",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                )
                Text(
                    text = "Gemini is integrating SOCRATES history and scanned prescription records into a doctor-readable SOAP report.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Slate600
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SoftBgPale)
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step 4 of 4 Progress Header
        com.example.ui.components.IntakeStepProgressHeader(
            step = 4,
            totalSteps = 4,
            stepTitle = "Clinical Summary & Doctor Review"
        )

        // Patient Header & Triage Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = patient.name.ifBlank { "Patient Record" },
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                        Text(
                            text = "${patient.age} yrs • ${patient.gender} • ${if (patient.isNewPatient) "New Patient" else "ABHA: ${patient.abhaId}"}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = Slate600)
                        )
                    }

                    // Triage Priority Badge
                    val isUrgent = summary.triagePriority.contains("Urgent", ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isUrgent) MediRedLight else MediTealLight
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isUrgent) MediRedUrgent else MediTealDark)
                            )
                            Text(
                                text = "Triage: ${summary.triagePriority.uppercase()}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUrgent) MediRedUrgent else MediTealDark
                                )
                            )
                        }
                    }
                }

                HorizontalDivider(color = Slate200)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalHospital,
                            contentDescription = null,
                            tint = MediBluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Assigned Dept: ${summary.recommendedDepartment}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MediBlueDark
                            )
                        )
                    }

                    Text(
                        text = "Est. Wait: ${summary.estimatedWaitMinutes} mins",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate500,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // Section Cards (Doctor Readable Layout)
        ClinicalSectionCard(
            title = "Chief Complaint",
            content = summary.chiefComplaint,
            icon = Icons.Default.WarningAmber,
            iconTint = MediAmberWarning,
            onEdit = {
                editingSectionTitle = "Chief Complaint"
                editingSectionContent = summary.chiefComplaint
            }
        )

        ClinicalSectionCard(
            title = "History of Present Illness",
            content = summary.historyOfPresentIllness,
            icon = Icons.Default.Timeline,
            iconTint = MediBluePrimary,
            onEdit = {
                editingSectionTitle = "History of Present Illness"
                editingSectionContent = summary.historyOfPresentIllness
            }
        )

        ClinicalSectionCard(
            title = "Past Medical/Surgical History",
            content = summary.pastMedicalHistory,
            icon = Icons.Default.History,
            iconTint = MediTealSecondary,
            onEdit = {
                editingSectionTitle = "Past Medical/Surgical History"
                editingSectionContent = summary.pastMedicalHistory
            }
        )

        ClinicalSectionCard(
            title = "Drug & Allergy History",
            content = summary.drugAndAllergyHistory,
            icon = Icons.Default.Medication,
            iconTint = MediRedUrgent,
            onEdit = {
                editingSectionTitle = "Drug & Allergy History"
                editingSectionContent = summary.drugAndAllergyHistory
            }
        )

        ClinicalSectionCard(
            title = "Family History",
            content = summary.familyHistory,
            icon = Icons.Default.FamilyRestroom,
            iconTint = Slate700,
            onEdit = {
                editingSectionTitle = "Family History"
                editingSectionContent = summary.familyHistory
            }
        )

        ClinicalSectionCard(
            title = "Review of Systems",
            content = summary.reviewOfSystems,
            icon = Icons.Default.Checklist,
            iconTint = MediGreenSuccess,
            onEdit = {
                editingSectionTitle = "Review of Systems"
                editingSectionContent = summary.reviewOfSystems
            }
        )

        ClinicalSectionCard(
            title = "Prior Investigations",
            content = summary.priorInvestigations,
            icon = Icons.Default.Science,
            iconTint = MediBlueDark,
            onEdit = {
                editingSectionTitle = "Prior Investigations"
                editingSectionContent = summary.priorInvestigations
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Final Action: Submit Intake
        LargeActionButton(
            text = "Submit Intake to Doctor",
            onClick = onSubmitClick,
            icon = Icons.Default.CheckCircle,
            containerColor = MediGreenSuccess,
            testTag = "submit_intake_button"
        )
    }

    // Edit Section Dialog
    if (editingSectionTitle != null) {
        AlertDialog(
            onDismissRequest = { editingSectionTitle = null },
            title = {
                Text("Edit $editingSectionTitle", fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = editingSectionContent,
                    onValueChange = { editingSectionContent = it },
                    label = { Text("Clinical Notes") },
                    textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 15.sp),
                    colors = kioskTextFieldColors(focusedBorderColor = MediBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth().testTag("edit_section_text_field")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onEditSection(editingSectionTitle!!, editingSectionContent)
                        editingSectionTitle = null
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingSectionTitle = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Submission Success Confirmation Dialog
    if (isSubmitted) {
        AlertDialog(
            onDismissRequest = { /* Modal */ },
            icon = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MediGreenLight)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MediGreenSuccess,
                        modifier = Modifier.size(40.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Intake Submitted Successfully!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Your intake summary and records have been securely transmitted to the attending physician.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Slate600),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MediBlueLight,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "YOUR QUEUE TOKEN",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = MediBlueDark,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = summary.tokenNumber,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MediBlueDark,
                                    fontSize = 38.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Proceed to Waiting Area 2 • Counter 4",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Slate700,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onResetForNewPatient,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MediBluePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("start_new_patient_button")
                ) {
                    Text("Start New Patient Intake", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        )
    }
}

@Composable
fun ClinicalSectionCard(
    title: String,
    content: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        shadowElevation = 1.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            fontSize = 16.sp
                        )
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("edit_section_${title.take(8).replace(" ", "_")}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit $title",
                        tint = MediBluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = content.ifBlank { "None documented." },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Slate800,
                    lineHeight = 22.sp,
                    fontSize = 15.sp
                )
            )
        }
    }
}
