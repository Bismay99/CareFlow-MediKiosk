import React, { useState, useEffect } from 'react';
import {
  AlertTriangle,
  Flame,
  PhoneCall,
  UserCheck,
  ShieldAlert,
  X,
  HeartPulse,
  Activity,
  CheckCircle,
  HelpCircle,
} from 'lucide-react';
import { FIRST_AID_GUIDES } from '../data/mockData';

interface CareFlowEmergencyDialogProps {
  isOpen: boolean;
  isEmergencyActive: boolean;
  activeEmergencyType: string;
  emergencyEtaSeconds: number;
  onDismiss: () => void;
  onTriggerSos: (type: string) => void;
  onCancelSos: () => void;
  onCallEmergencyDesk: () => void;
  onDispatchPorter: () => void;
}

export const CareFlowEmergencyDialog: React.FC<CareFlowEmergencyDialogProps> = ({
  isOpen,
  isEmergencyActive,
  activeEmergencyType,
  emergencyEtaSeconds,
  onDismiss,
  onTriggerSos,
  onCancelSos,
  onCallEmergencyDesk,
  onDispatchPorter,
}) => {
  const [activeTab, setActiveTab] = useState<'ACTIONS' | 'GUIDES'>('ACTIONS');
  const [selectedGuide, setSelectedGuide] = useState<number>(0);
  const [countdown, setCountdown] = useState(emergencyEtaSeconds);

  useEffect(() => {
    if (isEmergencyActive) {
      setCountdown(emergencyEtaSeconds);
      const interval = setInterval(() => {
        setCountdown((prev) => (prev > 0 ? prev - 1 : 0));
      }, 1000);
      return () => clearInterval(interval);
    }
  }, [isEmergencyActive, emergencyEtaSeconds]);

  if (!isOpen) return null;

  const criticalSymptoms = [
    'Crushing Chest Pain / Heart Pressure',
    'Facial Droop / Stroke-Like Paralysis',
    'Severe Breathing Distress / Hypoxia',
    'Syncope / Collapse / Unconscious',
    'Massive Hemorrhage / Acute Bleeding',
    'Severe Anaphylactic Allergic Reaction',
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/75 backdrop-blur-md animate-fade-in">
      <div className="w-full max-w-2xl bg-white rounded-3xl shadow-2xl border-2 border-rose-500 overflow-hidden flex flex-col max-h-[90vh]">
        {/* Urgent Header */}
        <div className="p-5 bg-gradient-to-r from-rose-600 to-rose-700 text-white flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="p-2.5 bg-white/20 rounded-2xl animate-pulse">
              <Flame className="w-6 h-6 text-white" />
            </div>
            <div>
              <div className="flex items-center gap-2">
                <span className="text-xs font-black tracking-widest uppercase bg-white text-rose-700 px-2 py-0.5 rounded">
                  Critical Care Center
                </span>
                <span className="text-xs font-bold text-rose-100">
                  Apollo Emergency Code 108
                </span>
              </div>
              <h2 className="text-xl font-black mt-0.5">
                Hospital Rapid Emergency Response
              </h2>
            </div>
          </div>
          <button
            onClick={onDismiss}
            className="p-2 hover:bg-white/20 rounded-xl transition cursor-pointer text-white/80 hover:text-white"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Emergency Status Banner if triggered */}
        {isEmergencyActive && (
          <div className="bg-rose-100 border-b border-rose-300 p-4 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 animate-pulse">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-rose-600 text-white rounded-xl">
                <ShieldAlert className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs font-bold text-rose-700 uppercase tracking-wide">
                  🚨 Active Hospital Code Dispatched
                </p>
                <p className="text-sm font-black text-rose-950">
                  {activeEmergencyType}
                </p>
              </div>
            </div>

            <div className="flex items-center gap-3 self-end sm:self-center">
              <div className="text-right">
                <span className="text-[10px] uppercase font-bold text-rose-700 block">
                  Responder ETA
                </span>
                <span className="text-lg font-black text-rose-900 font-mono">
                  ~{countdown}s
                </span>
              </div>
              <button
                onClick={onCancelSos}
                className="px-3 py-1.5 bg-white border border-rose-300 text-rose-700 font-bold text-xs rounded-xl hover:bg-rose-50 transition cursor-pointer"
              >
                Cancel SOS
              </button>
            </div>
          </div>
        )}

        {/* Tab Navigation */}
        <div className="flex border-b border-slate-200 bg-slate-50 px-4 pt-2">
          <button
            onClick={() => setActiveTab('ACTIONS')}
            className={`pb-2.5 px-4 font-bold text-sm border-b-2 transition cursor-pointer ${
              activeTab === 'ACTIONS'
                ? 'border-rose-600 text-rose-700'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            Immediate Emergency Actions
          </button>
          <button
            onClick={() => setActiveTab('GUIDES')}
            className={`pb-2.5 px-4 font-bold text-sm border-b-2 transition cursor-pointer ${
              activeTab === 'GUIDES'
                ? 'border-rose-600 text-rose-700'
                : 'border-transparent text-slate-500 hover:text-slate-800'
            }`}
          >
            Clinical First-Aid Guides
          </button>
        </div>

        {/* Modal Body */}
        <div className="p-5 overflow-y-auto flex-1 space-y-5">
          {activeTab === 'ACTIONS' ? (
            <>
              {/* Big 1-Tap SOS Dispatch */}
              <div className="bg-rose-50 border border-rose-200 rounded-2xl p-4 text-center">
                <p className="text-xs font-bold text-rose-800 uppercase tracking-wider mb-2">
                  Immediate Kiosk Beacon Alert
                </p>
                <button
                  onClick={() => onTriggerSos('1-Tap Primary SOS Code Red')}
                  className="w-full py-4 bg-rose-600 hover:bg-rose-700 active:scale-98 text-white font-black text-lg rounded-2xl shadow-lg shadow-rose-600/30 flex items-center justify-center gap-3 transition cursor-pointer"
                >
                  <AlertTriangle className="w-6 h-6 animate-bounce" />
                  <span>DISPATCH EMERGENCY RESCUE TEAM NOW</span>
                </button>
                <p className="text-[11px] text-rose-700 font-medium mt-2">
                  Automatically broadcasts your location (Kiosk 01 - Main Atrium) to ER Triage & Nursing staff.
                </p>
              </div>

              {/* Direct ER Assistance Buttons */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <button
                  onClick={onCallEmergencyDesk}
                  className="flex items-center gap-3 p-3.5 bg-slate-900 hover:bg-slate-800 text-white rounded-2xl transition cursor-pointer active:scale-98"
                >
                  <div className="p-2.5 bg-rose-500 text-white rounded-xl">
                    <PhoneCall className="w-5 h-5" />
                  </div>
                  <div className="text-left">
                    <div className="text-sm font-bold">Call ER Triage Desk</div>
                    <div className="text-xs text-slate-300">
                      Extension 108 / Direct Nurse Audio
                    </div>
                  </div>
                </button>

                <button
                  onClick={onDispatchPorter}
                  className="flex items-center gap-3 p-3.5 bg-slate-900 hover:bg-slate-800 text-white rounded-2xl transition cursor-pointer active:scale-98"
                >
                  <div className="p-2.5 bg-amber-500 text-white rounded-xl">
                    <UserCheck className="w-5 h-5" />
                  </div>
                  <div className="text-left">
                    <div className="text-sm font-bold">Dispatch Porter / Stretcher</div>
                    <div className="text-xs text-slate-300">
                      Wheelchair escort to Kiosk 01
                    </div>
                  </div>
                </button>
              </div>

              {/* Report Specific Critical Symptom */}
              <div>
                <h4 className="text-xs font-black uppercase tracking-wider text-slate-500 mb-2.5">
                  Report Acute Critical Symptom:
                </h4>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
                  {criticalSymptoms.map((symptom) => (
                    <button
                      key={symptom}
                      onClick={() => onTriggerSos(`Critical Symptom: ${symptom}`)}
                      className="flex items-center gap-2 p-2.5 bg-white hover:bg-rose-50 border border-slate-200 hover:border-rose-400 text-slate-800 rounded-xl text-left text-xs font-semibold transition active:scale-98 cursor-pointer"
                    >
                      <Activity className="w-4 h-4 text-rose-600 flex-shrink-0" />
                      <span>{symptom}</span>
                    </button>
                  ))}
                </div>
              </div>
            </>
          ) : (
            /* First Aid Guides */
            <div className="space-y-4">
              {/* Guide selector */}
              <div className="flex flex-wrap gap-2">
                {FIRST_AID_GUIDES.map((guide, idx) => (
                  <button
                    key={guide.title}
                    onClick={() => setSelectedGuide(idx)}
                    className={`px-3 py-1.5 rounded-xl text-xs font-bold transition cursor-pointer ${
                      selectedGuide === idx
                        ? 'bg-rose-600 text-white shadow-sm'
                        : 'bg-slate-100 text-slate-700 hover:bg-slate-200'
                    }`}
                  >
                    {guide.title}
                  </button>
                ))}
              </div>

              {/* Selected Guide Details */}
              <div className="bg-slate-50 border border-slate-200 rounded-2xl p-4 space-y-3">
                <div className="flex items-center gap-2 text-rose-700 font-bold text-sm">
                  <HeartPulse className="w-5 h-5" />
                  <span>{FIRST_AID_GUIDES[selectedGuide].title} Protocol</span>
                </div>
                <ol className="space-y-2">
                  {FIRST_AID_GUIDES[selectedGuide].steps.map((step, sIdx) => (
                    <li key={sIdx} className="flex items-start gap-2.5 text-xs text-slate-800">
                      <span className="flex-shrink-0 w-5 h-5 rounded-full bg-rose-200 text-rose-900 font-bold flex items-center justify-center text-[10px]">
                        {sIdx + 1}
                      </span>
                      <span className="pt-0.5 leading-relaxed">{step}</span>
                    </li>
                  ))}
                </ol>
              </div>

              <div className="p-3 bg-amber-50 border border-amber-200 rounded-xl text-xs text-amber-900">
                <strong>Important:</strong> First aid guides are for bystander guidance only. In any life-threatening situation, tap the SOS button to alert hospital clinical staff immediately.
              </div>
            </div>
          )}
        </div>

        {/* Modal Footer */}
        <div className="p-4 bg-slate-50 border-t border-slate-200 flex justify-between items-center">
          <div className="flex items-center gap-2 text-xs text-slate-500">
            <CheckCircle className="w-4 h-4 text-emerald-600" />
            <span>Monitored 24/7 by City General Emergency Department</span>
          </div>
          <button
            onClick={onDismiss}
            className="px-4 py-2 bg-slate-200 hover:bg-slate-300 text-slate-800 font-bold text-xs rounded-xl transition cursor-pointer"
          >
            Close Emergency Hub
          </button>
        </div>
      </div>
    </div>
  );
};
