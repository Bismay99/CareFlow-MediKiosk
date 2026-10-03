export type Screen =
  | 'ROLE_SELECTION'
  | 'USER_LOGIN'
  | 'ADMIN_LOGIN'
  | 'ADMIN_DASHBOARD'
  | 'WELCOME'
  | 'IDENTIFY'
  | 'CONVERSATIONAL_INTAKE'
  | 'DOCUMENT_SCAN'
  | 'SUMMARY'
  | 'PROFILE'
  | 'APPOINTMENTS'
  | 'TELEMEDICINE_DOCTORS'
  | 'TELEMEDICINE_CALL'
  | 'HEALTH_MONITORING'
  | 'AI_HEALTH_ASSISTANT'
  | 'DOCTOR_VERIFICATION'
  | 'MEDICAL_RECORDS'
  | 'DOCTOR_DASHBOARD';

export type UserRole = 'PATIENT' | 'STAFF' | 'DOCTOR' | 'ADMIN';
export type AppThemeMode = 'LIGHT' | 'DARK' | 'SYSTEM';
export type VoiceAssistantState = 'IDLE' | 'LISTENING' | 'PROCESSING' | 'EXECUTING' | 'SPEAKING' | 'ERROR';

export interface Language {
  code: string;
  displayName: string;
  nativeName: string;
  welcomeText: string;
  tagline: string;
  consentText: string;
  ttsPrompt: string;
}

export interface PatientProfile {
  name: string;
  age: string;
  gender: string;
  isNewPatient: boolean;
  abhaId: string;
  consentGiven: boolean;
}

export interface SocratesIntake {
  chiefComplaint: string;
  site: string;
  onset: string;
  character: string;
  radiation: string;
  associations: string;
  timing: string;
  exacerbatingRelieving: string;
  severity: string;
  pastMedicalHistory: string;
  currentMedications: string;
  allergies: string;
  familyHistory: string;
}

export interface ChatMessage {
  id: string;
  sender: 'PATIENT' | 'AI';
  text: string;
  quickReplies?: string[];
  timestamp: number;
}

export interface MedicationItem {
  name: string;
  dosage: string;
  frequency: string;
  instructions: string;
}

export interface LabValueItem {
  testName: string;
  value: string;
  unit: string;
  referenceRange: string;
  status: 'Normal' | 'High' | 'Low';
}

export interface ExtractedDocument {
  documentType: string;
  diagnosis: string;
  medications: MedicationItem[];
  labValues: LabValueItem[];
  rawNotes: string;
}

export interface ClinicalSummary {
  chiefComplaint: string;
  historyOfPresentIllness: string;
  pastMedicalHistory: string;
  drugAndAllergyHistory: string;
  familyHistory: string;
  reviewOfSystems: string;
  priorInvestigations: string;
  triagePriority: 'Urgent' | 'Moderate' | 'Routine';
  triageReason: string;
  recommendedDepartment: string;
  tokenNumber: string;
  estimatedWaitMinutes: number;
}

export interface AuthUser {
  id: string;
  email: string;
  name: string;
  role: UserRole;
  phone: string;
  abhaId: string;
  profilePhotoUrl?: string;
  isDemoUser: boolean;
}

export interface HospitalAppointment {
  id: string;
  patientId: string;
  doctorName: string;
  specialty: string;
  department: string;
  date: string;
  time: string;
  room: string;
  status: string; // 'Confirmed' | 'Completed' | 'Checked In' | 'Calling Now'
  visitType: string; // 'In-Person' | 'Video Consult'
}

export interface QueueItem {
  token: string;
  patientName: string;
  department: string;
  room: string;
  position: number;
  status: string; // 'In Consultation' | 'Near Turn' | 'Waiting' | 'Calling Now' | 'Completed'
  estimatedWaitMinutes: number;
}

export interface StaffAssistanceRequest {
  id: string;
  patientName: string;
  location: string;
  reason: string;
  timestamp: string;
  isUrgent: boolean;
  status: 'PENDING' | 'DISPATCHED' | 'RESOLVED';
  assignedStaff?: string;
}

export interface ActiveVisit {
  hasActiveVisit: boolean;
  department: string;
  doctorName: string;
  tokenNumber: string;
  queuePosition: number;
  totalInQueue: number;
  roomNumber: string;
  status: string;
  estimatedWaitMinutes: number;
}

export interface RecentlyViewedItem {
  id: string;
  type: string;
  title: string;
  dateOrStatus: string;
  iconType: string;
}

export interface HealthInsight {
  id: string;
  title: string;
  description: string;
  tag: string;
  iconType: string;
}

export interface RecentRecordItem {
  id: string;
  department: string;
  recordType: string;
  doctorOrFacility: string;
  date: string;
  summary: string;
}

export interface RecentActivityItem {
  id: string;
  title: string;
  subtitle: string;
  timestamp: string;
  isCompleted: boolean;
}

export interface HospitalNotification {
  id: string;
  title: string;
  message: string;
  timestamp: string;
  isUrgent: boolean;
  isRead: boolean;
}

export type MedicineFrequency =
  | 'ONCE_DAILY_MORNING'
  | 'ONCE_DAILY_NIGHT'
  | 'TWICE_DAILY'
  | 'THRICE_DAILY'
  | 'FOUR_TIMES_DAILY'
  | 'EVERY_ALTERNATE_DAY'
  | 'ONCE_WEEKLY'
  | 'AS_NEEDED';

export type MedicineFoodTiming =
  | 'BEFORE_BREAKFAST_EMPTY_STOMACH'
  | 'AFTER_BREAKFAST'
  | 'BEFORE_LUNCH'
  | 'AFTER_LUNCH'
  | 'BEFORE_DINNER'
  | 'AFTER_DINNER'
  | 'WITH_MEALS'
  | 'BEDTIME'
  | 'AS_NEEDED_PRN';

export interface PrescriptionMedicine {
  id: string;
  medicineName: string;
  genericName?: string;
  dosage: string;
  form: string;
  frequency: MedicineFrequency;
  foodTiming: MedicineFoodTiming;
  duration: string;
  specialInstructions: string;
  isMorning: boolean;
  isAfternoon: boolean;
  isEvening: boolean;
  isNight: boolean;
  status: 'Active' | 'Completed' | 'Discontinued';
}

export interface ConsultationVitals {
  bloodPressure: string;
  pulseRate: string;
  oxygenSaturationSpO2: string;
  temperature: string;
  bloodSugarFasting: string;
  weightKg: string;
  bmi: string;
}

export interface ConsultationHistoryEntry {
  id: string;
  patientId: string;
  patientName: string;
  doctorName: string;
  doctorSpecialty: string;
  department: string;
  consultationDateTime: string;
  roomNumber: string;
  chiefComplaint: string;
  diagnosis: string;
  clinicalNotes: string;
  vitals: ConsultationVitals;
  medicines: PrescriptionMedicine[];
  labInvestigations: string[];
  followUpDate: string;
  doctorAdvice: string;
  isRecordedByAdmin: boolean;
}

export interface PatientMasterHistory {
  patientId: string;
  patientName: string;
  age: string;
  gender: string;
  abhaId: string;
  phone: string;
  bloodGroup: string;
  allergies: string[];
  chronicConditions: string[];
  emergencyContact: string;
  consultations: ConsultationHistoryEntry[];
}

export interface DemoPatientData {
  profile: PatientProfile;
  user: AuthUser;
  visit: ActiveVisit;
  appointments: HospitalAppointment[];
  records: RecentRecordItem[];
  activities: RecentActivityItem[];
  notifications: HospitalNotification[];
}
