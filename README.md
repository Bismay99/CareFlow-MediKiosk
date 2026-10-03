# MediKiosk - AI-Powered Hospital Patient Intake, Queue & Clinical Workflow

MediKiosk is an intelligent, multimodal hospital intake kiosk and clinical workflow application built natively for Android using Kotlin and Jetpack Compose. It is designed for hospital waiting rooms, emergency triage, reception kiosks, patients, staff, doctors, and administrators.

---

## 1. Project Overview

MediKiosk streamlines the hospital arrival journey from entry to consultation:
- **Multilingual Patient Check-In**: Supports English, Hindi, Bengali, Tamil, Telugu, and Marathi with localized speech-to-text, text-to-speech audio guidance, and high-contrast accessibility options.
- **SOCRATES Conversational AI Intake**: Step-by-step clinical triage engine powered by Google Gemini that asks targeted questions (Site, Onset, Character, Radiation, Associations, Time course, Exacerbating/Relieving factors, Severity).
- **Smart Red-Flag Alerting**: Detects critical emergency symptoms (e.g., crushing chest pain, facial drooping/stroke indicators, severe dyspnea) in real-time, immediately alerting waiting room staff.
- **Document & Prescription Scanner**: Multi-format medical document processing that uses OCR and Gemini Vision to extract diagnosis, active medications, dosages, frequencies, and blood test lab values into structured, editable tables.
- **Doctor-Readable SOAP Synthesis**: Synthesizes the conversational history and scanned records into a structured SOAP (Subjective, Objective, Assessment, Plan) report ready for clinician sign-off.
- **Role-Based Workflows**: Tailored views and controls for Patients, Triage Staff, Attending Doctors, and Hospital Administrators.

---

## 2. Architecture & Tech Stack

```
app/
 ├── data/
 │   ├── api/          # GeminiApiService, Gemini Vision & Text Client, Local Mock Fallbacks
 │   ├── model/        # PatientProfile, SocratesIntake, ExtractedDocument, ClinicalSummary, Language
 │   └── repository/   # Data repositories, StateFlow, Room & Local Persistence
 ├── ui/
 │   ├── components/   # IntakeStepProgressHeader, UrgentRedFlagAlertBanner, KioskTopBar, KioskBottomBar, etc.
 │   ├── screens/      # WelcomeScreen, PatientIdentifyScreen, HistoryIntakeScreen, DocumentScanScreen, SummaryScreen
 │   ├── theme/        # Color, Theme, Type (supporting high-contrast & accessible large-print modes)
 │   └── viewmodel/    # KioskViewModel, KioskScreen state machine
 └── MainActivity.kt   # System Edge-to-Edge, BackHandler routing, TopBar & BottomBar scaffold
```

- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose with Material 3
- **State Management**: MVVM with Kotlin Coroutines & StateFlow (`collectAsStateWithLifecycle`)
- **AI Integration**: Google Gemini API (Gemini 2.5 Flash / Flash Lite) for natural-language clinical interviewing, document extraction, and SOAP synthesis
- **Voice & Accessibility**: Android `TextToSpeech` and `SpeechRecognizer` for bidirectional voice interactions
- **Testing**: JUnit4, Robolectric, and Roborazzi visual regression testing

---

## 3. Setup & Android Studio Requirements

1. **Android Studio**: Android Studio Koala Feature Drop (2024.1.2) or Ladybug (2024.2.1)+
2. **JDK**: Java 11 or Java 17
3. **Android SDK**:
   - `compileSdk`: 36 (Android 16 DP)
   - `minSdk`: 24 (Android 7.0 Nougat)
   - `targetSdk`: 36

---

## 4. API Key & Environment Configuration

MediKiosk manages API credentials securely through the Secrets Gradle Plugin:

1. Create a `.env` file in the project root:
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
2. The key is injected automatically into `BuildConfig.GEMINI_API_KEY` at compile time.
3. If no key is supplied, MediKiosk transitions gracefully to its local offline synthesis engine, ensuring zero crashes or disruptions during demonstrations and offline usage.

---

## 5. Firebase & Firestore Configuration

### Collections Schema:
- `/patients/{patientId}`: Patient demographics, ABHA ID, language preference, emergency contacts.
- `/intakeSessions/{sessionId}`: Conversational transcripts, SOCRATES fields, triage priority.
- `/documents/{docId}`: Scanned prescriptions, lab reports, extracted medication lists.
- `/clinicalNotes/{noteId}`: Doctor-approved SOAP notes, differential considerations, orders.
- `/queue/{queueId}`: Real-time token number, department assignment, wait time estimates.
- `/alerts/{alertId}`: Urgent red-flag emergency escalations sent to triage nurses.

---

## 6. AI Safety & Non-Diagnostic Boundary

- MediKiosk is strictly an **administrative and clinical intake assistant**.
- The AI does **NOT** diagnose illnesses, prescribe medication, or formulate treatment regimens autonomously.
- All AI-generated SOAP notes and summaries are prominently labeled as preliminary AI drafts requiring physician review and verification before entering the electronic medical record (EMR).
- Whenever dangerous or life-threatening symptoms are reported, the app displays an immediate red-flag banner and instructs the patient to notify staff or call hospital emergency services directly.

---

## 7. Testing & Verification

Run local JVM Robolectric unit tests:
```bash
gradle :app:testDebugUnitTest
```

Run Roborazzi visual screenshot regression tests:
```bash
gradle :app:verifyRoborazziDebug
```
