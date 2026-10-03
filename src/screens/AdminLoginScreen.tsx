import React, { useState } from 'react';
import {
  ArrowLeft,
  Shield,
  Lock,
  KeyRound,
  Eye,
  EyeOff,
  LogIn,
  Zap,
  AlertCircle,
} from 'lucide-react';

interface AdminLoginScreenProps {
  onAdminLoginClick: (email: string, pass: string, pin: string) => void;
  onQuickAdminFill: () => void;
  onBackClick: () => void;
}

export const AdminLoginScreen: React.FC<AdminLoginScreenProps> = ({
  onAdminLoginClick,
  onQuickAdminFill,
  onBackClick,
}) => {
  const [email, setEmail] = useState('admin@apollohospital.org');
  const [password, setPassword] = useState('Admin2026!');
  const [pin, setPin] = useState('8492');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!email.trim() || !password.trim()) {
      setError('Please provide administrative email and password.');
      return;
    }
    setError(null);
    onAdminLoginClick(email.trim(), password, pin.trim());
  };

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 flex flex-col justify-between p-4 sm:p-6">
      {/* Top Header */}
      <div className="w-full max-w-lg mx-auto flex items-center gap-3 pt-2">
        <button
          onClick={onBackClick}
          className="p-2.5 rounded-2xl bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-300 shadow-sm transition active:scale-95 cursor-pointer"
        >
          <ArrowLeft className="w-5 h-5 text-indigo-400" />
        </button>
        <div>
          <h2 className="text-lg font-black text-white leading-tight">
            Administrator Command Access
          </h2>
          <p className="text-xs text-slate-400">
            Authorized hospital personnel & clinical administration only
          </p>
        </div>
      </div>

      {/* Main Admin Login Card */}
      <div className="w-full max-w-lg mx-auto my-auto py-6">
        <div className="bg-slate-800/90 rounded-3xl border border-slate-700/80 p-6 sm:p-8 shadow-2xl backdrop-blur">
          {/* Header Icon */}
          <div className="flex items-center gap-3 mb-6">
            <div className="p-3 bg-indigo-500/20 text-indigo-400 rounded-2xl border border-indigo-500/30">
              <Shield className="w-6 h-6" />
            </div>
            <div>
              <h3 className="text-xl font-black text-white">
                Admin Authentication
              </h3>
              <p className="text-xs text-slate-400">
                Dr. Alok Verma / Chief Medical Officer Console
              </p>
            </div>
          </div>

          {error && (
            <div className="p-3.5 bg-rose-950/70 border border-rose-700/60 text-rose-300 text-xs font-semibold rounded-2xl mb-4 flex items-center gap-2">
              <AlertCircle className="w-4 h-4 flex-shrink-0 text-rose-400" />
              <span>{error}</span>
            </div>
          )}

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-black uppercase tracking-wider text-slate-400 mb-1.5">
                Hospital Admin Email
              </label>
              <div className="relative">
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="admin@apollohospital.org"
                  className="w-full pl-10 pr-4 py-3 bg-slate-900/80 border border-slate-700 rounded-2xl text-sm font-medium text-white placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
                <Shield className="w-5 h-5 text-slate-500 absolute left-3.5 top-3.5" />
              </div>
            </div>

            <div>
              <label className="block text-xs font-black uppercase tracking-wider text-slate-400 mb-1.5">
                Admin Password
              </label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••••••"
                  className="w-full pl-10 pr-11 py-3 bg-slate-900/80 border border-slate-700 rounded-2xl text-sm font-medium text-white placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
                <Lock className="w-5 h-5 text-slate-500 absolute left-3.5 top-3.5" />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="p-2 text-slate-400 hover:text-slate-200 absolute right-2.5 top-2.5 cursor-pointer"
                >
                  {showPassword ? (
                    <EyeOff className="w-4 h-4" />
                  ) : (
                    <Eye className="w-4 h-4" />
                  )}
                </button>
              </div>
            </div>

            <div>
              <label className="block text-xs font-black uppercase tracking-wider text-slate-400 mb-1.5">
                Terminal Security PIN (4-Digits)
              </label>
              <div className="relative">
                <input
                  type="password"
                  maxLength={4}
                  value={pin}
                  onChange={(e) => setPin(e.target.value)}
                  placeholder="8492"
                  className="w-full pl-10 pr-4 py-3 bg-slate-900/80 border border-slate-700 rounded-2xl text-sm font-mono tracking-widest text-white placeholder:text-slate-500 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
                <KeyRound className="w-5 h-5 text-slate-500 absolute left-3.5 top-3.5" />
              </div>
            </div>

            <button
              type="submit"
              className="w-full py-3.5 bg-indigo-600 hover:bg-indigo-700 active:scale-98 text-white font-black text-sm rounded-2xl shadow-lg shadow-indigo-600/30 transition flex items-center justify-center gap-2 cursor-pointer mt-4"
            >
              <LogIn className="w-4 h-4" />
              <span>Authenticate Hospital Admin</span>
            </button>
          </form>

          {/* Quick Demo Fill button */}
          <div className="mt-6 pt-5 border-t border-slate-700/60">
            <button
              type="button"
              onClick={onQuickAdminFill}
              className="w-full py-3 bg-slate-700/60 hover:bg-slate-700 text-indigo-300 font-bold text-xs rounded-2xl border border-slate-600 flex items-center justify-center gap-2 transition active:scale-98 cursor-pointer"
            >
              <Zap className="w-4 h-4 text-amber-400 fill-amber-400" />
              <span>1-Tap Demo Admin Login (Dr. Alok Verma)</span>
            </button>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div className="text-center text-xs text-slate-500 py-2">
        CareFlow Admin Console • Strict Audit Logging Active
      </div>
    </div>
  );
};
