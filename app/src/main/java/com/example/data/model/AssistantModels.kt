package com.example.data.model

/**
 * Structured intents for the MediKiosk AI Voice Assistant.
 */
enum class AssistantIntent {
    GET_QUEUE,
    GET_APPOINTMENT,
    GET_UPCOMING_APPOINTMENTS,
    GET_PATIENT_PROFILE,
    GET_DOCUMENTS,
    GET_MEDICAL_RECORDS,
    GET_LATEST_REPORT,
    GET_RECENT_ACTIVITY,
    GET_CURRENT_VISIT,
    SEARCH_HOSPITAL_SERVICE,
    FIND_DEPARTMENT,
    REQUEST_STAFF_HELP,
    START_VISIT,
    OPEN_HOME,
    OPEN_HISTORY,
    OPEN_DOCUMENTS,
    OPEN_APPOINTMENTS,
    OPEN_PROFILE,
    CHANGE_LANGUAGE,
    TOGGLE_EASY_MODE,
    GENERAL_QUESTION,
    MEDICAL_INFORMATION,
    UNSUPPORTED_MEDICAL_REQUEST,
    UNKNOWN
}

/**
 * Result of intent classification from user speech or text input.
 */
data class IntentClassification(
    val intent: AssistantIntent,
    val confidence: Double,
    val requiresPatientData: Boolean,
    val requiresTool: Boolean,
    val parameters: Map<String, String> = emptyMap(),
    val clarificationQuestion: String? = null
)

/**
 * Verified result from the internal Android tool execution layer.
 */
data class ToolExecutionResult(
    val toolName: String,
    val success: Boolean,
    val dataSummary: String,
    val spokenDirectFallback: String,
    val navigationTarget: String? = null,
    val executingDescription: String = "Processing request...",
    val isEmergency: Boolean = false,
    val extraData: String? = null
)
