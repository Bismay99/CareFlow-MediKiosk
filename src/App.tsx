import React, { useState, useEffect, useRef } from 'react';
import {
  Screen,
  Language,
  PatientProfile,
  AuthUser,
  ActiveVisit,
  HospitalAppointment,
  RecentRecordItem,
  RecentActivityItem,
  HospitalNotification,
  QueueItem,
  StaffAssistanceRequest,
  PatientMasterHistory,
  AppThemeMode,
  VoiceAssistantState,
  SocratesIntake,
  ChatMessage,
  ExtractedDocument,
  ClinicalSummary,
  MedicationItem,
  LabValueItem,
  PrescriptionMedicine,
  ConsultationVitals,
} from './types';
import {
  LANGUAGES,
  DEMO_PATIENTS,
  DEMO_ADMIN,
  INITIAL_QUEUES,
  INITIAL_STAFF_REQUESTS,
  INITIAL_PATIENT_HISTORIES,
  HOSPITAL_SERVICES,
} from './data/mockData';
import { speakText, stopSpeaking } from './utils/speech';

// Screen Components
import { RoleSelectionScreen } from './screens/RoleSelectionScreen';
import { UserLoginScreen } from './screens/UserLoginScreen';
import { AdminLoginScreen } from './screens/AdminLoginScreen';
import { AdminDashboardScreen } from './screens/AdminDashboardScreen';
import { PatientHomeScreen } from './screens/PatientHomeScreen';
import { PatientIdentifyScreen } from './screens/PatientIdentifyScreen';
import { HistoryIntakeScreen } from './screens/HistoryIntakeScreen';
import { DocumentScanScreen } from './screens/DocumentScanScreen';
import { SummaryScreen } from './screens/SummaryScreen';
import { AppointmentsScreen } from './screens/AppointmentsScreen';
import { PatientProfileScreen } from './screens/PatientProfileScreen';

// Modals and Bars
import { KioskTopBar } from './components/KioskTopBar';
import { KioskBottomBar } from './components/KioskBottomBar';
import { LanguageDialog } from './components/LanguageDialog';
import { CareFlowEmergencyDialog } from './components/CareFlowEmergencyDialog';
import { VoiceAssistantModal } from './components/VoiceAssistantModal';

