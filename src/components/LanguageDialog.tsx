import React from 'react';
import { Check, Globe, X } from 'lucide-react';
import { Language } from '../types';
import { LANGUAGES } from '../data/mockData';

interface LanguageDialogProps {
  selectedLanguage: Language;
  onLanguageSelected: (lang: Language) => void;
  onDismiss: () => void;
}

export const LanguageDialog: React.FC<LanguageDialogProps> = ({
  selectedLanguage,
  onLanguageSelected,
  onDismiss,
}) => {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
      <div className="w-full max-w-md bg-white rounded-3xl shadow-2xl border border-slate-200 overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between p-5 border-b border-slate-100 bg-slate-50/70">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-sky-100 text-sky-700 rounded-2xl">
              <Globe className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-slate-900">
                Select Your Language
              </h3>
              <p className="text-xs text-slate-500">
                अपनी भाषा चुनें / Choose preferred tongue
              </p>
            </div>
          </div>
          <button
            onClick={onDismiss}
            className="p-2 text-slate-400 hover:text-slate-600 rounded-xl hover:bg-slate-200/50 transition cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Language Options */}
        <div className="p-4 grid grid-cols-1 sm:grid-cols-2 gap-2.5 max-h-[60vh] overflow-y-auto">
          {LANGUAGES.map((lang) => {
            const isSelected = selectedLanguage.code === lang.code;
            return (
              <button
                key={lang.code}
                onClick={() => {
                  onLanguageSelected(lang);
                  onDismiss();
                }}
                className={`flex items-center justify-between p-3.5 rounded-2xl border text-left transition active:scale-98 cursor-pointer ${
                  isSelected
                    ? 'bg-sky-50 border-sky-500 text-sky-900 ring-2 ring-sky-200'
                    : 'bg-white border-slate-200 hover:bg-slate-50 text-slate-800'
                }`}
              >
                <div>
                  <div className="text-base font-bold">{lang.nativeName}</div>
                  <div className="text-xs text-slate-500 font-medium">
                    {lang.displayName}
                  </div>
                </div>
                {isSelected && (
                  <div className="p-1 bg-sky-600 text-white rounded-full">
                    <Check className="w-4 h-4" />
                  </div>
                )}
              </button>
            );
          })}
        </div>

        {/* Footer info */}
        <div className="p-4 bg-slate-50 border-t border-slate-100 text-center">
          <p className="text-xs text-slate-500">
            CareFlow Voice Assistant & Intake will immediately adapt to your chosen language.
          </p>
        </div>
      </div>
    </div>
  );
};
