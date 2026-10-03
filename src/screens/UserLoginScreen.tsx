import React, { useState } from 'react';
import {
  ArrowLeft,
  User,
  Lock,
  Eye,
  EyeOff,
  LogIn,
  Zap,
  CheckCircle,
  AlertCircle,
  HelpCircle,
} from 'lucide-react';
import { DemoPatientData } from '../types';

interface UserLoginScreenProps {
  demoPatients: DemoPatientData[];
  onLoginClick: (identifier: string, pass: string) => void;
  onSelectDemoPatient: (index: number) => void;
  onBackClick: () => void;
}

export const UserLoginScreen: React.FC<UserLoginScreenProps> = ({
  demoPatients,
  onLoginClick,
  onSelectDemoPatient,
  onBackClick,
}) => {
  const [identifier, setIdentifier] = useState('rajesh.sharma@apollohealth.org');
  const [password, setPassword] = useState('Patient2026!');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showForgotModal, setShowForgotModal] = useState(false);
  const [showRegisterModal, setShowRegisterModal] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!identifier.trim() || !password.trim()) {
      setError('Please provide your email, phone, or name, along with your password.');
      return;
    }
    setError(null);
    onLoginClick(identifier.trim(), password);
  };

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-between p-4 sm:p-6">
      {/* Top Header */}
      <div className="w-full max-w-xl mx-auto flex items-center gap-3 pt-2">
        <button
          onClick={onBackClick}
          className="p-2.5 rounded-2xl bg-white hover:bg-slate-100 border border-slate-200 text-slate-700 shadow-sm transition active:scale-95 cursor-pointer"
        >
          <ArrowLeft className="w-5 h-5 text-sky-700" />
        </button>
        <div>
          <h2 className="text-lg font-black text-slate-900 leading-tight">
            Patient Portal Login
          </h2>
          <p className="text-xs text-slate-500">
            Access your OPD tokens, medical records & AI triage
          </p>
        </div>
      </div>

      {/* Main Login Card */}
      <div className="w-full max-w-xl mx-auto my-auto py-6">
        <div className="bg-white rounded-3xl border border-slate-200 p-6 sm:p-8 shadow-sm">
          {/* Greeting Icon */}
          <div className="flex items-center gap-3 mb-6">
            <div className="p-3 bg-sky-50 text-sky-700 rounded-2xl border border-sky-100">
              <User className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-xl font-black text-slate-900">
                Welcome Back
              </h3>
              <p className="text-xs text-slate-500">
                Sign in with your registered credentials or select a demo patient
              </p>
            </div>
          </div>

          {error && (
            <div className="p-3.5 bg-rose-50 border border-rose-200 text-rose-800 text-xs font-semibold rounded-2xl mb-4 flex items-center gap-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0 text-rose-600" />
              <span>{error}</span>
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-black uppercase tracking-wider text-slate-500 mb-1.5">
                Email, Phone or Patient Name
              </label>
              <div className="relative">
                <input
                  type="text"
                  value={identifier}
                  onChange={(e) => setIdentifier(e.target.value)}
                  placeholder="e.g. rajesh.sharma@apollohealth.org"
                  className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-medium text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-sky-500"
                />
                <User className="w-5 h-5 text-slate-400 absolute left-3.5 top-3.5" />
              </div>
            </div>

            <div>
              <div className="flex items-center justify-between mb-1.5">
                <label className="block text-xs font-black uppercase tracking-wider text-slate-500">
                  Password
                </label>
                <button
                  type="button"
                  onClick={() => setShowForgotModal(true)}
                  className="text-xs font-semibold text-sky-600 hover:text-sky-800 cursor-pointer"
                >
                  Forgot?
                </button>
              </div>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter your password"
                  className="w-full pl-10 pr-11 py-3 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-medium text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-sky-500"
                />
                <Lock className="w-5 h-5 text-slate-400 absolute left-3.5 top-3.5" />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="p-2 text-slate-400 hover:text-slate-600 absolute right-2.5 top-2.5 cursor-pointer"
                >
                  {showPassword ? (
                    <EyeOff className="w-4 h-4" />
                  ) : (
                    <Eye className="w-4 h-4" />
                  )}
                </button>
              </div>
            </div>

            <button
              type="submit"
              className="w-full py-3.5 bg-sky-600 hover:bg-sky-700 active:scale-98 text-white font-black text-sm rounded-2xl shadow-md shadow-sky-600/20 transition flex items-center justify-center gap-2 cursor-pointer mt-2"
            >
              <LogIn className="w-4 h-4" />
              <span>Sign In to Hospital Kiosk</span>
            </button>
          </form>

          {/* Quick Register link */}
          <div className="mt-4 text-center">
            <button
              type="button"
              onClick={() => setShowRegisterModal(true)}
              className="text-xs font-medium text-slate-500 hover:text-sky-700 cursor-pointer"
            >
              First time at CareFlow Kiosk? <span className="font-bold text-sky-600">Register New Patient</span>
            </button>
          </div>

          {/* Demo Patient Fast Selector */}
          <div className="mt-8 pt-6 border-t border-slate-100">
            <div className="flex items-center gap-1.5 text-xs font-black uppercase tracking-wider text-slate-500 mb-3">
              <Zap className="w-4 h-4 text-amber-500 fill-amber-500" />
              <span>Or Choose a Sample Demo Patient (1-Tap):</span>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 max-h-56 overflow-y-auto pr-1">
              {demoPatients.map((demo, idx) => (
                <button
                  key={demo.user.id}
                  onClick={() => onSelectDemoPatient(idx)}
                  className="flex items-start gap-2.5 p-3 rounded-2xl bg-slate-50 hover:bg-sky-50 border border-slate-200 hover:border-sky-300 text-left transition active:scale-98 cursor-pointer group"
                >
                  <div className="w-8 h-8 rounded-full bg-white group-hover:bg-sky-600 group-hover:text-white border border-slate-200 flex items-center justify-center font-bold text-xs text-sky-700 flex-shrink-0 transition">
                    {idx + 1}
                  </div>
                  <div className="min-w-0">
                    <div className="text-xs font-black text-slate-900 group-hover:text-sky-900 truncate">
                      {demo.profile.name}
                    </div>
                    <div className="text-[11px] text-slate-500 truncate">
                      {demo.profile.age} yrs • {demo.visit.department}
                    </div>
                  </div>
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Forgot Password Modal */}
      {showForgotModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-3xl p-6 max-w-sm w-full shadow-2xl border border-slate-200 space-y-4">
            <div className="p-3 bg-sky-100 text-sky-700 rounded-2xl w-fit">
              <HelpCircle className="w-6 h-6" />
            </div>
            <div>
              <h4 className="text-lg font-black text-slate-900">
                Password Assistance
              </h4>
              <p className="text-xs text-slate-500 mt-1 leading-relaxed">
                If you have forgotten your portal password, approach the Hospital Registration Desk (Counter 1-3) with your ABHA ID or government identity document.
              </p>
            </div>
            <button
              onClick={() => setShowForgotModal(false)}
              className="w-full py-2.5 bg-slate-900 text-white font-bold text-xs rounded-xl cursor-pointer"
            >
              Understood
            </button>
          </div>
        </div>
      )}

      {/* Register Info Modal */}
      {showRegisterModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-3xl p-6 max-w-sm w-full shadow-2xl border border-slate-200 space-y-4">
            <div className="p-3 bg-emerald-100 text-emerald-700 rounded-2xl w-fit">
              <CheckCircle className="w-6 h-6" />
            </div>
            <div>
              <h4 className="text-lg font-black text-slate-900">
                New Patient Registration
              </h4>
              <p className="text-xs text-slate-500 mt-1 leading-relaxed">
                You can directly sign in using any demo patient or click the Demo launcher to test all features immediately without requiring prior hospital records.
              </p>
            </div>
            <button
              onClick={() => {
                setShowRegisterModal(false);
                onSelectDemoPatient(0);
              }}
              className="w-full py-2.5 bg-sky-600 text-white font-bold text-xs rounded-xl cursor-pointer"
            >
              Continue as Demo Patient (Rajesh Sharma)
            </button>
          </div>
        </div>
      )}

      {/* Footer */}
      <div className="text-center text-xs text-slate-400 py-2">
        CareFlow MediKiosk • Hospital Security Guaranteed
      </div>
    </div>
  );
};
