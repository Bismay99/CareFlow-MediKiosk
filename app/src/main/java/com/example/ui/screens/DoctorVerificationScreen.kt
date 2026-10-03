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
import com.example.data.model.ClinicalVerificationStatus
import com.example.data.model.VerifiableClinicalEntity
import com.example.data.repository.DoctorVerificationRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorVerificationScreen(
    verificationRepo: DoctorVerificationRepository,
    doctorName: String = "Dr. Ananya Sharma",
    onBackClick: () -> Unit,
    onCommitToEhr: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pendingEntities by verificationRepo.pendingEntities.collectAsState()
    val auditLog by verificationRepo.verifiedAuditLog.collectAsState()

    var statusFilter by remember { mutableStateOf("All") }
    var selectedEntityForEdit by remember { mutableStateOf<VerifiableClinicalEntity?>(null) }
    var editedValueInput by remember { mutableStateOf("") }
    var editRemarksInput by remember { mutableStateOf("") }

    var selectedEntityForReject by remember { mutableStateOf<VerifiableClinicalEntity?>(null) }
    var rejectReasonInput by remember { mutableStateOf("") }
    var showCommitSuccessDialog by remember { mutableStateOf(false) }

    val filteredEntities = pendingEntities.filter { entity ->
        when (statusFilter) {
            "Pending" -> entity.status == ClinicalVerificationStatus.AI_EXTRACTED_PENDING
            "Verified" -> entity.status == ClinicalVerificationStatus.DOCTOR_VERIFIED
            "Edited" -> entity.status == ClinicalVerificationStatus.DOCTOR_EDITED
            "Rejected" -> entity.status == ClinicalVerificationStatus.DOCTOR_REJECTED
            else -> true
        }
    }

    val pendingCount = pendingEntities.count { it.status == ClinicalVerificationStatus.AI_EXTRACTED_PENDING }
    val verifiedCount = pendingEntities.count { it.status == ClinicalVerificationStatus.DOCTOR_VERIFIED || it.status == ClinicalVerificationStatus.DOCTOR_EDITED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Clinical Verification Queue",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Signed in as $doctorName • Human-in-the-Loop Sign-Off",
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
                    if (pendingCount > 0) {
                        TextButton(
                            onClick = { verificationRepo.acceptAllPending(doctorName) }
                        ) {
                            Text("Accept All ($pendingCount)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$verifiedCount items verified",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "$pendingCount awaiting sign-off",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                        )
                    }

                    Button(
                        onClick = {
                            onCommitToEhr()
                            showCommitSuccessDialog = true
                        },
                        enabled = verifiedCount > 0,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Commit to Master EHR")
                    }
                }
            }
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
            // 1. Mandatory Regulatory Governance Notice
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PHYSICIAN VERIFICATION REQUIRED",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = "Only clinician-accepted items are promoted to official patient medical records. AI extracted suggestions are strictly drafts.",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            }

            // 2. Status Filters
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("All", "Pending", "Verified", "Edited", "Rejected")) { tab ->
                        FilterChip(
                            selected = statusFilter == tab,
                            onClick = { statusFilter = tab },
                            label = { Text(tab) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            // 3. Clinical Entity Cards
            items(filteredEntities) { entity ->
                VerifiableEntityCard(
                    entity = entity,
                    onAccept = { verificationRepo.acceptField(entity.id, doctorName) },
                    onEdit = {
                        selectedEntityForEdit = entity
                        editedValueInput = entity.verifiedValue ?: entity.extractedValue
                        editRemarksInput = entity.doctorRemarks
                    },
                    onReject = {
                        selectedEntityForReject = entity
                        rejectReasonInput = ""
                    }
                )
            }

            // 4. Real-time Audit Trail Log
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Verification Audit Trail",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        auditLog.take(4).forEach { log ->
                            Row(verticalAlignment = Alignment.Top) {
                                Text("•", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = log,
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // ==========================================
    // EDIT CLINICAL FIELD DIALOG
    // ==========================================
    selectedEntityForEdit?.let { entity ->
        Dialog(onDismissRequest = { selectedEntityForEdit = null }) {
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
                    Text(
                        text = "Edit Extracted Value",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Field: ${entity.fieldName} (${entity.fieldCategory})",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.outline)
                    )

                    OutlinedTextField(
                        value = editedValueInput,
                        onValueChange = { editedValueInput = it },
                        label = { Text("Corrected Value") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = editRemarksInput,
                        onValueChange = { editRemarksInput = it },
                        label = { Text("Clinician Notes / Justification") },
                        placeholder = { Text("e.g. Dose adjusted per kidney function") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedEntityForEdit = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                verificationRepo.editField(entity.id, editedValueInput.trim(), editRemarksInput.trim(), doctorName)
                                selectedEntityForEdit = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Correction")
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // REJECT CLINICAL FIELD DIALOG
    // ==========================================
    selectedEntityForReject?.let { entity ->
        Dialog(onDismissRequest = { selectedEntityForReject = null }) {
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
                    Text(
                        text = "Reject AI Extraction",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    )
                    Text(
                        text = "Reason for rejecting '${entity.fieldName}':",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    OutlinedTextField(
                        value = rejectReasonInput,
                        onValueChange = { rejectReasonInput = it },
                        placeholder = { Text("e.g. OCR misread artifact, discontinued medication...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedEntityForReject = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                verificationRepo.rejectField(entity.id, rejectReasonInput.ifBlank { "Rejected by clinician" }, doctorName)
                                selectedEntityForReject = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Confirm Reject")
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // SUCCESS COMMIT DIALOG
    // ==========================================
    if (showCommitSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showCommitSuccessDialog = false },
            icon = {
                Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(36.dp))
            },
            title = { Text("Committed to Master EHR", fontWeight = FontWeight.Bold) },
            text = {
                Text("All verified clinical entities have been permanently signed by $doctorName and recorded in the patient's official electronic medical history.")
            },
            confirmButton = {
                Button(onClick = { showCommitSuccessDialog = false }) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
private fun VerifiableEntityCard(
    entity: VerifiableClinicalEntity,
    onAccept: () -> Unit,
    onEdit: () -> Unit,
    onReject: () -> Unit
) {
    val (statusBg, statusText, statusLabel) = when (entity.status) {
        ClinicalVerificationStatus.AI_EXTRACTED_PENDING -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), "AI DRAFT • PENDING")
        ClinicalVerificationStatus.DOCTOR_VERIFIED -> Triple(Color(0xFFDCFCE7), Color(0xFF16A34A), "DOCTOR VERIFIED")
        ClinicalVerificationStatus.DOCTOR_EDITED -> Triple(Color(0xFFE0F2FE), Color(0xFF0284C7), "DOCTOR EDITED")
        ClinicalVerificationStatus.DOCTOR_REJECTED -> Triple(Color(0xFFFEE2E2), Color(0xFFDC2626), "REJECTED")
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Category & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entity.fieldCategory.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = statusText, fontSize = 10.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Field Name
            Text(
                text = entity.fieldName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            // Extracted / Verified Value
            val displayVal = entity.verifiedValue ?: entity.extractedValue
            Text(
                text = displayVal,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (entity.status == ClinicalVerificationStatus.DOCTOR_REJECTED) Color.Gray else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            )

            if (entity.doctorRemarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Clinician Note: ${entity.doctorRemarks}",
                    style = MaterialTheme.typography.bodySmall.copy(color = statusText, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Provenance / Source Evidence
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Source: ${entity.sourceDocumentName} (${entity.sourceLocation})",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                )
            }

            if (entity.reviewedByDoctorName != null) {
                Text(
                    text = "Signed by: Dr. ${entity.reviewedByDoctorName} at ${entity.reviewedAtTimestamp}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF64748B))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Accept / Edit / Reject Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", fontSize = 12.sp)
                }

                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Accept", fontSize = 12.sp)
                }
            }
        }
    }
}
