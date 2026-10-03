import React, { useState } from 'react';
import {
  Shield,
  LogOut,
  Users,
  FileText,
  HelpCircle,
  Activity,
  CheckCircle,
  PhoneCall,
  Clock,
  Plus,
  Pill,
  Volume2,
  AlertTriangle,
  Search,
  Calendar,
  X,
  UserCheck,
  Stethoscope,
} from 'lucide-react';
import {
  AuthUser,
  QueueItem,
  StaffAssistanceRequest,
  PatientMasterHistory,
  PrescriptionMedicine,
  ConsultationVitals,
  MedicineFrequency,
  MedicineFoodTiming,
} from '../types';

interface AdminDashboardScreenProps {
  adminUser: AuthUser | null;
  staffRequests: StaffAssistanceRequest[];
  liveQueues: QueueItem[];
  patientHistories: PatientMasterHistory[];
  onCallNextPatient: (dept: string) => void;
  onMarkInConsultation: (token: string) => void;
  onDispatchStaff: (reqId: string) => void;
  onResolveStaffRequest: (reqId: string) => void;
  onRecordConsultation: (
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
  ) => void;
  onAddMedicineToConsultation: (consultId: string, medicine: PrescriptionMedicine) => void;
  onUpdateMedicineStatus: (patientId: string, medId: string, status: 'Active' | 'Completed' | 'Discontinued') => void;
  onSpeakPrescription: (patientName: string, medicines: PrescriptionMedicine[]) => void;
  onLogoutClick: () => void;
}

