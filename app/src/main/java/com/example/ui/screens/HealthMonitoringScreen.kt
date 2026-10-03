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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.health.DemoHealthDataProvider
import com.example.data.health.HealthConnectManager
import com.example.data.model.*
import com.example.ui.components.HealthTrendChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthMonitoringScreen(
    healthConnectManager: HealthConnectManager,
    initialDashboard: PatientHealthDashboardSummary,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMetricType by remember { mutableStateOf(HealthMetricType.HEART_RATE) }
    var selectedTimeRange by remember { mutableStateOf(TimeRangeFilter.SEVEN_DAYS) }
    var showManualEntryDialog by remember { mutableStateOf(false) }

    // Health Connect availability & active mode
    val isHealthConnectAvailable by healthConnectManager.isHealthConnectAvailable.collectAsState()
    val activeSource by healthConnectManager.activeDataSource.collectAsState()

    // Dynamic trend calculation from provider
    var trendSummary by remember(selectedMetricType, selectedTimeRange) {
        mutableStateOf(DemoHealthDataProvider.getTrendSummary(selectedMetricType, selectedTimeRange))
    }

    // Manual Vitals Input state
    var manualMetricType by remember { mutableStateOf(HealthMetricType.HEART_RATE) }
    var manualPrimaryVal by remember { mutableStateOf("") }
    var manualSecondaryVal by remember { mutableStateOf("") }
    var manualValidationError by remember { mutableStateOf<String?>(null) }
    var showSaveToast by remember { mutableStateOf(false) }

    val metricColor = when (selectedMetricType) {
        HealthMetricType.HEART_RATE -> Color(0xFFEF4444)
        HealthMetricType.SPO2 -> Color(0xFF0284C7)
        HealthMetricType.BLOOD_PRESSURE -> Color(0xFF0D9488)
        HealthMetricType.BODY_TEMPERATURE -> Color(0xFFF97316)
        HealthMetricType.STEPS -> Color(0xFF8B5CF6)
        HealthMetricType.SLEEP -> Color(0xFF6366F1)
        HealthMetricType.CALORIES -> Color(0xFFE11D48)
        HealthMetricType.RESPIRATORY_RATE -> Color(0xFF059669)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Health & Vitals Monitor",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Wearables, Health Connect & Biometric Trends",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showManualEntryDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "Manual Entry",
                            tint = MaterialTheme.colorScheme.primary
                        )
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
            // 1. Data Source Watermark Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (activeSource == MetricDataSource.HEALTH_CONNECT) {
                            Color(0xFFDCFCE7)
                        } else {
                            Color(0xFFFEF3C7)
                        }
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (activeSource == MetricDataSource.HEALTH_CONNECT) Color(0xFF16A34A) else Color(0xFFD97706)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (activeSource == MetricDataSource.HEALTH_CONNECT) Icons.Default.HealthAndSafety else Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (activeSource == MetricDataSource.HEALTH_CONNECT) "[HEALTH CONNECT ACTIVE]" else "[DEMO DATA]",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (activeSource == MetricDataSource.HEALTH_CONNECT) Color(0xFF14532D) else Color(0xFF92400E)
                                    )
                                )
                            }
                            Text(
                                text = if (activeSource == MetricDataSource.HEALTH_CONNECT) {
                                    "Connected to Android Health Connect. Live synced biometric telemetry."
                                } else {
                                    "Health Connect permissions unavailable on this device. Displaying realistic verified DEMO DATA."
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                            )
                        }

                        TextButton(
                            onClick = { healthConnectManager.toggleSourceMode() }
                        ) {
                            Text(
                                text = "Switch",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // 2. Overview Vitals Grid Cards (Heart Rate, SpO2, BP, Temp)
            item {
                Text(
                    text = "Current Physiological Vitals",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VitalSummaryCard(
                        title = "Heart Rate",
                        value = initialDashboard.heartRateLatest.displayValueString,
                        subtitle = "Resting: 68 bpm",
                        isSelected = selectedMetricType == HealthMetricType.HEART_RATE,
                        indicatorColor = Color(0xFFEF4444),
                        onClick = { selectedMetricType = HealthMetricType.HEART_RATE },
                        modifier = Modifier.weight(1f)
                    )

                    VitalSummaryCard(
                        title = "Blood Oxygen",
                        value = initialDashboard.spO2Latest.displayValueString,
                        subtitle = "Normal (> 95%)",
                        isSelected = selectedMetricType == HealthMetricType.SPO2,
                        indicatorColor = Color(0xFF0284C7),
                        onClick = { selectedMetricType = HealthMetricType.SPO2 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VitalSummaryCard(
                        title = "Blood Pressure",
                        value = initialDashboard.bloodPressureLatest.displayValueString,
                        subtitle = "Stage 1 Controlled",
                        isSelected = selectedMetricType == HealthMetricType.BLOOD_PRESSURE,
                        indicatorColor = Color(0xFF0D9488),
                        onClick = { selectedMetricType = HealthMetricType.BLOOD_PRESSURE },
                        modifier = Modifier.weight(1f)
                    )

                    VitalSummaryCard(
                        title = "Temperature",
                        value = initialDashboard.temperatureLatest.displayValueString,
                        subtitle = "Afebrile",
                        isSelected = selectedMetricType == HealthMetricType.BODY_TEMPERATURE,
                        indicatorColor = Color(0xFFF97316),
                        onClick = { selectedMetricType = HealthMetricType.BODY_TEMPERATURE },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 3. Activity & Sleep Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VitalSummaryCard(
                        title = "Daily Steps",
                        value = String.format("%,d", initialDashboard.todaySteps),
                        subtitle = "${initialDashboard.todayCaloriesBurned} kcal burned",
                        isSelected = selectedMetricType == HealthMetricType.STEPS,
                        indicatorColor = Color(0xFF8B5CF6),
                        onClick = { selectedMetricType = HealthMetricType.STEPS },
                        modifier = Modifier.weight(1f)
                    )

                    VitalSummaryCard(
                        title = "Sleep Duration",
                        value = "${initialDashboard.lastNightSleepHours} hrs",
                        subtitle = "Deep & REM normal",
                        isSelected = selectedMetricType == HealthMetricType.SLEEP,
                        indicatorColor = Color(0xFF6366F1),
                        onClick = { selectedMetricType = HealthMetricType.SLEEP },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. Interactive Trend Chart Section with 7-Day & 30-Day View
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${selectedMetricType.displayName} Trend",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Reference Range: ${selectedMetricType.normalRange}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    // 7-day vs 30-day Tab Selector
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                            .padding(2.dp)
                    ) {
                        listOf(TimeRangeFilter.SEVEN_DAYS, TimeRangeFilter.THIRTY_DAYS).forEach { filter ->
                            val isSelected = selectedTimeRange == filter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { selectedTimeRange = filter }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = filter.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 5. Render Trend Chart
            item {
                HealthTrendChart(
                    points = trendSummary.points,
                    metricType = selectedMetricType,
                    lineColor = metricColor
                )
            }

            // 6. Clinical Safety Observation Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Intelligent Biometric Observation",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = trendSummary.aiClinicianObservation,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AI assists with telemetry structuring. Medical diagnosis remains with the healthcare professional.",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // ==========================================
    // MANUAL VITAL ENTRY DIALOG WITH VALIDATION
    // ==========================================
    if (showManualEntryDialog) {
        Dialog(onDismissRequest = { showManualEntryDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Record Manual Vitals",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        IconButton(onClick = { showManualEntryDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        text = "Choose Metric Type",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    // Select Metric Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(
                            listOf(
                                HealthMetricType.HEART_RATE,
                                HealthMetricType.SPO2,
                                HealthMetricType.BLOOD_PRESSURE,
                                HealthMetricType.BODY_TEMPERATURE
                            )
                        ) { type ->
                            FilterChip(
                                selected = manualMetricType == type,
                                onClick = {
                                    manualMetricType = type
                                    manualPrimaryVal = ""
                                    manualSecondaryVal = ""
                                    manualValidationError = null
                                },
                                label = { Text(type.displayName) },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Value Inputs
                    if (manualMetricType == HealthMetricType.BLOOD_PRESSURE) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = manualPrimaryVal,
                                onValueChange = { manualPrimaryVal = it },
                                label = { Text("Systolic (mmHg)") },
                                placeholder = { Text("120") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = manualSecondaryVal,
                                onValueChange = { manualSecondaryVal = it },
                                label = { Text("Diastolic (mmHg)") },
                                placeholder = { Text("80") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = manualPrimaryVal,
                            onValueChange = { manualPrimaryVal = it },
                            label = { Text("Measured Value (${manualMetricType.unit})") },
                            placeholder = {
                                Text(
                                    when (manualMetricType) {
                                        HealthMetricType.HEART_RATE -> "e.g. 72"
                                        HealthMetricType.SPO2 -> "e.g. 98"
                                        HealthMetricType.BODY_TEMPERATURE -> "e.g. 98.6"
                                        else -> "Value"
                                    }
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Error text
                    manualValidationError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showManualEntryDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                // Validation Rules
                                val pVal = manualPrimaryVal.toFloatOrNull()
                                if (pVal == null) {
                                    manualValidationError = "Please enter a valid numeric measurement."
                                    return@Button
                                }

                                when (manualMetricType) {
                                    HealthMetricType.HEART_RATE -> {
                                        if (pVal < 30 || pVal > 240) {
                                            manualValidationError = "Heart rate must be between 30 and 240 bpm."
                                            return@Button
                                        }
                                    }
                                    HealthMetricType.SPO2 -> {
                                        if (pVal < 50 || pVal > 100) {
                                            manualValidationError = "SpO2 oxygen saturation must be between 50% and 100%."
                                            return@Button
                                        }
                                    }
                                    HealthMetricType.BODY_TEMPERATURE -> {
                                        if (pVal < 92.0 || pVal > 108.0) {
                                            manualValidationError = "Body temperature must be between 92.0°F and 108.0°F."
                                            return@Button
                                        }
                                    }
                                    HealthMetricType.BLOOD_PRESSURE -> {
                                        val sVal = manualSecondaryVal.toFloatOrNull()
                                        if (sVal == null) {
                                            manualValidationError = "Please enter a diastolic measurement."
                                            return@Button
                                        }
                                        if (pVal < 60 || pVal > 260 || sVal < 40 || sVal > 160) {
                                            manualValidationError = "Blood pressure values out of physiological range (60-260 / 40-160 mmHg)."
                                            return@Button
                                        }
                                    }
                                    else -> {}
                                }

                                // Successfully validated
                                showManualEntryDialog = false
                                showSaveToast = true
                            },
                            modifier = Modifier.weight(1.5f)
                        ) {
                            Text("Save Entry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VitalSummaryCard(
    title: String,
    value: String,
    subtitle: String,
    isSelected: Boolean,
    indicatorColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) indicatorColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, indicatorColor) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = indicatorColor)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.outline)
            )
        }
    }
}
