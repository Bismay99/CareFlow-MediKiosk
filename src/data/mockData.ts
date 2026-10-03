import {
  Language,
  DemoPatientData,
  AuthUser,
  PatientMasterHistory,
  QueueItem,
  StaffAssistanceRequest,
} from '../types';

export const LANGUAGES: Language[] = [
  {
    code: 'en',
    displayName: 'English',
    nativeName: 'English',
    welcomeText: 'Welcome to MediKiosk',
    tagline: 'Smart Hospital Check-in & AI Triage Assistant',
    consentText: 'I consent to sharing my symptoms and medical history for automated clinical triage with hospital staff.',
    ttsPrompt: 'I consent to sharing my symptoms and medical history for automated clinical triage with hospital staff.'
  },
  {
    code: 'hi',
    displayName: 'Hindi',
    nativeName: 'हिन्दी',
    welcomeText: 'मेडीकियोस्क में आपका स्वागत है',
    tagline: 'अस्पताल चेक-इन और एआई ट्राइएज सहायक',
    consentText: 'मैं अस्पताल के कर्मचारियों के साथ ट्राइएज के लिए अपने लक्षण और चिकित्सा इतिहास साझा करने की सहमति देता/देती हूँ।',
    ttsPrompt: 'मैं अस्पताल के कर्मचारियों के साथ ट्राइएज के लिए अपने लक्षण और चिकित्सा इतिहास साझा करने की सहमति देता हूँ।'
  },
  {
    code: 'bn',
    displayName: 'Bengali',
    nativeName: 'বাংলা',
    welcomeText: 'মেডিকিয়স্কে স্বাগতম',
    tagline: 'স্মার্ট হাসপাতাল চেক-ইন এবং এআই ট্রায়াজ সহকারী',
    consentText: 'আমি হাসপাতালের কর্মীদের সাথে ট্রায়াজের জন্য আমার লক্ষণ এবং চিকিৎসার ইতিহাস শেয়ার করতে সম্মতি দিচ্ছি।',
    ttsPrompt: 'আমি হাসপাতালের কর্মীদের সাথে ট্রায়াজের জন্য আমার লক্ষণ শেয়ার করতে সম্মতি দিচ্ছি।'
  },
  {
    code: 'ta',
    displayName: 'Tamil',
    nativeName: 'தமிழ்',
    welcomeText: 'மெடிகியோஸ்க்கிற்கு வரவேற்கிறோம்',
    tagline: 'மருத்துவமனை செக்-இன் மற்றும் ஏஐ ட்ரையேஜ் உதவியாளர்',
    consentText: 'மருத்துவமனை ஊழியர்களுடன் எனது அறிகுறிகள் மற்றும் மருத்துவ வரலாற்றைப் பகிர்ந்து கொள்ள ஒப்புக்கொள்கிறேன்.',
    ttsPrompt: 'மருத்துவமனை ஊழியர்களுடன் எனது அறிகுறிகளைப் பகிர்ந்து கொள்ள ஒப்புக்கொள்கிறேன்.'
  },
  {
    code: 'te',
    displayName: 'Telugu',
    nativeName: 'తెలుగు',
    welcomeText: 'మెడికియోస్క్‌కి స్వాగతం',
    tagline: 'ఆసుపత్రి చెక్-ఇన్ మరియు ఏఐ ట్రియాజ్ అసిస్టెంట్',
    consentText: 'ఆసుపత్రి సిబ్బందితో నా లక్షణాలు మరియు వైద్య చరిత్రను పంచుకోవడానికి నేను అంగీకరిస్తున్నాను.',
    ttsPrompt: 'ఆసుపత్రి సిబ్బందితో నా లక్షణాలు పంచుకోవడానికి అంగీకరిస్తున్నాను.'
  },
  {
    code: 'mr',
    displayName: 'Marathi',
    nativeName: 'मराठी',
    welcomeText: 'मेडीकियोस्क मध्ये आपले स्वागत आहे',
    tagline: 'रुग्णालय चेक-इन आणि एआय ट्रायज सहाय्यक',
    consentText: 'मी रुग्णालयातील कर्मचाऱ्यांसोबत ट्राइएजसाठी माझी लक्षणे आणि वैद्यकीय माहिती शेअर करण्यास संमती देतो/देते.',
    ttsPrompt: 'मी रुग्णालयातील कर्मचाऱ्यांसोबत माझी लक्षणे शेअर करण्यास संमती देतो.'
  }
];

