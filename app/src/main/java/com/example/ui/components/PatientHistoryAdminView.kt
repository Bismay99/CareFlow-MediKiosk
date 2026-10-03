package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHistoryAdminView(
    patientHistories: List<PatientMasterHistory>,
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
    ) -> Unit,
    onAddMedicineToConsultation: (consultationId: String, medicine: PrescriptionMedicine) -> Unit,
    onUpdateMedicineStatus: (patientId: String, medicineId: String, status: String) -> Unit,
    onSpeakPrescription: (patientName: String, medicines: List<PrescriptionMedicine>) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterChip by remember { mutableStateOf("All Patients") }
    var showRecordNewDialog by remember { mutableStateOf(false) }
    var selectedPatientForDetails by remember { mutableStateOf<PatientMasterHistory?>(null) }
    var selectedConsultationForRxSlip by remember { mutableStateOf<Pair<PatientMasterHistory, ConsultationHistoryEntry>?>(null) }
    var showAddMedDialogForConsultation by remember { mutableStateOf<ConsultationHistoryEntry?>(null) }

    val filterChips = listOf(
        "All Patients",
        "Active Prescriptions",
        "General Medicine",
        "Cardiology",
        "Orthopedics",
        "Diabetology",
        "Obstetrics"
    )

    // Filter patients based on search and chip selection
    val filteredPatients = remember(patientHistories, searchQuery, selectedFilterChip) {
        patientHistories.filter { patient ->
            val matchesSearch = searchQuery.isBlank() ||
                patient.patientName.contains(searchQuery, ignoreCase = true) ||
                patient.abhaId.contains(searchQuery, ignoreCase = true) ||
                patient.chronicConditions.any { it.contains(searchQuery, ignoreCase = true) } ||
                patient.consultations.any { consult ->
                    consult.doctorName.contains(searchQuery, ignoreCase = true) ||
                    consult.diagnosis.contains(searchQuery, ignoreCase = true) ||
                    consult.department.contains(searchQuery, ignoreCase = true) ||
                    consult.medicines.any { med ->
                        med.medicineName.contains(searchQuery, ignoreCase = true) ||
                        med.genericName.contains(searchQuery, ignoreCase = true)
                    }
                }

            val matchesChip = when (selectedFilterChip) {
                "All Patients" -> true
                "Active Prescriptions" -> patient.activeMedicines.isNotEmpty()
                "General Medicine" -> patient.consultations.any { it.department.contains("Medicine", ignoreCase = true) }
                "Cardiology" -> patient.consultations.any { it.department.contains("Cardiology", ignoreCase = true) }
                "Orthopedics" -> patient.consultations.any { it.department.contains("Orthopedics", ignoreCase = true) }
                "Diabetology" -> patient.consultations.any { it.department.contains("Diabetology", ignoreCase = true) || it.diagnosis.contains("Diabetes", ignoreCase = true) }
                "Obstetrics" -> patient.consultations.any { it.department.contains("Obstetrics", ignoreCase = true) }
                else -> true
            }

            matchesSearch && matchesChip
        }
    }

    val totalPatients = patientHistories.size
    val totalConsultations = patientHistories.sumOf { it.totalConsultations }
    val totalActivePrescriptions = patientHistories.sumOf { it.activeMedicines.size }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Tool Header with Summary Counters & Action Button
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = TealSecondary.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.Default.MedicalInformation,
                                    contentDescription = null,
                                    tint = TealSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Patient History & Prescription EHR",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = "Clinical records, doctors, timing & when to eat medicine",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    // Record New Patient History Button
                    Button(
                        onClick = { showRecordNewDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("admin_record_new_history_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Record History", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                // Summary Micro-Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickBadge(
                        label = "Patients",
                        value = totalPatients.toString(),
                        color = NavyPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    QuickBadge(
                        label = "Consultations",
                        value = totalConsultations.toString(),
                        color = Emerald700,
                        modifier = Modifier.weight(1f)
                    )
                    QuickBadge(
                        label = "Active Rx",
                        value = totalActivePrescriptions.toString(),
                        color = CoralError,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by Patient, ABHA ID, Doctor, Diagnosis, Medicine...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_patient_history_search_input")
        )

        // 3. Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filterChips) { chip ->
                val isSelected = selectedFilterChip == chip
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilterChip = chip },
                    label = { Text(chip, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NavyPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // 4. Patients List
        if (filteredPatients.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No patient history records match your search",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try adjusting your search terms or click 'Record History' to add a new clinical entry.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showRecordNewDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Record New Patient Consultation")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                items(filteredPatients, key = { it.patientId }) { patient ->
                    PatientMasterHistoryCard(
                        patient = patient,
                        onViewDetails = { selectedPatientForDetails = patient },
                        onQuickRecordConsultation = {
                            selectedPatientForDetails = patient
                            showRecordNewDialog = true
                        },
                        onSpeakMeds = {
                            onSpeakPrescription(patient.patientName, patient.activeMedicines)
                        }
                    )
                }
            }
        }
    }

    // =========================================================================
    // MODAL 1: DETAILED PATIENT HISTORY & PRESCRIPTION TIMELINE DIALOG
    // =========================================================================
    selectedPatientForDetails?.let { patient ->
        val currentPatientInState = patientHistories.find { it.patientId == patient.patientId } ?: patient
        PatientFullHistoryDetailsDialog(
            patient = currentPatientInState,
            onDismiss = { selectedPatientForDetails = null },
            onRecordNewConsultationForPatient = {
                showRecordNewDialog = true
            },
            onAddMedicineToConsultation = { consult ->
                showAddMedDialogForConsultation = consult
            },
            onUpdateMedicineStatus = { medId, status ->
                onUpdateMedicineStatus(patient.patientId, medId, status)
            },
            onViewPrescriptionSlip = { consult ->
                selectedConsultationForRxSlip = Pair(patient, consult)
            },
            onSpeakPrescription = {
                onSpeakPrescription(patient.patientName, patient.activeMedicines)
            }
        )
    }

    // =========================================================================
    // MODAL 2: RECORD NEW PATIENT HISTORY & CONSULTATION DIALOG
    // =========================================================================
    if (showRecordNewDialog) {
        RecordNewConsultationDialog(
            existingPatients = patientHistories,
            preselectedPatient = selectedPatientForDetails,
            onDismiss = { showRecordNewDialog = false },
            onSave = { pId, pName, age, gender, abha, phone, doc, spec, dept, dt, room, cc, diag, notes, vit, meds, labs, fup, adv, bg, allerg, conds ->
                onRecordConsultation(pId, pName, age, gender, abha, phone, doc, spec, dept, dt, room, cc, diag, notes, vit, meds, labs, fup, adv, bg, allerg, conds)
                showRecordNewDialog = false
            }
        )
    }

    // =========================================================================
    // MODAL 3: ADD MEDICINE QUICK FORM TO AN EXISTING CONSULTATION
    // =========================================================================
    showAddMedDialogForConsultation?.let { consult ->
        AddMedicineDialog(
            consultation = consult,
            onDismiss = { showAddMedDialogForConsultation = null },
            onAdd = { med ->
                onAddMedicineToConsultation(consult.id, med)
                showAddMedDialogForConsultation = null
            }
        )
    }

    // =========================================================================
    // MODAL 4: PRESCRIPTION SLIP & TIMELINE PRINT VIEW
    // =========================================================================
    selectedConsultationForRxSlip?.let { (patient, consult) ->
        PrescriptionSlipDialog(
            patient = patient,
            consultation = consult,
            onDismiss = { selectedConsultationForRxSlip = null },
            onSpeak = { onSpeakPrescription(patient.patientName, consult.medicines) }
        )
    }
}

