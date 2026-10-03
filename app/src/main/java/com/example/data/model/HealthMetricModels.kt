package com.example.data.model

import java.text.SimpleDateFormat
import java.util.*

enum class HealthMetricType(val displayName: String, val unit: String, val normalRange: String) {
    HEART_RATE("Heart Rate", "bpm", "60 - 100 bpm"),
    SPO2("Blood Oxygen (SpO2)", "%", "95 - 100%"),
    BLOOD_PRESSURE("Blood Pressure", "mmHg", "< 120/80 mmHg"),
    BODY_TEMPERATURE("Body Temperature", "°F", "97.0 - 99.0 °F"),
    STEPS("Daily Steps", "steps", "6,000 - 10,000"),
    CALORIES("Active Calories", "kcal", "400 - 600 kcal"),
    SLEEP("Sleep Duration", "hrs", "7.0 - 9.0 hrs"),
    RESPIRATORY_RATE("Respiration Rate", "br/min", "12 - 20 br/min")
}

enum class MetricDataSource(val label: String) {
    HEALTH_CONNECT("Health Connect"),
    WEARABLE_BLUETOOTH("Smart Device"),
    MANUAL_INPUT("Patient Reported"),
    DEMO_DATA("DEMO DATA")
}

data class HealthMetricPoint(
    val timestampMillis: Long,
    val value: Float,
    val secondaryValue: Float? = null, // e.g. Diastolic for BP
    val type: HealthMetricType,
    val source: MetricDataSource = MetricDataSource.DEMO_DATA,
    val isAbnormal: Boolean = false,
    val note: String = ""
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return sdf.format(Date(timestampMillis))
        }

    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            return sdf.format(Date(timestampMillis))
        }

    val displayValueString: String
        get() = when (type) {
            HealthMetricType.BLOOD_PRESSURE -> {
                if (secondaryValue != null) "${value.toInt()}/${secondaryValue.toInt()} mmHg"
                else "${value.toInt()} mmHg"
            }
            HealthMetricType.HEART_RATE -> "${value.toInt()} bpm"
            HealthMetricType.SPO2 -> "${value.toInt()}%"
            HealthMetricType.BODY_TEMPERATURE -> String.format(Locale.US, "%.1f °F", value)
            HealthMetricType.SLEEP -> String.format(Locale.US, "%.1f hrs", value)
            HealthMetricType.STEPS -> String.format(Locale.US, "%,d", value.toInt())
            HealthMetricType.CALORIES -> "${value.toInt()} kcal"
            HealthMetricType.RESPIRATORY_RATE -> "${value.toInt()} br/min"
        }
}

enum class TimeRangeFilter(val label: String, val daysCount: Int) {
    TODAY("Today", 1),
    SEVEN_DAYS("7 Days", 7),
    THIRTY_DAYS("30 Days", 30)
}

data class HealthTrendSummary(
    val type: HealthMetricType,
    val currentLatestValue: HealthMetricPoint,
    val averageValue: Float,
    val secondaryAverage: Float? = null,
    val minValue: Float,
    val maxValue: Float,
    val points: List<HealthMetricPoint>,
    val trendDirection: String = "Stable", // Increasing, Decreasing, Stable
    val aiClinicianObservation: String = "All parameters within expected normative variance."
)

data class PatientHealthDashboardSummary(
    val heartRateLatest: HealthMetricPoint,
    val spO2Latest: HealthMetricPoint,
    val bloodPressureLatest: HealthMetricPoint,
    val temperatureLatest: HealthMetricPoint,
    val todaySteps: Int = 7420,
    val todayCaloriesBurned: Int = 465,
    val lastNightSleepHours: Float = 7.5f,
    val overallHealthScore: Int = 92, // 0 - 100
    val dataSourceBadge: String = "DEMO DATA — NOT FOR REAL MEDICAL USE"
)