export const DEMO_PATIENTS: DemoPatientData[] = [
  {
    profile: {
      name: 'Rajesh Sharma',
      age: '54',
      gender: 'Male',
      isNewPatient: false,
      abhaId: '14-2951-8402-1920',
      consentGiven: true
    },
    user: {
      id: 'patient-001',
      email: 'rajesh.sharma@apollohealth.org',
      name: 'Rajesh Sharma',
      role: 'PATIENT',
      phone: '+91 98201 44521',
      abhaId: '14-2951-8402-1920',
      isDemoUser: true
    },
    visit: {
      hasActiveVisit: true,
      department: 'General Medicine',
      doctorName: 'Dr. Rajesh Mehta, MD',
      tokenNumber: '#24',
      queuePosition: 6,
      totalInQueue: 11,
      roomNumber: 'Room 104 - 1st Floor',
      status: 'Waiting',
      estimatedWaitMinutes: 18
    },
    appointments: [
      {
        id: 'apt-101',
        patientId: 'patient-001',
        doctorName: 'Dr. V. Rao',
        specialty: 'Cardiology',
        department: 'Cardiology Wing',
        date: 'Tomorrow',
        time: '10:30 AM',
        room: 'Room 204',
        status: 'Confirmed',
        visitType: 'In-Person'
      },
      {
        id: 'apt-102',
        patientId: 'patient-001',
        doctorName: 'Dr. Rajesh Mehta',
        specialty: 'Internal Medicine',
        department: 'General Medicine',
        date: 'Today',
        time: '11:15 AM',
        room: 'Room 104',
        status: 'Checked In',
        visitType: 'In-Person'
      }
    ],
    records: [
      {
        id: 'rec-1',
        department: 'General Medicine',
        recordType: 'Consultation',
        doctorOrFacility: 'Dr. Rajesh Mehta, MD',
        date: '02 Sep 2026',
        summary: 'Stage 1 Essential Hypertension monitoring, regular blood pressure check, medication adherence.'
      },
      {
        id: 'rec-2',
        department: 'Apollo Diagnostic Lab',
        recordType: 'Lab Report',
        doctorOrFacility: 'Apollo Reference Laboratory',
        date: '28 Aug 2026',
        summary: 'Total Cholesterol: 228 mg/dL [High], Fasting Glucose: 104 mg/dL [High], Creatinine: 0.92 mg/dL.'
      },
      {
        id: 'rec-3',
        department: 'Cardiology Clinic',
        recordType: 'Prescription',
        doctorOrFacility: 'Dr. V. Rao (Reg #KMC-49210)',
        date: '12 Aug 2026',
        summary: 'Telmisartan 40mg OD, Atorvastatin 10mg HS, Ecosprin 75mg OD. Advised low sodium diet.'
      }
    ],
    activities: [
      { id: 'act-1', title: 'Queue updated', subtitle: 'Token #24 assigned in General Medicine queue (Room 104)', timestamp: 'Today, 10:00 AM', isCompleted: true },
      { id: 'act-2', title: 'Document uploaded', subtitle: 'Apollo Fasting Lipid & Blood Glucose Report scanned and parsed', timestamp: 'Yesterday, 04:30 PM', isCompleted: true },
      { id: 'act-3', title: 'Appointment booked', subtitle: 'Follow-up consultation confirmed with Dr. V. Rao in Cardiology', timestamp: '03 Sep 2026', isCompleted: true }
    ],
    notifications: [
      { id: 'notif-1', title: 'Token #24 Approaching', message: 'You are #6 in queue for Room 104. Please remain seated in Waiting Area B.', timestamp: '5 mins ago', isUrgent: true, isRead: false },
      { id: 'notif-2', title: 'Lab Results Verified', message: 'Your blood test profile has been verified by City General Pathology.', timestamp: '1 hour ago', isUrgent: false, isRead: false },
      { id: 'notif-3', title: 'Welcome to MediKiosk', message: 'Need wheelchair or physical assistance? Tap Request Staff Help on your screen.', timestamp: 'Today, 08:30 AM', isUrgent: false, isRead: true }
    ]
  },
  {
    profile: {
      name: 'Sunita Patel',
      age: '42',
      gender: 'Female',
      isNewPatient: false,
      abhaId: '21-4829-1950-3341',
      consentGiven: true
    },
    user: {
      id: 'patient-002',
      email: 'sunita.patel@healthnet.org',
      name: 'Sunita Patel',
      role: 'PATIENT',
      phone: '+91 97112 88402',
      abhaId: '21-4829-1950-3341',
      isDemoUser: true
    },
    visit: {
      hasActiveVisit: true,
      department: 'Cardiology',
      doctorName: 'Dr. Ananya Sen, MD',
      tokenNumber: '#12',
      queuePosition: 2,
      totalInQueue: 8,
      roomNumber: 'Room 202 - 2nd Floor',
      status: 'Near Turn',
      estimatedWaitMinutes: 6
    },
    appointments: [
      { id: 'apt-201', patientId: 'patient-002', doctorName: 'Dr. Ananya Sen', specialty: 'Cardiology', department: 'Cardiology Wing', date: 'Today', time: '10:45 AM', room: 'Room 202', status: 'Checked In', visitType: 'In-Person' },
      { id: 'apt-202', patientId: 'patient-002', doctorName: 'Dr. Suresh Reddy', specialty: 'Echocardiography', department: 'Diagnostic Lab', date: '12 Sep 2026', time: '02:00 PM', room: 'Eco Room 3', status: 'Confirmed', visitType: 'In-Person' }
    ],
    records: [
      { id: 'rec-201', department: 'Cardiology', recordType: 'ECG Report', doctorOrFacility: 'City Cardiac Center', date: '01 Sep 2026', summary: 'Normal sinus rhythm, heart rate 74 bpm, no acute ST-T elevation.' },
      { id: 'rec-202', department: 'Pathology', recordType: 'Cardiac Biomarkers', doctorOrFacility: 'Apollo Diagnostics', date: '25 Aug 2026', summary: 'High-sensitivity Troponin T: <0.01 ng/mL [Normal], Lipid panel within target.' }
    ],
    activities: [
      { id: 'act-201', title: 'Check-in completed', subtitle: 'Arrived at Cardiology OPD Waiting Area', timestamp: 'Today, 09:45 AM', isCompleted: true },
      { id: 'act-202', title: 'ECG uploaded', subtitle: '12-Lead ECG synced via Kiosk document scanner', timestamp: '01 Sep 2026', isCompleted: true }
    ],
    notifications: [
      { id: 'notif-201', title: 'Queue Alert: Token #12', message: 'Your token is next in 2 turns. Please proceed to Room 202.', timestamp: '2 mins ago', isUrgent: true, isRead: false }
    ]
  },
  {
    profile: {
      name: 'Vikram Malhotra',
      age: '68',
      gender: 'Male',
      isNewPatient: false,
      abhaId: '92-1048-5529-8472',
      consentGiven: true
    },
    user: {
      id: 'patient-003',
      email: 'vikram.malhotra@eldercare.org',
      name: 'Vikram Malhotra',
      role: 'PATIENT',
      phone: '+91 98450 11923',
      abhaId: '92-1048-5529-8472',
      isDemoUser: true
    },
    visit: {
      hasActiveVisit: true,
      department: 'Orthopedics',
      doctorName: 'Dr. Sanjay Deshmukh',
      tokenNumber: '#18',
      queuePosition: 4,
      totalInQueue: 14,
      roomNumber: 'Room 310 - 3rd Floor',
      status: 'Waiting',
      estimatedWaitMinutes: 15
    },
    appointments: [
      { id: 'apt-301', patientId: 'patient-003', doctorName: 'Dr. Sanjay Deshmukh', specialty: 'Orthopedics', department: 'Joint Replacement Wing', date: 'Today', time: '11:30 AM', room: 'Room 310', status: 'Confirmed', visitType: 'In-Person' }
    ],
    records: [
      { id: 'rec-301', department: 'Radiology', recordType: 'X-Ray Bilateral Knees', doctorOrFacility: 'Apollo Imaging Wing', date: '20 Aug 2026', summary: 'Bilateral Kellgren-Lawrence Grade 3 osteoarthritis of knee joints with medial joint space narrowing.' }
    ],
    activities: [
      { id: 'act-301', title: 'Wheelchair requested', subtitle: 'Assistance escort assigned to Lobby Gate 2', timestamp: 'Today, 09:15 AM', isCompleted: true }
    ],
    notifications: [
      { id: 'notif-301', title: 'Orthopedic OPD', message: 'Dr. Deshmukh has commenced clinic in Room 310.', timestamp: '15 mins ago', isUrgent: false, isRead: false }
    ]
  },
  {
    profile: {
      name: 'Ananya Iyer',
      age: '29',
      gender: 'Female',
      isNewPatient: false,
      abhaId: '33-7840-2219-9011',
      consentGiven: true
    },
    user: {
      id: 'patient-004',
      email: 'ananya.iyer@hospitalmail.org',
      name: 'Ananya Iyer',
      role: 'PATIENT',
      phone: '+91 99002 67384',
      abhaId: '33-7840-2219-9011',
      isDemoUser: true
    },
    visit: {
      hasActiveVisit: true,
      department: 'Obstetrics & Gynecology',
      doctorName: 'Dr. Shalini Mukhopadhyay',
      tokenNumber: '#08',
      queuePosition: 1,
      totalInQueue: 9,
      roomNumber: 'Room 108 - 1st Floor',
      status: 'Calling Now',
      estimatedWaitMinutes: 2
    },
    appointments: [
      { id: 'apt-401', patientId: 'patient-004', doctorName: 'Dr. Shalini Mukhopadhyay', specialty: 'Obstetrics', department: 'Maternal Care OPD', date: 'Today', time: '10:15 AM', room: 'Room 108', status: 'Calling Now', visitType: 'In-Person' }
    ],
    records: [
      { id: 'rec-401', department: 'Ultrasound', recordType: 'Obstetric Anomaly Scan', doctorOrFacility: 'Fetal Medicine Dept', date: '15 Aug 2026', summary: 'Single live intrauterine gestation, 22 weeks. Normal fetal anatomy and amniotic fluid.' }
    ],
    activities: [
      { id: 'act-401', title: 'Vitals Recorded', subtitle: 'Blood pressure 114/72 mmHg, Pulse 76 bpm recorded at Nursing Desk', timestamp: 'Today, 09:30 AM', isCompleted: true }
    ],
    notifications: [
      { id: 'notif-401', title: 'Doctor Calling: Token #08', message: 'Dr. Shalini Mukhopadhyay is ready for you in Room 108.', timestamp: 'Just now', isUrgent: true, isRead: false }
    ]
  },
  {
    profile: {
      name: 'Mohammed Farooq',
      age: '47',
      gender: 'Male',
      isNewPatient: false,
      abhaId: '55-9012-4431-7890',
      consentGiven: true
    },
    user: {
      id: 'patient-005',
      email: 'm.farooq@apollohealth.org',
      name: 'Mohammed Farooq',
      role: 'PATIENT',
      phone: '+91 97401 55621',
      abhaId: '55-9012-4431-7890',
      isDemoUser: true
    },
    visit: {
      hasActiveVisit: true,
      department: 'Endocrinology & Diabetology',
      doctorName: 'Dr. Preeti Khurana',
      tokenNumber: '#31',
      queuePosition: 8,
      totalInQueue: 16,
      roomNumber: 'Room 214 - 2nd Floor',
      status: 'Waiting',
      estimatedWaitMinutes: 24
    },
    appointments: [
      { id: 'apt-501', patientId: 'patient-005', doctorName: 'Dr. Preeti Khurana', specialty: 'Diabetology', department: 'Endocrine Clinic', date: 'Today', time: '12:00 PM', room: 'Room 214', status: 'Confirmed', visitType: 'In-Person' }
    ],
    records: [
      { id: 'rec-501', department: 'Endocrinology', recordType: 'HbA1c & Fasting Glucose', doctorOrFacility: 'Apollo Reference Lab', date: '29 Aug 2026', summary: 'HbA1c: 7.4% [Moderate control], Fasting Blood Sugar: 142 mg/dL. Microalbuminuria: Negative.' }
    ],
    activities: [
      { id: 'act-501', title: 'Lab Report synced', subtitle: 'Quarterly Diabetic Profile imported into EHR', timestamp: 'Yesterday', isCompleted: true }
    ],
    notifications: [
      { id: 'notif-501', title: 'Dietitian Consultation', message: 'Complimentary diabetic dietary counseling available on Floor 2.', timestamp: '2 hours ago', isUrgent: false, isRead: true }
    ]
  }
];

