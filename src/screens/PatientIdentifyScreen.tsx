import React from 'react';
import {
  User,
  Calendar,
  Volume2,
  ArrowRight,
  ShieldCheck,
  CheckCircle,
  FileBadge,
} from 'lucide-react';
import { Language, PatientProfile } from '../types';
import { IntakeStepProgressHeader } from '../components/IntakeStepProgressHeader';
import { speakText } from '../utils/speech';

interface PatientIdentifyScreenProps {
  profile: PatientProfile;
  selectedLanguage: Language;
  onNameChange: (name: string) => void;
  onAgeChange: (age: string) => void;
  onGenderChange: (gender: string) => void;
  onAbhaChange: (id: string) => void;
  onNewPatientToggle: (isNew: boolean) => void;
  onConsentToggle: (consent: boolean) => void;
  onReadConsentAloud: () => void;
  onProceedClick: () => void;
}

export const PatientIdentifyScreen: React.FC<PatientIdentifyScreenProps> = ({
  profile,
  selectedLanguage,
  onNameChange,
  onAgeChange,
  onGenderChange,
  onAbhaChange,
  onNewPatientToggle,
  onConsentToggle,
  onReadConsentAloud,
  onProceedClick,
}) => {
  const ageChips = ['24', '35', '45', '54', '68'];
  const genders = ['Male', 'Female', 'Other'];

  const canProceed = profile.name.trim().length > 0 && profile.consentGiven;

  return (
    <div className="min-h-screen bg-slate-50 p-4 sm:p-6 pb-24">
      <div className="max-w-2xl mx-auto space-y-4">
        {/* Step 1 Progress Bar */}
        <IntakeStepProgressHeader
          step={1}
          totalSteps={4}
          stepTitle="Patient Identification & Consent"
        />

        {/* Identification Form Card */}
        <div className="bg-white rounded-3xl border border-slate-200 p-6 sm:p-8 shadow-sm space-y-5">
          <div className="border-b border-slate-100 pb-3">
            <h2 className="text-xl font-black text-slate-900">
              Personal Information
            </h2>
            <p className="text-xs text-slate-500 mt-0.5">
              Enter your demographic details to create or link your hospital EHR record.
            </p>
          </div>

          {/* Full Name */}
          <div>
            <label className="block text-xs font-black uppercase tracking-wider text-slate-500 mb-1.5">
              Full Legal Name / पूरा नाम *
            </label>
            <div className="relative">
              <input
                type="text"
                value={profile.name}
                onChange={(e) => onNameChange(e.target.value)}
                placeholder="e.g. Rajesh Sharma"
                className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-bold text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-sky-500"
              />
              <User className="w-5 h-5 text-sky-600 absolute left-3.5 top-3.5" />
            </div>
          </div>

          {/* Age with Quick Age Selectors */}
          <div>
            <label className="block text-xs font-black uppercase tracking-wider text-slate-500 mb-1.5">
              Age (Years) / उम्र
            </label>
            <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-3">
              <div className="relative w-full sm:w-36">
                <input
                  type="number"
                  value={profile.age}
                  onChange={(e) => onAgeChange(e.target.value)}
                  placeholder="54"
                  className="w-full pl-10 pr-3 py-3 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-bold text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-sky-500"
                />
                <Calendar className="w-5 h-5 text-sky-600 absolute left-3.5 top-3.5" />
              </div>

              {/* Quick Age Chips */}
              <div className="flex items-center gap-1.5 overflow-x-auto py-1">
                <span className="text-[11px] font-bold text-slate-400 mr-1 hidden sm:inline">
                  Quick:
                </span>
                {ageChips.map((chip) => (
                  <button
                    key={chip}
                    type="button"
                    onClick={() => onAgeChange(chip)}
                    className={`px-3 py-2 rounded-xl text-xs font-bold border transition cursor-pointer active:scale-95 ${
                      profile.age === chip
                        ? 'bg-sky-600 text-white border-sky-700'
                        : 'bg-slate-100 hover:bg-slate-200 text-slate-700 border-slate-200'
                    }`}
                  >
                    {chip} yrs
                  </button>
                ))}
              </div>
            </div>
          </div>

          {/* Gender */}
          <div>
            <label className="block text-xs font-black uppercase tracking-wider text-slate-500 mb-1.5">
              Gender / लिंग
            </label>
            <div className="grid grid-cols-3 gap-2">
              {genders.map((g) => (
                <button
                  key={g}
                  type="button"
                  onClick={() => onGenderChange(g)}
                  className={`py-3 rounded-2xl text-xs font-bold border transition cursor-pointer active:scale-95 ${
                    profile.gender.toLowerCase() === g.toLowerCase()
                      ? 'bg-sky-600 text-white border-sky-700 shadow-sm'
                      : 'bg-slate-50 hover:bg-slate-100 text-slate-700 border-slate-200'
                  }`}
                >
                  {g}
                </button>
              ))}
            </div>
          </div>

          {/* ABHA ID / Health Card */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="block text-xs font-black uppercase tracking-wider text-slate-500">
                Ayushman Bharat Health Account (ABHA ID)
              </label>
              <label className="flex items-center gap-1.5 text-xs text-slate-600 cursor-pointer">
                <input
                  type="checkbox"
                  checked={profile.isNewPatient}
                  onChange={(e) => onNewPatientToggle(e.target.checked)}
                  className="rounded text-sky-600 focus:ring-sky-500 w-4 h-4 cursor-pointer"
                />
                <span className="font-semibold">New Patient (No ABHA)</span>
              </label>
            </div>

            {!profile.isNewPatient ? (
              <div className="relative">
                <input
                  type="text"
                  value={profile.abhaId}
                  onChange={(e) => onAbhaChange(e.target.value)}
                  placeholder="14-2951-8402-1920"
                  className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-mono text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-sky-500"
                />
                <FileBadge className="w-5 h-5 text-sky-600 absolute left-3.5 top-3.5" />
              </div>
            ) : (
              <div className="p-3 bg-slate-100 rounded-2xl text-xs text-slate-600 border border-slate-200">
                A temporary hospital visit MRN token will be issued for this consultation.
              </div>
            )}
          </div>

          {/* Informed Clinical Consent */}
          <div className="p-4 bg-sky-50 border border-sky-200 rounded-2xl space-y-2">
            <div className="flex items-start justify-between gap-3">
              <label className="flex items-start gap-3 cursor-pointer">
                <input
                  type="checkbox"
                  checked={profile.consentGiven}
                  onChange={(e) => onConsentToggle(e.target.checked)}
                  className="rounded text-sky-600 focus:ring-sky-500 w-5 h-5 mt-0.5 cursor-pointer flex-shrink-0"
                />
                <span className="text-xs text-slate-800 leading-relaxed font-medium">
                  {selectedLanguage.consentText}
                </span>
              </label>

              <button
                type="button"
                onClick={onReadConsentAloud}
                className="p-2 text-sky-700 hover:bg-sky-200/50 rounded-xl transition cursor-pointer flex-shrink-0"
                title="Read consent aloud"
              >
                <Volume2 className="w-5 h-5" />
              </button>
            </div>
          </div>

          {/* Submit Button */}
          <button
            type="button"
            disabled={!canProceed}
            onClick={onProceedClick}
            className="w-full py-4 bg-sky-600 hover:bg-sky-700 disabled:opacity-50 text-white font-black text-sm rounded-2xl shadow-md shadow-sky-600/20 transition flex items-center justify-center gap-2 cursor-pointer active:scale-98"
          >
            <span>Proceed to Step 2: Conversational Intake</span>
            <ArrowRight className="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  );
};
