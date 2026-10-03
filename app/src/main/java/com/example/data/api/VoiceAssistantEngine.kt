package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Result of the full AI Assistant processing pipeline.
 */
data class AssistantPipelineResult(
    val intent: AssistantIntent,
    val confidence: Double,
    val selectedTool: String?,
    val executingStatus: String,
    val spokenResponse: String,
    val navigationTarget: String? = null,
    val isEmergency: Boolean = false,
    val extraData: String? = null,
    val latencyMs: Long = 0L
)

class VoiceAssistantEngine(
    private val toolRegistry: AssistantToolRegistry
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val primaryModel = "gemini-3.5-flash"
    private val fallbackModel = "gemini-3.6-flash"

    private fun getGenerateContentUrl(apiKey: String, model: String): String {
        return "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
    }

    private fun executeGenerateContent(apiKey: String, requestJson: JSONObject): String? {
        val modelsToTry = listOf(primaryModel, fallbackModel, "gemini-flash-latest")
        for (model in modelsToTry) {
            try {
                val url = getGenerateContentUrl(apiKey, model)
                val request = Request.Builder()
                    .url(url)
                    .post(requestJson.toString().toRequestBody(jsonMediaType))
                    .build()

                val response = client.newCall(request).execute()
                val responseString = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val rootJson = JSONObject(responseString)
                    val candidates = rootJson.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val content = firstCandidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val rawText = parts?.optJSONObject(0)?.optString("text")
                    if (!rawText.isNullOrBlank()) {
                        return rawText
                    }
                } else {
                    Log.w("VoiceAssistantEngine", "Model $model failed: code=${response.code} body=$responseString")
                }
            } catch (e: Exception) {
                Log.w("VoiceAssistantEngine", "Exception calling model $model", e)
            }
        }
        return null
    }

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    /**
     * Complete multi-step assistant processing pipeline:
     * 1. Normalize input
     * 2. Check emergency red-flags
     * 3. Intent Detection (Gemini structured JSON + deterministic fallback)
     * 4. Safe Tool Execution on verified local application state
     * 5. Response generation (Gemini grounded strictly in tool data or deterministic verified fallback)
     * 6. Debug logging
     */
    suspend fun processQuery(
        rawQuery: String,
        patientProfile: PatientProfile,
        currentUser: AuthUser?,
        activeVisit: ActiveVisit?,
        appointments: List<HospitalAppointment>,
        recentRecords: List<RecentRecordItem>,
        recentActivities: List<RecentActivityItem>,
        extractedDoc: ExtractedDocument?,
        language: Language,
        conversationHistory: List<Pair<String, String>>,
        onStatusUpdate: (String) -> Unit = {}
    ): AssistantPipelineResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        // 1. Normalize Input
        val query = rawQuery.trim()
        if (query.isBlank()) {
            val latency = System.currentTimeMillis() - startTime
            logAssistantPipeline(query, AssistantIntent.UNKNOWN, 0.0, null, "EMPTY_INPUT", latency)
            return@withContext AssistantPipelineResult(
                intent = AssistantIntent.UNKNOWN,
                confidence = 0.0,
                selectedTool = null,
                executingStatus = "Empty input",
                spokenResponse = "I couldn't hear that clearly. Please try again.",
                latencyMs = latency
            )
        }

        val qLower = query.lowercase()

        // Security check: Never allow accessing other patients' records
        if (qLower.contains("another patient") || qLower.contains("other patient") ||
            qLower.contains("someone else's record") || qLower.contains("different patient") ||
            qLower.contains("दूसरे मरीज") || qLower.contains("किसी और का")
        ) {
            val latency = System.currentTimeMillis() - startTime
            logAssistantPipeline(query, AssistantIntent.GET_MEDICAL_RECORDS, 1.0, "SECURITY_BLOCKED", "UNAUTHORIZED", latency)
            return@withContext AssistantPipelineResult(
                intent = AssistantIntent.GET_MEDICAL_RECORDS,
                confidence = 1.0,
                selectedTool = null,
                executingStatus = "Security restriction",
                spokenResponse = "I can only access records for the currently signed-in patient.",
                latencyMs = latency
            )
        }

        // 2. Emergency Red-Flag Check
        if (isEmergencyQuery(qLower)) {
            val latency = System.currentTimeMillis() - startTime
            logAssistantPipeline(query, AssistantIntent.UNSUPPORTED_MEDICAL_REQUEST, 1.0, "EMERGENCY_ALERT", "SUCCESS", latency)
            return@withContext AssistantPipelineResult(
                intent = AssistantIntent.UNSUPPORTED_MEDICAL_REQUEST,
                confidence = 1.0,
                selectedTool = "EMERGENCY_ALERT",
                executingStatus = "Alerting Emergency Triage...",
                spokenResponse = "This may require urgent medical attention. Please contact hospital staff or emergency services immediately.",
                isEmergency = true,
                latencyMs = latency
            )
        }

        // 3. Classify Intent (Gemini JSON classification with instant local deterministic fallback)
        onStatusUpdate("Thinking...")
        val classification = classifyIntent(query, language, conversationHistory)

        // 4. Check for ambiguous questions needing clarification
        if (classification.intent == AssistantIntent.UNKNOWN && classification.clarificationQuestion != null) {
            val latency = System.currentTimeMillis() - startTime
            logAssistantPipeline(query, AssistantIntent.UNKNOWN, classification.confidence, null, "CLARIFICATION_REQUIRED", latency)
            return@withContext AssistantPipelineResult(
                intent = AssistantIntent.UNKNOWN,
                confidence = classification.confidence,
                selectedTool = null,
                executingStatus = "Clarification needed",
                spokenResponse = classification.clarificationQuestion,
                latencyMs = latency
            )
        }

        // 5. Execute internal application tool
        var toolResult: ToolExecutionResult? = null
        var executingStatus = "Processing your request..."

        if (classification.requiresTool) {
            val toolName = mapIntentToToolName(classification.intent)
            executingStatus = getExecutingDescription(classification.intent)
            onStatusUpdate(executingStatus)

            toolResult = executeTool(
                intent = classification.intent,
                patientProfile = patientProfile,
                currentUser = currentUser,
                activeVisit = activeVisit,
                appointments = appointments,
                recentRecords = recentRecords,
                recentActivities = recentActivities,
                extractedDoc = extractedDoc,
                parameters = classification.parameters,
                query = query
            )
        }

        // 6. Generate grounded response
        val spokenResponse = generateFinalResponse(
            query = query,
            intent = classification.intent,
            toolResult = toolResult,
            language = language,
            conversationHistory = conversationHistory
        )

        val totalLatency = System.currentTimeMillis() - startTime
        logAssistantPipeline(
            input = query,
            intent = classification.intent,
            confidence = classification.confidence,
            tool = toolResult?.toolName,
            status = if (toolResult?.success != false) "SUCCESS" else "FAILED",
            latencyMs = totalLatency
        )

        return@withContext AssistantPipelineResult(
            intent = classification.intent,
            confidence = classification.confidence,
            selectedTool = toolResult?.toolName,
            executingStatus = executingStatus,
            spokenResponse = spokenResponse,
            navigationTarget = toolResult?.navigationTarget,
            isEmergency = toolResult?.isEmergency ?: false,
            extraData = toolResult?.extraData,
            latencyMs = totalLatency
        )
    }

    /**
     * Emergency query detector across multiple languages.
     */
    private fun isEmergencyQuery(q: String): Boolean {
        return q.contains("chest pain") || q.contains("heart attack") ||
               q.contains("can't breathe") || q.contains("cannot breathe") ||
               q.contains("difficulty breathing") || q.contains("shortness of breath") ||
               q.contains("unconscious") || q.contains("collapsed") ||
               q.contains("severe bleeding") || q.contains("bleeding heavily") ||
               q.contains("stroke") || q.contains("paralysis") ||
               q.contains("सीने में दर्द") || q.contains("सांस नहीं आ रही") ||
               q.contains("बेहोश") || q.contains("खून बह रहा") ||
               q.contains("हार्ट अटैक") || q.contains("நெஞ்சு வலி") ||
               q.contains("శ్వాస ఆడటం లేదు") || q.contains("छातीत दुखणे")
    }

    /**
     * Classifies intent using deterministic regex / token matching first for 0ms latency,
     * falling back to Gemini JSON classification for complex nuances.
     */
    private suspend fun classifyIntent(
        query: String,
        language: Language,
        conversationHistory: List<Pair<String, String>>
    ): IntentClassification {
        // Evaluate deterministic rules first
        val local = evaluateDeterministicIntent(query, conversationHistory)
        if (local != null) {
            return local
        }

        // Use Gemini structured JSON classifier
        val apiKey = getApiKey()
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val geminiIntent = callGeminiIntentClassifier(query, language)
                if (geminiIntent != null) {
                    return geminiIntent
                }
            } catch (e: Exception) {
                Log.w("VoiceAssistantEngine", "Gemini intent classification error: ${e.message}")
            }
        }

        // Final default fallback
        return IntentClassification(
            intent = AssistantIntent.GENERAL_QUESTION,
            confidence = 0.5,
            requiresPatientData = false,
            requiresTool = false
        )
    }

    /**
     * Ultra-accurate deterministic classifier covering all standard Kiosk queries
     * and multilingual variations.
     */
    fun evaluateDeterministicIntent(
        query: String,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): IntentClassification? {
        val q = query.lowercase().trim()

        // Handle context from previous turns
        val lastModelMessage = conversationHistory.lastOrNull { it.first == "model" }?.second?.lowercase() ?: ""
        val isQueueContext = lastModelMessage.contains("queue") || lastModelMessage.contains("token") || lastModelMessage.contains("wait")

        // 1. Ambiguous query check ("Show me that", "Open that", "What is that")
        if (q == "show me that" || q == "open that" || q == "show that" || q == "tell me that" || q == "kholo") {
            return IntentClassification(
                intent = AssistantIntent.UNKNOWN,
                confidence = 0.3,
                requiresPatientData = false,
                requiresTool = false,
                clarificationQuestion = "What would you like me to show you — your records, documents, or appointment?"
            )
        }

        // 2. Queue number & wait inquiries
        // "What is my queue number?", "How many people are before me?", "How long do I need to wait?",
        // "Mera queue number kya hai?", "Doctor se milne mein kitna time lagega?", "How long will it take?"
        if (q.contains("queue number") || q.contains("token number") || q.contains("my token") ||
            q.contains("mera queue") || q.contains("mera token") || q.contains("queue status") ||
            q.contains("people are before me") || q.contains("people ahead") || q.contains("ahead of me") ||
            q.contains("how many people") || q.contains("kitne log") || q.contains("kitne mareez") ||
            q.contains("how long do i need to wait") || q.contains("how long to wait") ||
            q.contains("kitna time lagega") || q.contains("kitna samay") || q.contains("wait time") ||
            q.contains("doctor se milne mein") || q.contains("doctor kab milenge") ||
            (isQueueContext && (q.contains("how long") || q.contains("when") || q.contains("kitna")))
        ) {
            return IntentClassification(
                intent = AssistantIntent.GET_QUEUE,
                confidence = 0.98,
                requiresPatientData = true,
                requiresTool = true
            )
        }

        // 3. Appointment inquiries
        // "When is my appointment?", "Do I have an appointment tomorrow?", "Show my appointments"
        if (q.contains("appointment tomorrow") || q.contains("kal appointment") || q.contains("kal koi")) {
            return IntentClassification(
                intent = AssistantIntent.GET_UPCOMING_APPOINTMENTS,
                confidence = 0.98,
                requiresPatientData = true,
                requiresTool = true,
                parameters = mapOf("tomorrow" to "true")
            )
        }
        if (q.contains("appointment") || q.contains("schedule") || q.contains("doctor visit") ||
            q.contains("doctor kab") || q.contains("appointment kab hai")
        ) {
            return IntentClassification(
                intent = AssistantIntent.GET_APPOINTMENT,
                confidence = 0.96,
                requiresPatientData = true,
                requiresTool = true
            )
        }

        // 4. Latest Report inquiries
        // "Show my latest report", "Latest lab report", "Blood test result"
        if (q.contains("latest report") || q.contains("latest test") || q.contains("blood report") ||
            q.contains("lab report") || q.contains("sugar report") || q.contains("lipid report") ||
            q.contains("taza report") || q.contains("report dikhao") || q.contains("nai report")
        ) {
            return IntentClassification(
                intent = AssistantIntent.GET_LATEST_REPORT,
                confidence = 0.97,
                requiresPatientData = true,
                requiresTool = true
            )
        }

        // 5. Medical Records inquiries
        // "Show my medical records", "My past records", "Previous history"
        if (q.contains("medical records") || q.contains("my records") || q.contains("past records") ||
            q.contains("all reports") || q.contains("purani report") || q.contains("medical file")
        ) {
            return IntentClassification(
                intent = AssistantIntent.GET_MEDICAL_RECORDS,
                confidence = 0.96,
                requiresPatientData = true,
                requiresTool = true
            )
        }

        // 6. Uploaded Documents
        // "What documents have I uploaded?", "Uploaded prescriptions"
        if (q.contains("documents have i uploaded") || q.contains("uploaded documents") ||
            q.contains("uploaded files") || q.contains("documents uploaded") || q.contains("kya upload kiya")
        ) {
            return IntentClassification(
                intent = AssistantIntent.GET_DOCUMENTS,
                confidence = 0.96,
                requiresPatientData = true,
                requiresTool = true
            )
        }

        // 7. Current Visit
        // "Show my current visit", "Active visit status"
        if (q.contains("current visit") || q.contains("active visit") || q.contains("visit status") ||
            q.contains("check in status") || q.contains("aaj ka visit")
        ) {
            return IntentClassification(
                intent = AssistantIntent.GET_CURRENT_VISIT,
                confidence = 0.96,
                requiresPatientData = true,
                requiresTool = true
            )
        }

        // 8. Navigation Actions
        // "Open my documents", "Open my history", "Go to my profile", "Go home", "Open appointments"
        if (q.contains("open my documents") || q.contains("open documents") || q.contains("go to documents") ||
            q.contains("scan document") || q.contains("document screen")
        ) {
            return IntentClassification(
                intent = AssistantIntent.OPEN_DOCUMENTS,
                confidence = 0.99,
                requiresPatientData = false,
                requiresTool = true
            )
        }
        if (q.contains("open my history") || q.contains("open history") || q.contains("show my history") ||
            q.contains("medical intake") || q.contains("history screen")
        ) {
            return IntentClassification(
                intent = AssistantIntent.OPEN_HISTORY,
                confidence = 0.99,
                requiresPatientData = false,
                requiresTool = true
            )
        }
        if (q.contains("go to my profile") || q.contains("open profile") || q.contains("show profile") ||
            q.contains("profile screen") || q.contains("my details")
        ) {
            return IntentClassification(
                intent = AssistantIntent.OPEN_PROFILE,
                confidence = 0.99,
                requiresPatientData = false,
                requiresTool = true
            )
        }
        if (q.contains("go home") || q.contains("open home") || q.contains("home screen") || q.contains("main menu")) {
            return IntentClassification(
                intent = AssistantIntent.OPEN_HOME,
                confidence = 0.99,
                requiresPatientData = false,
                requiresTool = true
            )
        }
        if (q.contains("open appointments") || q.contains("show appointments screen")) {
            return IntentClassification(
                intent = AssistantIntent.OPEN_APPOINTMENTS,
                confidence = 0.99,
                requiresPatientData = false,
                requiresTool = true
            )
        }

        // 9. Hospital Wayfinding / Directions
        // "Where is cardiology?", "Find cardiology", "Where is the lab?", "Pharmacy kahan hai?"
        if (q.contains("where is") || q.contains("find ") || q.contains("directions to") ||
            q.contains("kahan hai") || q.contains("kidhar hai") || q.contains("raste") ||
            q.contains("cardiology") || q.contains("orthopedic") || q.contains("pathology") ||
            q.contains("pharmacy") || q.contains("radiology")
        ) {
            return IntentClassification(
                intent = AssistantIntent.FIND_DEPARTMENT,
                confidence = 0.95,
                requiresPatientData = false,
                requiresTool = true,
                parameters = mapOf("department" to extractDepartmentQuery(q))
            )
        }

        // 10. Staff Assistance
        // "I need help", "Call staff", "Need wheelchair", "Mujhe help chahiye", "Nurse bulao"
        if (q.contains("need help") || q.contains("staff help") || q.contains("help chahiye") ||
            q.contains("call nurse") || q.contains("wheelchair") || q.contains("madad chahiye") ||
            q.contains("sahayata") || q == "help" || q == "i need assistance"
        ) {
            return IntentClassification(
                intent = AssistantIntent.REQUEST_STAFF_HELP,
                confidence = 0.98,
                requiresPatientData = false,
                requiresTool = true,
                parameters = mapOf("reason" to query)
            )
        }

        // 11. Start a new visit
        // "Start a new visit", "Start visit", "New visit", "Check in"
        if (q.contains("start a new visit") || q.contains("start visit") || q.contains("begin visit") ||
            q.contains("check in") || q.contains("naya visit")
        ) {
            return IntentClassification(
                intent = AssistantIntent.START_VISIT,
                confidence = 0.98,
                requiresPatientData = false,
                requiresTool = true
            )
        }

        // 12. General Medical Knowledge
        // "What is an ECG?", "What is an MRI?", "What is blood pressure?", "ECG kya hota hai?"
        if (q.contains("what is an ecg") || q.contains("what is ecg") || q.contains("ecg kya") ||
            q.contains("what is an mri") || q.contains("what is mri") || q.contains("mri kya") ||
            q.contains("what is blood pressure") || q.contains("bp kya hota hai") ||
            q.contains("what should i bring") || q.contains("fasting blood test")
        ) {
            return IntentClassification(
                intent = AssistantIntent.MEDICAL_INFORMATION,
                confidence = 0.95,
                requiresPatientData = false,
                requiresTool = false
            )
        }

        // 13. Language switching
        if (q.contains("switch to hindi") || q.contains("hindi mein") || q.contains("hindi bolo")) {
            return IntentClassification(
                intent = AssistantIntent.CHANGE_LANGUAGE,
                confidence = 0.99,
                requiresPatientData = false,
                requiresTool = true,
                parameters = mapOf("lang" to "hi")
            )
        }
        if (q.contains("switch to english") || q.contains("english please")) {
            return IntentClassification(
                intent = AssistantIntent.CHANGE_LANGUAGE,
                confidence = 0.99,
                requiresPatientData = false,
                requiresTool = true,
                parameters = mapOf("lang" to "en")
            )
        }

        // 14. Easy mode / Accessibility
        if (q.contains("easy mode") || q.contains("large text") || q.contains("accessibility")) {
            return IntentClassification(
                intent = AssistantIntent.TOGGLE_EASY_MODE,
                confidence = 0.99,
                requiresPatientData = false,
                requiresTool = true
            )
        }

        return null
    }

    private fun extractDepartmentQuery(q: String): String {
        return when {
            q.contains("cardio") || q.contains("heart") -> "Cardiology"
            q.contains("ortho") || q.contains("bone") || q.contains("joint") -> "Orthopedics"
            q.contains("lab") || q.contains("blood") || q.contains("pathology") -> "Pathology"
            q.contains("radiology") || q.contains("x-ray") || q.contains("mri") || q.contains("ct") -> "Radiology"
            q.contains("pharmacy") || q.contains("medicine") || q.contains("dawa") -> "Pharmacy"
            q.contains("pediatric") || q.contains("child") -> "Pediatrics"
            q.contains("gyne") || q.contains("maternity") -> "Obstetrics"
            else -> "General Medicine"
        }
    }

    private fun mapIntentToToolName(intent: AssistantIntent): String {
        return when (intent) {
            AssistantIntent.GET_QUEUE -> "getCurrentQueue"
            AssistantIntent.GET_APPOINTMENT, AssistantIntent.GET_UPCOMING_APPOINTMENTS -> "getAppointment"
            AssistantIntent.GET_PATIENT_PROFILE -> "getPatientProfile"
            AssistantIntent.GET_DOCUMENTS -> "getDocuments"
            AssistantIntent.GET_MEDICAL_RECORDS -> "getMedicalRecords"
            AssistantIntent.GET_LATEST_REPORT -> "getLatestReport"
            AssistantIntent.GET_RECENT_ACTIVITY -> "getRecentActivity"
            AssistantIntent.GET_CURRENT_VISIT -> "getCurrentVisit"
            AssistantIntent.SEARCH_HOSPITAL_SERVICE -> "searchHospitalServices"
            AssistantIntent.FIND_DEPARTMENT -> "findDepartment"
            AssistantIntent.REQUEST_STAFF_HELP -> "requestStaffHelp"
            AssistantIntent.START_VISIT -> "startNewVisit"
            AssistantIntent.OPEN_HISTORY -> "openHistory"
            AssistantIntent.OPEN_DOCUMENTS -> "openDocuments"
            AssistantIntent.OPEN_APPOINTMENTS -> "openAppointments"
            AssistantIntent.OPEN_PROFILE -> "openProfile"
            AssistantIntent.OPEN_HOME -> "openHome"
            AssistantIntent.CHANGE_LANGUAGE -> "changeLanguage"
            AssistantIntent.TOGGLE_EASY_MODE -> "toggleEasyMode"
            else -> "none"
        }
    }

    private fun getExecutingDescription(intent: AssistantIntent): String {
        return when (intent) {
            AssistantIntent.GET_QUEUE -> "Getting your queue information..."
            AssistantIntent.GET_APPOINTMENT, AssistantIntent.GET_UPCOMING_APPOINTMENTS -> "Checking your appointment schedule..."
            AssistantIntent.GET_PATIENT_PROFILE -> "Opening patient profile..."
            AssistantIntent.GET_DOCUMENTS -> "Fetching uploaded documents..."
            AssistantIntent.GET_MEDICAL_RECORDS -> "Loading medical records..."
            AssistantIntent.GET_LATEST_REPORT -> "Locating latest diagnostic report..."
            AssistantIntent.GET_RECENT_ACTIVITY -> "Checking recent activities..."
            AssistantIntent.GET_CURRENT_VISIT -> "Retrieving visit status..."
            AssistantIntent.SEARCH_HOSPITAL_SERVICE, AssistantIntent.FIND_DEPARTMENT -> "Finding hospital directions..."
            AssistantIntent.REQUEST_STAFF_HELP -> "Dispatching hospital staff assistance..."
            AssistantIntent.START_VISIT -> "Starting new visit and generating token..."
            AssistantIntent.OPEN_HISTORY -> "Opening medical history..."
            AssistantIntent.OPEN_DOCUMENTS -> "Opening document scanner..."
            AssistantIntent.OPEN_APPOINTMENTS -> "Opening appointments..."
            AssistantIntent.OPEN_PROFILE -> "Opening profile..."
            AssistantIntent.OPEN_HOME -> "Returning to home..."
            AssistantIntent.CHANGE_LANGUAGE -> "Updating language..."
            AssistantIntent.TOGGLE_EASY_MODE -> "Toggling easy mode..."
            else -> "Thinking..."
        }
    }

    /**
     * Executes the requested tool safely against the verified Android state.
     */
    private fun executeTool(
        intent: AssistantIntent,
        patientProfile: PatientProfile,
        currentUser: AuthUser?,
        activeVisit: ActiveVisit?,
        appointments: List<HospitalAppointment>,
        recentRecords: List<RecentRecordItem>,
        recentActivities: List<RecentActivityItem>,
        extractedDoc: ExtractedDocument?,
        parameters: Map<String, String>,
        query: String
    ): ToolExecutionResult {
        return when (intent) {
            AssistantIntent.GET_QUEUE ->
                toolRegistry.getCurrentQueue(patientProfile.name, activeVisit)

            AssistantIntent.GET_APPOINTMENT -> {
                val isTomorrow = parameters["tomorrow"] == "true" || query.contains("tomorrow", ignoreCase = true) || query.contains("kal", ignoreCase = true)
                toolRegistry.getAppointment(appointments, isTomorrowQuery = isTomorrow)
            }

            AssistantIntent.GET_UPCOMING_APPOINTMENTS ->
                toolRegistry.getUpcomingAppointments(appointments)

            AssistantIntent.GET_PATIENT_PROFILE ->
                toolRegistry.getPatientProfile(patientProfile, currentUser)

            AssistantIntent.GET_DOCUMENTS ->
                toolRegistry.getDocuments(extractedDoc, recentRecords)

            AssistantIntent.GET_MEDICAL_RECORDS ->
                toolRegistry.getMedicalRecords(recentRecords)

            AssistantIntent.GET_LATEST_REPORT ->
                toolRegistry.getLatestReport(recentRecords)

            AssistantIntent.GET_RECENT_ACTIVITY ->
                toolRegistry.getRecentActivity(recentActivities)

            AssistantIntent.GET_CURRENT_VISIT ->
                toolRegistry.getCurrentVisit(activeVisit)

            AssistantIntent.SEARCH_HOSPITAL_SERVICE ->
                toolRegistry.searchHospitalServices(query)

            AssistantIntent.FIND_DEPARTMENT ->
                toolRegistry.findDepartment(parameters["department"] ?: query)

            AssistantIntent.REQUEST_STAFF_HELP ->
                toolRegistry.requestStaffHelp(parameters["reason"] ?: query, patientProfile.name)

            AssistantIntent.START_VISIT ->
                toolRegistry.startNewVisit()

            AssistantIntent.OPEN_HISTORY ->
                toolRegistry.openHistory()

            AssistantIntent.OPEN_DOCUMENTS ->
                toolRegistry.openDocuments()

            AssistantIntent.OPEN_APPOINTMENTS ->
                toolRegistry.openAppointments()

            AssistantIntent.OPEN_PROFILE ->
                toolRegistry.openProfile()

            AssistantIntent.OPEN_HOME ->
                toolRegistry.openHome()

            AssistantIntent.CHANGE_LANGUAGE -> {
                val langCode = parameters["lang"] ?: "en"
                ToolExecutionResult(
                    toolName = "changeLanguage",
                    success = true,
                    dataSummary = "LANGUAGE_CHANGED: $langCode",
                    spokenDirectFallback = if (langCode == "hi") "भाषा हिन्दी में बदल दी गई है।" else "Language switched to English.",
                    extraData = langCode
                )
            }

            AssistantIntent.TOGGLE_EASY_MODE -> {
                ToolExecutionResult(
                    toolName = "toggleEasyMode",
                    success = true,
                    dataSummary = "EASY_MODE_TOGGLED",
                    spokenDirectFallback = "Easy Mode with larger text and high contrast has been updated."
                )
            }

            else -> {
                ToolExecutionResult(
                    toolName = "none",
                    success = true,
                    dataSummary = "NO_TOOL_EXECUTION",
                    spokenDirectFallback = "I am here to help you navigate MediKiosk. How can I assist you today?"
                )
            }
        }
    }

    /**
     * Gemini Structured JSON Intent Classifier.
     */
    private suspend fun callGeminiIntentClassifier(
        query: String,
        language: Language
    ): IntentClassification? {
        val apiKey = getApiKey()
        if (apiKey.isBlank()) return null

        val systemPrompt = """
            You are the Intent Classification engine for the MediKiosk Hospital Kiosk.
            Your task is to classify the user's speech/text into exactly one intent with strict JSON format.
            
            Allowed Intents:
            - GET_QUEUE (queries about queue token, queue position, wait time, how many people ahead)
            - GET_APPOINTMENT (queries about appointment date, time, doctor)
            - GET_UPCOMING_APPOINTMENTS (upcoming consultations, appointments tomorrow)
            - GET_PATIENT_PROFILE (patient demographics, name, age, ABHA ID)
            - GET_DOCUMENTS (uploaded prescriptions, files)
            - GET_MEDICAL_RECORDS (past consultations, all reports, history)
            - GET_LATEST_REPORT (latest blood test, lab result, lipid report)
            - GET_RECENT_ACTIVITY (recent actions at the kiosk)
            - GET_CURRENT_VISIT (active visit status, room, token)
            - FIND_DEPARTMENT (where is cardiology, lab directions, pharmacy location)
            - REQUEST_STAFF_HELP (help needed, wheelchair, call nurse, assistance)
            - START_VISIT (start a new visit, check in)
            - OPEN_HISTORY (navigate to medical history)
            - OPEN_DOCUMENTS (navigate to document scan screen)
            - OPEN_APPOINTMENTS (navigate to appointments screen)
            - OPEN_PROFILE (navigate to profile screen)
            - OPEN_HOME (navigate to home)
            - MEDICAL_INFORMATION (general non-diagnostic medical questions like What is an ECG, What is an MRI)
            - UNSUPPORTED_MEDICAL_REQUEST (medical emergency, diagnosis request, prescription change)
            - UNKNOWN (ambiguous queries like "show that", unclear input)
            
            Output JSON strictly matching this schema:
            {
              "intent": "INTENT_NAME",
              "confidence": 0.95,
              "requires_patient_data": true,
              "requires_tool": true,
              "parameters": {},
              "clarification_question": null
            }
        """.trimIndent()

        val contentsArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", query)))
            })
        }

        val requestBodyJson = JSONObject().apply {
            put("contents", contentsArray)
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.1)
                put("responseMimeType", "application/json")
            })
        }

        val rawText = executeGenerateContent(apiKey, requestBodyJson) ?: return null

        val clean = rawText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
        val json = JSONObject(clean)

        val intentStr = json.optString("intent", "UNKNOWN")
        val intent = try {
            AssistantIntent.valueOf(intentStr)
        } catch (e: Exception) {
            AssistantIntent.UNKNOWN
        }

        val confidence = json.optDouble("confidence", 0.9)
        val requiresData = json.optBoolean("requires_patient_data", false)
        val requiresTool = json.optBoolean("requires_tool", false)
        val clarification = if (json.has("clarification_question") && !json.isNull("clarification_question")) json.optString("clarification_question") else null

        val params = mutableMapOf<String, String>()
        val paramsJson = json.optJSONObject("parameters")
        paramsJson?.keys()?.forEach { key ->
            params[key] = paramsJson.optString(key)
        }

        return IntentClassification(
            intent = intent,
            confidence = confidence,
            requiresPatientData = requiresData,
            requiresTool = requiresTool,
            parameters = params,
            clarificationQuestion = clarification
        )
    }

    /**
     * Generates a grounded, natural spoken response.
     * If Gemini fails, rate limits, or is offline, falls back immediately to the deterministic verified tool fallback.
     */
    private suspend fun generateFinalResponse(
        query: String,
        intent: AssistantIntent,
        toolResult: ToolExecutionResult?,
        language: Language,
        conversationHistory: List<Pair<String, String>>
    ): String {
        // Special case: Informational queries without tool execution
        if (intent == AssistantIntent.MEDICAL_INFORMATION) {
            val qLower = query.lowercase()
            if (qLower.contains("ecg")) {
                return "An Electrocardiogram, or ECG, is a quick and painless test that records the electrical activity and rhythm of your heart. It helps doctors detect heart conditions."
            }
            if (qLower.contains("mri")) {
                return "An MRI, or Magnetic Resonance Imaging, is a non-invasive scan that uses magnetic fields and radio waves to produce detailed images of your organs and tissues."
            }
            if (qLower.contains("blood pressure") || qLower.contains("bp")) {
                return "Blood pressure is the measure of the force that your blood exerts against the walls of your blood vessels. A typical normal reading is around 120 over 80 mmHg."
            }
        }

        // If tool result provided a direct verified fallback, try Gemini synthesis; if anything fails, use the direct verified fallback
        if (toolResult != null) {
            val apiKey = getApiKey()
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return toolResult.spokenDirectFallback
            }

            try {
                val systemPrompt = """
                    You are MediKiosk AI Voice Assistant at City General Hospital.
                    User Language: ${language.displayName}
                    
                    MANDATORY NON-HALLUCINATION RULES:
                    1. The provided TOOL DATA is the absolute source of truth.
                    2. NEVER invent dates, appointments, doctors, wait times, or test values.
                    3. If the tool data says "NO_APPOINTMENTS_FOUND", say clearly: "I couldn't find an appointment for you."
                    4. If the tool data says "NO_DOCUMENTS_FOUND", say clearly that no documents were found.
                    5. Medical records, lab results, and document names must remain in English even if explaining in Hindi.
                    6. Never diagnose illnesses or change prescriptions.
                    7. Keep your answer concise (1-2 sentences) and natural for text-to-speech.
                    
                    Tool Result: ${toolResult.dataSummary}
                """.trimIndent()

                val contentsArray = JSONArray().apply {
                    for ((role, text) in conversationHistory.takeLast(2)) {
                        put(JSONObject().apply {
                            put("role", if (role == "user") "user" else "model")
                            put("parts", JSONArray().put(JSONObject().put("text", text)))
                        })
                    }
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", query)))
                    })
                }

                val requestBodyJson = JSONObject().apply {
                    put("contents", contentsArray)
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.2)
                        put("maxOutputTokens", 120)
                    })
                }

                val rawText = executeGenerateContent(apiKey, requestBodyJson)
                if (!rawText.isNullOrBlank()) {
                    return rawText.trim()
                }
            } catch (e: Exception) {
                Log.w("VoiceAssistantEngine", "Gemini response generation failed, falling back to direct tool result", e)
            }

            return toolResult.spokenDirectFallback
        }

        // For General, Medical Information, or Search inquiries without a specific tool invocation:
        val apiKey = getApiKey()
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val systemPrompt = """
                    You are MediKiosk AI Hospital Assistant at City General Hospital.
                    Language: ${language.displayName}
                    
                    HOSPITAL INFORMATION & DIRECTORY:
                    - Emergency & Trauma: Ground Floor, Gate A (Open 24/7).
                    - General Medicine: 1st Floor, Room 102 (Dr. Rajesh Sharma, Dr. Sunita Rao).
                    - Cardiology: 2nd Floor, Room 204 (Dr. Arvind Mehta).
                    - Orthopedics: 1st Floor, Room 118 (Dr. Priya Nair).
                    - Pediatrics: Ground Floor, Room 12 (Dr. Vikram Seth).
                    - Pathology & Blood Tests: Ground Floor, Room 08 (Fasting samples: 7:30 AM - 11:30 AM).
                    - Radiology (X-Ray, Ultrasound, CT Scan, MRI): Basement 1, Wing B.
                    - Pharmacy / Chemist: Ground Floor near Main Exit (Open 24/7).
                    - Billing & Insurance Counters: Ground Floor Main Atrium (Counters 1 to 6).
                    - ABHA / Ayushman Bharat Desk: Registration Counter 1.
                    - OPD Hours: 8:00 AM - 8:00 PM (Monday through Saturday).
                    - Payment Options: UPI (Google Pay, PhonePe, Paytm), Debit/Credit Cards, Cash.
                    - Wheelchair Assistance: Available near Main Entrance Gate A or ask any kiosk assistant.
                    - Cafeteria: 3rd Floor East Wing (Drinking water stations on every floor).
                    
                    RESPONSE INSTRUCTIONS:
                    1. Provide a direct, polite, helpful, and meaningful response in 2-3 sentences.
                    2. For symptoms or medical advice inquiries, provide general guidance and recommend consulting the appropriate OPD doctor.
                    3. Respond in ${language.displayName}.
                """.trimIndent()

                val contentsArray = JSONArray().apply {
                    for ((role, text) in conversationHistory.takeLast(2)) {
                        put(JSONObject().apply {
                            put("role", if (role == "user") "user" else "model")
                            put("parts", JSONArray().put(JSONObject().put("text", text)))
                        })
                    }
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().put(JSONObject().put("text", query)))
                    })
                }

                val requestBodyJson = JSONObject().apply {
                    put("contents", contentsArray)
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.4)
                        put("maxOutputTokens", 150)
                    })
                }

                val rawText = executeGenerateContent(apiKey, requestBodyJson)
                if (!rawText.isNullOrBlank()) {
                    return rawText.trim()
                }
            } catch (e: Exception) {
                Log.w("VoiceAssistantEngine", "Gemini general response generation failed, falling back to smart local response", e)
            }
        }

        // Smart Local Knowledge Fallbacks for offline or missing API key
        val qLower = query.lowercase()
        return when {
            qLower.contains("pharmacy") || qLower.contains("medicine") || qLower.contains("chemist") || qLower.contains("dawa") ->
                "The Hospital Pharmacy is located on the Ground Floor near the Main Exit and operates 24/7."
            qLower.contains("time") || qLower.contains("hour") || qLower.contains("timing") || qLower.contains("open") || qLower.contains("close") ->
                "Our OPD operates from 8:00 AM to 8:00 PM, Monday through Saturday. The Emergency Ward is open 24 hours every day."
            qLower.contains("pay") || qLower.contains("billing") || qLower.contains("upi") || qLower.contains("cash") || qLower.contains("card") || qLower.contains("fee") ->
                "You can pay via UPI (GPay/PhonePe), Credit/Debit Cards, or Cash at the Billing Counters on the Ground Floor."
            qLower.contains("wheelchair") || qLower.contains("stretcher") || qLower.contains("help") ->
                "Free wheelchairs and assistance are available at the Main Entrance Gate A. You can also tap 'Request Staff Help' on this kiosk."
            qLower.contains("water") || qLower.contains("canteen") || qLower.contains("food") || qLower.contains("cafeteria") ->
                "Water dispensers are available on every floor next to the elevators. The Hospital Cafeteria is located on the 3rd Floor."
            qLower.contains("blood") || qLower.contains("lab") || qLower.contains("test") || qLower.contains("pathology") ->
                "The Pathology Lab and Blood Collection Center is on the Ground Floor, Room 08. Fasting samples are collected from 7:30 AM to 11:30 AM."
            qLower.contains("x-ray") || qLower.contains("mri") || qLower.contains("ct") || qLower.contains("ultrasound") || qLower.contains("radiology") ->
                "The Radiology Department for X-Ray, CT scans, and MRI is located in Basement 1, Wing B."
            qLower.contains("abha") || qLower.contains("ayushman") || qLower.contains("card") || qLower.contains("insurance") ->
                "You can create or link your ABHA health ID in the Profile section of this kiosk or at Registration Counter 1."
            qLower.contains("doctor") || qLower.contains("fever") || qLower.contains("cough") || qLower.contains("pain") || qLower.contains("ache") ->
                "For general illnesses or consultations, please visit General Medicine on the 1st Floor (Room 102). In case of severe discomfort, visit Emergency on the Ground Floor immediately."
            else ->
                "I can help you check queue tokens, view appointments, navigate departments, review medical records, or answer hospital queries. What would you like assistance with?"
        }
    }

    /**
     * Development debug logging as specified in Section 21.
     */
    private fun logAssistantPipeline(
        input: String,
        intent: AssistantIntent,
        confidence: Double,
        tool: String?,
        status: String,
        latencyMs: Long
    ) {
        if (BuildConfig.DEBUG) {
            Log.d("MediKioskAI", "==================================================")
            Log.d("MediKioskAI", "AI INPUT: \"$input\"")
            Log.d("MediKioskAI", "INTENT: $intent")
            Log.d("MediKioskAI", "CONFIDENCE: $confidence")
            Log.d("MediKioskAI", "TOOL: ${tool ?: "none"}")
            Log.d("MediKioskAI", "RESULT: $status")
            Log.d("MediKioskAI", "LATENCY: ${latencyMs}ms")
            Log.d("MediKioskAI", "==================================================")
        }
    }
}
