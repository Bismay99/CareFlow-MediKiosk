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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ConsultationHistoryEntry
import com.example.data.model.PatientMasterHistory
import com.example.data.model.RecentRecordItem

enum class RecordProvenance(val label: String, val bg: Color, val textColor: Color) {
    DOCTOR_VERIFIED("DOCTOR VERIFIED", Color(0xFFDCFCE7), Color(0xFF16A34A)),
    PATIENT_REPORTED("PATIENT REPORTED", Color(0xFFCCFBF1), Color(0xFF0F766E)),
    AI_EXTRACTED("AI EXTRACTED", Color(0xFFE0F2FE), Color(0xFF0284C7)),
    AI_GENERATED("AI GENERATED", Color(0xFFFEF3C7), Color(0xFFD97706)),
    DEMO_DATA("DEMO DATA", Color(0xFFF3E8FF), Color(0xFF7E22CE))
}

data class UnifiedRecordItem(
    val id: String,
    val title: String,
    val category: String, // Consultation, Prescription, Lab Report, Document, AI Draft
    val dateFormatted: String,
    val providerOrDoctor: String,
    val summary: String,
    val provenance: RecordProvenance,
    val medications: List<String> = emptyList(),
    val labFindings: List<String> = emptyList(),
    val advice: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalRecordsScreen(
    patientHistories: List<PatientMasterHistory>,
    recentRecords: List<RecentRecordItem>,
    patientName: String = "Rajesh Sharma",
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedRecordForDetail by remember { mutableStateOf<UnifiedRecordItem?>(null) }

    // Synthesize all records with provenance labeling
    val allRecords = remember(patientHistories, recentRecords) {
        val list = mutableListOf<UnifiedRecordItem>()

        // 1. Ingest Consultations from PatientMasterHistory (Doctor Verified)
        patientHistories.forEach { patient ->
            patient.consultations.forEach { consult ->
                list.add(
                    UnifiedRecordItem(
                        id = consult.id,
                        title = "${consult.department}: ${consult.diagnosis}",
                        category = "Consultation",
                        dateFormatted = consult.consultationDateTime,
                        providerOrDoctor = "Dr. ${consult.doctorName} (${consult.doctorSpecialty})",
                        summary = consult.clinicalNotes.ifBlank { consult.chiefComplaint },
                        provenance = RecordProvenance.DOCTOR_VERIFIED,
                        medications = consult.medicines.map { "${it.medicineName} ${it.dosage} (${it.frequency.label})" },
                        labFindings = consult.labInvestigations,
                        advice = consult.doctorAdvice
                    )
                )
            }
        }

        // 2. Ingest Lab & Diagnostic Records (AI Extracted from scan)
        list.add(
            UnifiedRecordItem(
                id = "doc-scan-01",
                title = "Apollo Comprehensive Lipid Panel & Fasting Glucose",
                category = "Lab Report",
                dateFormatted = "28 Aug 2026",
                providerOrDoctor = "Apollo Diagnostics Reference Lab",
                summary = "Total Cholesterol: 228 mg/dL [High], Fasting Blood Glucose: 104 mg/dL [High], Creatinine: 0.92 mg/dL [Normal].",
                provenance = RecordProvenance.AI_EXTRACTED,
                labFindings = listOf("Total Cholesterol: 228 mg/dL", "Fasting Glucose: 104 mg/dL", "Serum Creatinine: 0.92 mg/dL")
            )
        )

        // 3. Ingest Patient Reported Symptoms
        list.add(
            UnifiedRecordItem(
                id = "pt-report-01",
                title = "Triage Intake: Morning Headache & Bilateral Knee Discomfort",
                category = "Symptoms",
                dateFormatted = "02 Sep 2026",
                providerOrDoctor = "Self-Reported via CareFlow Intake Kiosk",
                summary = "Onset: 4 days, Severity: 5/10. Intermittent morning occipital ache without acute visual deficit.",
                provenance = RecordProvenance.PATIENT_REPORTED
            )
        )

        // 4. Ingest AI Draft Intake Summary
        list.add(
            UnifiedRecordItem(
                id = "ai-draft-01",
                title = "Provisional SOCRATES Intake Draft (General Medicine)",
                category = "AI Draft",
                dateFormatted = "Today",
                providerOrDoctor = "CareFlow Clinical Intake AI",
                summary = "Structured SOAP synthesis awaiting attending physician sign-off. Priority: Routine Outpatient Review.",
                provenance = RecordProvenance.AI_GENERATED,
                advice = "Draft for doctor review. Not a confirmed diagnosis."
            )
        )

        list
    }

    val categories = listOf("All", "Consultation", "Prescription", "Lab Report", "Symptoms", "AI Draft")

    val filteredRecords = allRecords.filter { item ->
        val matchesCategory = selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = item.title.contains(searchQuery, ignoreCase = true) ||
                item.providerOrDoctor.contains(searchQuery, ignoreCase = true) ||
                item.summary.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "My Medical Records",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Patient: $patientName • Provenance-Indexed EHR",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search records, medications, lab tests...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // 2. Category Filter Chips
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            // 3. Provenance Legend Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "DATA PROVENANCE TRANSPARENCY",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Every clinical observation clearly indicates its origin: [DOCTOR VERIFIED], [PATIENT REPORTED], [AI EXTRACTED], or [AI GENERATED].",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            // 4. Record Cards
            items(filteredRecords) { record ->
                RecordItemCard(
                    record = record,
                    onClick = { selectedRecordForDetail = record }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // ==========================================
    // RECORD DETAIL DIALOG
    // ==========================================
    selectedRecordForDetail?.let { record ->
        Dialog(onDismissRequest = { selectedRecordForDetail = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = record.category,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(record.provenance.bg)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = record.provenance.label,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = record.provenance.textColor, fontSize = 10.sp)
                            )
                        }
                    }

                    Text(
                        text = record.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )

                    Text(
                        text = "Provider: ${record.providerOrDoctor} • Date: ${record.dateFormatted}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                    )

                    HorizontalDivider()

                    Text(
                        text = "Clinical Summary:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = record.summary,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (record.medications.isNotEmpty()) {
                        Text(
                            text = "Prescriptions on Record:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        record.medications.forEach { med ->
                            Text("• $med", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    if (record.labFindings.isNotEmpty()) {
                        Text(
                            text = "Laboratory Investigations:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        record.labFindings.forEach { lab ->
                            Text("• $lab", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    if (record.advice.isNotBlank()) {
                        Text(
                            text = "Doctor Advice:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = record.advice,
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        )
                    }

                    Button(
                        onClick = { selectedRecordForDetail = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordItemCard(
    record: UnifiedRecordItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${record.category} • ${record.dateFormatted}",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(record.provenance.bg)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = record.provenance.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = record.provenance.textColor,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = record.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            Text(
                text = record.providerOrDoctor,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = record.summary,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
