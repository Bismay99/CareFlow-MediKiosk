package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.ClinicalTriageEngine
import com.example.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiHealthAssistantScreen(
    patientProfile: PatientProfile,
    socratesIntake: SocratesIntake,
    vitalsSummary: PatientHealthDashboardSummary?,
    onBackClick: () -> Unit,
    onEmergencyTrigger: (String) -> Unit,
    onNavigateToTelemedicine: () -> Unit,
    onSpeakText: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var symptomsInput by remember { mutableStateOf(socratesIntake.chiefComplaint.ifBlank { "" }) }
    var durationInput by remember { mutableStateOf(socratesIntake.onset.ifBlank { "2 days" }) }
    var severityScore by remember { mutableIntStateOf(5) }
    var userQuestionInput by remember { mutableStateOf("") }

    // Evaluation State
    var triageResult by remember {
        mutableStateOf<AiTriageResult?>(
            ClinicalTriageEngine.evaluateTriage(
                symptomsText = symptomsInput.ifBlank { "Mild general fatigue and headache" },
                duration = durationInput,
                severityScale1To10 = severityScore,
                reportedVitals = mapOf(
                    "Heart Rate" to (vitalsSummary?.heartRateLatest?.displayValueString ?: "72 bpm"),
                    "SpO2" to (vitalsSummary?.spO2Latest?.displayValueString ?: "98%"),
                    "Blood Pressure" to (vitalsSummary?.bloodPressureLatest?.displayValueString ?: "120/80 mmHg")
                ),
                intake = socratesIntake
            )
        )
    }

    var showClinicianDraftDialog by remember { mutableStateOf(false) }

    val safetyStatement = "AI assists with information collection and clinical information structuring. Clinical decisions remain with the healthcare professional."

    // Re-evaluate triage whenever symptoms, duration, or severity changes
    fun runEvaluation() {
        val result = ClinicalTriageEngine.evaluateTriage(
            symptomsText = symptomsInput,
            duration = durationInput,
            severityScale1To10 = severityScore,
            reportedVitals = mapOf(
                "Heart Rate" to (vitalsSummary?.heartRateLatest?.displayValueString ?: "72 bpm"),
                "SpO2" to (vitalsSummary?.spO2Latest?.displayValueString ?: "98%"),
                "Blood Pressure" to (vitalsSummary?.bloodPressureLatest?.displayValueString ?: "120/80 mmHg")
            ),
            intake = socratesIntake
        )
        triageResult = result
        if (result.urgency == TriageUrgency.EMERGENCY) {
            onEmergencyTrigger(result.clinicalReasoning)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "AI Health Assistant & Triage",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Prepare better. Consult smarter.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onSpeakText(safetyStatement) }) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Read Safety Notice")
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Mandatory Safety Statement Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = safetyStatement,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // 2. Patient-Facing AI-Assisted Triage Result Badge
            triageResult?.let { result ->
                item {
                    val (badgeBg, badgeText, badgeIcon) = when (result.urgency) {
                        TriageUrgency.EMERGENCY -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), Icons.Default.Warning)
                        TriageUrgency.URGENT -> Triple(Color(0xFFFFEDD5), Color(0xFFEA580C), Icons.Default.PriorityHigh)
                        TriageUrgency.SOON -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), Icons.Default.Schedule)
                        TriageUrgency.ROUTINE -> Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), Icons.Default.CheckCircle)
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = badgeBg),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, badgeText.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(badgeIcon, contentDescription = null, tint = badgeText, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AI-Assisted Urgency: ${result.urgency.title}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = badgeText)
                                    )
                                }
                                AssistChip(
                                    onClick = { /* Informational */ },
                                    label = { Text("Non-Diagnostic", fontSize = 11.sp) },
                                    colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.8f))
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = result.clinicalReasoning,
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF1E293B))
                            )

                            if (result.concerningVitalsIdentified.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                result.concerningVitalsIdentified.forEach { vit ->
                                    Text(
                                        text = "• $vit",
                                        style = MaterialTheme.typography.bodySmall.copy(color = badgeText, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Recommended Next Steps
                            Text(
                                text = "Recommended Next Action:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                            )
                            result.recommendedNextSteps.take(2).forEach { step ->
                                Text(
                                    text = "→ $step",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155))
                                )
                            }
                        }
                    }
                }
            }

            // 3. Symptom Intake Interactive Form
            item {
                Text(
                    text = "Describe Your Primary Symptoms",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = symptomsInput,
                    onValueChange = {
                        symptomsInput = it
                        runEvaluation()
                    },
                    placeholder = { Text("e.g. Throbbing frontal headache with nausea and light sensitivity...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick Symptom Chips
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("Fever & Chills", "Persistent Cough", "Chest Tightness", "Abdominal Cramping", "Dizziness", "Joint Soreness")) { symp ->
                        SuggestionChip(
                            onClick = {
                                symptomsInput = if (symptomsInput.isBlank()) symp else "$symptomsInput, $symp"
                                runEvaluation()
                            },
                            label = { Text(symp) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            // 4. Duration & Pain/Severity Slider
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Duration",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = durationInput,
                            onValueChange = {
                                durationInput = it
                                runEvaluation()
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = "Severity (1 - 10): $severityScore/10",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Slider(
                            value = severityScore.toFloat(),
                            onValueChange = {
                                severityScore = it.toInt()
                                runEvaluation()
                            },
                            valueRange = 1f..10f,
                            steps = 8
                        )
                    }
                }
            }

            // 5. Patient Background & Measurements Snapshot
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Reported Patient Context & Vitals",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Known Conditions: ${patientProfile.chronicConditions.joinToString().ifBlank { "None Reported" }}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Allergies: ${patientProfile.allergies.joinToString().ifBlank { "None Reported" }}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (vitalsSummary != null) {
                            Text(
                                text = "Recent Biometrics: Pulse ${vitalsSummary.heartRateLatest.displayValueString} • SpO2 ${vitalsSummary.spO2Latest.displayValueString} • BP ${vitalsSummary.bloodPressureLatest.displayValueString}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }

            // 6. Action: Generate Clinician Intake Summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showClinicianDraftDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Summarize, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View Intake Summary")
                    }

                    Button(
                        onClick = onNavigateToTelemedicine,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Consult Doctor")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // ==========================================
    // CLINICIAN INTAKE SUMMARY DRAFT DIALOG
    // ==========================================
    if (showClinicianDraftDialog) {
        val draftSummary = ClinicalTriageEngine.formatClinicianIntakeDraft(
            patient = patientProfile,
            symptoms = symptomsInput,
            intake = socratesIntake,
            triageResult = triageResult ?: ClinicalTriageEngine.evaluateTriage(symptomsInput),
            vitals = vitalsSummary
        )

        AlertDialog(
            onDismissRequest = { showClinicianDraftDialog = false },
            title = {
                Text(
                    text = "AI-Assisted Intake Summary",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    item {
                        Text(
                            text = draftSummary,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showClinicianDraftDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}
