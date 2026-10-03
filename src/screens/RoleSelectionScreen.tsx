import React from 'react';
import {
  Building2,
  Globe,
  User,
  Shield,
  ArrowRight,
  Zap,
  Lock,
  Activity,
  CheckCircle,
} from 'lucide-react';
import { Language } from '../types';

interface RoleSelectionScreenProps {
  selectedLanguage: Language;
  onSelectPatientRole: () => void;
  onSelectAdminRole: () => void;
  onQuickDemoLaunch: () => void;
  onLanguageClick: () => void;
}

export const RoleSelectionScreen: React.FC<RoleSelectionScreenProps> = ({
  selectedLanguage,
  onSelectPatientRole,
  onSelectAdminRole,
  onQuickDemoLaunch,
  onLanguageClick,
}) => {
  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-50 via-sky-50/30 to-slate-100 flex flex-col justify-between p-4 sm:p-6">
      {/* Top Header */}
      <div className="w-full max-w-2xl mx-auto flex items-center justify-between pt-2">
        {/* Language selector button */}
        <button
          onClick={onLanguageClick}
          className="flex items-center gap-2 px-3.5 py-2 bg-white/90 hover:bg-white rounded-2xl border border-slate-200 shadow-sm text-slate-800 text-xs font-bold transition active:scale-95 cursor-pointer"
        >
          <Globe className="w-4 h-4 text-sky-600" />
          <span>{selectedLanguage.nativeName}</span>
        </button>

        {/* Status Indicator */}
        <div className="flex items-center gap-2 px-3 py-1.5 bg-emerald-50 border border-emerald-200 rounded-full text-emerald-700 text-xs font-semibold">
          <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse" />
          <span>Kiosk Terminal Online</span>
        </div>
      </div>

      {/* Main Content Card Container */}
      <div className="w-full max-w-xl mx-auto my-auto py-8 text-center flex flex-col items-center">
        {/* Brand Logo Emblem */}
        <div className="relative mb-4 group">
          <div className="w-20 h-20 rounded-3xl bg-gradient-to-tr from-sky-600 to-indigo-600 shadow-xl shadow-sky-600/25 flex items-center justify-center text-white border-2 border-white">
            <Building2 className="w-10 h-10" />
          </div>
          <div className="absolute -bottom-1 -right-1 p-1.5 bg-emerald-500 text-white rounded-xl shadow border-2 border-white">
            <Activity className="w-3.5 h-3.5" />
          </div>
        </div>

        <h1 className="text-3xl sm:text-4xl font-black text-slate-900 tracking-tight">
          CAREFLOW AI
        </h1>
        <p className="text-base sm:text-lg font-semibold text-sky-700 mt-1">
          AI-Powered Hospital & Critical Care Assistant
        </p>
        <p className="text-xs text-slate-500 max-w-md mx-auto mt-1">
          City General Hospital & Apollo Healthcare Smart Intake Kiosk
        </p>

        {/* Role Cards Container */}
        <div className="w-full mt-8 space-y-3.5">
          <div className="flex items-center justify-center gap-3 my-2">
            <div className="h-px bg-slate-200 flex-1" />
            <span className="text-xs font-black uppercase tracking-wider text-slate-400">
              Continue As
            </span>
            <div className="h-px bg-slate-200 flex-1" />
          </div>

          {/* 1. Patient Role Card */}
          <button
            onClick={onSelectPatientRole}
            className="w-full bg-white hover:bg-slate-50 border-2 border-slate-200 hover:border-sky-500 rounded-3xl p-5 shadow-sm hover:shadow-md transition-all duration-200 flex items-center justify-between text-left group cursor-pointer active:scale-98"
          >
            <div className="flex items-center gap-4">
              <div className="w-14 h-14 rounded-2xl bg-sky-100 text-sky-700 flex items-center justify-center flex-shrink-0 group-hover:bg-sky-600 group-hover:text-white transition-colors duration-200">
                <User className="w-7 h-7" />
              </div>
              <div>
                <h3 className="text-lg font-black text-slate-900 group-hover:text-sky-700 transition-colors">
                  PATIENT / VISITOR
                </h3>
                <p className="text-xs text-slate-500 font-medium leading-relaxed mt-0.5">
                  Check-in, AI symptom triage, scan prescriptions & view queues
                </p>
              </div>
            </div>
            <div className="w-10 h-10 rounded-full bg-slate-100 group-hover:bg-sky-50 text-slate-400 group-hover:text-sky-600 flex items-center justify-center flex-shrink-0 transition">
              <ArrowRight className="w-5 h-5" />
            </div>
          </button>

          {/* 2. Admin Role Card */}
          <button
            onClick={onSelectAdminRole}
            className="w-full bg-white hover:bg-slate-50 border-2 border-slate-200 hover:border-indigo-500 rounded-3xl p-5 shadow-sm hover:shadow-md transition-all duration-200 flex items-center justify-between text-left group cursor-pointer active:scale-98"
          >
            <div className="flex items-center gap-4">
              <div className="w-14 h-14 rounded-2xl bg-indigo-100 text-indigo-700 flex items-center justify-center flex-shrink-0 group-hover:bg-indigo-600 group-hover:text-white transition-colors duration-200">
                <Shield className="w-7 h-7" />
              </div>
              <div>
                <h3 className="text-lg font-black text-slate-900 group-hover:text-indigo-700 transition-colors">
                  HOSPITAL ADMIN & CLINICIAN
                </h3>
                <p className="text-xs text-slate-500 font-medium leading-relaxed mt-0.5">
                  Live OPD queues, staff assistance desk & EHR consultation recorder
                </p>
              </div>
            </div>
            <div className="w-10 h-10 rounded-full bg-slate-100 group-hover:bg-indigo-50 text-slate-400 group-hover:text-indigo-600 flex items-center justify-center flex-shrink-0 transition">
              <ArrowRight className="w-5 h-5" />
            </div>
          </button>
        </div>

        {/* 1-Tap Quick Demo Launcher */}
        <div className="w-full mt-6">
          <button
            onClick={onQuickDemoLaunch}
            className="w-full py-3.5 px-4 bg-gradient-to-r from-sky-50 to-indigo-50 hover:from-sky-100 hover:to-indigo-100 border border-sky-200 rounded-2xl text-sky-800 text-xs sm:text-sm font-bold flex items-center justify-center gap-2.5 shadow-sm transition active:scale-98 cursor-pointer"
          >
            <Zap className="w-4 h-4 text-sky-600 fill-sky-600" />
            <span>Instant Demo Mode (5 Sample Patients & Live Queues)</span>
          </button>
        </div>
      </div>

      {/* Footer Notice */}
      <div className="w-full max-w-xl mx-auto flex items-center justify-center gap-2 text-slate-400 text-xs py-2">
        <Lock className="w-3.5 h-3.5" />
        <span>HIPAA & NDHM Compliant Secure Clinical Kiosk System</span>
      </div>
    </div>
  );
};
