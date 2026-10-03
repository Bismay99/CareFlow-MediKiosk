package com.example.data.model

import androidx.compose.ui.graphics.vector.ImageVector

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class VoiceAssistantState {
    IDLE,
    LISTENING,
    PROCESSING,
    EXECUTING,
    SPEAKING,
    ERROR
}

data class ActiveVisit(
    val hasActiveVisit: Boolean = true,
    val department: String = "General Medicine",
    val doctorName: String = "Dr. Rajesh Mehta",
    val tokenNumber: String = "#24",
    val queuePosition: Int = 6,
    val totalInQueue: Int = 11,
    val roomNumber: String = "Room 104 - 1st Floor",
    val status: String = "Waiting",
    val estimatedWaitMinutes: Int = 18
)

data class RecentlyViewedItem(
    val id: String,
    val type: String,
    val title: String,
    val dateOrStatus: String,
    val iconType: String
)

data class HealthInsight(
    val id: String,
    val title: String,
    val description: String,
    val tag: String = "AI-assisted",
    val iconType: String
)

data class RecentRecordItem(
    val id: String,
    val department: String,
    val recordType: String,
    val doctorOrFacility: String,
    val date: String,
    val summary: String
)

data class RecentActivityItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val timestamp: String,
    val isCompleted: Boolean = true
)

data class HospitalNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val isUrgent: Boolean = false,
    val isRead: Boolean = false
)