/**
 * Micro stat badge for the tool header
 */
@Composable
private fun QuickBadge(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }
    }
}

/**
 * Summary card for each patient in the master directory
 */
@Composable
fun PatientMasterHistoryCard(
    patient: PatientMasterHistory,
    onViewDetails: () -> Unit,
    onQuickRecordConsultation: () -> Unit,
    onSpeakMeds: () -> Unit
) {
    val latest = patient.latestConsultation

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Patient Basic Demographics & ABHA ID
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Avatar
                    Surface(
                        shape = CircleShape,
                        color = NavyPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = patient.patientName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            )
                        }
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = patient.patientName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = "${patient.age} y/o • ${patient.gender}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "ABHA: ${patient.abhaId} • Blood: ${patient.bloodGroup}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Consultation count badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TealSecondary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${patient.totalConsultations} Consults",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TealSecondary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Chronic Conditions and Allergies Chips
            if (patient.chronicConditions.isNotEmpty() || patient.allergies.any { it != "None Known" }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    patient.chronicConditions.take(2).forEach { condition ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        ) {
                            Text(
                                text = condition,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    patient.allergies.filter { it != "None Known" }.take(1).forEach { allergy ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CoralError.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "⚠️ $allergy",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = CoralError,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Latest Consultation Snapshot
            if (latest != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
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
                                    imageVector = Icons.Default.Event,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = latest.consultationDateTime,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }

                            Text(
                                text = latest.doctorName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        Text(
                            text = "Diagnosis: ${latest.diagnosis}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Quick summary of prescribed medicines & when to eat
                        if (latest.medicines.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Medication,
                                    contentDescription = null,
                                    tint = Emerald700,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Medicines (${latest.medicines.size}): ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald700,
                                        fontSize = 10.sp
                                    )
                                )
                                Text(
                                    text = latest.medicines.take(2).joinToString(", ") { "${it.medicineName} (${it.foodTiming.label})" },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Action Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (patient.activeMedicines.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onSpeakMeds,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Speak Schedule", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Read Rx Timing", fontSize = 11.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Button(
                    onClick = onViewDetails,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("View Full History & Rx", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

/**
 * Detailed Full Patient History Inspection Modal
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientFullHistoryDetailsDialog(
    patient: PatientMasterHistory,
    onDismiss: () -> Unit,
    onRecordNewConsultationForPatient: () -> Unit,
    onAddMedicineToConsultation: (ConsultationHistoryEntry) -> Unit,
    onUpdateMedicineStatus: (medicineId: String, status: String) -> Unit,
    onViewPrescriptionSlip: (ConsultationHistoryEntry) -> Unit,
    onSpeakPrescription: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp,
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = NavyPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = NavyPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = patient.patientName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "EHR Record • ABHA ID: ${patient.abhaId}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Patient Demographics Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Patient Clinical Profile",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ProfileField("Age / Gender", "${patient.age} Years • ${patient.gender}")
                                ProfileField("Blood Group", patient.bloodGroup)
                                ProfileField("Contact", patient.phone)
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ProfileField("Chronic Conditions", patient.chronicConditions.joinToString(", ").ifBlank { "None Reported" })
                                ProfileField("Known Allergies", patient.allergies.joinToString(", ").ifBlank { "None Known" })
                            }
                        }
                    }

                    // Active Medications & When to Eat Table
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    Icon(Icons.Default.Medication, contentDescription = null, tint = Emerald700)
                                    Text(
                                        text = "Active Prescriptions & Food Timing",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                if (patient.activeMedicines.isNotEmpty()) {
                                    IconButton(
                                        onClick = onSpeakPrescription,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.VolumeUp, contentDescription = "Read Aloud", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }

                            if (patient.activeMedicines.isEmpty()) {
                                Text(
                                    text = "No active medicines on record for this patient.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            } else {
                                patient.activeMedicines.forEach { med ->
                                    MedicineScheduleItem(
                                        medicine = med,
                                        onStatusToggle = { newStatus ->
                                            onUpdateMedicineStatus(med.id, newStatus)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Consultation Timeline Section Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Consultation History Timeline (${patient.totalConsultations})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Button(
                            onClick = onRecordNewConsultationForPatient,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Consultation", fontSize = 11.sp)
                        }
                    }

                    // Timeline entries
                    patient.consultations.forEachIndexed { index, consult ->
                        ConsultationTimelineCard(
                            consultation = consult,
                            isLatest = index == 0,
                            onAddMedicine = { onAddMedicineToConsultation(consult) },
                            onPrintSlip = { onViewPrescriptionSlip(consult) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

/**
 * Detailed Consultation Card within Timeline
 */
@Composable
fun ConsultationTimelineCard(
    consultation: ConsultationHistoryEntry,
    isLatest: Boolean,
    onAddMedicine: () -> Unit,
    onPrintSlip: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        border = BorderStroke(
            1.dp,
            if (isLatest) TealSecondary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Consultant Date/Time, Doctor & Department
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isLatest) Emerald500.copy(alpha = 0.15f) else Slate200
                    ) {
                        Text(
                            text = if (isLatest) "LATEST CONSULTATION" else "PAST RECORD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isLatest) Emerald700 else Slate700,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = consultation.consultationDateTime,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = consultation.roomNumber,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Doctor Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = TealSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = consultation.doctorName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${consultation.doctorSpecialty} • ${consultation.department}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Diagnosis & Chief Complaint
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Diagnosis: ${consultation.diagnosis}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Chief Complaint: ${consultation.chiefComplaint}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            // Vitals Strip
            val v = consultation.vitals
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    VitalItem("BP", v.bloodPressure)
                    VitalItem("Pulse", v.pulseRate)
                    VitalItem("SpO2", v.oxygenSaturationSpO2)
                    VitalItem("Temp", v.temperature)
                    if (v.bloodSugarFasting.isNotBlank()) {
                        VitalItem("FBS", v.bloodSugarFasting)
                    }
                }
            }

            // Prescribed Medicines Section with "When to Eat"
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Prescribed Medicines & Timings (${consultation.medicines.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    TextButton(
                        onClick = onAddMedicine,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(2.dp))
                        Text("+ Add Medicine", fontSize = 11.sp)
                    }
                }

                if (consultation.medicines.isEmpty()) {
                    Text(
                        text = "No medicines prescribed during this consultation.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                } else {
                    consultation.medicines.forEach { med ->
                        MedicineScheduleItem(medicine = med)
                    }
                }
            }

            // Lab Investigations
            if (consultation.labInvestigations.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Lab Investigations & Diagnostics:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        consultation.labInvestigations.forEach { lab ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "🧪 $lab",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Doctor's Advice & Clinical Notes
            if (consultation.doctorAdvice.isNotBlank() || consultation.clinicalNotes.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (consultation.doctorAdvice.isNotBlank()) {
                            Text(
                                text = "Advice: ${consultation.doctorAdvice}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                            )
                        }
                        if (consultation.clinicalNotes.isNotBlank()) {
                            Text(
                                text = "Notes: ${consultation.clinicalNotes}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            // Bottom Actions: Follow-up chip & Print Rx Slip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AmberWarning.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Follow-up: ${consultation.followUpDate}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Amber700,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                OutlinedButton(
                    onClick = onPrintSlip,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Rx Slip", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun VitalItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

/**
 * Rich medicine schedule item with food timing & dosage details
 */
@Composable
fun MedicineScheduleItem(
    medicine: PrescriptionMedicine,
    onStatusToggle: ((String) -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (medicine.status == "Active") Emerald500.copy(alpha = 0.4f) else Slate300
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Row 1: Name, Dosage, Frequency & Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = medicine.medicineName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = medicine.dosage,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    if (medicine.genericName.isNotBlank()) {
                        Text(
                            text = medicine.genericName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Frequency badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = TealSecondary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = medicine.frequency.code,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TealSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Row 2: WHEN TO EAT MEDICINE (The core user requirement)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = when (medicine.foodTiming) {
                    MedicineFoodTiming.BEFORE_BREAKFAST_EMPTY_STOMACH -> AmberWarning.copy(alpha = 0.15f)
                    MedicineFoodTiming.BEDTIME -> NavyPrimary.copy(alpha = 0.1f)
                    MedicineFoodTiming.AS_NEEDED_PRN -> CoralError.copy(alpha = 0.12f)
                    else -> Emerald500.copy(alpha = 0.12f)
                },
                border = BorderStroke(
                    1.dp,
                    when (medicine.foodTiming) {
                        MedicineFoodTiming.BEFORE_BREAKFAST_EMPTY_STOMACH -> Amber700.copy(alpha = 0.4f)
                        MedicineFoodTiming.BEDTIME -> NavyPrimary.copy(alpha = 0.3f)
                        MedicineFoodTiming.AS_NEEDED_PRN -> CoralError.copy(alpha = 0.3f)
                        else -> Emerald700.copy(alpha = 0.3f)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = when (medicine.foodTiming) {
                            MedicineFoodTiming.BEFORE_BREAKFAST_EMPTY_STOMACH -> Amber700
                            MedicineFoodTiming.BEDTIME -> NavyPrimary
                            MedicineFoodTiming.AS_NEEDED_PRN -> CoralError
                            else -> Emerald700
                        },
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "When to eat: ${medicine.foodTiming.label}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = when (medicine.foodTiming) {
                                MedicineFoodTiming.BEFORE_BREAKFAST_EMPTY_STOMACH -> Amber700
                                MedicineFoodTiming.BEDTIME -> NavyPrimary
                                MedicineFoodTiming.AS_NEEDED_PRN -> CoralError
                                else -> Emerald700
                            }
                        )
                    )
                }
            }

            // Detailed Food Directive & Timing Guidance
            Text(
                text = medicine.foodTiming.detailedInstruction,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Special instructions if present
            if (medicine.specialInstructions.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Doctor note: ${medicine.specialInstructions}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            // Row 3: Visual Pills Time of Day & Duration
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TimeOfDayChip("🌅 Morning", medicine.isMorning)
                    TimeOfDayChip("☀️ Noon", medicine.isAfternoon)
                    TimeOfDayChip("🌇 Evening", medicine.isEvening)
                    TimeOfDayChip("🌙 Night", medicine.isNight)
                }

                Text(
                    text = "Duration: ${medicine.duration}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
    }
}

@Composable
private fun TimeOfDayChip(label: String, active: Boolean) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = if (active) NavyPrimary.copy(alpha = 0.15f) else Slate100,
        border = BorderStroke(0.5.dp, if (active) NavyPrimary.copy(alpha = 0.5f) else Slate300)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                color = if (active) NavyPrimary else Slate500
            ),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

/**
 * MODAL: Record New Patient Consultation & Prescriptions Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordNewConsultationDialog(
    existingPatients: List<PatientMasterHistory>,
    preselectedPatient: PatientMasterHistory?,
    onDismiss: () -> Unit,
    onSave: (
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
    ) -> Unit
) {
    var selectedPatient by remember { mutableStateOf(preselectedPatient ?: existingPatients.firstOrNull()) }

    var patientName by remember { mutableStateOf(selectedPatient?.patientName ?: "Rajesh Sharma") }
    var age by remember { mutableStateOf(selectedPatient?.age ?: "54") }
    var gender by remember { mutableStateOf(selectedPatient?.gender ?: "Male") }
    var abhaId by remember { mutableStateOf(selectedPatient?.abhaId ?: "14-2951-8402-1920") }
    var phone by remember { mutableStateOf(selectedPatient?.phone ?: "+91 98201 44521") }
    var bloodGroup by remember { mutableStateOf(selectedPatient?.bloodGroup ?: "B+") }
    var allergiesText by remember { mutableStateOf(selectedPatient?.allergies?.joinToString(", ") ?: "None Known") }
    var chronicConditionsText by remember { mutableStateOf(selectedPatient?.chronicConditions?.joinToString(", ") ?: "Essential Hypertension") }

    var doctorName by remember { mutableStateOf("Dr. Rajesh Mehta, MD") }
    var doctorSpecialty by remember { mutableStateOf("Internal Medicine & Cardiology") }
    var department by remember { mutableStateOf("General Medicine") }
    var roomNumber by remember { mutableStateOf("Room 104 - 1st Floor") }
    val currentFormattedDate = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date()) }
    var consultationDateTime by remember { mutableStateOf(currentFormattedDate) }

    var chiefComplaint by remember { mutableStateOf("Occasional headache and blood pressure check-up") }
    var diagnosis by remember { mutableStateOf("Stage 1 Essential Hypertension (Follow-up)") }
    var clinicalNotes by remember { mutableStateOf("Patient compliant with dietary salt reduction. Systemic review normal.") }

    var bp by remember { mutableStateOf("130/84 mmHg") }
    var pulse by remember { mutableStateOf("74 bpm") }
    var spo2 by remember { mutableStateOf("98%") }
    var temp by remember { mutableStateOf("98.4 °F") }
    var fbs by remember { mutableStateOf("102 mg/dL") }
    var weight by remember { mutableStateOf("77 kg") }

    var followUpDate by remember { mutableStateOf("In 4 Weeks") }
    var doctorAdvice by remember { mutableStateOf("Continue morning walk, reduce salt, stay well hydrated.") }
    var labInvestigationsText by remember { mutableStateOf("Fasting Lipid Profile, Serum Creatinine") }

    // Prescription Medicines List Builder
    val medicinesList = remember {
        mutableStateListOf(
            PrescriptionMedicine(
                medicineName = "Telmisartan 40mg",
                genericName = "Telmisartan IP 40mg",
                dosage = "1 Tablet (40mg)",
                form = "Tablet",
                frequency = MedicineFrequency.ONCE_DAILY_MORNING,
                foodTiming = MedicineFoodTiming.AFTER_BREAKFAST,
                duration = "30 Days",
                specialInstructions = "Take daily after morning breakfast with water.",
                isMorning = true,
                isAfternoon = false,
                isEvening = false,
                isNight = false
            ),
            PrescriptionMedicine(
                medicineName = "Pantoprazole 40mg",
                genericName = "Pantoprazole Sodium IP 40mg",
                dosage = "1 Tablet (40mg)",
                form = "Tablet",
                frequency = MedicineFrequency.ONCE_DAILY_MORNING,
                foodTiming = MedicineFoodTiming.BEFORE_BREAKFAST_EMPTY_STOMACH,
                duration = "14 Days",
                specialInstructions = "Take 30 mins before breakfast on empty stomach.",
                isMorning = true,
                isAfternoon = false,
                isEvening = false,
                isNight = false
            )
        )
    }

    var showAddMedFormInline by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.94f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    shadowElevation = 3.dp,
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.PostAdd, contentDescription = null, tint = NavyPrimary)
                            Text(
                                text = "Record Patient History & Consultation",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Quick Patient Selector
                    Text(
                        text = "1. Select Patient",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(existingPatients) { p ->
                            val isSel = selectedPatient?.patientId == p.patientId
                            FilterChip(
                                selected = isSel,
                                onClick = {
                                    selectedPatient = p
                                    patientName = p.patientName
                                    age = p.age
                                    gender = p.gender
                                    abhaId = p.abhaId
                                    phone = p.phone
                                    bloodGroup = p.bloodGroup
                                    allergiesText = p.allergies.joinToString(", ")
                                    chronicConditionsText = p.chronicConditions.joinToString(", ")
                                },
                                label = { Text(p.patientName, fontSize = 11.sp) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Patient Details Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = patientName,
                            onValueChange = { patientName = it },
                            label = { Text("Patient Name") },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = age,
                            onValueChange = { age = it },
                            label = { Text("Age") },
                            modifier = Modifier.weight(0.7f),
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { gender = it },
                            label = { Text("Gender") },
                            modifier = Modifier.weight(0.8f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = abhaId,
                            onValueChange = { abhaId = it },
                            label = { Text("ABHA ID") },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = bloodGroup,
                            onValueChange = { bloodGroup = it },
                            label = { Text("Blood Group") },
                            modifier = Modifier.weight(0.8f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // 2. Doctor & Department Details
                    Text(
                        text = "2. Attending Doctor & Consultation Time",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = doctorName,
                            onValueChange = { doctorName = it },
                            label = { Text("Doctor Name") },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = department,
                            onValueChange = { department = it },
                            label = { Text("Department") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = consultationDateTime,
                            onValueChange = { consultationDateTime = it },
                            label = { Text("Consultant Date & Time") },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = roomNumber,
                            onValueChange = { roomNumber = it },
                            label = { Text("Room") },
                            modifier = Modifier.weight(0.8f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // 3. Clinical Assessment
                    Text(
                        text = "3. Clinical Assessment & Diagnosis",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = chiefComplaint,
                        onValueChange = { chiefComplaint = it },
                        label = { Text("Chief Complaint") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = diagnosis,
                        onValueChange = { diagnosis = it },
                        label = { Text("Diagnosis") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = clinicalNotes,
                        onValueChange = { clinicalNotes = it },
                        label = { Text("Clinical Notes & Observations") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2
                    )

                    // 4. Vitals
                    Text(
                        text = "4. Vitals Recorded at Consultation",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = bp,
                            onValueChange = { bp = it },
                            label = { Text("BP") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = pulse,
                            onValueChange = { pulse = it },
                            label = { Text("Pulse") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = spo2,
                            onValueChange = { spo2 = it },
                            label = { Text("SpO2") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // 5. Prescribed Medicines & When to Eat Instructions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "5. Prescribed Medicines & When to Eat (${medicinesList.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        Button(
                            onClick = { showAddMedFormInline = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("+ Add Rx", fontSize = 11.sp)
                        }
                    }

                    medicinesList.forEachIndexed { idx, med ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        text = "${idx + 1}. ${med.medicineName} • ${med.dosage}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "🍽️ When to eat: ${med.foodTiming.label}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Emerald700,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    Text(
                                        text = "Frequency: ${med.frequency.label} • Duration: ${med.duration}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    )
                                }

                                IconButton(onClick = { medicinesList.removeAt(idx) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = CoralError, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    // 6. Advice & Follow-up
                    Text(
                        text = "6. Doctor's Advice & Follow-up",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = doctorAdvice,
                        onValueChange = { doctorAdvice = it },
                        label = { Text("Doctor's Lifestyle & Dietary Advice") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = followUpDate,
                            onValueChange = { followUpDate = it },
                            label = { Text("Follow-up Date / Timeline") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = labInvestigationsText,
                            onValueChange = { labInvestigationsText = it },
                            label = { Text("Lab Investigations") },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // Save Action Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                val allergiesList = allergiesText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                val chronicList = chronicConditionsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                val labs = labInvestigationsText.split(",").map { it.trim() }.filter { it.isNotBlank() }

                                onSave(
                                    selectedPatient?.patientId ?: "pt-${System.currentTimeMillis()}",
                                    patientName,
                                    age,
                                    gender,
                                    abhaId,
                                    phone,
                                    doctorName,
                                    doctorSpecialty,
                                    department,
                                    consultationDateTime,
                                    roomNumber,
                                    chiefComplaint,
                                    diagnosis,
                                    clinicalNotes,
                                    ConsultationVitals(
                                        bloodPressure = bp,
                                        pulseRate = pulse,
                                        oxygenSaturationSpO2 = spo2,
                                        temperature = temp,
                                        bloodSugarFasting = fbs,
                                        weightKg = weight
                                    ),
                                    medicinesList.toList(),
                                    labs,
                                    followUpDate,
                                    doctorAdvice,
                                    bloodGroup,
                                    allergiesList,
                                    chronicList
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            modifier = Modifier.testTag("admin_save_history_record_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save to Patient History")
                        }
                    }
                }
            }
        }
    }

    // Inline Add Medicine Dialog
    if (showAddMedFormInline) {
        AddMedicineDialog(
            consultation = null,
            onDismiss = { showAddMedFormInline = false },
            onAdd = { newMed ->
                medicinesList.add(newMed)
                showAddMedFormInline = false
            }
        )
    }
}

/**
 * Dialog to add a single medicine with rich "When to eat" timing directives
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineDialog(
    consultation: ConsultationHistoryEntry?,
    onDismiss: () -> Unit,
    onAdd: (PrescriptionMedicine) -> Unit
) {
    var medName by remember { mutableStateOf("Amoxicillin-Clavulanate 625mg") }
    var genericName by remember { mutableStateOf("Amoxicillin 500mg + Potassium Clavulanate 125mg") }
    var dosage by remember { mutableStateOf("1 Tablet (625mg)") }
    var selectedFrequency by remember { mutableStateOf(MedicineFrequency.TWICE_DAILY) }
    var selectedFoodTiming by remember { mutableStateOf(MedicineFoodTiming.AFTER_BREAKFAST) }
    var duration by remember { mutableStateOf("5 Days") }
    var specialInstructions by remember { mutableStateOf("Take with a full glass of water. Complete full course.") }

    var isMorning by remember { mutableStateOf(true) }
    var isAfternoon by remember { mutableStateOf(false) }
    var isEvening by remember { mutableStateOf(false) }
    var isNight by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Add Medicine & Eating Schedule",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = medName,
                    onValueChange = { medName = it },
                    label = { Text("Medicine Brand Name") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = genericName,
                    onValueChange = { genericName = it },
                    label = { Text("Generic Formulation") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage / Strength") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Food Timing Selection ("When to eat medicine")
                Text(
                    text = "🍽️ When to Eat Medicine:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MedicineFoodTiming.values().forEach { timing ->
                        val isSelected = selectedFoodTiming == timing
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Emerald500.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            border = BorderStroke(1.dp, if (isSelected) Emerald700 else Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedFoodTiming = timing }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedFoodTiming = timing }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = timing.label,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Emerald700 else MaterialTheme.colorScheme.onSurface
                                        )
                                    )
                                    Text(
                                        text = timing.detailedInstruction,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Frequency Selection
                Text(
                    text = "Intake Frequency:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(MedicineFrequency.values()) { freq ->
                        val isSel = selectedFrequency == freq
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                selectedFrequency = freq
                                when (freq) {
                                    MedicineFrequency.ONCE_DAILY_MORNING -> { isMorning = true; isAfternoon = false; isEvening = false; isNight = false }
                                    MedicineFrequency.ONCE_DAILY_NIGHT -> { isMorning = false; isAfternoon = false; isEvening = false; isNight = true }
                                    MedicineFrequency.TWICE_DAILY -> { isMorning = true; isAfternoon = false; isEvening = false; isNight = true }
                                    MedicineFrequency.THRICE_DAILY -> { isMorning = true; isAfternoon = true; isEvening = false; isNight = true }
                                    else -> {}
                                }
                            },
                            label = { Text(freq.code, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = specialInstructions,
                    onValueChange = { specialInstructions = it },
                    label = { Text("Special Precautions / Doctor Advice") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val med = PrescriptionMedicine(
                                medicineName = medName,
                                genericName = genericName,
                                dosage = dosage,
                                frequency = selectedFrequency,
                                foodTiming = selectedFoodTiming,
                                duration = duration,
                                specialInstructions = specialInstructions,
                                isMorning = isMorning,
                                isAfternoon = isAfternoon,
                                isEvening = isEvening,
                                isNight = isNight
                            )
                            onAdd(med)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("Add to Prescription")
                    }
                }
            }
        }
    }
}

/**
 * Printable Digital Prescription & History Slip Modal
 */
@Composable
fun PrescriptionSlipDialog(
    patient: PatientMasterHistory,
    consultation: ConsultationHistoryEntry,
    onDismiss: () -> Unit,
    onSpeak: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Clinic Rx Header
                Surface(
                    color = NavyPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "CAREFLOW AI • OUTPATIENT PRESCRIPTION SLIP",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Apollo Health City OPD • 24/7 Digital Health Network",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                // Scrollable Prescription Body
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Patient Demographics Row
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Patient: ${patient.patientName}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "ABHA ID: ${patient.abhaId}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${patient.age}y / ${patient.gender} • Blood: ${patient.bloodGroup}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = consultation.consultationDateTime,
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                                )
                            }
                        }
                    }

                    // Doctor Info Row
                    Text(
                        text = "Consultant: ${consultation.doctorName} (${consultation.doctorSpecialty})",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TealSecondary
                        )
                    )
                    Text(
                        text = "Diagnosis: ${consultation.diagnosis}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Text(
                        text = "Rx Medications & Eating Directions:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    consultation.medicines.forEachIndexed { idx, med ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${idx + 1}. ${med.medicineName} (${med.dosage})",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${med.frequency.code} • ${med.duration}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Text(
                                    text = "🍽️ When to take: ${med.foodTiming.label} - ${med.foodTiming.detailedInstruction}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Emerald700,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    )
                                )
                                if (med.specialInstructions.isNotBlank()) {
                                    Text(
                                        text = "Note: ${med.specialInstructions}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 9.sp
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (consultation.doctorAdvice.isNotBlank()) {
                        Text(
                            text = "Diet / Advice: ${consultation.doctorAdvice}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                        )
                    }

                    Text(
                        text = "Follow-up: ${consultation.followUpDate}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Amber700
                        )
                    )
                }

                // Action Footer
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onSpeak,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Read Instructions", fontSize = 11.sp)
                        }

                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Text("Done", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