export const DEMO_ADMIN: AuthUser = {
  id: 'admin-901',
  email: 'admin@apollohospital.org',
  name: 'Dr. Alok Verma (Chief Medical Administrator)',
  role: 'ADMIN',
  phone: '+91 98100 23456',
  abhaId: 'ADMIN-SYS-01',
  isDemoUser: true
};

export const INITIAL_QUEUES: QueueItem[] = [
  { token: 'Token #21', patientName: 'Anil Kulkarni', department: 'General Medicine', room: 'Room 104', position: 1, status: 'In Consultation', estimatedWaitMinutes: 0 },
  { token: 'Token #22', patientName: 'Meenakshi Sundaram', department: 'General Medicine', room: 'Room 104', position: 2, status: 'Waiting', estimatedWaitMinutes: 4 },
  { token: 'Token #23', patientName: 'Harish Chandra', department: 'General Medicine', room: 'Room 104', position: 3, status: 'Waiting', estimatedWaitMinutes: 9 },
  { token: 'Token #24', patientName: 'Rajesh Sharma', department: 'General Medicine', room: 'Room 104', position: 4, status: 'Waiting', estimatedWaitMinutes: 14 },
  { token: 'Token #25', patientName: 'Fatima Begum', department: 'General Medicine', room: 'Room 104', position: 5, status: 'Waiting', estimatedWaitMinutes: 20 },
  { token: 'Token #11', patientName: 'Kavita Rao', department: 'Cardiology', room: 'Room 202', position: 1, status: 'In Consultation', estimatedWaitMinutes: 0 },
  { token: 'Token #12', patientName: 'Sunita Patel', department: 'Cardiology', room: 'Room 202', position: 2, status: 'Near Turn', estimatedWaitMinutes: 5 },
  { token: 'Token #08', patientName: 'Ananya Iyer', department: 'Obstetrics', room: 'Room 108', position: 1, status: 'Calling Now', estimatedWaitMinutes: 2 },
  { token: 'Token #18', patientName: 'Vikram Malhotra', department: 'Orthopedics', room: 'Room 310', position: 4, status: 'Waiting', estimatedWaitMinutes: 16 }
];