export const App: React.FC = () => {
  // Navigation
  const [currentScreen, setCurrentScreen] = useState<Screen>('ROLE_SELECTION');
  const [selectedLanguage, setSelectedLanguage] = useState<Language>(LANGUAGES[0]);

  // Auth & Roles
  const [currentUser, setCurrentUser] = useState<AuthUser | null>(null);

  // Patient Profile
  const [patientProfile, setPatientProfile] = useState<PatientProfile>(DEMO_PATIENTS[0].profile);
  const [activeVisit, setActiveVisit] = useState<ActiveVisit | null>(DEMO_PATIENTS[0].visit);
  const [appointments, setAppointments] = useState<HospitalAppointment[]>(DEMO_PATIENTS[0].appointments);
  const [recentRecords, setRecentRecords] = useState<RecentRecordItem[]>(DEMO_PATIENTS[0].records);
  const [recentActivities, setRecentActivities] = useState<RecentActivityItem[]>(DEMO_PATIENTS[0].activities);
  const [notifications, setNotifications] = useState<HospitalNotification[]>(DEMO_PATIENTS[0].notifications);

  // Hospital Ops
  const [liveQueues, setLiveQueues] = useState<QueueItem[]>(INITIAL_QUEUES);
  const [staffRequests, setStaffRequests] = useState<StaffAssistanceRequest[]>(INITIAL_STAFF_REQUESTS);
  const [patientHistories, setPatientHistories] = useState<PatientMasterHistory[]>(INITIAL_PATIENT_HISTORIES);

  // Display & Accessibility
  const [isAccessibilityMode, setIsAccessibilityMode] = useState<boolean>(false);
  const [themeMode, setThemeMode] = useState<AppThemeMode>('SYSTEM');
  const [isVoiceAssistantEnabled, setIsVoiceAssistantEnabled] = useState<boolean>(true);
  const [isTtsEnabled, setIsTtsEnabled] = useState<boolean>(true);
  const [isVoiceIntakeEnabled, setIsVoiceIntakeEnabled] = useState<boolean>(true);

  // Red Flag Alerts
  const [isRedFlagDetected, setIsRedFlagDetected] = useState<boolean>(false);
  const [redFlagMessage, setRedFlagMessage] = useState<string>('');

  // Emergency SOS
  const [isEmergencyAlertActive, setIsEmergencyAlertActive] = useState<boolean>(false);
  const [activeEmergencyType, setActiveEmergencyType] = useState<string>('Code Red: Rapid Response Dispatched');
  const [showEmergencyDialog, setShowEmergencyDialog] = useState<boolean>(false);
  const [emergencyEtaSeconds, setEmergencyEtaSeconds] = useState<number>(45);

  // Dialogs
  const [showLanguageDialog, setShowLanguageDialog] = useState<boolean>(false);

  // Real AI Voice Assistant State
  const [isVoiceAssistantOpen, setIsVoiceAssistantOpen] = useState<boolean>(false);
  const [voiceAssistantState, setVoiceAssistantState] = useState<VoiceAssistantState>('IDLE');
  const [voiceAssistantStatus, setVoiceAssistantStatus] = useState<string>('Tap the microphone or type below');
  const [voiceAssistantTranscript, setVoiceAssistantTranscript] = useState<string>('');
  const [voiceAssistantResponse, setVoiceAssistantResponse] = useState<string>('');

  // Conversational Intake
  const [socratesIntake, setSocratesIntake] = useState<SocratesIntake>({
    chiefComplaint: '',
    site: '',
    onset: '',
    character: '',
    radiation: '',
    associations: '',
    timing: '',
    exacerbatingRelieving: '',
    severity: '',
    pastMedicalHistory: '',
    currentMedications: '',
    allergies: '',
    familyHistory: '',
  });
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [quickReplies, setQuickReplies] = useState<string[]>([]);
  const [isAiThinking, setIsAiThinking] = useState<boolean>(false);

  // Document Scan & OCR
  const [scannedImageUrl, setScannedImageUrl] = useState<string | null>(null);
  const [isDocumentScanning, setIsDocumentScanning] = useState<boolean>(false);
  const [extractedDocument, setExtractedDocument] = useState<ExtractedDocument>({
    documentType: 'Cardio-Metabolic Rx & Lipid Profile',
    diagnosis: 'Essential Hypertension Stage 1, Hyperlipidemia',
    medications: [
      { name: 'Telmisartan', dosage: '40 mg', frequency: 'OD (Morning)', instructions: 'Take morning after breakfast' },
      { name: 'Atorvastatin', dosage: '10 mg', frequency: 'OD (Bedtime)', instructions: 'Take at bedtime after dinner' },
      { name: 'Aspirin (Ecosprin)', dosage: '75 mg', frequency: 'OD (Post Lunch)', instructions: 'Take strictly after lunch with water' },
    ],
    labValues: [
      { testName: 'Total Cholesterol', value: '228', unit: 'mg/dL', referenceRange: '< 200', status: 'High' },
      { testName: 'LDL Cholesterol', value: '142', unit: 'mg/dL', referenceRange: '< 100', status: 'High' },
      { testName: 'HDL Cholesterol', value: '46', unit: 'mg/dL', referenceRange: '> 40', status: 'Normal' },
      { testName: 'Fasting Blood Sugar', value: '104', unit: 'mg/dL', referenceRange: '70 - 99', status: 'High' },
      { testName: 'Serum Creatinine', value: '0.92', unit: 'mg/dL', referenceRange: '0.7 - 1.2', status: 'Normal' },
    ],
    rawNotes: 'Prior cardiology outpatient prescription from city hospital. Borderline elevated lipids and fasting glucose.',
  });

  // Summary
  const [clinicalSummary, setClinicalSummary] = useState<ClinicalSummary | null>(null);
  const [isGeneratingSummary, setIsGeneratingSummary] = useState<boolean>(false);
  const [isSubmitted, setIsSubmitted] = useState<boolean>(false);

  // Privacy Inactivity Watchdog (180s timeout)
  const [sessionSeconds, setSessionSeconds] = useState<number>(180);

  useEffect(() => {
    const handleActivity = () => {
      setSessionSeconds(180);
    };

    window.addEventListener('click', handleActivity);
    window.addEventListener('keydown', handleActivity);
    window.addEventListener('touchstart', handleActivity);

    const interval = setInterval(() => {
      if (
        currentScreen !== 'ROLE_SELECTION' &&
        currentScreen !== 'USER_LOGIN' &&
        currentScreen !== 'ADMIN_LOGIN'
      ) {
        setSessionSeconds((prev) => {
          if (prev <= 1) {
            logout();
            return 180;
          }
          return prev - 1;
        });
      }
    }, 1000);

    return () => {
      clearInterval(interval);
      window.removeEventListener('click', handleActivity);
      window.removeEventListener('keydown', handleActivity);
      window.removeEventListener('touchstart', handleActivity);
    };
  }, [currentScreen]);

  // Navigate helper
  const navigateTo = (screen: Screen) => {
    stopSpeaking();
    setCurrentScreen(screen);
    if (screen === 'CONVERSATIONAL_INTAKE' && chatMessages.length === 0) {
      startInitialChat();
    } else if (screen === 'SUMMARY' && !clinicalSummary) {
      generateSummary();
    }
  };

  // Auth Handlers
  const loginPatient = (identifier: string, _pass: string) => {
    const clean = identifier.toLowerCase().trim();
    const matched = DEMO_PATIENTS.find(
      (d) =>
        d.user.email.toLowerCase() === clean ||
        d.profile.name.toLowerCase().includes(clean)
    );
    if (matched) {
      applyDemoPatient(matched);
    } else {
      setPatientProfile({
        name: identifier.split('@')[0].replace(/[._]/g, ' '),
        age: '45',
        gender: 'Male',
        isNewPatient: false,
        abhaId: `14-${Math.floor(1000 + Math.random() * 9000)}-${Math.floor(1000 + Math.random() * 9000)}-${Math.floor(1000 + Math.random() * 9000)}`,
        consentGiven: true,
      });
      setCurrentUser({
        id: `pt-${Date.now()}`,
        name: identifier.split('@')[0],
        email: identifier,
        role: 'PATIENT',
        phone: '+91 98000 12345',
        abhaId: '14-2951-8402-1920',
        isDemoUser: false,
      });
    }
    navigateTo('WELCOME');
  };

  const loginAdmin = (_email: string, _pass: string, _pin: string) => {
    setCurrentUser(DEMO_ADMIN);
    navigateTo('ADMIN_DASHBOARD');
  };

  const loginDemoPatient = (index: number) => {
    const demo = DEMO_PATIENTS[index] || DEMO_PATIENTS[0];
    applyDemoPatient(demo);
    navigateTo('WELCOME');
  };

  const loginDemoAdmin = () => {
    setCurrentUser(DEMO_ADMIN);
    navigateTo('ADMIN_DASHBOARD');
  };

  const applyDemoPatient = (demo: typeof DEMO_PATIENTS[0]) => {
    setPatientProfile(demo.profile);
    setCurrentUser(demo.user);
    setActiveVisit(demo.visit);
    setAppointments(demo.appointments);
    setRecentRecords(demo.records);
    setRecentActivities(demo.activities);
    setNotifications(demo.notifications);
  };

  const logout = () => {
    stopSpeaking();
    setCurrentUser(null);
    setCurrentScreen('ROLE_SELECTION');
  };

  // Red Flag Clinical Symptom Evaluator
  const checkForRedFlags = (text: string) => {
    const lower = text.toLowerCase();
    const hasChestPain =
      lower.includes('chest pain') ||
      lower.includes('chest pressure') ||
      lower.includes('chest tightness') ||
      lower.includes('heart pain') ||
      lower.includes('सीने में दर्द');

    const hasBreathlessness =
      lower.includes('breathless') ||
      lower.includes('short of breath') ||
      lower.includes('shortness of breath') ||
      lower.includes('difficulty breathing') ||
      lower.includes('sweat') ||
      lower.includes('सांस फूलना');

    const hasStroke =
      lower.includes('stroke') ||
      lower.includes('facial droop') ||
      lower.includes('slurred speech') ||
      lower.includes('paralysis') ||
      lower.includes('arm weakness');

    const hasCollapse =
      lower.includes('passed out') ||
      lower.includes('unconscious') ||
      lower.includes('collapsed') ||
      lower.includes('severe bleeding');

    if (hasChestPain && hasBreathlessness) {
      setIsRedFlagDetected(true);
      setRedFlagMessage(
        'CRITICAL RED-FLAG ALERT: Combination of Chest Pain and Shortness of Breath detected! Rapid Response medical team requested. Do NOT wait in the routine queue.'
      );
      triggerEmergencySos('Code Red: Acute Cardiopulmonary Distress');
    } else if (hasStroke) {
      setIsRedFlagDetected(true);
      setRedFlagMessage(
        'CRITICAL RED-FLAG ALERT: Potential acute stroke-like signs detected! Triage nurses notified immediately.'
      );
      triggerEmergencySos('Code Red: Stroke Assessment');
    } else if (hasCollapse) {
      setIsRedFlagDetected(true);
      setRedFlagMessage(
        'URGENT CLINICAL ALERT: Severe high-risk acute emergency reported. Priority team alerted.'
      );
      triggerEmergencySos('Code Red: Emergency Rescue');
    }
  };

  // Emergency SOS Actions
  const triggerEmergencySos = (type: string = 'Code Red: Rapid Response Dispatched') => {
    setIsEmergencyAlertActive(true);
    setActiveEmergencyType(type);
    setEmergencyEtaSeconds(45);

    const sosNotif: HospitalNotification = {
      id: `sos-${Date.now()}`,
      title: '🚨 EMERGENCY CODE ACTIVATED',
      message: `${type} at Kiosk 01 (Main Atrium). Rapid response medical team dispatched.`,
      timestamp: 'Just now',
      isUrgent: true,
      isRead: false,
    };
    setNotifications((prev) => [sosNotif, ...prev]);

    // Add to staff desk requests
    const newReq: StaffAssistanceRequest = {
      id: `req-${Date.now()}`,
      patientName: patientProfile.name || 'Patient in Distress',
      location: 'Kiosk 01 (Main Atrium)',
      reason: `CRITICAL EMERGENCY SOS: ${type}`,
      timestamp: 'Just now',
      isUrgent: true,
      status: 'DISPATCHED',
      assignedStaff: 'Rapid Response Team (Code Red)',
    };
    setStaffRequests((prev) => [newReq, ...prev]);

    speakText(
      'Emergency alert activated. The hospital rapid response medical team is dispatched to your kiosk location immediately.',
      selectedLanguage,
      isTtsEnabled
    );
  };

  const cancelEmergencySos = () => {
    setIsEmergencyAlertActive(false);
    speakText('Emergency alert has been cancelled.', selectedLanguage, isTtsEnabled);
  };

  const alertHospitalStaff = () => {
    speakText('Urgent assistance requested. Hospital triage staff have been notified.', selectedLanguage, isTtsEnabled);
  };

  const requestHospitalHelp = () => {
    const newReq: StaffAssistanceRequest = {
      id: `req-${Date.now()}`,
      patientName: patientProfile.name || 'Kiosk Patient',
      location: 'Kiosk 01 (Main Atrium)',
      reason: 'In-person assistance requested from kiosk home screen',
      timestamp: 'Just now',
      isUrgent: false,
      status: 'PENDING',
    };
    setStaffRequests((prev) => [newReq, ...prev]);

    const notif: HospitalNotification = {
      id: `notif-${Date.now()}`,
      title: 'Staff Assistance Requested',
      message: 'A patient care coordinator is on the way to assist you at this kiosk.',
      timestamp: 'Just now',
      isUrgent: true,
      isRead: false,
    };
    setNotifications((prev) => [notif, ...prev]);

    speakText('Hospital staff have been notified. A patient care representative will assist you shortly.', selectedLanguage, isTtsEnabled);
  };

  // Conversational Intake Chat Logic
  const startInitialChat = async () => {
    setIsAiThinking(true);
    try {
      const res = await fetch('/api/chat-turn', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          patientName: patientProfile.name || 'Patient',
          patientAge: patientProfile.age || '54',
          patientGender: patientProfile.gender,
          intake: socratesIntake,
          language: selectedLanguage,
          chatHistory: [],
          latestUserReply: '',
        }),
      });
      const data = await res.json();
      setChatMessages([
        {
          id: `ai-${Date.now()}`,
          sender: 'AI',
          text: data.aiResponseText,
          quickReplies: data.quickReplies,
          timestamp: Date.now(),
        },
      ]);
      setQuickReplies(data.quickReplies || []);
    } catch {
      // Deterministic fallback
      setChatMessages([
        {
          id: `ai-${Date.now()}`,
          sender: 'AI',
          text: 'Hello! What primary symptom or discomfort brings you to the hospital today?',
          quickReplies: ['Chest Pain / Pressure', 'Fever & Cough', 'Severe Stomach Ache', 'Headache & Dizziness'],
          timestamp: Date.now(),
        },
      ]);
      setQuickReplies(['Chest Pain / Pressure', 'Fever & Cough', 'Severe Stomach Ache', 'Headache & Dizziness']);
    } finally {
      setIsAiThinking(false);
    }
  };

  const sendChatMessage = async (userText: string) => {
    if (!userText.trim() || isAiThinking) return;

    checkForRedFlags(userText);

    const userMsg: ChatMessage = {
      id: `user-${Date.now()}`,
      sender: 'PATIENT',
      text: userText,
      timestamp: Date.now(),
    };

    const updatedHistory = [...chatMessages, userMsg];
    setChatMessages(updatedHistory);
    setIsAiThinking(true);
    setQuickReplies([]);

    try {
      const res = await fetch('/api/chat-turn', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          patientName: patientProfile.name || 'Patient',
          patientAge: patientProfile.age || '54',
          patientGender: patientProfile.gender,
          intake: socratesIntake,
          language: selectedLanguage,
          chatHistory: updatedHistory,
          latestUserReply: userText,
        }),
      });
      const data = await res.json();

      if (data.extractedField && data.extractedValue) {
        setSocratesIntake((prev) => ({
          ...prev,
          [data.extractedField]: data.extractedValue,
        }));
      }

      const aiMsg: ChatMessage = {
        id: `ai-${Date.now()}`,
        sender: 'AI',
        text: data.aiResponseText,
        quickReplies: data.quickReplies,
        timestamp: Date.now(),
      };
      setChatMessages([...updatedHistory, aiMsg]);
      setQuickReplies(data.quickReplies || []);

      if (isTtsEnabled) {
        speakText(data.aiResponseText, selectedLanguage);
      }
    } catch {
      // Fallback response
      const fallbackAiMsg: ChatMessage = {
        id: `ai-${Date.now()}`,
        sender: 'AI',
        text: 'Thank you for sharing that. Where exactly in your body is this discomfort situated?',
        quickReplies: ['Chest / Center', 'Upper Abdomen', 'Head / Forehead', 'Joints / Legs'],
        timestamp: Date.now(),
      };
      setChatMessages([...updatedHistory, fallbackAiMsg]);
      setQuickReplies(['Chest / Center', 'Upper Abdomen', 'Head / Forehead', 'Joints / Legs']);
    } finally {
      setIsAiThinking(false);
    }
  };

  // OCR Document Analysis
  const handleImageCaptured = async (dataUrl: string) => {
    setScannedImageUrl(dataUrl);
    setIsDocumentScanning(true);

    try {
      const base64 = dataUrl.split(',')[1] || dataUrl;
      const res = await fetch('/api/analyze-document', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ imageBase64: base64 }),
      });
      const data = await res.json();
      setExtractedDocument(data);
    } catch {
      // Sample fallback already populated
    } finally {
      setIsDocumentScanning(false);
    }
  };

  const handleLoadSampleDocument = () => {
    // Generate clean canvas prescription preview
    const canvas = document.createElement('canvas');
    canvas.width = 600;
    canvas.height = 700;
    const ctx = canvas.getContext('2d');
    if (ctx) {
      ctx.fillStyle = '#ffffff';
      ctx.fillRect(0, 0, 600, 700);

      ctx.fillStyle = '#0369a1';
      ctx.font = 'bold 22px sans-serif';
      ctx.fillText('CITY GENERAL HOSPITAL - OUTPATIENT Rx', 30, 45);

      ctx.fillStyle = '#64748b';
      ctx.font = '14px sans-serif';
      ctx.fillText('Department of Cardiology & Internal Medicine', 30, 75);

      ctx.strokeStyle = '#e2e8f0';
      ctx.beginPath();
      ctx.moveTo(30, 95);
      ctx.lineTo(570, 95);
      ctx.stroke();

      ctx.fillStyle = '#0f172a';
      ctx.font = '15px sans-serif';
      ctx.fillText(`Patient: ${patientProfile.name} | Age: ${patientProfile.age} | Date: Today`, 30, 125);
      ctx.fillText('Diagnosis: Essential Hypertension Stage 1, Hyperlipidemia', 30, 155);

      ctx.fillStyle = '#0369a1';
      ctx.font = 'bold 16px sans-serif';
      ctx.fillText('Rx (Prescriptions):', 30, 195);

      ctx.fillStyle = '#1e293b';
      ctx.font = '14px sans-serif';
      ctx.fillText('1. Tab. Telmisartan 40mg - 1 Tab OD (Morning after breakfast)', 40, 225);
      ctx.fillText('2. Tab. Atorvastatin 10mg - 1 Tab HS (At bedtime after dinner)', 40, 255);
      ctx.fillText('3. Tab. Ecosprin 75mg - 1 Tab OD (After lunch with water)', 40, 285);

      ctx.fillStyle = '#0369a1';
      ctx.font = 'bold 16px sans-serif';
      ctx.fillText('Recent Apollo Lab Profile:', 30, 330);

      ctx.fillStyle = '#1e293b';
      ctx.font = '14px sans-serif';
      ctx.fillText('• Total Cholesterol: 228 mg/dL [High] (Ref: < 200)', 40, 360);
      ctx.fillText('• Fasting Blood Sugar: 104 mg/dL [High] (Ref: 70 - 99)', 40, 390);
      ctx.fillText('• Serum Creatinine: 0.92 mg/dL [Normal] (Ref: 0.7 - 1.2)', 40, 420);

      ctx.fillStyle = '#64748b';
      ctx.font = '13px sans-serif';
      ctx.fillText('Advice: Low sodium diet, 30 min daily brisk walking.', 30, 480);
      ctx.fillText('Dr. Rajesh Mehta, MD - Reg #KMC-49210', 320, 520);
    }
    const sampleDataUrl = canvas.toDataURL('image/png');
    handleImageCaptured(sampleDataUrl);
  };

  // Generate Clinical Summary
  const generateSummary = async () => {
    setIsGeneratingSummary(true);
    try {
      const res = await fetch('/api/generate-summary', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          patient: patientProfile,
          intake: socratesIntake,
          doc: extractedDocument,
        }),
      });
      const data = await res.json();
      setClinicalSummary(data);
    } catch {
      // Local fallback summary
      setClinicalSummary({
        chiefComplaint: socratesIntake.chiefComplaint || 'Occasional morning occipital headache and fatigue',
        historyOfPresentIllness: `Patient ${patientProfile.name}, a ${patientProfile.age}-year-old ${patientProfile.gender.toLowerCase()}, presents with symptoms described in triage.`,
        pastMedicalHistory: socratesIntake.pastMedicalHistory || 'Stage 1 Essential Hypertension, Hyperlipidemia.',
        drugAndAllergyHistory: 'Telmisartan 40mg OD, Atorvastatin 10mg HS, Ecosprin 75mg OD. Allergies: Penicillin.',
        familyHistory: 'Father had ischemic heart disease at age 62.',
        reviewOfSystems: 'No acute chest tightness at rest.',
        priorInvestigations: 'Total Cholesterol 228 mg/dL [High], Fasting Glucose 104 mg/dL [High].',
        triagePriority: 'Moderate',
        triageReason: 'Hypertensive monitoring and routine OPD review indicated.',
        recommendedDepartment: 'General Medicine',
        tokenNumber: 'MK-104',
        estimatedWaitMinutes: 14,
      });
    } finally {
      setIsGeneratingSummary(false);
    }
  };

  // Voice Assistant Queries
  const processVoiceAssistantQuery = async (query: string) => {
    setVoiceAssistantState('PROCESSING');
    setVoiceAssistantStatus('Thinking...');
    setVoiceAssistantTranscript(query);

    try {
      const res = await fetch('/api/voice-assistant', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          query,
          patientProfile,
          activeVisit,
          appointments,
          recentRecords,
          language: selectedLanguage,
        }),
      });
      const data = await res.json();

      setVoiceAssistantResponse(data.spokenResponse);
      setVoiceAssistantState('SPEAKING');
      setVoiceAssistantStatus('Ready');

      if (data.isEmergency) {
        triggerEmergencySos('Code Red via Voice Alert');
      }

      if (data.navigationTarget) {
        if (data.navigationTarget === 'DOCUMENTS') navigateTo('DOCUMENT_SCAN');
        else if (data.navigationTarget === 'APPOINTMENTS') navigateTo('APPOINTMENTS');
        else if (data.navigationTarget === 'PROFILE') navigateTo('PROFILE');
        else if (data.navigationTarget === 'HISTORY') navigateTo('CONVERSATIONAL_INTAKE');
        else if (data.navigationTarget === 'HOME') navigateTo('WELCOME');
      }

      if (isTtsEnabled) {
        speakText(data.spokenResponse, selectedLanguage);
      }
    } catch {
      const fallback = `You can ask me about your queue position, room directions, or book doctor appointments.`;
      setVoiceAssistantResponse(fallback);
      setVoiceAssistantState('SPEAKING');
      setVoiceAssistantStatus('Ready');
      if (isTtsEnabled) speakText(fallback, selectedLanguage);
    }
  };

  // Appointments Management
  const bookNewAppointment = (doctor: string, dept: string, date: string, room: string) => {
    const newApt: HospitalAppointment = {
      id: `apt-${Date.now()}`,
      patientId: currentUser?.id || 'pt-001',
      doctorName: doctor,
      specialty: dept,
      department: dept,
      date,
      time: date.includes(',') ? date.split(',')[1].trim() : '10:30 AM',
      room,
      status: 'Confirmed',
      visitType: 'In-Person',
    };
    setAppointments([newApt, ...appointments]);
    speakText(`Appointment with ${doctor} confirmed for ${date} in ${room}.`, selectedLanguage, isTtsEnabled);
  };

  // Admin Master EHR Consultation Recorder
  const recordPatientConsultation = (
    patientId: string,
    patientName: string,
    age: string,
    gender: string,
    abhaId: string,
    phone: string,
    doctorName: string,
    doctorSpecialty: string,
    department: string,
    consultationDateTime: string,
    roomNumber: string,
    chiefComplaint: string,
    diagnosis: string,
    clinicalNotes: string,
    vitals: ConsultationVitals,
    medicines: PrescriptionMedicine[],
    labInvestigations: string[],
    followUpDate: string,
    doctorAdvice: string,
    bloodGroup: string,
    allergies: string[],
    chronicConditions: string[]
  ) => {
    const entry = {
      id: `consult-${Date.now()}`,
      patientId,
      patientName,
      doctorName,
      doctorSpecialty,
      department,
      consultationDateTime,
      roomNumber,
      chiefComplaint,
      diagnosis,
      clinicalNotes,
      vitals,
      medicines,
      labInvestigations,
      followUpDate,
      doctorAdvice,
      isRecordedByAdmin: true,
    };

    setPatientHistories((prev) => {
      const copy = [...prev];
      const idx = copy.findIndex((p) => p.patientId === patientId || p.patientName === patientName);
      if (idx >= 0) {
        copy[idx] = {
          ...copy[idx],
          consultations: [entry, ...copy[idx].consultations],
        };
      } else {
        copy.unshift({
          patientId,
          patientName,
          age,
          gender,
          abhaId,
          phone,
          bloodGroup,
          allergies,
          chronicConditions,
          emergencyContact: '+91 98201 44521',
          consultations: [entry],
        });
      }
      return copy;
    });

    // Notify patient
    const notif: HospitalNotification = {
      id: `notif-${Date.now()}`,
      title: `EHR Consultation Signed: ${patientName}`,
      message: `Dr. ${doctorName} completed consultation notes & added ${medicines.length} prescriptions.`,
      timestamp: 'Just now',
      isUrgent: false,
      isRead: false,
    };
    setNotifications((prev) => [notif, ...prev]);
  };

  const updateMedicineStatus = (patientId: string, medId: string, status: 'Active' | 'Completed' | 'Discontinued') => {
    setPatientHistories((prev) =>
      prev.map((patient) => {
        if (patient.patientId === patientId) {
          return {
            ...patient,
            consultations: patient.consultations.map((c) => ({
              ...c,
              medicines: c.medicines.map((m) => (m.id === medId ? { ...m, status } : m)),
            })),
          };
        }
        return patient;
      })
    );
  };

  const speakPrescriptionSchedule = (patientName: string, meds: PrescriptionMedicine[]) => {
    const active = meds.filter((m) => m.status === 'Active');
    if (active.length === 0) {
      speakText(`No active prescriptions recorded for ${patientName}.`, selectedLanguage, isTtsEnabled);
      return;
    }
    const text = `Prescription schedule for ${patientName}: ` +
      active.map((m, i) => `${i + 1}: ${m.medicineName}, ${m.dosage}. Frequency: ${m.frequency.replace(/_/g, ' ')}. Take: ${m.foodTiming.replace(/_/g, ' ')}.`).join(' ');
    speakText(text, selectedLanguage, isTtsEnabled);
  };

  const resetForNewPatient = () => {
    stopSpeaking();
    setSocratesIntake({
      chiefComplaint: '',
      site: '',
      onset: '',
      character: '',
      radiation: '',
      associations: '',
      timing: '',
      exacerbatingRelieving: '',
      severity: '',
      pastMedicalHistory: '',
      currentMedications: '',
      allergies: '',
      familyHistory: '',
    });
    setChatMessages([]);
    setQuickReplies([]);
    setScannedImageUrl(null);
    setClinicalSummary(null);
    setIsSubmitted(false);
    setIsRedFlagDetected(false);
    setPatientProfile({
      name: '',
      age: '40',
      gender: 'Male',
      isNewPatient: true,
      abhaId: '',
      consentGiven: false,
    });
    navigateTo('WELCOME');
  };

  // Top Bar & Bottom Bar visibility
  const showTopBar = ['IDENTIFY', 'CONVERSATIONAL_INTAKE', 'DOCUMENT_SCAN', 'SUMMARY'].includes(currentScreen);
  const showBottomBar = ['WELCOME', 'DOCUMENT_SCAN', 'APPOINTMENTS', 'PROFILE'].includes(currentScreen);

  return (
    <div className={`min-h-screen ${themeMode === 'DARK' ? 'dark bg-slate-900 text-slate-100' : 'bg-slate-50 text-slate-900'}`}>
      {/* Top Bar on intake screens */}
      {showTopBar && (
        <KioskTopBar
          currentScreen={currentScreen}
          selectedLanguage={selectedLanguage}
          isAccessibilityMode={isAccessibilityMode}
          onToggleAccessibility={() => setIsAccessibilityMode(!isAccessibilityMode)}
          onBackClick={() => {
            if (currentScreen === 'IDENTIFY') navigateTo('WELCOME');
            else if (currentScreen === 'CONVERSATIONAL_INTAKE') navigateTo('IDENTIFY');
            else if (currentScreen === 'DOCUMENT_SCAN') navigateTo('CONVERSATIONAL_INTAKE');
            else if (currentScreen === 'SUMMARY') navigateTo('DOCUMENT_SCAN');
          }}
          onLanguageClick={() => setShowLanguageDialog(true)}
          onEmergencySosClick={() => setShowEmergencyDialog(true)}
        />
      )}

      {/* Screen Router */}
      <main className="w-full">
        {currentScreen === 'ROLE_SELECTION' && (
          <RoleSelectionScreen
            selectedLanguage={selectedLanguage}
            onSelectPatientRole={() => navigateTo('USER_LOGIN')}
            onSelectAdminRole={() => navigateTo('ADMIN_LOGIN')}
            onQuickDemoLaunch={() => loginDemoPatient(0)}
            onLanguageClick={() => setShowLanguageDialog(true)}
          />
        )}

        {currentScreen === 'USER_LOGIN' && (
          <UserLoginScreen
            demoPatients={DEMO_PATIENTS}
            onLoginClick={loginPatient}
            onSelectDemoPatient={loginDemoPatient}
            onBackClick={() => navigateTo('ROLE_SELECTION')}
          />
        )}

        {currentScreen === 'ADMIN_LOGIN' && (
          <AdminLoginScreen
            onAdminLoginClick={loginAdmin}
            onQuickAdminFill={loginDemoAdmin}
            onBackClick={() => navigateTo('ROLE_SELECTION')}
          />
        )}

        {currentScreen === 'ADMIN_DASHBOARD' && (
          <AdminDashboardScreen
            adminUser={currentUser}
            staffRequests={staffRequests}
            liveQueues={liveQueues}
            patientHistories={patientHistories}
            onCallNextPatient={(dept) => {
              setLiveQueues((prev) => {
                const copy = [...prev];
                const inConsultIdx = copy.findIndex((q) => q.department.includes(dept) && q.status === 'In Consultation');
                if (inConsultIdx >= 0) copy[inConsultIdx] = { ...copy[inConsultIdx], status: 'Completed' };
                const nextIdx = copy.findIndex((q) => q.department.includes(dept) && (q.status === 'Waiting' || q.status === 'Near Turn'));
                if (nextIdx >= 0) copy[nextIdx] = { ...copy[nextIdx], status: 'Calling Now' };
                return copy;
              });
            }}
            onMarkInConsultation={(token) => {
              setLiveQueues((prev) =>
                prev.map((q) => (q.token === token ? { ...q, status: 'In Consultation' } : q))
              );
            }}
            onDispatchStaff={(reqId) => {
              setStaffRequests((prev) =>
                prev.map((r) => (r.id === reqId ? { ...r, status: 'DISPATCHED', assignedStaff: 'Nurse On-Duty' } : r))
              );
            }}
            onResolveStaffRequest={(reqId) => {
              setStaffRequests((prev) =>
                prev.map((r) => (r.id === reqId ? { ...r, status: 'RESOLVED' } : r))
              );
            }}
            onRecordConsultation={recordPatientConsultation}
            onAddMedicineToConsultation={() => {}}
            onUpdateMedicineStatus={updateMedicineStatus}
            onSpeakPrescription={speakPrescriptionSchedule}
            onLogoutClick={logout}
          />
        )}

        {currentScreen === 'WELCOME' && (
          <PatientHomeScreen
            patientProfile={patientProfile}
            selectedLanguage={selectedLanguage}
            activeVisit={activeVisit}
            healthInsights={[
              { id: 'hi-1', title: 'Upcoming Cardiology Consultation', description: 'Your scheduled follow-up with Dr. V. Rao is set for tomorrow at 10:30 AM in Room 204.', tag: 'AI-assisted', iconType: 'calendar' },
              { id: 'hi-2', title: 'Diagnostic Lab Profile Ready', description: 'Your fasting lipid and blood sugar panel results are verified by city pathology.', tag: 'AI-assisted', iconType: 'document' },
              { id: 'hi-3', title: 'Inpatient Check-in Advisory', description: 'Remember to present your ABHA health ID card and previous outpatient prescription slips.', tag: 'AI-assisted', iconType: 'info' },
            ]}
            recentlyViewed={[
              { id: 'rv-1', type: 'Medical Report', title: 'Lipid Panel & Fasting Glucose', dateOrStatus: '02 Sep 2026 • Verified', iconType: 'report' },
              { id: 'rv-2', type: 'Appointment', title: 'Cardiology with Dr. V. Rao', dateOrStatus: 'Tomorrow 10:30 AM • Confirmed', iconType: 'appointment' },
              { id: 'rv-3', type: 'Prescription', title: 'Outpatient Rx #KMC-49210', dateOrStatus: '12 Aug 2026 • Active Rx', iconType: 'prescription' },
            ]}
            recentRecords={recentRecords}
            recentActivities={recentActivities}
            notifications={notifications}
            isAccessibilityMode={isAccessibilityMode}
            themeMode={themeMode}
            isRedFlagDetected={isRedFlagDetected}
            redFlagMessage={redFlagMessage}
            isEmergencyAlertActive={isEmergencyAlertActive}
            activeEmergencyType={activeEmergencyType}
            onNavigate={navigateTo}
            onToggleAccessibility={() => setIsAccessibilityMode(!isAccessibilityMode)}
            onToggleTheme={() => setThemeMode(themeMode === 'DARK' ? 'LIGHT' : 'DARK')}
            onOpenLanguageDialog={() => setShowLanguageDialog(true)}
            onOpenEmergencyCenter={() => setShowEmergencyDialog(true)}
            onTriggerSos={triggerEmergencySos}
            onOpenVoiceAssistant={(q) => {
              setIsVoiceAssistantOpen(true);
              if (q) processVoiceAssistantQuery(q);
            }}
            onRequestStaffHelp={requestHospitalHelp}
            onDismissRedFlag={() => setIsRedFlagDetected(false)}
            onAlertStaff={alertHospitalStaff}
            onToggleActiveVisit={() => {
              if (activeVisit?.hasActiveVisit) {
                setActiveVisit({ ...activeVisit, hasActiveVisit: false });
                speakText('You have checked out of your active visit.', selectedLanguage, isTtsEnabled);
              } else {
                setActiveVisit(DEMO_PATIENTS[0].visit);
                speakText('Visit started. Your token number is 24 in General Medicine, Room 104.', selectedLanguage, isTtsEnabled);
              }
            }}
            onMarkNotificationRead={(id) => {
              setNotifications((prev) =>
                prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
              );
            }}
            onBookServiceAction={(service, detail) => {
              const notif: HospitalNotification = {
                id: `svc-${Date.now()}`,
                title: `${service} Booked`,
                message: `Your booking request has been confirmed. ${detail || 'Hospital desk notified.'}`,
                timestamp: 'Just now',
                isUrgent: false,
                isRead: false,
              };
              setNotifications((prev) => [notif, ...prev]);
              speakText(`${service} has been booked successfully.`, selectedLanguage, isTtsEnabled);
            }}
          />
        )}

        {currentScreen === 'IDENTIFY' && (
          <PatientIdentifyScreen
            profile={patientProfile}
            selectedLanguage={selectedLanguage}
            onNameChange={(name) => setPatientProfile({ ...patientProfile, name })}
            onAgeChange={(age) => setPatientProfile({ ...patientProfile, age })}
            onGenderChange={(gender) => setPatientProfile({ ...patientProfile, gender })}
            onAbhaChange={(abhaId) => setPatientProfile({ ...patientProfile, abhaId })}
            onNewPatientToggle={(isNewPatient) => setPatientProfile({ ...patientProfile, isNewPatient })}
            onConsentToggle={(consentGiven) => setPatientProfile({ ...patientProfile, consentGiven })}
            onReadConsentAloud={() => speakText(selectedLanguage.consentText, selectedLanguage, isTtsEnabled)}
            onProceedClick={() => navigateTo('CONVERSATIONAL_INTAKE')}
          />
        )}

        {currentScreen === 'CONVERSATIONAL_INTAKE' && (
          <HistoryIntakeScreen
            chatMessages={chatMessages}
            quickReplies={quickReplies}
            isAiThinking={isAiThinking}
            socratesIntake={socratesIntake}
            selectedLanguage={selectedLanguage}
            isRedFlagDetected={isRedFlagDetected}
            redFlagMessage={redFlagMessage}
            onAlertStaff={alertHospitalStaff}
            onDismissRedFlag={() => setIsRedFlagDetected(false)}
            onSendMessage={sendChatMessage}
            onReadAloud={(txt) => speakText(txt, selectedLanguage, isTtsEnabled)}
            onProceedToDocumentScan={() => navigateTo('DOCUMENT_SCAN')}
          />
        )}

        {currentScreen === 'DOCUMENT_SCAN' && (
          <DocumentScanScreen
            scannedImageUrl={scannedImageUrl}
            isScanning={isDocumentScanning}
            extractedDoc={extractedDocument}
            onImageCaptured={handleImageCaptured}
            onLoadSampleDocument={handleLoadSampleDocument}
            onUpdateDiagnosis={(diagnosis) => setExtractedDocument({ ...extractedDocument, diagnosis })}
            onAddMedication={(med) => setExtractedDocument({ ...extractedDocument, medications: [...extractedDocument.medications, med] })}
            onUpdateMedication={(idx, med) => {
              const copy = [...extractedDocument.medications];
              copy[idx] = med;
              setExtractedDocument({ ...extractedDocument, medications: copy });
            }}
            onDeleteMedication={(idx) => {
              setExtractedDocument({ ...extractedDocument, medications: extractedDocument.medications.filter((_, i) => i !== idx) });
            }}
            onAddLabValue={(lab) => setExtractedDocument({ ...extractedDocument, labValues: [...extractedDocument.labValues, lab] })}
            onUpdateLabValue={(idx, lab) => {
              const copy = [...extractedDocument.labValues];
              copy[idx] = lab;
              setExtractedDocument({ ...extractedDocument, labValues: copy });
            }}
            onDeleteLabValue={(idx) => {
              setExtractedDocument({ ...extractedDocument, labValues: extractedDocument.labValues.filter((_, i) => i !== idx) });
            }}
            onProceedToSummary={() => navigateTo('SUMMARY')}
          />
        )}

        {currentScreen === 'SUMMARY' && (
          <SummaryScreen
            patient={patientProfile}
            summary={clinicalSummary}
            isGenerating={isGeneratingSummary}
            isSubmitted={isSubmitted}
            onEditSection={(section, content) => {
              if (!clinicalSummary) return;
              const keyMap: Record<string, keyof ClinicalSummary> = {
                'Chief Complaint': 'chiefComplaint',
                'History of Present Illness': 'historyOfPresentIllness',
                'Past Medical/Surgical History': 'pastMedicalHistory',
                'Drug & Allergy History': 'drugAndAllergyHistory',
                'Family History': 'familyHistory',
                'Review of Systems': 'reviewOfSystems',
                'Prior Investigations': 'priorInvestigations',
              };
              const k = keyMap[section];
              if (k) {
                setClinicalSummary({ ...clinicalSummary, [k]: content });
              }
            }}
            onSubmitClick={() => {
              setIsSubmitted(true);
              speakText('Intake submitted. Your hospital visit token is ready.', selectedLanguage, isTtsEnabled);
            }}
            onResetForNewPatient={resetForNewPatient}
            onRegenerateSummary={generateSummary}
          />
        )}

        {currentScreen === 'APPOINTMENTS' && (
          <AppointmentsScreen
            appointments={appointments}
            onBackClick={() => navigateTo('WELCOME')}
            onBookNewAppointment={bookNewAppointment}
          />
        )}

        {currentScreen === 'PROFILE' && (
          <PatientProfileScreen
            profile={patientProfile}
            user={currentUser}
            selectedLanguage={selectedLanguage}
            themeMode={themeMode}
            isAccessibilityMode={isAccessibilityMode}
            isVoiceAssistantEnabled={isVoiceAssistantEnabled}
            isTtsEnabled={isTtsEnabled}
            isVoiceIntakeEnabled={isVoiceIntakeEnabled}
            onBackClick={() => navigateTo('WELCOME')}
            onLanguageClick={() => setShowLanguageDialog(true)}
            onThemeSelect={(mode) => setThemeMode(mode)}
            onToggleEasyMode={() => setIsAccessibilityMode(!isAccessibilityMode)}
            onToggleVoiceAssistant={(en) => setIsVoiceAssistantEnabled(en)}
            onToggleTts={(en) => setIsTtsEnabled(en)}
            onToggleVoiceIntake={(en) => setIsVoiceIntakeEnabled(en)}
            onLogoutClick={logout}
          />
        )}
      </main>

      {/* Bottom Bar on standard tabs */}
      {showBottomBar && (
        <KioskBottomBar
          currentScreen={currentScreen}
          onScreenSelect={navigateTo}
          onAppointmentsClick={() => navigateTo('APPOINTMENTS')}
        />
      )}

      {/* Global Dialogs */}
      {showLanguageDialog && (
        <LanguageDialog
          selectedLanguage={selectedLanguage}
          onLanguageSelected={(lang) => {
            setSelectedLanguage(lang);
            setShowLanguageDialog(false);
            speakText(`Language changed to ${lang.displayName}`, lang, isTtsEnabled);
          }}
          onDismiss={() => setShowLanguageDialog(false)}
        />
      )}

      <CareFlowEmergencyDialog
        isOpen={showEmergencyDialog}
        isEmergencyActive={isEmergencyAlertActive}
        activeEmergencyType={activeEmergencyType}
        emergencyEtaSeconds={emergencyEtaSeconds}
        onDismiss={() => setShowEmergencyDialog(false)}
        onTriggerSos={triggerEmergencySos}
        onCancelSos={cancelEmergencySos}
        onCallEmergencyDesk={() => {
          speakText('Connecting to Hospital Emergency Triage Desk at extension 108. Please remain near the kiosk.', selectedLanguage, isTtsEnabled);
        }}
        onDispatchPorter={() => {
          triggerEmergencySos('Emergency Wheelchair & Stretcher Dispatch');
        }}
      />

      {isVoiceAssistantOpen && (
        <VoiceAssistantModal
          isOpen={isVoiceAssistantOpen}
          state={voiceAssistantState}
          statusText={voiceAssistantStatus}
          transcript={voiceAssistantTranscript}
          response={voiceAssistantResponse}
          language={selectedLanguage}
          onClose={() => setIsVoiceAssistantOpen(false)}
          onSendCommand={processVoiceAssistantQuery}
          isAccessibilityMode={isAccessibilityMode}
        />
      )}
    </div>
  );
};
