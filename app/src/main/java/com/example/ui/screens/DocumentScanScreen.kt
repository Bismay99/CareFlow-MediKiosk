package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExtractedDocument
import com.example.data.model.LabValueItem
import com.example.data.model.MedicationItem
import com.example.ui.components.LargeActionButton
import com.example.ui.components.kioskTextFieldColors
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScanScreen(
    scannedBitmap: Bitmap?,
    isScanning: Boolean,
    extractedDoc: ExtractedDocument,
    onBitmapCaptured: (Bitmap) -> Unit,
    onLoadSampleDocument: () -> Unit,
    onUpdateDiagnosis: (String) -> Unit,
    onAddMedication: (MedicationItem) -> Unit,
    onUpdateMedication: (Int, MedicationItem) -> Unit,
    onDeleteMedication: (Int) -> Unit,
    onAddLabValue: (LabValueItem) -> Unit,
    onUpdateLabValue: (Int, LabValueItem) -> Unit,
    onDeleteLabValue: (Int) -> Unit,
    onProceedToSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showAddMedDialog by remember { mutableStateOf(false) }
    var showAddLabDialog by remember { mutableStateOf(false) }
    var editingMedIndex by remember { mutableStateOf<Int?>(null) }
    var editingLabIndex by remember { mutableStateOf<Int?>(null) }

    // Helper function to decode Uri into a reliable software bitmap
    fun decodeUriToSoftwareBitmap(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }
            } else {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                bmp
            }
        } catch (e: Exception) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                bmp
            } catch (e2: Exception) {
                Log.e("DocumentScanScreen", "Failed to decode bitmap from URI: $uri", e2)
                null
            }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            onBitmapCaptured(bitmap)
        }
    }

    // Modern Android Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            decodeUriToSoftwareBitmap(uri)?.let { bmp ->
                onBitmapCaptured(bmp)
            }
        }
    }

    // Gallery / Content fallback launcher
    val galleryFallbackLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            decodeUriToSoftwareBitmap(uri)?.let { bmp ->
                onBitmapCaptured(bmp)
            }
        }
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
        // Step 3 of 4 Progress Header
        com.example.ui.components.IntakeStepProgressHeader(
            step = 3,
            totalSteps = 4,
            stepTitle = "Document & Prescription Scan"
        )

        // Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Document & Prescription Scan",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            )
            Text(
                text = "Photograph prior prescriptions, discharge summaries, or blood test reports. Gemini Vision will extract medicines and lab values into an editable table.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Slate600)
            )
        }

        // Capture Action Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Camera Button
            Button(
                onClick = { cameraLauncher.launch(null) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("camera_capture_button")
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Camera", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }

            // Photo Button
            OutlinedButton(
                onClick = {
                    try {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    } catch (e: Exception) {
                        Log.w("DocumentScanScreen", "PickVisualMedia failed, launching GetContent fallback", e)
                        galleryFallbackLauncher.launch("image/*")
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("gallery_upload_button")
            ) {
                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Photo", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Quick Demo Test Button: Load Sample Prescription
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MediTealLight,
            border = androidx.compose.foundation.BorderStroke(1.dp, MediTealDark.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onLoadSampleDocument() }
                .testTag("load_sample_doc_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MediTealDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Test with Sample Hospital Prescription",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MediTealDark,
                                fontSize = 14.sp
                            )
                        )
                        Text(
                            text = "One-tap demo Rx (Telmisartan, Atorvastatin, Lipid panel)",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MediTealDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Image Preview & OCR progress
        if (scannedBitmap != null) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = androidx.compose.ui.graphics.Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate300),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Scanned Document Preview",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Image(
                        bitmap = scannedBitmap.asImageBitmap(),
                        contentDescription = "Scanned prescription",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                }
            }
        }

        if (isScanning) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MediBlueLight,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = MediBluePrimary,
                        strokeWidth = 3.dp
                    )
                    Column {
                        Text(
                            text = "Gemini Vision OCR Analyzing...",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MediBlueDark
                            )
                        )
                        Text(
                            text = "Extracting diagnosis, medications, and laboratory reference ranges",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate700)
                        )
                    }
                }
            }
        }

        // Section: Editable Diagnosis
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = androidx.compose.ui.graphics.Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.FactCheck,
                        contentDescription = null,
                        tint = MediBluePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Extracted Diagnosis / Condition",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    )
                }

                OutlinedTextField(
                    value = extractedDoc.diagnosis,
                    onValueChange = onUpdateDiagnosis,
                    placeholder = { Text("e.g. Essential Hypertension, Type 2 Diabetes") },
                    textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 16.sp),
                    colors = kioskTextFieldColors(focusedBorderColor = MediBluePrimary),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("extracted_diagnosis_input")
                )
            }
        }

        // Section: Editable Medications Table
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = androidx.compose.ui.graphics.Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
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
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            tint = MediTealSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Prescribed Medications (${extractedDoc.medications.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                    }

                    TextButton(
                        onClick = { showAddMedDialog = true },
                        modifier = Modifier.testTag("add_medication_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Med", fontWeight = FontWeight.Bold)
                    }
                }

                if (extractedDoc.medications.isEmpty()) {
                    Text(
                        text = "No medications extracted yet. Scan a document or tap 'Add Med'.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Slate500,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                } else {
                    extractedDoc.medications.forEachIndexed { index, med ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Slate50,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${med.name} (${med.dosage})",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Slate900
                                        )
                                    )
                                    Text(
                                        text = "${med.frequency} • ${med.instructions}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Slate600,
                                            fontSize = 13.sp
                                        )
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = { editingMedIndex = index },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit Medication",
                                            tint = MediBluePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteMedication(index) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Medication",
                                            tint = MediRedUrgent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section: Editable Lab Values Table
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = androidx.compose.ui.graphics.Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
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
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = null,
                            tint = MediAmberWarning,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Lab Investigations (${extractedDoc.labValues.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        )
                    }

                    TextButton(
                        onClick = { showAddLabDialog = true },
                        modifier = Modifier.testTag("add_lab_value_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Lab", fontWeight = FontWeight.Bold)
                    }
                }

                if (extractedDoc.labValues.isEmpty()) {
                    Text(
                        text = "No laboratory values recorded yet. Add blood tests or scan lab reports.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Slate500,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                } else {
                    extractedDoc.labValues.forEachIndexed { index, lab ->
                        val isAbnormal = lab.status.equals("High", ignoreCase = true) || lab.status.equals("Low", ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isAbnormal) MediAmberLight.copy(alpha = 0.35f) else Slate50,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isAbnormal) MediAmberWarning.copy(alpha = 0.5f) else Slate200
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = lab.testName,
                                            style = MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Slate900
                                            )
                                        )
                                        if (isAbnormal) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (lab.status.equals("High", ignoreCase = true)) MediRedLight else MediAmberLight
                                            ) {
                                                Text(
                                                    text = lab.status.uppercase(),
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        color = if (lab.status.equals("High", ignoreCase = true)) MediRedUrgent else MediAmberWarning,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.sp
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${lab.value} ${lab.unit}  (Ref: ${lab.referenceRange})",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Slate600,
                                            fontSize = 13.sp
                                        )
                                    )
                                }

                                Row {
                                    IconButton(
                                        onClick = { editingLabIndex = index },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit Lab Result",
                                            tint = MediBluePrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteLabValue(index) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete Lab Result",
                                            tint = MediRedUrgent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons: Proceed or Skip
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            LargeActionButton(
                text = "Proceed to Clinical Summary",
                onClick = onProceedToSummary,
                testTag = "proceed_to_summary_button"
            )

            TextButton(
                onClick = onProceedToSummary,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("skip_doc_scan_button")
            ) {
                Text(
                    text = "No documents to scan / Skip to Summary",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Slate600,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }

    // Add/Edit Medication Dialog
    if (showAddMedDialog || editingMedIndex != null) {
        val isEditing = editingMedIndex != null
        val initial = if (isEditing) extractedDoc.medications[editingMedIndex!!] else MedicationItem()
        var name by remember { mutableStateOf(initial.name) }
        var dosage by remember { mutableStateOf(initial.dosage) }
        var frequency by remember { mutableStateOf(initial.frequency) }
        var instructions by remember { mutableStateOf(initial.instructions) }

        AlertDialog(
            onDismissRequest = {
                showAddMedDialog = false
                editingMedIndex = null
            },
            title = {
                Text(
                    text = if (isEditing) "Edit Medication" else "Add Medication",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Medicine Name") },
                        placeholder = { Text("e.g. Telmisartan") },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        colors = kioskTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("med_dialog_name")
                    )
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage") },
                        placeholder = { Text("e.g. 40 mg") },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        colors = kioskTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("med_dialog_dosage")
                    )
                    OutlinedTextField(
                        value = frequency,
                        onValueChange = { frequency = it },
                        label = { Text("Frequency") },
                        placeholder = { Text("e.g. Once daily (OD)") },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        colors = kioskTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("med_dialog_frequency")
                    )
                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Instructions") },
                        placeholder = { Text("e.g. After breakfast") },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        colors = kioskTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("med_dialog_instructions")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val item = MedicationItem(name, dosage, frequency, instructions)
                        if (isEditing) {
                            onUpdateMedication(editingMedIndex!!, item)
                        } else {
                            onAddMedication(item)
                        }
                        showAddMedDialog = false
                        editingMedIndex = null
                    },
                    enabled = name.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddMedDialog = false
                        editingMedIndex = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add/Edit Lab Value Dialog
    if (showAddLabDialog || editingLabIndex != null) {
        val isEditing = editingLabIndex != null
        val initial = if (isEditing) extractedDoc.labValues[editingLabIndex!!] else LabValueItem()
        var testName by remember { mutableStateOf(initial.testName) }
        var value by remember { mutableStateOf(initial.value) }
        var unit by remember { mutableStateOf(initial.unit) }
        var refRange by remember { mutableStateOf(initial.referenceRange) }
        var status by remember { mutableStateOf(initial.status) }

        AlertDialog(
            onDismissRequest = {
                showAddLabDialog = false
                editingLabIndex = null
            },
            title = {
                Text(
                    text = if (isEditing) "Edit Lab Value" else "Add Lab Investigation",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = testName,
                        onValueChange = { testName = it },
                        label = { Text("Test Name") },
                        placeholder = { Text("e.g. Total Cholesterol") },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        colors = kioskTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("lab_dialog_name")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = value,
                            onValueChange = { value = it },
                            label = { Text("Value") },
                            placeholder = { Text("228") },
                            textStyle = LocalTextStyle.current.copy(color = Color.Black),
                            colors = kioskTextFieldColors(),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("lab_dialog_value")
                        )
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit") },
                            placeholder = { Text("mg/dL") },
                            textStyle = LocalTextStyle.current.copy(color = Color.Black),
                            colors = kioskTextFieldColors(),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("lab_dialog_unit")
                        )
                    }
                    OutlinedTextField(
                        value = refRange,
                        onValueChange = { refRange = it },
                        label = { Text("Reference Range") },
                        placeholder = { Text("< 200") },
                        textStyle = LocalTextStyle.current.copy(color = Color.Black),
                        colors = kioskTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("lab_dialog_ref")
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("Normal", "High", "Low").forEach { st ->
                            FilterChip(
                                selected = status.equals(st, ignoreCase = true),
                                onClick = { status = st },
                                label = { Text(st) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val item = LabValueItem(testName, value, unit, refRange, status)
                        if (isEditing) {
                            onUpdateLabValue(editingLabIndex!!, item)
                        } else {
                            onAddLabValue(item)
                        }
                        showAddLabDialog = false
                        editingLabIndex = null
                    },
                    enabled = testName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddLabDialog = false
                        editingLabIndex = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
