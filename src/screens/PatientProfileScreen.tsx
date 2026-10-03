import React from 'react';
import {
  ArrowLeft,
  User,
  Shield,
  Eye,
  Globe,
  Sun,
  Moon,
  Volume2,
  Mic,
  LogOut,
  FileBadge,
  Phone,
  CheckCircle,
} from 'lucide-react';
import {
  AppThemeMode,
  AuthUser,
  Language,
  PatientProfile,
} from '../types';

interface PatientProfileScreenProps {
  profile: PatientProfile;
  user: AuthUser | null;
  selectedLanguage: Language;
  themeMode: AppThemeMode;
  isAccessibilityMode: boolean;
  isVoiceAssistantEnabled: boolean;
  isTtsEnabled: boolean;
  isVoiceIntakeEnabled: boolean;
  onBackClick: () => void;
  onLanguageClick: () => void;
  onThemeSelect: (mode: AppThemeMode) => void;
  onToggleEasyMode: () => void;
  onToggleVoiceAssistant: (enabled: boolean) => void;
  onToggleTts: (enabled: boolean) => void;
  onToggleVoiceIntake: (enabled: boolean) => void;
  onLogoutClick: () => void;
}

export const PatientProfileScreen: React.FC<PatientProfileScreenProps> = ({
  profile,
  user,
  selectedLanguage,
  themeMode,
  isAccessibilityMode,
  isVoiceAssistantEnabled,
  isTtsEnabled,
  isVoiceIntakeEnabled,
  onBackClick,
  onLanguageClick,
  onThemeSelect,
  onToggleEasyMode,
  onToggleVoiceAssistant,
  onToggleTts,
  onToggleVoiceIntake,
  onLogoutClick,
}) => {
  const displayName = profile.name.trim() || user?.name || 'Rajesh Sharma';

  return (
    <div className="min-h-screen bg-slate-50 p-4 sm:p-6 pb-24">
      <div className="max-w-xl mx-auto space-y-4">
        {/* Top Header */}
        <div className="flex items-center justify-between border-b border-slate-200 pb-3">
          <div className="flex items-center gap-3">
            <button
              onClick={onBackClick}
              className="p-2.5 rounded-2xl bg-white hover:bg-slate-100 border border-slate-200 text-slate-700 shadow-sm transition active:scale-95 cursor-pointer"
            >
              <ArrowLeft className="w-5 h-5 text-sky-700" />
            </button>
            <div>
              <h2 className="text-xl font-black text-slate-900 leading-tight">
                Profile & Settings
              </h2>
              <p className="text-xs text-slate-500">
                Hospital ID card & accessibility preferences
              </p>
            </div>
          </div>

          <button
            onClick={onLogoutClick}
            className="flex items-center gap-1.5 px-3 py-2 bg-rose-50 hover:bg-rose-100 text-rose-700 border border-rose-200 rounded-xl text-xs font-bold transition active:scale-95 cursor-pointer"
          >
            <LogOut className="w-4 h-4" />
            <span>Sign Out</span>
          </button>
        </div>

        {/* Patient Identity Badge Card */}
        <div className="bg-gradient-to-br from-sky-700 to-indigo-900 text-white rounded-3xl p-6 shadow-xl space-y-4">
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-3">
              <div className="w-14 h-14 rounded-2xl bg-white/20 backdrop-blur flex items-center justify-center font-black text-xl text-white">
                <User className="w-8 h-8" />
              </div>
              <div>
                <span className="text-[10px] font-black uppercase tracking-widest text-sky-200 bg-white/10 px-2 py-0.5 rounded">
                  Hospital Health Card
                </span>
                <h3 className="text-xl font-black mt-1">{displayName}</h3>
                <p className="text-xs text-sky-200">
                  {profile.age} yrs • {profile.gender}
                </p>
              </div>
            </div>
            <div className="p-2 bg-white/10 rounded-xl">
              <Shield className="w-6 h-6 text-emerald-300" />
            </div>
          </div>

          <div className="space-y-1.5 pt-2 border-t border-white/20 text-xs">
            <div className="flex justify-between">
              <span className="text-sky-200">ABHA Health ID:</span>
              <span className="font-mono font-bold tracking-wider">{profile.abhaId || '14-2951-8402-1920'}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-sky-200">Registered Phone:</span>
              <span className="font-mono font-bold">{user?.phone || '+91 98201 44521'}</span>
            </div>
          </div>

          {/* Card Barcode */}
          <div className="pt-2 text-center">
            <div className="h-6 bg-white/20 rounded flex items-center justify-around px-2 font-mono text-[8px] tracking-widest text-white/80 select-none">
              |||||| | |||| || |||||| | |||||||| ||||
            </div>
          </div>
        </div>

        {/* Accessibility & Interface Preferences */}
        <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-sm space-y-4">
          <h3 className="text-base font-black text-slate-900 border-b border-slate-100 pb-2">
            Accessibility & Display
          </h3>

          <div className="space-y-3">
            {/* Easy Mode Toggle */}
            <div className="flex items-center justify-between p-3.5 bg-slate-50 rounded-2xl border border-slate-200">
              <div className="flex items-center gap-3">
                <div className="p-2.5 bg-sky-100 text-sky-700 rounded-xl">
                  <Eye className="w-5 h-5" />
                </div>
                <div>
                  <div className="text-xs font-bold text-slate-900">
                    Easy Mode (Large Fonts & High Contrast)
                  </div>
                  <div className="text-[11px] text-slate-500">
                    Optimized for elderly & low-vision patients
                  </div>
                </div>
              </div>
              <button
                onClick={onToggleEasyMode}
                className={`w-12 h-6 flex items-center rounded-full p-1 transition cursor-pointer ${
                  isAccessibilityMode ? 'bg-sky-600 justify-end' : 'bg-slate-300 justify-start'
                }`}
              >
                <div className="bg-white w-4 h-4 rounded-full shadow-md transform transition" />
              </button>
            </div>

            {/* Language Selector */}
            <div className="flex items-center justify-between p-3.5 bg-slate-50 rounded-2xl border border-slate-200">
              <div className="flex items-center gap-3">
                <div className="p-2.5 bg-indigo-100 text-indigo-700 rounded-xl">
                  <Globe className="w-5 h-5" />
                </div>
                <div>
                  <div className="text-xs font-bold text-slate-900">
                    Preferred Language
                  </div>
                  <div className="text-[11px] text-slate-500">
                    {selectedLanguage.displayName} ({selectedLanguage.nativeName})
                  </div>
                </div>
              </div>
              <button
                onClick={onLanguageClick}
                className="px-3 py-1.5 bg-white border border-slate-200 rounded-xl text-xs font-bold text-sky-600 hover:bg-slate-100 cursor-pointer"
              >
                Change
              </button>
            </div>

            {/* Theme Selector */}
            <div className="flex items-center justify-between p-3.5 bg-slate-50 rounded-2xl border border-slate-200">
              <div className="flex items-center gap-3">
                <div className="p-2.5 bg-amber-100 text-amber-700 rounded-xl">
                  <Sun className="w-5 h-5" />
                </div>
                <div>
                  <div className="text-xs font-bold text-slate-900">
                    Display Color Theme
                  </div>
                  <div className="text-[11px] text-slate-500">
                    Currently: {themeMode}
                  </div>
                </div>
              </div>
              <div className="flex gap-1">
                {(['LIGHT', 'DARK', 'SYSTEM'] as AppThemeMode[]).map((mode) => (
                  <button
                    key={mode}
                    onClick={() => onThemeSelect(mode)}
                    className={`px-2.5 py-1 rounded-lg text-xs font-bold transition cursor-pointer ${
                      themeMode === mode
                        ? 'bg-sky-600 text-white'
                        : 'bg-white border border-slate-200 text-slate-700 hover:bg-slate-100'
                    }`}
                  >
                    {mode.slice(0, 1) + mode.slice(1).toLowerCase()}
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Voice & Audio Controls */}
        <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-sm space-y-4">
          <h3 className="text-base font-black text-slate-900 border-b border-slate-100 pb-2">
            AI Voice & Audio Preferences
          </h3>

          <div className="space-y-3">
            {/* Home Voice Assistant Toggle */}
            <div className="flex items-center justify-between p-3.5 bg-slate-50 rounded-2xl border border-slate-200">
              <div className="flex items-center gap-3">
                <div className="p-2.5 bg-sky-100 text-sky-700 rounded-xl">
                  <Mic className="w-5 h-5" />
                </div>
                <div>
                  <div className="text-xs font-bold text-slate-900">
                    AI Voice Assistant Enabled
                  </div>
                  <div className="text-[11px] text-slate-500">
                    Enables voice queries on home screen
                  </div>
                </div>
              </div>
              <button
                onClick={() => onToggleVoiceAssistant(!isVoiceAssistantEnabled)}
                className={`w-12 h-6 flex items-center rounded-full p-1 transition cursor-pointer ${
                  isVoiceAssistantEnabled ? 'bg-sky-600 justify-end' : 'bg-slate-300 justify-start'
                }`}
              >
                <div className="bg-white w-4 h-4 rounded-full shadow-md" />
              </button>
            </div>

            {/* TTS Read Aloud Toggle */}
            <div className="flex items-center justify-between p-3.5 bg-slate-50 rounded-2xl border border-slate-200">
              <div className="flex items-center gap-3">
                <div className="p-2.5 bg-sky-100 text-sky-700 rounded-xl">
                  <Volume2 className="w-5 h-5" />
                </div>
                <div>
                  <div className="text-xs font-bold text-slate-900">
                    Spoken Voice Readout (TTS)
                  </div>
                  <div className="text-[11px] text-slate-500">
                    Reads answers and triage guidance aloud
                  </div>
                </div>
              </div>
              <button
                onClick={() => onToggleTts(!isTtsEnabled)}
                className={`w-12 h-6 flex items-center rounded-full p-1 transition cursor-pointer ${
                  isTtsEnabled ? 'bg-sky-600 justify-end' : 'bg-slate-300 justify-start'
                }`}
              >
                <div className="bg-white w-4 h-4 rounded-full shadow-md" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
