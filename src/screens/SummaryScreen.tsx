import React, { useState } from 'react';
import {
  FileText,
  Sparkles,
  CheckCircle,
  AlertTriangle,
  Clock,
  Printer,
  Edit3,
  RefreshCw,
  ArrowRight,
  RotateCcw,
  Building2,
  X,
  MapPin,
  Pill,
} from 'lucide-react';
import { ClinicalSummary, PatientProfile } from '../types';
import { IntakeStepProgressHeader } from '../components/IntakeStepProgressHeader';

interface SummaryScreenProps {
  patient: PatientProfile;
  summary: ClinicalSummary | null;
  isGenerating: boolean;
  isSubmitted: boolean;
  onEditSection: (section: string, content: string) => void;
  onSubmitClick: () => void;
  onResetForNewPatient: () => void;
  onRegenerateSummary: () => void;
}

export const SummaryScreen: React.FC<SummaryScreenProps> = ({
  patient,
  summary,
  isGenerating,
  isSubmitted,
  onEditSection,
  onSubmitClick,
  onResetForNewPatient,
  onRegenerateSummary,
}) => {
  const [editingSection, setEditingSection] = useState<string | null>(null);
  const [editContent, setEditContent] = useState('');

  if (isGenerating || !summary) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center p-6 text-center">
        <div className="bg-white p-8 rounded-3xl shadow-sm border border-slate-200 max-w-md w-full space-y-4 animate-pulse">
          <div className="w-16 h-16 rounded-full bg-sky-100 text-sky-600 mx-auto flex items-center justify-center">
            <Sparkles className="w-8 h-8 animate-spin" />
          </div>
          <h2 className="text-xl font-black text-slate-900">
            Synthesizing Clinical SOAP Summary...
          </h2>
          <p className="text-xs text-slate-500 leading-relaxed">
            Gemini is correlating SOCRATES interview findings with scanned prescription & lab metrics to draft a doctor-ready intake note.
          </p>
        </div>
      </div>
    );
  }

  // Submitted Token Receipt View
  if (isSubmitted) {
    return (
      <div className="min-h-screen bg-slate-50 p-4 sm:p-6 pb-24 flex items-center justify-center">
        <div className="max-w-md w-full bg-white rounded-3xl border-2 border-emerald-500 shadow-2xl p-6 sm:p-8 space-y-6 text-center animate-fade-in">
          <div className="w-16 h-16 rounded-3xl bg-emerald-100 text-emerald-700 mx-auto flex items-center justify-center">
            <CheckCircle className="w-9 h-9" />
          </div>

          <div>
            <span className="text-[10px] font-black uppercase tracking-widest text-emerald-700 bg-emerald-50 px-2.5 py-1 rounded-full border border-emerald-200">
              EHR Intake Committed & Signed
            </span>
            <h2 className="text-2xl font-black text-slate-900 mt-2">
              Check-In Successful!
            </h2>
            <p className="text-xs text-slate-500 mt-1">
              Your clinical summary has been routed to the attending physician's desk.
            </p>
          </div>

          {/* Hospital Token Card */}
          <div className="p-5 bg-slate-50 border border-slate-200 rounded-3xl space-y-3 text-left">
            <div className="flex items-center justify-between border-b border-slate-200 pb-3">
              <div>
                <span className="text-[10px] font-bold text-slate-400 uppercase">Assigned Token</span>
                <div className="text-3xl font-black text-sky-700 font-mono">
                  {summary.tokenNumber}
                </div>
              </div>
              <div className="text-right">
                <span className="text-[10px] font-bold text-slate-400 uppercase">Priority</span>
                <span className="block px-2 py-0.5 rounded-lg text-xs font-black bg-sky-100 text-sky-800">
                  {summary.triagePriority}
                </span>
              </div>
            </div>

            <div className="space-y-1.5 text-xs">
              <div className="flex justify-between">
                <span className="text-slate-500">Patient:</span>
                <span className="font-bold text-slate-800">{patient.name} ({patient.age}y/{patient.gender})</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Department:</span>
                <span className="font-bold text-slate-800">{summary.recommendedDepartment}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Estimated Wait:</span>
                <span className="font-bold text-emerald-700">~{summary.estimatedWaitMinutes} minutes</span>
              </div>
            </div>

            {/* Mock barcode strip */}
            <div className="pt-2 text-center">
              <div className="h-8 bg-slate-900/10 rounded flex items-center justify-around px-2 font-mono text-[9px] tracking-widest text-slate-500 select-none">
                |||||| | |||| || |||||| | |||||||| ||||
              </div>
              <span className="text-[10px] text-slate-400 font-mono block mt-1">
                REF: {summary.tokenNumber}-EHR-2026
              </span>
            </div>
          </div>

          {/* Actions */}
          <div className="space-y-2">
            <button
              onClick={() => window.print()}
              className="w-full py-3 bg-slate-900 hover:bg-slate-800 text-white rounded-2xl font-bold text-xs flex items-center justify-center gap-2 cursor-pointer transition active:scale-98"
            >
              <Printer className="w-4 h-4" />
              <span>Print Check-in Slip</span>
            </button>

            <button
              onClick={onResetForNewPatient}
              className="w-full py-3 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-2xl font-bold text-xs flex items-center justify-center gap-2 cursor-pointer transition active:scale-98"
            >
              <RotateCcw className="w-4 h-4" />
              <span>Reset Kiosk for Next Patient</span>
            </button>
          </div>
        </div>
      </div>
    );
  }

  const sections = [
    { title: 'Chief Complaint', content: summary.chiefComplaint },
    { title: 'History of Present Illness', content: summary.historyOfPresentIllness },
    { title: 'Past Medical/Surgical History', content: summary.pastMedicalHistory },
    { title: 'Drug & Allergy History', content: summary.drugAndAllergyHistory },
    { title: 'Family History', content: summary.familyHistory },
    { title: 'Review of Systems', content: summary.reviewOfSystems },
    { title: 'Prior Investigations', content: summary.priorInvestigations },
  ];

  return (
    <div className="min-h-screen bg-slate-50 p-4 sm:p-6 pb-24">
      <div className="max-w-3xl mx-auto space-y-4">
        {/* Step 4 Progress Bar */}
        <IntakeStepProgressHeader
          step={4}
          totalSteps={4}
          stepTitle="Clinical Summary & Doctor Review (SOAP)"
        />

        {/* Patient & Triage Header */}
        <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-sm space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-100 pb-3">
            <div>
              <span className="text-[10px] font-black uppercase tracking-wider text-sky-600 bg-sky-50 px-2 py-0.5 rounded">
                Verified Clinical Intake
              </span>
              <h2 className="text-xl font-black text-slate-900 mt-1">
                {patient.name}
              </h2>
              <p className="text-xs text-slate-500">
                {patient.age} yrs • {patient.gender} • {patient.isNewPatient ? 'New Patient' : `ABHA: ${patient.abhaId}`}
              </p>
            </div>

            {/* Triage Priority Badge */}
            <div className="flex items-center gap-2 self-start sm:self-auto">
              <span
                className={`px-3 py-1 rounded-xl text-xs font-black uppercase tracking-wider ${
                  summary.triagePriority === 'Urgent'
                    ? 'bg-rose-100 text-rose-800 border border-rose-300'
                    : 'bg-sky-100 text-sky-800 border border-sky-300'
                }`}
              >
                {summary.triagePriority} Triage
              </span>

              <button
                onClick={onRegenerateSummary}
                className="p-2 text-slate-400 hover:text-sky-700 rounded-xl hover:bg-slate-100 transition cursor-pointer"
                title="Regenerate with Gemini"
              >
                <RefreshCw className="w-4 h-4" />
              </button>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs bg-slate-50 p-3.5 rounded-2xl border border-slate-200">
            <div>
              <span className="text-[10px] text-slate-400 font-bold uppercase block">
                Recommended Department
              </span>
              <strong className="text-slate-900 text-sm">{summary.recommendedDepartment}</strong>
            </div>
            <div>
              <span className="text-[10px] text-slate-400 font-bold uppercase block">
                Triage Clinical Reason
              </span>
              <span className="text-slate-700 font-medium">{summary.triageReason}</span>
            </div>
          </div>
        </div>

        {/* Structured Sections */}
        <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-sm space-y-4">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <h3 className="text-base font-black text-slate-900">
              Doctor-Readable SOAP Sections
            </h3>
            <span className="text-xs text-slate-400">Tap edit on any section to customize</span>
          </div>

          <div className="space-y-4 divide-y divide-slate-100">
            {sections.map((s) => (
              <div key={s.title} className="pt-3 first:pt-0">
                <div className="flex items-center justify-between mb-1">
                  <h4 className="text-xs font-black uppercase tracking-wider text-slate-500">
                    {s.title}
                  </h4>
                  <button
                    onClick={() => {
                      setEditingSection(s.title);
                      setEditContent(s.content);
                    }}
                    className="flex items-center gap-1 text-[11px] font-bold text-sky-600 hover:text-sky-800 transition cursor-pointer"
                  >
                    <Edit3 className="w-3.5 h-3.5" />
                    <span>Edit</span>
                  </button>
                </div>
                <p className="text-xs sm:text-sm text-slate-800 leading-relaxed font-medium whitespace-pre-line">
                  {s.content || 'None declared.'}
                </p>
              </div>
            ))}
          </div>
        </div>

        {/* Final Sign-Off & Transmit Button */}
        <button
          onClick={onSubmitClick}
          className="w-full py-4 bg-emerald-600 hover:bg-emerald-700 active:scale-98 text-white font-black text-sm rounded-2xl shadow-lg shadow-emerald-600/25 transition flex items-center justify-center gap-2 cursor-pointer"
        >
          <CheckCircle className="w-5 h-5" />
          <span>Confirm & Transmit Intake to Hospital Attending Physician</span>
        </button>
      </div>

      {/* Edit Section Modal */}
      {editingSection && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-3xl max-w-lg w-full p-6 shadow-2xl border border-slate-200 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-black text-slate-900">
                Edit Section: {editingSection}
              </h3>
              <button
                onClick={() => setEditingSection(null)}
                className="p-1 text-slate-400 hover:text-slate-600 rounded-lg cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <textarea
              rows={5}
              value={editContent}
              onChange={(e) => setEditContent(e.target.value)}
              className="w-full p-3 bg-slate-50 border border-slate-200 rounded-2xl text-xs sm:text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-sky-500"
            />

            <div className="flex gap-2">
              <button
                onClick={() => {
                  onEditSection(editingSection, editContent);
                  setEditingSection(null);
                }}
                className="flex-1 py-2.5 bg-sky-600 hover:bg-sky-700 text-white font-bold text-xs rounded-xl cursor-pointer"
              >
                Save Changes
              </button>
              <button
                onClick={() => setEditingSection(null)}
                className="px-4 py-2.5 bg-slate-200 text-slate-800 font-bold text-xs rounded-xl cursor-pointer"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
