import React from 'react';
import { ArrowLeft, Globe, Eye, Flame } from 'lucide-react';
import { Language, Screen } from '../types';

interface KioskTopBarProps {
  currentScreen: Screen;
  selectedLanguage: Language;
  isAccessibilityMode: boolean;
  onToggleAccessibility: () => void;
  onBackClick: () => void;
  onLanguageClick: () => void;
  onEmergencySosClick: () => void;
}

export const KioskTopBar: React.FC<KioskTopBarProps> = ({
  currentScreen,
  selectedLanguage,
  isAccessibilityMode,
  onToggleAccessibility,
  onBackClick,
  onLanguageClick,
  onEmergencySosClick,
}) => {
  const getScreenTitle = () => {
    switch (currentScreen) {
      case 'IDENTIFY':
        return 'Step 1: Patient Identification';
      case 'CONVERSATIONAL_INTAKE':
        return 'Step 2: Medical History Intake';
      case 'DOCUMENT_SCAN':
        return 'Step 3: Document & Prescription Scan';
      case 'SUMMARY':
        return 'Step 4: Clinical Summary & Review';
      case 'APPOINTMENTS':
        return 'My Hospital Appointments';
      case 'PROFILE':
        return 'Patient Profile & Preferences';
      default:
        return 'CareFlow MediKiosk';
    }
  };

  return (
    <header className="sticky top-0 z-40 w-full bg-white/95 backdrop-blur border-b border-slate-200 px-4 py-2.5 shadow-sm">
      <div className="max-w-4xl mx-auto flex items-center justify-between gap-3">
        {/* Left: Back button & Title */}
        <div className="flex items-center gap-3">
          <button
            onClick={onBackClick}
            data-testid="kiosk_top_bar_back"
            className="p-2 rounded-xl text-slate-700 hover:bg-slate-100 border border-slate-200 active:scale-95 transition cursor-pointer"
            aria-label="Back"
          >
            <ArrowLeft className="w-5 h-5 text-sky-700" />
          </button>
          <div>
            <h1
              className={`font-black text-slate-900 leading-tight ${
                isAccessibilityMode ? 'text-xl' : 'text-base'
              }`}
            >
              {getScreenTitle()}
            </h1>
            <p className="text-[11px] text-slate-500 font-medium hidden sm:block">
              Apollo & City General Hospital Network
            </p>
          </div>
        </div>

        {/* Right: Quick actions */}
        <div className="flex items-center gap-2">
          {/* Emergency SOS quick tap */}
          <button
            onClick={onEmergencySosClick}
            data-testid="kiosk_top_bar_sos"
            className="flex items-center gap-1.5 px-3 py-1.5 bg-rose-600 hover:bg-rose-700 text-white rounded-xl font-black text-xs shadow-sm transition active:scale-95 cursor-pointer animate-pulse"
          >
            <Flame className="w-4 h-4 fill-white" />
            <span>SOS</span>
          </button>

          {/* Easy mode toggle */}
          <button
            onClick={onToggleAccessibility}
            data-testid="kiosk_top_bar_easy_mode"
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl font-bold text-xs border transition active:scale-95 cursor-pointer ${
              isAccessibilityMode
                ? 'bg-sky-700 text-white border-sky-800'
                : 'bg-slate-100 hover:bg-slate-200 text-slate-700 border-slate-200'
            }`}
          >
            <Eye className="w-4 h-4" />
            <span className="hidden xs:inline">
              {isAccessibilityMode ? 'Easy ON' : 'Easy Mode'}
            </span>
          </button>

          {/* Language selector */}
          <button
            onClick={onLanguageClick}
            data-testid="kiosk_top_bar_language"
            className="flex items-center gap-1.5 px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 border border-slate-200 rounded-xl font-semibold text-xs transition active:scale-95 cursor-pointer"
          >
            <Globe className="w-4 h-4 text-sky-600" />
            <span>{selectedLanguage.nativeName.slice(0, 4)}</span>
          </button>
        </div>
      </div>
    </header>
  );
};