export const INITIAL_STAFF_REQUESTS: StaffAssistanceRequest[] = [
  {
    id: 'req-101',
    patientName: 'Vikram Malhotra',
    location: 'Kiosk 04 (Lobby Floor)',
    reason: 'Wheelchair & Escort Required for Orthopedics 3rd Floor',
    timestamp: '3 mins ago',
    isUrgent: false,
    status: 'DISPATCHED',
    assignedStaff: 'Nurse Priya (Care Escort)'
  },
  {
    id: 'req-102',
    patientName: 'Suresh Nambiar',
    location: 'Kiosk 02 (Ground Floor Diagnostics)',
    reason: 'Language Translation Assistance (Malayalam to English)',
    timestamp: '7 mins ago',
    isUrgent: false,
    status: 'PENDING'
  }
];

export const INITIAL_PATIENT_HISTORIES: PatientMasterHistory[] = [
  {
    patientId: 'patient-001',
    patientName: 'Rajesh Sharma',
    age: '54',
    gender: 'Male',
    abhaId: '14-2951-8402-1920',
    phone: '+91 98201 44521',
    bloodGroup: 'B+',
    allergies: ['Penicillin (Mild rash)'],
    chronicConditions: ['Essential Hypertension', 'Hyperlipidemia', 'Borderline Pre-Diabetes'],
    emergencyContact: '+91 98201 44521',
    consultations: [
      {
        id: 'consult-001',
        patientId: 'patient-001',
        patientName: 'Rajesh Sharma',
        doctorName: 'Dr. Rajesh Mehta, MD',
        doctorSpecialty: 'Internal Medicine & Hypertension',
        department: 'General Medicine',
        consultationDateTime: '02 Sep 2026, 11:15 AM',
        roomNumber: 'Room 104 - 1st Floor',
        chiefComplaint: 'Occasional morning occipital headache and mild exertional fatigue for 3 weeks.',
        diagnosis: 'Stage 1 Essential Hypertension (Moderate Control) & Mixed Dyslipidemia',
        clinicalNotes: 'Patient shows steady blood pressure improvement. Advised strict restriction of dietary sodium (<2g/day) and 30 mins brisk morning walk.',
        vitals: {
          bloodPressure: '138/88 mmHg',
          pulseRate: '74 bpm',
          oxygenSaturationSpO2: '98%',
          temperature: '98.4 °F',
          bloodSugarFasting: '104 mg/dL',
          weightKg: '78 kg',
          bmi: '26.2'
        },
        medicines: [
          {
            id: 'med-001',
            medicineName: 'Telmisartan 40mg',
            genericName: 'Telmisartan IP 40mg',
            dosage: '1 Tablet (40mg)',
            form: 'Tablet',
            frequency: 'ONCE_DAILY_MORNING',
            foodTiming: 'AFTER_BREAKFAST',
            duration: '90 Days (Long-term)',
            specialInstructions: 'Take daily at 8:00 AM with water after morning breakfast. Do not skip.',
            isMorning: true,
            isAfternoon: false,
            isEvening: false,
            isNight: false,
            status: 'Active'
          },
          {
            id: 'med-002',
            medicineName: 'Atorvastatin 10mg',
            genericName: 'Atorvastatin Calcium IP 10mg',
            dosage: '1 Tablet (10mg)',
            form: 'Tablet',
            frequency: 'ONCE_DAILY_NIGHT',
            foodTiming: 'BEDTIME',
            duration: '90 Days',
            specialInstructions: 'Take at bedtime after dinner. Avoid grapefruit juice.',
            isMorning: false,
            isAfternoon: false,
            isEvening: false,
            isNight: true,
            status: 'Active'
          },
          {
            id: 'med-003',
            medicineName: 'Ecosprin 75mg',
            genericName: 'Aspirin Gastro-resistant IP 75mg',
            dosage: '1 Tablet (75mg)',
            form: 'Tablet',
            frequency: 'ONCE_DAILY_MORNING',
            foodTiming: 'AFTER_LUNCH',
            duration: '90 Days',
            specialInstructions: 'Take strictly AFTER lunch with full glass of water to avoid gastric irritation.',
            isMorning: false,
            isAfternoon: true,
            isEvening: false,
            isNight: false,
            status: 'Active'
          }
        ],
        labInvestigations: ['Fasting Lipid Profile', 'Fasting Blood Sugar (FBS)', 'Serum Creatinine & eGFR', 'Urine Routine & Microalbumin'],
        followUpDate: '28 Sep 2026',
        doctorAdvice: 'Maintain BP log chart twice weekly. Limit salt intake to under 1 teaspoon daily. Brisk walking 30 mins.',
        isRecordedByAdmin: true
      }
    ]
  }
];

