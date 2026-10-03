package com.example.data.health

import android.content.Context
import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HealthConnectManager(private val context: Context) {

    private val _isHealthConnectAvailable = MutableStateFlow(false)
    val isHealthConnectAvailable: StateFlow<Boolean> = _isHealthConnectAvailable.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _activeDataSource = MutableStateFlow(MetricDataSource.DEMO_DATA)
    val activeDataSource: StateFlow<MetricDataSource> = _activeDataSource.asStateFlow()

    init {
        checkHealthConnectAvailability()
    }

    private fun checkHealthConnectAvailability() {
        // Safe check for Health Connect SDK client presence
        try {
            // Check if Health Connect package or client exists on device
            val packageManager = context.packageManager
            val intent = packageManager.getLaunchIntentForPackage("com.google.android.apps.healthdata")
            _isHealthConnectAvailable.value = (intent != null)
        } catch (e: Exception) {
            _isHealthConnectAvailable.value = false
        }
    }

    suspend fun getLiveDashboardSummary(): PatientHealthDashboardSummary {
        // When real Health Connect client is wired, queries aggregate records here.
        // For development and testing resilience, loads verified realistic Demo Health Data
        return DemoHealthDataProvider.getDashboardVitals()
    }

    suspend fun getMetricTrend(type: HealthMetricType, timeRange: TimeRangeFilter): HealthTrendSummary {
        return DemoHealthDataProvider.getTrendSummary(type, timeRange)
    }

    fun toggleSourceMode() {
        _activeDataSource.value = if (_activeDataSource.value == MetricDataSource.DEMO_DATA) {
            MetricDataSource.HEALTH_CONNECT
        } else {
            MetricDataSource.DEMO_DATA
        }
    }
}