export const AdminDashboardScreen: React.FC<AdminDashboardScreenProps> = ({
  adminUser,
  staffRequests,
  liveQueues,
  patientHistories,
  onCallNextPatient,
  onMarkInConsultation,
  onDispatchStaff,
  onResolveStaffRequest,
  onRecordConsultation,
  onUpdateMedicineStatus,
  onSpeakPrescription,
  onLogoutClick,
}) => {
  const [activeTab, setActiveTab] = useState<'QUEUES' | 'EHR' | 'STAFF' | 'SYSTEM'>('QUEUES');
  const [selectedDeptFilter, setSelectedDeptFilter] = useState<string>('ALL');
  const [selectedPatientId, setSelectedPatientId] = useState<string>(patientHistories[0]?.patientId || '');
  const [searchQuery, setSearchQuery] = useState('');
  const [showConsultModal, setShowConsultModal] = useState(false);
  const [actionFeedback, setActionFeedback] = useState<string | null>(null);

  // New Consultation Form state
  const [formPatient, setFormPatient] = useState({
    patientId: '',
    patientName: '',
    age: '54',
    gender: 'Male',
    abhaId: '',
    phone: '',
    bloodGroup: 'B+',
    doctorName: 'Dr. Alok Verma, MD',
    doctorSpecialty: 'Internal Medicine',
    department: 'General Medicine',
    roomNumber: 'Room 104',
    chiefComplaint: '',
    diagnosis: '',
    clinicalNotes: '',
    bp: '120/80 mmHg',
    pulse: '72 bpm',
    spo2: '98%',
    temp: '98.4 °F',
    sugar: '104 mg/dL',
    weight: '72 kg',
    bmi: '24.2',
    followUp: 'In 2 Weeks',
    advice: 'Low sodium diet, brisk walking 30 minutes daily.',
  });

  const [formMeds, setFormMeds] = useState<PrescriptionMedicine[]>([]);
  const [newMed, setNewMed] = useState({
    name: '',
    dosage: '40 mg',
    form: 'Tablet',
    frequency: 'ONCE_DAILY_MORNING' as MedicineFrequency,
    foodTiming: 'AFTER_BREAKFAST' as MedicineFoodTiming,
    duration: '30 Days',
    instructions: 'Take in the morning with water.',
  });

  const departments = ['General Medicine', 'Cardiology', 'Orthopedics', 'Obstetrics'];

  const filteredQueues = liveQueues.filter((q) => {
    if (selectedDeptFilter === 'ALL') return true;
    return q.department.toLowerCase().includes(selectedDeptFilter.toLowerCase());
  });

  const selectedPatient = patientHistories.find((p) => p.patientId === selectedPatientId) || patientHistories[0];

  const filteredPatients = patientHistories.filter((p) => {
    const q = searchQuery.toLowerCase();
    return p.patientName.toLowerCase().includes(q) || p.abhaId.toLowerCase().includes(q);
  });

  const triggerFeedback = (msg: string) => {
    setActionFeedback(msg);
    setTimeout(() => setActionFeedback(null), 4000);
  };

  const handleAddMedToForm = () => {
    if (!newMed.name.trim()) return;
    const medItem: PrescriptionMedicine = {
      id: `med-${Date.now()}`,
      medicineName: newMed.name,
      dosage: newMed.dosage,
      form: newMed.form,
      frequency: newMed.frequency,
      foodTiming: newMed.foodTiming,
      duration: newMed.duration,
      specialInstructions: newMed.instructions,
      isMorning: newMed.frequency.includes('MORNING') || newMed.frequency.includes('TWICE') || newMed.frequency.includes('THRICE'),
      isAfternoon: newMed.frequency.includes('THRICE'),
      isEvening: false,
      isNight: newMed.frequency.includes('NIGHT') || newMed.frequency.includes('TWICE') || newMed.frequency.includes('THRICE'),
      status: 'Active',
    };
    setFormMeds([...formMeds, medItem]);
    setNewMed({
      name: '',
      dosage: '40 mg',
      form: 'Tablet',
      frequency: 'ONCE_DAILY_MORNING',
      foodTiming: 'AFTER_BREAKFAST',
      duration: '30 Days',
      instructions: 'Take in the morning with water.',
    });
  };

  const handleSaveConsultation = () => {
    if (!formPatient.patientName.trim() || !formPatient.diagnosis.trim()) {
      alert('Please fill out patient name and clinical diagnosis.');
      return;
    }

    const vitals: ConsultationVitals = {
      bloodPressure: formPatient.bp,
      pulseRate: formPatient.pulse,
      oxygenSaturationSpO2: formPatient.spo2,
      temperature: formPatient.temp,
      bloodSugarFasting: formPatient.sugar,
      weightKg: formPatient.weight,
      bmi: formPatient.bmi,
    };

    onRecordConsultation(
      formPatient.patientId || `pt-${Date.now()}`,
      formPatient.patientName,
      formPatient.age,
      formPatient.gender,
      formPatient.abhaId,
      formPatient.phone,
      formPatient.doctorName,
      formPatient.doctorSpecialty,
      formPatient.department,
      new Date().toLocaleString('en-US', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }),
      formPatient.roomNumber,
      formPatient.chiefComplaint,
      formPatient.diagnosis,
      formPatient.clinicalNotes,
      vitals,
      formMeds,
      ['Complete Blood Count', 'Fasting Blood Sugar'],
      formPatient.followUp,
      formPatient.advice,
      formPatient.bloodGroup,
      ['None Known'],
      []
    );

    setShowConsultModal(false);
    triggerFeedback(`Recorded new consultation & ${formMeds.length} prescriptions for ${formPatient.patientName}!`);
  };

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 flex flex-col">
      {/* Admin Top Navigation Bar */}
      <header className="sticky top-0 z-40 bg-slate-950 border-b border-slate-800 px-4 py-3 shadow-md">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-indigo-600/20 text-indigo-400 border border-indigo-500/30 rounded-2xl">
              <Shield className="w-6 h-6" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="text-[10px] font-black uppercase tracking-wider text-indigo-400 bg-indigo-950 px-2 py-0.5 rounded border border-indigo-800">
                  Command Console
                </span>
                <span className="text-xs text-slate-400">Apollo & City General</span>
              </div>
              <h1 className="text-base font-black text-white">
                {adminUser?.name || 'Dr. Alok Verma (Administrator)'}
              </h1>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <button
              onClick={() => {
                // Populate default for Rajesh Sharma
                setFormPatient({
                  ...formPatient,
                  patientId: selectedPatient?.patientId || 'patient-001',
                  patientName: selectedPatient?.patientName || 'Rajesh Sharma',
                  age: selectedPatient?.age || '54',
                  gender: selectedPatient?.gender || 'Male',
                  abhaId: selectedPatient?.abhaId || '14-2951-8402-1920',
                  phone: selectedPatient?.phone || '+91 98201 44521',
                  bloodGroup: selectedPatient?.bloodGroup || 'B+',
                  chiefComplaint: 'Follow-up consultation for blood pressure review',
                  diagnosis: 'Essential Hypertension - Regulated Control',
                  clinicalNotes: 'Patient adhering to dietary sodium restriction. Vitals stable.',
                });
                setShowConsultModal(true);
              }}
              className="flex items-center gap-2 px-3.5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl text-xs font-bold shadow transition active:scale-95 cursor-pointer"
            >
              <Stethoscope className="w-4 h-4" />
              <span className="hidden sm:inline">Record New Consultation</span>
            </button>

            <button
              onClick={onLogoutClick}
              className="p-2.5 rounded-xl bg-slate-800 hover:bg-rose-950/60 text-slate-400 hover:text-rose-300 border border-slate-700 transition cursor-pointer"
              title="Sign Out"
            >
              <LogOut className="w-5 h-5" />
            </button>
          </div>
        </div>
      </header>

      {/* Action Feedback Banner */}
      {actionFeedback && (
        <div className="bg-emerald-950/80 border-b border-emerald-700 text-emerald-200 px-4 py-2.5 flex items-center justify-between text-xs font-bold animate-fade-in">
          <div className="flex items-center gap-2 max-w-7xl mx-auto w-full">
            <CheckCircle className="w-4 h-4 text-emerald-400 flex-shrink-0" />
            <span>{actionFeedback}</span>
          </div>
        </div>
      )}

      {/* Tabs */}
      <div className="border-b border-slate-800 bg-slate-950/60 px-4">
        <div className="max-w-7xl mx-auto flex overflow-x-auto space-x-1 sm:space-x-4">
          {[
            { id: 'QUEUES', label: 'Live Department Queues', count: liveQueues.length, icon: Users },
            { id: 'EHR', label: 'Master Patient EHR & History', count: patientHistories.length, icon: FileText },
            { id: 'STAFF', label: 'Staff Assistance Desk', count: staffRequests.filter(s => s.status === 'PENDING').length, icon: HelpCircle },
            { id: 'SYSTEM', label: 'System Telemetry', count: null, icon: Activity },
          ].map((tab) => {
            const Icon = tab.icon;
            const isActive = activeTab === tab.id;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id as any)}
                className={`flex items-center gap-2 py-3 px-3.5 font-bold text-xs sm:text-sm border-b-2 transition whitespace-nowrap cursor-pointer ${
                  isActive
                    ? 'border-indigo-500 text-indigo-400'
                    : 'border-transparent text-slate-400 hover:text-slate-200'
                }`}
              >
                <Icon className="w-4 h-4" />
                <span>{tab.label}</span>
                {tab.count !== null && (
                  <span
                    className={`px-1.5 py-0.5 rounded-full text-[10px] ${
                      isActive ? 'bg-indigo-500 text-white' : 'bg-slate-800 text-slate-400'
                    }`}
                  >
                    {tab.count}
                  </span>
                )}
              </button>
            );
          })}
        </div>
      </div>

      {/* Tab Panels */}
      <main className="max-w-7xl mx-auto w-full p-4 sm:p-6 flex-1">
        {/* 1. LIVE QUEUES TAB */}
        {activeTab === 'QUEUES' && (
          <div className="space-y-4">
            {/* Filter buttons */}
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div className="flex flex-wrap items-center gap-1.5">
                <span className="text-xs font-bold text-slate-400 uppercase tracking-wider mr-1">
                  Department:
                </span>
                {['ALL', ...departments].map((dept) => (
                  <button
                    key={dept}
                    onClick={() => setSelectedDeptFilter(dept)}
                    className={`px-3 py-1.5 rounded-xl text-xs font-bold transition cursor-pointer ${
                      selectedDeptFilter === dept
                        ? 'bg-indigo-600 text-white'
                        : 'bg-slate-800 text-slate-400 hover:bg-slate-700'
                    }`}
                  >
                    {dept}
                  </button>
                ))}
              </div>

              {/* Quick Call Action per department */}
              <div className="flex items-center gap-2">
                {departments.map((dept) => (
                  <button
                    key={dept}
                    onClick={() => {
                      onCallNextPatient(dept);
                      triggerFeedback(`Called next token in ${dept}!`);
                    }}
                    className="px-3 py-1.5 bg-slate-800 hover:bg-indigo-900 border border-slate-700 rounded-xl text-xs font-bold text-slate-200 flex items-center gap-1.5 transition active:scale-95 cursor-pointer"
                  >
                    <Volume2 className="w-3.5 h-3.5 text-indigo-400" />
                    <span>Call Next: {dept.split(' ')[0]}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* Queue Table */}
            <div className="bg-slate-800/80 rounded-3xl border border-slate-700/80 overflow-hidden shadow-xl">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="bg-slate-900/90 text-slate-400 uppercase tracking-wider text-[10px] border-b border-slate-700">
                    <tr>
                      <th className="p-4 font-black">Token</th>
                      <th className="p-4 font-black">Patient Name</th>
                      <th className="p-4 font-black">Department</th>
                      <th className="p-4 font-black">Room</th>
                      <th className="p-4 font-black">Queue Pos</th>
                      <th className="p-4 font-black">Status</th>
                      <th className="p-4 font-black text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-700/50">
                    {filteredQueues.map((item) => (
                      <tr key={item.token} className="hover:bg-slate-750 transition">
                        <td className="p-4 font-black text-white text-sm">
                          {item.token}
                        </td>
                        <td className="p-4 font-bold text-slate-200">
                          {item.patientName}
                        </td>
                        <td className="p-4 text-slate-300 font-medium">
                          {item.department}
                        </td>
                        <td className="p-4 text-slate-400 font-mono">
                          {item.room}
                        </td>
                        <td className="p-4 text-slate-300">
                          #{item.position} in line
                        </td>
                        <td className="p-4">
                          <span
                            className={`px-2.5 py-1 rounded-full text-[10px] font-bold ${
                              item.status === 'In Consultation'
                                ? 'bg-emerald-950 text-emerald-300 border border-emerald-700'
                                : item.status === 'Calling Now'
                                ? 'bg-amber-950 text-amber-300 border border-amber-700 animate-pulse'
                                : 'bg-slate-700 text-slate-300'
                            }`}
                          >
                            {item.status}
                          </span>
                        </td>
                        <td className="p-4 text-right space-x-2">
                          {item.status !== 'In Consultation' && (
                            <button
                              onClick={() => {
                                onMarkInConsultation(item.token);
                                triggerFeedback(`Token ${item.token} marked in consultation.`);
                              }}
                              className="px-2.5 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg font-bold text-[11px] transition active:scale-95 cursor-pointer"
                            >
                              In Consultation
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* 2. MASTER PATIENT EHR TAB */}
        {activeTab === 'EHR' && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Left list of patients */}
            <div className="space-y-3">
              <div className="relative">
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="Search patient name or ABHA..."
                  className="w-full pl-9 pr-4 py-2.5 bg-slate-800 border border-slate-700 rounded-2xl text-xs text-white focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
                <Search className="w-4 h-4 text-slate-400 absolute left-3 top-3" />
              </div>

              <div className="space-y-2 max-h-[70vh] overflow-y-auto pr-1">
                {filteredPatients.map((p) => {
                  const isSelected = p.patientId === selectedPatient?.patientId;
                  return (
                    <button
                      key={p.patientId}
                      onClick={() => setSelectedPatientId(p.patientId)}
                      className={`w-full text-left p-3.5 rounded-2xl border transition active:scale-98 cursor-pointer ${
                        isSelected
                          ? 'bg-indigo-950/70 border-indigo-500 ring-2 ring-indigo-500/20'
                          : 'bg-slate-800/80 hover:bg-slate-800 border-slate-700 text-slate-300'
                      }`}
                    >
                      <div className="flex items-center justify-between mb-1">
                        <span className="font-black text-white text-sm">
                          {p.patientName}
                        </span>
                        <span className="text-[10px] font-mono text-indigo-400 bg-slate-900 px-2 py-0.5 rounded">
                          {p.bloodGroup}
                        </span>
                      </div>
                      <div className="text-xs text-slate-400">
                        {p.age} yrs • {p.gender}
                      </div>
                      <div className="text-[11px] text-slate-500 font-mono mt-0.5 truncate">
                        ABHA: {p.abhaId}
                      </div>
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Right patient details & consultations */}
            <div className="lg:col-span-2 space-y-4">
              {selectedPatient ? (
                <>
                  {/* Demographics Card */}
                  <div className="bg-slate-800/90 rounded-3xl border border-slate-700/80 p-5 space-y-3">
                    <div className="flex flex-wrap items-center justify-between gap-3">
                      <div>
                        <h2 className="text-xl font-black text-white">
                          {selectedPatient.patientName}
                        </h2>
                        <p className="text-xs text-slate-400">
                          {selectedPatient.age} yrs • {selectedPatient.gender} • Blood Group: {selectedPatient.bloodGroup} • Phone: {selectedPatient.phone}
                        </p>
                        <p className="text-xs font-mono text-indigo-400 mt-0.5">
                          ABHA ID: {selectedPatient.abhaId}
                        </p>
                      </div>

                      <button
                        onClick={() => {
                          const activeMeds = selectedPatient.consultations.flatMap((c) => c.medicines);
                          onSpeakPrescription(selectedPatient.patientName, activeMeds);
                        }}
                        className="flex items-center gap-1.5 px-3 py-1.5 bg-slate-700 hover:bg-slate-600 text-indigo-300 rounded-xl text-xs font-bold border border-slate-600 transition cursor-pointer"
                      >
                        <Volume2 className="w-4 h-4" />
                        <span>Read Rx Schedule</span>
                      </button>
                    </div>

                    {/* Chronic conditions & allergies badges */}
                    <div className="flex flex-wrap gap-2 pt-2 border-t border-slate-700/60">
                      {selectedPatient.chronicConditions.map((c) => (
                        <span key={c} className="px-2.5 py-1 rounded-lg bg-sky-950 text-sky-300 border border-sky-800 text-[10px] font-bold">
                          {c}
                        </span>
                      ))}
                      {selectedPatient.allergies.map((a) => (
                        <span key={a} className="px-2.5 py-1 rounded-lg bg-rose-950 text-rose-300 border border-rose-800 text-[10px] font-bold">
                          Allergy: {a}
                        </span>
                      ))}
                    </div>
                  </div>

                  {/* Consultation History Entries */}
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <h3 className="text-sm font-black uppercase tracking-wider text-slate-400">
                        Consultation History ({selectedPatient.consultations.length})
                      </h3>
                    </div>

                    {selectedPatient.consultations.map((entry) => (
                      <div key={entry.id} className="bg-slate-800/90 rounded-3xl border border-slate-700/80 p-5 space-y-4 shadow-sm">
                        <div className="flex flex-wrap items-center justify-between gap-2 border-b border-slate-700/60 pb-3">
                          <div>
                            <span className="text-[10px] font-black uppercase tracking-wider text-indigo-400 bg-slate-900 px-2 py-0.5 rounded">
                              {entry.department}
                            </span>
                            <h4 className="text-base font-black text-white mt-1">
                              {entry.diagnosis}
                            </h4>
                            <p className="text-xs text-slate-400">
                              Attending: {entry.doctorName} ({entry.doctorSpecialty}) • {entry.consultationDateTime}
                            </p>
                          </div>
                          <span className="text-xs font-bold text-slate-400">
                            {entry.roomNumber}
                          </span>
                        </div>

                        {/* Vitals summary */}
                        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 bg-slate-900/60 p-3 rounded-2xl text-xs">
                          <div>
                            <span className="text-[10px] text-slate-500 uppercase block">Blood Pressure</span>
                            <span className="font-bold text-white">{entry.vitals.bloodPressure}</span>
                          </div>
                          <div>
                            <span className="text-[10px] text-slate-500 uppercase block">Pulse Rate</span>
                            <span className="font-bold text-white">{entry.vitals.pulseRate}</span>
                          </div>
                          <div>
                            <span className="text-[10px] text-slate-500 uppercase block">SpO2 Oxygen</span>
                            <span className="font-bold text-white">{entry.vitals.oxygenSaturationSpO2}</span>
                          </div>
                          <div>
                            <span className="text-[10px] text-slate-500 uppercase block">Fasting Sugar</span>
                            <span className="font-bold text-white">{entry.vitals.bloodSugarFasting || 'N/A'}</span>
                          </div>
                        </div>

                        {/* Chief complaint & clinical notes */}
                        <div className="space-y-1.5 text-xs text-slate-300">
                          <p>
                            <strong className="text-slate-400">Chief Complaint:</strong> {entry.chiefComplaint}
                          </p>
                          <p>
                            <strong className="text-slate-400">Clinical Notes:</strong> {entry.clinicalNotes}
                          </p>
                          {entry.doctorAdvice && (
                            <p>
                              <strong className="text-slate-400">Physician Advice:</strong> {entry.doctorAdvice}
                            </p>
                          )}
                        </div>

                        {/* Prescribed Medicines with schedule */}
                        {entry.medicines.length > 0 && (
                          <div className="space-y-2 pt-2 border-t border-slate-700/60">
                            <span className="text-xs font-black uppercase tracking-wider text-slate-400 flex items-center gap-1.5">
                              <Pill className="w-3.5 h-3.5 text-indigo-400" />
                              Prescription Schedule ({entry.medicines.length}):
                            </span>

                            <div className="space-y-2">
                              {entry.medicines.map((med) => (
                                <div
                                  key={med.id}
                                  className="p-3 bg-slate-900/80 rounded-2xl border border-slate-700/60 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 text-xs"
                                >
                                  <div>
                                    <div className="flex items-center gap-2">
                                      <span className="font-black text-white text-sm">
                                        {med.medicineName}
                                      </span>
                                      <span className="text-[10px] font-bold text-indigo-300 bg-indigo-950 px-2 py-0.5 rounded">
                                        {med.dosage}
                                      </span>
                                      <span
                                        className={`text-[10px] font-bold px-2 py-0.5 rounded ${
                                          med.status === 'Active'
                                            ? 'bg-emerald-950 text-emerald-300'
                                            : 'bg-slate-700 text-slate-400'
                                        }`}
                                      >
                                        {med.status}
                                      </span>
                                    </div>
                                    <p className="text-slate-400 mt-1">
                                      Timing: <strong>{med.foodTiming.replace(/_/g, ' ')}</strong> • Frequency: {med.frequency.replace(/_/g, ' ')} • Duration: {med.duration}
                                    </p>
                                    {med.specialInstructions && (
                                      <p className="text-[11px] text-amber-300 mt-0.5">
                                        Note: {med.specialInstructions}
                                      </p>
                                    )}
                                  </div>

                                  <div className="flex items-center gap-1.5">
                                    <button
                                      onClick={() => {
                                        const newSt = med.status === 'Active' ? 'Discontinued' : 'Active';
                                        onUpdateMedicineStatus(selectedPatient.patientId, med.id, newSt);
                                        triggerFeedback(`Updated ${med.medicineName} status to ${newSt}.`);
                                      }}
                                      className="px-2.5 py-1 bg-slate-800 hover:bg-slate-700 text-slate-300 text-[10px] font-bold rounded-lg border border-slate-700 cursor-pointer"
                                    >
                                      {med.status === 'Active' ? 'Discontinue' : 'Reactivate'}
                                    </button>
                                  </div>
                                </div>
                              ))}
                            </div>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                </>
              ) : (
                <div className="p-8 text-center text-slate-500">
                  Select a patient to inspect consultation history and prescriptions.
                </div>
              )}
            </div>
          </div>
        )}

        {/* 3. STAFF ASSISTANCE DESK TAB */}
        {activeTab === 'STAFF' && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-lg font-black text-white">
                  Kiosk In-Person & Wheelchair Help Requests
                </h3>
                <p className="text-xs text-slate-400">
                  Real-time distress and mobility requests from atrium terminals
                </p>
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {staffRequests.map((req) => (
                <div
                  key={req.id}
                  className={`p-5 rounded-3xl border transition shadow-sm ${
                    req.status === 'PENDING'
                      ? 'bg-amber-950/20 border-amber-500/50'
                      : req.status === 'DISPATCHED'
                      ? 'bg-sky-950/20 border-sky-500/50'
                      : 'bg-slate-800/80 border-slate-700 text-slate-400'
                  }`}
                >
                  <div className="flex items-start justify-between gap-2 mb-2">
                    <div>
                      <span className="text-[10px] font-black uppercase tracking-wider text-slate-400 block mb-0.5">
                        {req.location} • {req.timestamp}
                      </span>
                      <h4 className="text-base font-black text-white">
                        {req.patientName}
                      </h4>
                    </div>
                    <span
                      className={`px-2.5 py-1 rounded-full text-[10px] font-black uppercase ${
                        req.status === 'PENDING'
                          ? 'bg-amber-500 text-slate-950'
                          : req.status === 'DISPATCHED'
                          ? 'bg-sky-500 text-white'
                          : 'bg-emerald-900 text-emerald-200'
                      }`}
                    >
                      {req.status}
                    </span>
                  </div>

                  <p className="text-xs text-slate-200 font-medium mb-3">
                    Reason: {req.reason}
                  </p>

                  {req.assignedStaff && (
                    <p className="text-[11px] text-sky-400 mb-3">
                      Assigned Responder: <strong>{req.assignedStaff}</strong>
                    </p>
                  )}

                  <div className="flex items-center gap-2 pt-2 border-t border-slate-700/60">
                    {req.status === 'PENDING' && (
                      <button
                        onClick={() => {
                          onDispatchStaff(req.id);
                          triggerFeedback(`Staff dispatched for ${req.patientName}!`);
                        }}
                        className="px-3.5 py-1.5 bg-sky-600 hover:bg-sky-700 text-white rounded-xl text-xs font-bold transition active:scale-95 cursor-pointer"
                      >
                        Dispatch Escort Nurse
                      </button>
                    )}
                    {req.status !== 'RESOLVED' && (
                      <button
                        onClick={() => {
                          onResolveStaffRequest(req.id);
                          triggerFeedback(`Resolved request ${req.id}.`);
                        }}
                        className="px-3.5 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl text-xs font-bold transition active:scale-95 cursor-pointer"
                      >
                        Mark Completed
                      </button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* 4. SYSTEM STATUS TAB */}
        {activeTab === 'SYSTEM' && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-slate-800/80 p-5 rounded-3xl border border-slate-700 space-y-2">
              <span className="text-[10px] font-black uppercase tracking-wider text-slate-400">
                Hospital Telemetry
              </span>
              <div className="text-2xl font-black text-emerald-400">ONLINE 99.9%</div>
              <p className="text-xs text-slate-400">Vite + Node Server Port 3000</p>
            </div>

            <div className="bg-slate-800/80 p-5 rounded-3xl border border-slate-700 space-y-2">
              <span className="text-[10px] font-black uppercase tracking-wider text-slate-400">
                AI Engine
              </span>
              <div className="text-2xl font-black text-indigo-400">Gemini 2.5 Flash</div>
              <p className="text-xs text-slate-400">Triage & Multimodal Document OCR</p>
            </div>

            <div className="bg-slate-800/80 p-5 rounded-3xl border border-slate-700 space-y-2">
              <span className="text-[10px] font-black uppercase tracking-wider text-slate-400">
                Privacy Watchdog
              </span>
              <div className="text-2xl font-black text-amber-400">ACTIVE</div>
              <p className="text-xs text-slate-400">180s Session Inactivity Timer</p>
            </div>

            <div className="bg-slate-800/80 p-5 rounded-3xl border border-slate-700 space-y-2">
              <span className="text-[10px] font-black uppercase tracking-wider text-slate-400">
                Emergency Beacon
              </span>
              <div className="text-2xl font-black text-rose-400">READY</div>
              <p className="text-xs text-slate-400">Code 108 Direct Hospital Dispatch</p>
            </div>
          </div>
        )}
      </main>

      {/* Record New Consultation Modal */}
      {showConsultModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-fade-in">
          <div className="bg-slate-900 border border-slate-700 rounded-3xl max-w-3xl w-full max-h-[90vh] flex flex-col overflow-hidden shadow-2xl">
            <div className="p-5 bg-slate-950 border-b border-slate-800 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="p-2.5 bg-indigo-600/20 text-indigo-400 rounded-2xl">
                  <Stethoscope className="w-5 h-5" />
                </div>
                <div>
                  <h3 className="text-base font-black text-white">
                    Record New Clinical Consultation & Prescriptions
                  </h3>
                  <p className="text-xs text-slate-400">
                    Official EHR Entry • Signed by {formPatient.doctorName}
                  </p>
                </div>
              </div>
              <button
                onClick={() => setShowConsultModal(false)}
                className="p-2 text-slate-400 hover:text-white rounded-xl transition cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-6 overflow-y-auto space-y-5 text-xs text-slate-300">
              {/* Patient Profile Details */}
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div>
                  <label className="font-bold text-slate-400 block mb-1">Patient Full Name</label>
                  <input
                    type="text"
                    value={formPatient.patientName}
                    onChange={(e) => setFormPatient({ ...formPatient, patientName: e.target.value })}
                    className="w-full p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-white font-bold"
                  />
                </div>
                <div>
                  <label className="font-bold text-slate-400 block mb-1">Age & Gender</label>
                  <div className="flex gap-2">
                    <input
                      type="text"
                      value={formPatient.age}
                      onChange={(e) => setFormPatient({ ...formPatient, age: e.target.value })}
                      className="w-16 p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-white"
                    />
                    <select
                      value={formPatient.gender}
                      onChange={(e) => setFormPatient({ ...formPatient, gender: e.target.value })}
                      className="flex-1 p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-white"
                    >
                      <option>Male</option>
                      <option>Female</option>
                      <option>Other</option>
                    </select>
                  </div>
                </div>
                <div>
                  <label className="font-bold text-slate-400 block mb-1">Department</label>
                  <select
                    value={formPatient.department}
                    onChange={(e) => setFormPatient({ ...formPatient, department: e.target.value })}
                    className="w-full p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-white"
                  >
                    {departments.map((d) => (
                      <option key={d}>{d}</option>
                    ))}
                  </select>
                </div>
              </div>

              {/* Vitals row */}
              <div className="p-3 bg-slate-950/60 rounded-2xl border border-slate-800 space-y-2">
                <span className="font-black text-slate-400 uppercase tracking-wider block text-[10px]">
                  Consultation Vitals:
                </span>
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                  <div>
                    <label className="text-[10px] text-slate-500 block">BP</label>
                    <input
                      type="text"
                      value={formPatient.bp}
                      onChange={(e) => setFormPatient({ ...formPatient, bp: e.target.value })}
                      className="w-full p-1.5 bg-slate-800 border border-slate-700 rounded-lg text-white"
                    />
                  </div>
                  <div>
                    <label className="text-[10px] text-slate-500 block">Pulse</label>
                    <input
                      type="text"
                      value={formPatient.pulse}
                      onChange={(e) => setFormPatient({ ...formPatient, pulse: e.target.value })}
                      className="w-full p-1.5 bg-slate-800 border border-slate-700 rounded-lg text-white"
                    />
                  </div>
                  <div>
                    <label className="text-[10px] text-slate-500 block">SpO2</label>
                    <input
                      type="text"
                      value={formPatient.spo2}
                      onChange={(e) => setFormPatient({ ...formPatient, spo2: e.target.value })}
                      className="w-full p-1.5 bg-slate-800 border border-slate-700 rounded-lg text-white"
                    />
                  </div>
                  <div>
                    <label className="text-[10px] text-slate-500 block">Fasting Sugar</label>
                    <input
                      type="text"
                      value={formPatient.sugar}
                      onChange={(e) => setFormPatient({ ...formPatient, sugar: e.target.value })}
                      className="w-full p-1.5 bg-slate-800 border border-slate-700 rounded-lg text-white"
                    />
                  </div>
                </div>
              </div>

              {/* Chief complaint & Diagnosis */}
              <div className="space-y-3">
                <div>
                  <label className="font-bold text-slate-400 block mb-1">Chief Complaint</label>
                  <input
                    type="text"
                    value={formPatient.chiefComplaint}
                    onChange={(e) => setFormPatient({ ...formPatient, chiefComplaint: e.target.value })}
                    placeholder="e.g. Occasional occipital headache and fatigue"
                    className="w-full p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-white"
                  />
                </div>
                <div>
                  <label className="font-bold text-slate-400 block mb-1">Clinical Diagnosis</label>
                  <input
                    type="text"
                    value={formPatient.diagnosis}
                    onChange={(e) => setFormPatient({ ...formPatient, diagnosis: e.target.value })}
                    placeholder="e.g. Stage 1 Essential Hypertension"
                    className="w-full p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-white font-bold text-indigo-300"
                  />
                </div>
                <div>
                  <label className="font-bold text-slate-400 block mb-1">Clinical Notes & Findings</label>
                  <textarea
                    rows={2}
                    value={formPatient.clinicalNotes}
                    onChange={(e) => setFormPatient({ ...formPatient, clinicalNotes: e.target.value })}
                    placeholder="Physical examination notes, auscultation, risk factor counseling..."
                    className="w-full p-2.5 bg-slate-800 border border-slate-700 rounded-xl text-white"
                  />
                </div>
              </div>

              {/* Medicine Builder Section */}
              <div className="space-y-3 p-4 bg-slate-950/60 rounded-2xl border border-slate-800">
                <span className="font-black text-slate-300 uppercase tracking-wider block text-xs">
                  Add Prescription Medicines ({formMeds.length}):
                </span>

                <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
                  <input
                    type="text"
                    value={newMed.name}
                    onChange={(e) => setNewMed({ ...newMed, name: e.target.value })}
                    placeholder="Medicine Name (e.g. Telmisartan)"
                    className="p-2 bg-slate-800 border border-slate-700 rounded-xl text-white"
                  />
                  <input
                    type="text"
                    value={newMed.dosage}
                    onChange={(e) => setNewMed({ ...newMed, dosage: e.target.value })}
                    placeholder="Dosage (e.g. 40 mg)"
                    className="p-2 bg-slate-800 border border-slate-700 rounded-xl text-white"
                  />
                  <select
                    value={newMed.foodTiming}
                    onChange={(e) => setNewMed({ ...newMed, foodTiming: e.target.value as any })}
                    className="p-2 bg-slate-800 border border-slate-700 rounded-xl text-white"
                  >
                    <option value="AFTER_BREAKFAST">After Breakfast</option>
                    <option value="BEFORE_BREAKFAST_EMPTY_STOMACH">Before Breakfast (Empty Stomach)</option>
                    <option value="AFTER_LUNCH">After Lunch</option>
                    <option value="AFTER_DINNER">After Dinner</option>
                    <option value="BEDTIME">At Bedtime (HS)</option>
                    <option value="WITH_MEALS">With Meals</option>
                    <option value="AS_NEEDED_PRN">As Needed (SOS)</option>
                  </select>
                </div>

                <div className="flex gap-2">
                  <input
                    type="text"
                    value={newMed.instructions}
                    onChange={(e) => setNewMed({ ...newMed, instructions: e.target.value })}
                    placeholder="Special instructions (e.g. Take daily at 8 AM with water)"
                    className="flex-1 p-2 bg-slate-800 border border-slate-700 rounded-xl text-white"
                  />
                  <button
                    type="button"
                    onClick={handleAddMedToForm}
                    className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-xl flex items-center gap-1 cursor-pointer"
                  >
                    <Plus className="w-4 h-4" />
                    <span>Add Med</span>
                  </button>
                </div>

                {/* List of currently staged meds */}
                {formMeds.length > 0 && (
                  <div className="space-y-1.5 pt-2">
                    {formMeds.map((m, idx) => (
                      <div key={idx} className="flex items-center justify-between p-2 bg-slate-800 rounded-lg text-xs">
                        <span><strong>{m.medicineName}</strong> {m.dosage} • {m.foodTiming.replace(/_/g, ' ')}</span>
                        <button
                          type="button"
                          onClick={() => setFormMeds(formMeds.filter((_, i) => i !== idx))}
                          className="text-rose-400 hover:text-rose-300 font-bold"
                        >
                          Remove
                        </button>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>

            <div className="p-4 bg-slate-950 border-t border-slate-800 flex justify-end gap-3">
              <button
                type="button"
                onClick={() => setShowConsultModal(false)}
                className="px-4 py-2 bg-slate-800 text-slate-300 font-bold text-xs rounded-xl cursor-pointer"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleSaveConsultation}
                className="px-5 py-2 bg-indigo-600 hover:bg-indigo-700 text-white font-bold text-xs rounded-xl cursor-pointer"
              >
                Sign & Commit Consultation to Master EHR
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
