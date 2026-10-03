package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.GeminiApiService
import com.example.data.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MediKiosk", appName)
    }

    @Test
    fun `patient profile validation works correctly`() {
        val incomplete = PatientProfile(name = "Rajesh", age = "52", consentGiven = false)
        assertFalse(incomplete.isValid)

        val validNewPatient = PatientProfile(
            name = "Rajesh",
            age = "52",
            isNewPatient = true,
            consentGiven = true
        )
        assertTrue(validNewPatient.isValid)

        val validAbhaPatient = PatientProfile(
            name = "Ramesh",
            age = "45",
            isNewPatient = false,
            abhaId = "91-1234-5678-9012",
            consentGiven = true
        )
        assertTrue(validAbhaPatient.isValid)
    }

    @Test
    fun `socrates intake tracks stages count`() {
        val intake = SocratesIntake()
        assertEquals(0, intake.getCompletedFieldsCount())

        intake.chiefComplaint = "Chest pain and shortness of breath"
        intake.site = "Center chest"
        intake.onset = "Sudden, 2 hours ago"
        assertEquals(3, intake.getCompletedFieldsCount())
    }

    @Test
    fun `local clinical summary synthesis categorizes chest pain as urgent`() {
        val apiService = GeminiApiService()
        val patient = PatientProfile(name = "Amit Kumar", age = "58", gender = "Male")
        val intake = SocratesIntake(
            chiefComplaint = "Severe retrosternal chest pain",
            site = "Chest",
            severity = "8/10"
        )
        val doc = apiService.mockDocumentExtraction()

        val summary = apiService.buildSynthesizedSummaryLocally(patient, intake, doc)
        assertEquals("Urgent", summary.triagePriority)
        assertTrue(summary.recommendedDepartment.contains("Cardiology"))
        assertTrue(summary.historyOfPresentIllness.contains("Amit Kumar"))
    }

    @Test
    fun `red flag symptom detection flags chest pain and stroke symptoms`() {
        val redFlagSymptoms = listOf(
            "I have intense chest pain radiating to my arm",
            "Sudden weakness and face drooping",
            "Severe shortness of breath and coughing blood",
            "Unconscious and seizures"
        )
        val nonUrgent = listOf(
            "Mild headache since yesterday",
            "Skin rash on my knee",
            "Routine checkup for blood sugar"
        )

        redFlagSymptoms.forEach { symptom ->
            val lower = symptom.lowercase()
            val isFlagged = lower.contains("chest pain") ||
                    lower.contains("face drooping") ||
                    lower.contains("shortness of breath") ||
                    lower.contains("unconscious")
            assertTrue("Expected red flag for: $symptom", isFlagged)
        }

        nonUrgent.forEach { symptom ->
            val lower = symptom.lowercase()
            val isFlagged = lower.contains("chest pain") ||
                    lower.contains("face drooping") ||
                    lower.contains("shortness of breath") ||
                    lower.contains("unconscious")
            assertFalse("Expected no red flag for: $symptom", isFlagged)
        }
    }
}