export const HOSPITAL_SERVICES = [
  { key: 'lab_tests', title: 'Diagnostic Lab Tests', desc: 'Blood panels, ECG, lipid tests', icon: 'FlaskConical', color: 'bg-emerald-50 text-emerald-700' },
  { key: 'surgery', title: 'Surgery & Procedures', desc: 'Pre-admission counseling & OT slots', icon: 'Activity', color: 'bg-amber-50 text-amber-700' },
  { key: 'medicines', title: 'Order Medicines', desc: 'Hospital pharmacy dispatch & refills', icon: 'Pill', color: 'bg-blue-50 text-blue-700' },
  { key: 'health_records', title: 'Clinical Summaries', desc: 'Verified SOAP records & triage notes', icon: 'FileText', color: 'bg-purple-50 text-purple-700' },
  { key: 'appointments', title: 'Consultations', desc: 'Book OPD or view upcoming tokens', icon: 'Calendar', color: 'bg-sky-50 text-sky-700' },
  { key: 'prescriptions', title: 'Scan Prescription', desc: 'OCR parsing of handwritten slips', icon: 'Scan', color: 'bg-teal-50 text-teal-700' }
];

export const FIRST_AID_GUIDES = [
  {
    title: 'Chest Pain / Angina',
    steps: [
      'Stop all physical exertion immediately and sit upright.',
      'Loosen tight clothing around neck and chest.',
      'Take deep, calm breaths and do not walk around.',
      'Wait for the hospital rapid response team arriving at this kiosk.'
    ]
  },
  {
    title: 'Suspected Stroke (F.A.S.T.)',
    steps: [
      'Face: Check if one side of the face is drooping.',
      'Arms: Ask person to raise both arms; check if one drifts downward.',
      'Speech: Check if words are slurred or confusing.',
      'Time: Immediate critical code call is already activated.'
    ]
  },
  {
    title: 'Choking / Blocked Airway',
    steps: [
      'Encourage the person to cough forcefully if able.',
      'Stand behind person, place arms around waist.',
      'Make a fist above navel and give quick inward/upward thrusts (Heimlich Maneuver).',
      'If person becomes unresponsive, begin chest compressions.'
    ]
  },
  {
    title: 'CPR Essentials',
    steps: [
      'Check for responsiveness and normal breathing.',
      'Place heel of hand on center of chest, other hand on top.',
      'Push hard and fast (100–120 beats per minute, 2 inches deep).',
      'Continue chest compressions until medical personnel arrive.'
    ]
  }
];
