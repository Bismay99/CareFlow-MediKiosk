import React from 'react';
import { AlertTriangle, PhoneCall, X } from 'lucide-react';

interface IntakeStepProgressHeaderProps {
  step: number;
  totalSteps: number;
  stepTitle: string;
}

export const IntakeStepProgressHeader: React.FC<IntakeStepProgressHeaderProps> = ({
  step,
  totalSteps,
  stepTitle,
}) => {
  const steps = [
    { num: 1, label: 'Identity' },
    { num: 2, label: 'History' },
    { num: 3, label: 'Docs' },
    { num: 4, label: 'Summary' },
  ];

  return (
    <div className="w-full bg-white rounded-2xl border border-slate-200 shadow-sm p-4 mb-4">
      <div className="flex items-center justify-between mb-3">
        <div>
          <span className="text-xs font-bold uppercase tracking-wider text-sky-600 bg-sky-50 px-2.5 py-1 rounded-md">
            Step {step} of {totalSteps}
          </span>
          <h2 className="text-lg font-bold text-slate-900 mt-1">{stepTitle}</h2>
        </div>
        <div className="text-xs font-medium text-slate-500">
          CareFlow Clinical Flow
        </div>
      </div>

      {/* Progress track */}
      <div className="grid grid-cols-4 gap-2">
        {steps.map((s) => {
          const isDone = s.num < step;
          const isCurrent = s.num === step;
          return (
            <div key={s.num} className="flex flex-col gap-1">
              <div
                className={`h-2 rounded-full transition-all duration-300 ${
                  isDone
                    ? 'bg-emerald-500'
                    : isCurrent
                    ? 'bg-sky-600 ring-2 ring-sky-200'
                    : 'bg-slate-200'
                }`}
              />
              <span
                className={`text-[11px] font-semibold truncate ${
                  isCurrent
                    ? 'text-sky-700'
                    : isDone
                    ? 'text-emerald-700'
                    : 'text-slate-400'
                }`}
              >
                {s.num}. {s.label}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
};

interface UrgentRedFlagAlertBannerProps {
  message: string;
  onCallStaff: () => void;
  onDismiss: () => void;
}

export const UrgentRedFlagAlertBanner: React.FC<UrgentRedFlagAlertBannerProps> = ({
  message,
  onCallStaff,
  onDismiss,
}) => {
  return (
    <div className="w-full bg-rose-50 border-2 border-rose-500 rounded-2xl p-4 shadow-md mb-4 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 animate-pulse">
      <div className="flex items-start gap-3">
        <div className="p-2 bg-rose-600 text-white rounded-xl flex-shrink-0 mt-0.5">
          <AlertTriangle className="w-5 h-5" />
        </div>
        <div>
          <div className="flex items-center gap-2">
            <span className="text-xs font-black uppercase tracking-wider text-rose-700 bg-rose-100 px-2 py-0.5 rounded">
              High Priority Warning
            </span>
          </div>
          <p className="text-sm font-bold text-rose-900 mt-1 leading-snug">
            {message}
          </p>
        </div>
      </div>

      <div className="flex items-center gap-2 self-end sm:self-center flex-shrink-0">
        <button
          onClick={onCallStaff}
          className="flex items-center gap-1.5 px-3.5 py-2 bg-rose-600 hover:bg-rose-700 text-white font-bold text-xs rounded-xl shadow-sm transition active:scale-95 cursor-pointer"
        >
          <PhoneCall className="w-4 h-4" />
          Alert Triage Staff
        </button>
        <button
          onClick={onDismiss}
          className="p-2 text-rose-700 hover:bg-rose-200/50 rounded-xl transition cursor-pointer"
          title="Dismiss warning"
        >
          <X className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};
