package com.example.data.health

import com.example.data.model.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

object DemoHealthDataProvider {

    fun getDashboardVitals(): PatientHealthDashboardSummary {
        val now = System.currentTimeMillis()
        return PatientHealthDashboardSummary(
            heartRateLatest = HealthMetricPoint(
                timestampMillis = now - TimeUnit.MINUTES.toMillis(4),
                value = 74f,
                type = HealthMetricType.HEART_RATE,
                source = MetricDataSource.DEMO_DATA,
                isAbnormal = false
            ),
            spO2Latest = HealthMetricPoint(
                timestampMillis = now - TimeUnit.MINUTES.toMillis(4),
                value = 98f,
                type = HealthMetricType.SPO2,
                source = MetricDataSource.DEMO_DATA,
                isAbnormal = false
            ),
            bloodPressureLatest = HealthMetricPoint(
                timestampMillis = now - TimeUnit.MINUTES.toMillis(15),
                value = 122f,
                secondaryValue = 78f,
                type = HealthMetricType.BLOOD_PRESSURE,
                source = MetricDataSource.DEMO_DATA,
                isAbnormal = false
            ),
            temperatureLatest = HealthMetricPoint(
                timestampMillis = now - TimeUnit.MINUTES.toMillis(20),
                value = 98.4f,
                type = HealthMetricType.BODY_TEMPERATURE,
                source = MetricDataSource.DEMO_DATA,
                isAbnormal = false
            ),
            todaySteps = 7840,
            todayCaloriesBurned = 512,
            lastNightSleepHours = 7.6f,
            overallHealthScore = 94,
            dataSourceBadge = "DEMO DATA — NOT FOR REAL MEDICAL USE"
        )
    }

    fun getTrendSummary(type: HealthMetricType, timeRange: TimeRangeFilter): HealthTrendSummary {
        val points = generateMetricHistory(type, timeRange)
        val values = points.map { it.value }
        val avg = if (values.isNotEmpty()) values.average().toFloat() else 0f
        val min = values.minOrNull() ?: 0f
        val max = values.maxOrNull() ?: 0f
        val latest = points.lastOrNull() ?: HealthMetricPoint(
            System.currentTimeMillis(),
            avg,
            null,
            type,
            MetricDataSource.DEMO_DATA
        )

        val secAvg = if (type == HealthMetricType.BLOOD_PRESSURE) {
            val secVals = points.mapNotNull { it.secondaryValue }
            if (secVals.isNotEmpty()) secVals.average().toFloat() else null
        } else null

        val trend = when {
            points.size < 2 -> "Stable"
            points.last().value > points.first().value * 1.05f -> "Mild Upward Trend"
            points.last().value < points.first().value * 0.95f -> "Improving Trend"
            else -> "Consistent & Stable"
        }

        val observation = when (type) {
            HealthMetricType.HEART_RATE -> "Resting heart rate demonstrates consistent diurnal rhythms with optimal nighttime recovery dips (56–64 bpm)."
            HealthMetricType.SPO2 -> "Oxygen saturation maintains therapeutic safety margins (97–99%), showing no desaturations during nocturnal windows."
            HealthMetricType.BLOOD_PRESSURE -> "Systolic and diastolic pressures remain controlled in the optimal stage 1 normotensive zone."
            HealthMetricType.BODY_TEMPERATURE -> "Afebrile circadian baseline consistent with recovery."
            HealthMetricType.SLEEP -> "Circadian consistency score 88%. Adequate restorative REM and deep sleep architecture recorded."
            HealthMetricType.STEPS -> "Daily ambulatory activity satisfies cardioprotective benchmarks (average > 7,000 steps/day)."
            HealthMetricType.CALORIES -> "Daily caloric expenditure aligned with active metabolism."
            HealthMetricType.RESPIRATORY_RATE -> "Eupneic resting respiration without tachypneic spikes."
        }

        return HealthTrendSummary(
            type = type,
            currentLatestValue = latest,
            averageValue = avg,
            secondaryAverage = secAvg,
            minValue = min,
            maxValue = max,
            points = points,
            trendDirection = trend,
            aiClinicianObservation = observation
        )
    }

    private fun generateMetricHistory(type: HealthMetricType, range: TimeRangeFilter): List<HealthMetricPoint> {
        val now = System.currentTimeMillis()
        val count = when (range) {
            TimeRangeFilter.TODAY -> 12 // every 2 hours
            TimeRangeFilter.SEVEN_DAYS -> 14 // 2 per day
            TimeRangeFilter.THIRTY_DAYS -> 30 // 1 per day
        }

        val stepMillis = when (range) {
            TimeRangeFilter.TODAY -> TimeUnit.HOURS.toMillis(2)
            TimeRangeFilter.SEVEN_DAYS -> TimeUnit.HOURS.toMillis(12)
            TimeRangeFilter.THIRTY_DAYS -> TimeUnit.DAYS.toMillis(1)
        }

        val list = mutableListOf<HealthMetricPoint>()
        for (i in (count - 1) downTo 0) {
            val time = now - (i * stepMillis)
            val point = when (type) {
                HealthMetricType.HEART_RATE -> {
                    val base = 70f + Random.nextInt(-6, 9)
                    HealthMetricPoint(time, base, null, type, MetricDataSource.DEMO_DATA)
                }
                HealthMetricType.SPO2 -> {
                    val base = 97f + Random.nextInt(0, 3)
                    HealthMetricPoint(time, base, null, type, MetricDataSource.DEMO_DATA)
                }
                HealthMetricType.BLOOD_PRESSURE -> {
                    val sys = 118f + Random.nextInt(-4, 10)
                    val dia = 76f + Random.nextInt(-3, 8)
                    HealthMetricPoint(time, sys, dia, type, MetricDataSource.DEMO_DATA)
                }
                HealthMetricType.BODY_TEMPERATURE -> {
                    val base = 98.2f + (Random.nextInt(-3, 6) / 10f)
                    HealthMetricPoint(time, base, null, type, MetricDataSource.DEMO_DATA)
                }
                HealthMetricType.SLEEP -> {
                    val hrs = 7.0f + (Random.nextInt(-10, 16) / 10f)
                    HealthMetricPoint(time, hrs, null, type, MetricDataSource.DEMO_DATA)
                }
                HealthMetricType.STEPS -> {
                    val steps = 6500f + Random.nextInt(-1500, 3500)
                    HealthMetricPoint(time, steps, null, type, MetricDataSource.DEMO_DATA)
                }
                HealthMetricType.CALORIES -> {
                    val cal = 420f + Random.nextInt(-50, 180)
                    HealthMetricPoint(time, cal, null, type, MetricDataSource.DEMO_DATA)
                }
                HealthMetricType.RESPIRATORY_RATE -> {
                    val rate = 15f + Random.nextInt(-2, 3)
                    HealthMetricPoint(time, rate, null, type, MetricDataSource.DEMO_DATA)
                }
            }
            list.add(point)
        }
        return list
    }
}
