import React, { useState } from 'react';
import {
  Building2,
  Bell,
  Globe,
  Eye,
  Sun,
  Moon,
  Flame,
  User,
  Search,
  Mic,
  Volume2,
  Clock,
  ArrowRight,
  MapPin,
  Calendar,
  FileText,
  Activity,
  Sparkles,
  HelpCircle,
  Pill,
  FlaskConical,
  X,
  CheckCircle,
  AlertTriangle,
  ChevronRight,
  ShieldAlert,
} from 'lucide-react';
import {
  Language,
  PatientProfile,
  ActiveVisit,
  HealthInsight,
  RecentlyViewedItem,
  RecentRecordItem,
  RecentActivityItem,
  HospitalNotification,
  AppThemeMode,
  Screen,
} from '../types';
import { speakText } from '../utils/speech';

interface PatientHomeScreenProps {
  patientProfile: PatientProfile;
  selectedLanguage: Language;
  activeVisit: ActiveVisit | null;
  healthInsights: HealthInsight[];
  recentlyViewed: RecentlyViewedItem[];
  recentRecords: RecentRecordItem[];
  recentActivities: RecentActivityItem[];
  notifications: HospitalNotification[];
  isAccessibilityMode: boolean;
  themeMode: AppThemeMode;
  isRedFlagDetected: boolean;
  redFlagMessage: string;
  isEmergencyAlertActive: boolean;
  activeEmergencyType: string;
  onNavigate: (screen: Screen) => void;
  onToggleAccessibility: () => void;
  onToggleTheme: () => void;
  onOpenLanguageDialog: () => void;
  onOpenEmergencyCenter: () => void;
  onTriggerSos: (type: string) => void;
  onOpenVoiceAssistant: (initialQuery?: string) => void;
  onRequestStaffHelp: () => void;
  onDismissRedFlag: () => void;
  onAlertStaff: () => void;
  onToggleActiveVisit: () => void;
  onMarkNotificationRead: (id: string) => void;
  onBookServiceAction: (title: string, detail: string) => void;
}

export const PatientHomeScreen: React.FC<PatientHomeScreenProps> = ({
  patientProfile,
  selectedLanguage,
  activeVisit,
  healthInsights,
  recentlyViewed,
  recentRecords,
  recentActivities,
  notifications,
  isAccessibilityMode,
  themeMode,
  isRedFlagDetected,
  redFlagMessage,
  isEmergencyAlertActive,
  activeEmergencyType,
  onNavigate,
  onToggleAccessibility,
  onToggleTheme,
  onOpenLanguageDialog,
  onOpenEmergencyCenter,
  onTriggerSos,
  onOpenVoiceAssistant,
  onRequestStaffHelp,
  onDismissRedFlag,
  onAlertStaff,
  onToggleActiveVisit,
  onMarkNotificationRead,
  onBookServiceAction,
}) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [showNotificationsModal, setShowNotificationsModal] = useState(false);
  const [showDirectionsModal, setShowDirectionsModal] = useState(false);
  const [selectedRecordDetail, setSelectedRecordDetail] = useState<RecentRecordItem | null>(null);
  const [serviceBookingModal, setServiceBookingModal] = useState<string | null>(null);
  const [bookingNote, setBookingNote] = useState('');

  const unreadCount = notifications.filter((n) => !n.isRead).length;
  const patientDisplayName = patientProfile.name.trim() || 'Rajesh Sharma';

  const handleSearchSubmit = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (searchQuery.trim()) {
      onOpenVoiceAssistant(searchQuery.trim());
    } else {
      onOpenVoiceAssistant();
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 pb-24">
      {/* 1. TOP HEADER */}
      <header className="sticky top-0 z-30 bg-white/95 backdrop-blur border-b border-slate-200 px-4 py-3 shadow-xs">
        <div className="max-w-4xl mx-auto flex items-center justify-between gap-2">
          {/* Logo & Hospital info */}
          <div className="flex items-center gap-2.5">
            <div className="w-10 h-10 rounded-2xl bg-gradient-to-tr from-sky-600 to-indigo-600 text-white flex items-center justify-center shadow-sm">
              <Building2 className="w-5 h-5" />
            </div>
            <div>
              <h1 className="text-base font-black text-slate-900 tracking-tight leading-tight">
                CareFlow Ai
              </h1>
              <p className="text-[11px] text-slate-500 font-medium">
                Apollo City General Hospital
              </p>
            </div>
          </div>

          {/* Header Action Buttons */}
          <div className="flex items-center gap-1.5 sm:gap-2">
            {/* SOS Emergency button */}
            <button
              onClick={onOpenEmergencyCenter}
              className="flex items-center gap-1.5 px-2.5 py-1.5 bg-rose-600 hover:bg-rose-700 text-white rounded-xl font-black text-xs shadow-sm transition active:scale-95 cursor-pointer animate-pulse"
              title="Emergency SOS Center"
            >
              <Flame className="w-4 h-4 fill-white" />
              <span className="hidden xs:inline">SOS</span>
            </button>

            {/* Easy Mode Toggle */}
            <button
              onClick={onToggleAccessibility}
              className={`flex items-center gap-1 px-2.5 py-1.5 rounded-xl font-bold text-xs border transition active:scale-95 cursor-pointer ${
                isAccessibilityMode
                  ? 'bg-sky-600 text-white border-sky-700'
                  : 'bg-slate-100 hover:bg-slate-200 text-slate-700 border-slate-200'
              }`}
              title="Toggle Large Font Easy Mode"
            >
              <Eye className="w-4 h-4" />
              <span className="hidden sm:inline">
                {isAccessibilityMode ? 'Easy ON' : 'Easy'}
              </span>
            </button>

            {/* Language Selector */}
            <button
              onClick={onOpenLanguageDialog}
              className="flex items-center gap-1 px-2.5 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 border border-slate-200 rounded-xl font-semibold text-xs transition active:scale-95 cursor-pointer"
            >
              <Globe className="w-4 h-4 text-sky-600" />
              <span>{selectedLanguage.nativeName.slice(0, 4)}</span>
            </button>

            {/* Notifications with Unread Badge */}
            <button
              onClick={() => setShowNotificationsModal(true)}
              className="relative p-2 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 border border-slate-200 transition active:scale-95 cursor-pointer"
              title="Hospital Notifications"
            >
              <Bell className="w-4 h-4" />
              {unreadCount > 0 && (
                <span className="absolute -top-1 -right-1 w-4 h-4 rounded-full bg-rose-600 text-white text-[10px] font-black flex items-center justify-center">
                  {unreadCount}
                </span>
              )}
            </button>

            {/* Profile Avatar button */}
            <button
              onClick={() => onNavigate('PROFILE')}
              className="w-8 h-8 rounded-full bg-sky-100 border border-sky-300 text-sky-800 flex items-center justify-center font-bold text-xs shadow-xs transition active:scale-95 cursor-pointer hover:bg-sky-200"
              title="Profile & Settings"
            >
              <User className="w-4 h-4" />
            </button>
          </div>
        </div>
      </header>

      {/* MAIN CONTAINER */}
      <main className="max-w-4xl mx-auto px-4 py-4 space-y-4">
        {/* 0. Urgent Red Flag Alert Banner (If triggered) */}
        {isRedFlagDetected && (
          <div className="w-full bg-rose-50 border-2 border-rose-500 rounded-3xl p-4 shadow-md flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 animate-pulse">
            <div className="flex items-start gap-3">
              <div className="p-2.5 bg-rose-600 text-white rounded-2xl flex-shrink-0 mt-0.5">
                <AlertTriangle className="w-5 h-5" />
              </div>
              <div>
                <span className="text-xs font-black uppercase tracking-wider text-rose-700 bg-rose-100 px-2 py-0.5 rounded">
                  Critical Symptom Alert
                </span>
                <p className="text-sm font-bold text-rose-950 mt-1 leading-snug">
                  {redFlagMessage}
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2 self-end sm:self-center">
              <button
                onClick={onAlertStaff}
                className="px-3.5 py-2 bg-rose-600 hover:bg-rose-700 text-white font-bold text-xs rounded-xl shadow-sm cursor-pointer"
              >
                Alert ER Staff
              </button>
              <button
                onClick={onDismissRedFlag}
                className="p-2 text-rose-700 hover:bg-rose-200/50 rounded-xl cursor-pointer"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>
        )}

        {/* 1. Critical Care & Emergency Section */}
        <section className={`w-full rounded-3xl border p-4 sm:p-5 transition shadow-sm ${
          isEmergencyAlertActive
            ? 'bg-rose-50 border-rose-400'
            : 'bg-white border-slate-200'
        }`}>
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
            <div className="flex items-center gap-3.5">
              <div className="w-12 h-12 rounded-2xl bg-rose-600 text-white flex items-center justify-center flex-shrink-0 shadow-md shadow-rose-600/25">
                <Flame className="w-6 h-6 animate-pulse" />
              </div>
              <div>
                <span className="text-[10px] font-black uppercase tracking-widest text-rose-600 block">
                  Critical Emergency
                </span>
                <h3 className={`font-black text-slate-900 leading-tight ${isAccessibilityMode ? 'text-xl' : 'text-lg'}`}>
                  {isEmergencyAlertActive ? '🚨 Emergency Team En Route' : 'Immediate Medical Emergency SOS'}
                </h3>
                <p className="text-xs text-slate-500 font-medium">
                  {isEmergencyAlertActive
                    ? activeEmergencyType
                    : '1-tap distress alert broadcasts kiosk location to ER desk'}
                </p>
              </div>
            </div>

            <div className="flex items-center gap-2 self-stretch sm:self-center">
              <button
                onClick={() => onTriggerSos('1-Tap SOS Emergency Distress')}
                className="flex-1 sm:flex-none px-4 py-3 bg-rose-600 hover:bg-rose-700 active:scale-95 text-white font-black text-xs sm:text-sm rounded-2xl shadow-md shadow-rose-600/30 transition flex items-center justify-center gap-2 cursor-pointer"
              >
                <AlertTriangle className="w-4 h-4" />
                <span>1-Tap Code Red</span>
              </button>

              <button
                onClick={onOpenEmergencyCenter}
                className="px-3.5 py-3 bg-slate-100 hover:bg-slate-200 text-slate-800 font-bold text-xs sm:text-sm rounded-2xl border border-slate-200 transition cursor-pointer"
              >
                Guides & Hub
              </button>
            </div>
          </div>
        </section>

        {/* 2. GREETING */}
        <section className="flex items-center justify-between gap-3 pt-1">
          <div>
            <h2 className={`font-black text-slate-900 ${isAccessibilityMode ? 'text-2xl' : 'text-xl sm:text-2xl'}`}>
              Hello, {patientDisplayName}
            </h2>
            <p className="text-xs sm:text-sm text-slate-500 font-medium mt-0.5">
              {selectedLanguage.welcomeText} • How can we assist your visit today?
            </p>
          </div>

          <button
            onClick={() => speakText(`Hello, ${patientDisplayName}. Welcome to MediKiosk. How can we assist you today?`, selectedLanguage)}
            className="p-3 bg-sky-100 hover:bg-sky-200 text-sky-800 rounded-2xl transition cursor-pointer shadow-xs active:scale-95"
            title="Read Greeting Aloud"
          >
            <Volume2 className="w-5 h-5" />
          </button>
        </section>

        {/* 3. SEARCH + ASK AI BUTTON */}
        <section className="bg-white rounded-3xl border border-slate-200 p-2 shadow-xs flex items-center gap-2">
          <form onSubmit={handleSearchSubmit} className="flex-1 flex items-center gap-2 pl-3">
            <Search className="w-5 h-5 text-sky-600 flex-shrink-0" />
            <input
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder={isAccessibilityMode ? "Search hospital..." : "Ask queue status, room directions, doctor names..."}
              className="w-full py-2 bg-transparent text-sm text-slate-800 placeholder:text-slate-400 focus:outline-none"
            />
            {searchQuery && (
              <button
                type="button"
                onClick={() => setSearchQuery('')}
                className="p-1 text-slate-400 hover:text-slate-600"
              >
                <X className="w-4 h-4" />
              </button>
            )}
          </form>

          <button
            onClick={() => handleSearchSubmit()}
            className="flex items-center gap-2 px-4 py-2.5 bg-sky-600 hover:bg-sky-700 active:scale-95 text-white font-bold text-xs sm:text-sm rounded-2xl shadow-sm transition cursor-pointer flex-shrink-0"
          >
            <Mic className="w-4 h-4" />
            <span>Ask AI</span>
          </button>
        </section>

        {/* 4. CURRENT VISIT CARD */}
        {activeVisit && activeVisit.hasActiveVisit && (
          <section className="bg-gradient-to-br from-sky-50 via-white to-indigo-50/40 rounded-3xl border-2 border-sky-300 p-5 shadow-sm space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="px-2.5 py-1 bg-sky-700 text-white font-black text-xs uppercase tracking-wider rounded-lg">
                  Current Visit
                </span>
                <span className="flex items-center gap-1.5 px-2.5 py-1 bg-amber-100 text-amber-900 font-bold text-xs rounded-lg">
                  <span className="w-2 h-2 rounded-full bg-amber-500 animate-pulse" />
                  {activeVisit.status}
                </span>
              </div>

              <span className="px-3 py-1 bg-sky-600 text-white font-black text-sm rounded-xl shadow-xs">
                Token {activeVisit.tokenNumber}
              </span>
            </div>

            <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-3">
              <div>
                <h3 className="text-xl font-black text-slate-900">
                  {activeVisit.department}
                </h3>
                <p className="text-sm font-semibold text-slate-600">
                  {activeVisit.doctorName}
                </p>
                <p className="text-xs text-sky-700 font-bold flex items-center gap-1 mt-1">
                  <MapPin className="w-3.5 h-3.5" />
                  <span>{activeVisit.roomNumber}</span>
                </p>
              </div>

              <div className="text-right sm:text-right">
                <div className="text-xs text-slate-500">Estimated wait</div>
                <div className="text-2xl font-black text-slate-900">
                  ~{activeVisit.estimatedWaitMinutes} mins
                </div>
              </div>
            </div>

            {/* Queue Position Bar */}
            <div className="space-y-1.5">
              <div className="flex justify-between text-xs font-bold text-slate-600">
                <span>You are #{activeVisit.queuePosition} in queue</span>
                <span>{activeVisit.totalInQueue} patients total</span>
              </div>
              <div className="w-full bg-slate-200 h-2.5 rounded-full overflow-hidden">
                <div
                  className="bg-sky-600 h-full rounded-full transition-all duration-500"
                  style={{
                    width: `${Math.max(10, 100 - (activeVisit.queuePosition / activeVisit.totalInQueue) * 100)}%`,
                  }}
                />
              </div>
            </div>

            {/* Card Buttons */}
            <div className="flex flex-wrap items-center gap-2 pt-1 border-t border-sky-100">
              <button
                onClick={() => setShowDirectionsModal(true)}
                className="flex items-center gap-1.5 px-3.5 py-2 bg-sky-600 hover:bg-sky-700 text-white rounded-xl text-xs font-bold transition active:scale-95 cursor-pointer"
              >
                <MapPin className="w-4 h-4" />
                <span>View Hospital Directions</span>
              </button>

              <button
                onClick={onToggleActiveVisit}
                className="px-3.5 py-2 bg-white hover:bg-slate-100 text-slate-700 border border-slate-200 rounded-xl text-xs font-semibold transition active:scale-95 cursor-pointer"
              >
                Check Out / Finish Visit
              </button>
            </div>
          </section>
        )}

        {/* 5. CONSULTATION SECTION (In-Person OPD Check-In & Video) */}
        <section className="bg-white rounded-3xl border border-slate-200 p-5 shadow-xs space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-black text-slate-900">
              Clinical Consultation & Intake
            </h3>
            <span className="text-xs font-medium text-slate-500">
              Self-service check-in
            </span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {/* Start In-Person Intake Check-In */}
            <button
              onClick={() => onNavigate('IDENTIFY')}
              className="p-4 rounded-2xl bg-sky-50/70 hover:bg-sky-100/70 border border-sky-200 text-left transition active:scale-98 cursor-pointer flex items-center justify-between group"
            >
              <div className="flex items-center gap-3">
                <div className="p-3 bg-sky-600 text-white rounded-xl group-hover:scale-105 transition">
                  <Activity className="w-5 h-5" />
                </div>
                <div>
                  <div className="text-sm font-black text-slate-900">
                    Start In-Person OPD Intake
                  </div>
                  <div className="text-xs text-slate-500 font-medium">
                    Guided SOCRATES triage & queue assignment
                  </div>
                </div>
              </div>
              <ArrowRight className="w-5 h-5 text-sky-700" />
            </button>

            {/* Book Video Consultation */}
            <button
              onClick={() => setServiceBookingModal('Video Consultation with Specialist')}
              className="p-4 rounded-2xl bg-indigo-50/70 hover:bg-indigo-100/70 border border-indigo-200 text-left transition active:scale-98 cursor-pointer flex items-center justify-between group"
            >
              <div className="flex items-center gap-3">
                <div className="p-3 bg-indigo-600 text-white rounded-xl group-hover:scale-105 transition">
                  <Calendar className="w-5 h-5" />
                </div>
                <div>
                  <div className="text-sm font-black text-slate-900">
                    Schedule Remote Teleconsult
                  </div>
                  <div className="text-xs text-slate-500 font-medium">
                    Video consult link sent via SMS
                  </div>
                </div>
              </div>
              <ArrowRight className="w-5 h-5 text-indigo-700" />
            </button>
          </div>
        </section>

        {/* 6. HOSPITAL SERVICES & RECORDS GRID */}
        <section className="bg-white rounded-3xl border border-slate-200 p-5 shadow-xs space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-black text-slate-900">
              Hospital Services & Quick Records
            </h3>
            <span className="text-xs text-slate-500">1-Tap kiosk actions</span>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">
            {[
              { title: 'Diagnostic Lab Tests', desc: 'Book blood/ECG slot', icon: FlaskConical, action: () => setServiceBookingModal('Diagnostic Lab Test') },
              { title: 'Prescription Refills', desc: 'Pharmacy order dispatch', icon: Pill, action: () => setServiceBookingModal('Pharmacy Medication Refill') },
              { title: 'Scan Prescription', desc: 'OCR parsing of slips', icon: FileText, action: () => onNavigate('DOCUMENT_SCAN') },
              { title: 'My Appointments', desc: 'Upcoming clinic dates', icon: Calendar, action: () => onNavigate('APPOINTMENTS') },
              { title: 'Clinical Summaries', desc: 'Doctor SOAP report', icon: Sparkles, action: () => onNavigate('SUMMARY') },
              { title: 'Surgery Counseling', desc: 'Pre-admission planning', icon: Building2, action: () => setServiceBookingModal('Elective Surgery Counseling') },
            ].map((svc) => {
              const Icon = svc.icon;
              return (
                <button
                  key={svc.title}
                  onClick={svc.action}
                  className="p-3.5 rounded-2xl bg-slate-50 hover:bg-sky-50/60 border border-slate-200 hover:border-sky-300 text-left transition active:scale-98 cursor-pointer flex flex-col justify-between"
                >
                  <div className="p-2 w-fit rounded-xl bg-white border border-slate-200 text-sky-700 mb-2">
                    <Icon className="w-5 h-5" />
                  </div>
                  <div>
                    <div className="text-xs sm:text-sm font-bold text-slate-900 leading-snug">
                      {svc.title}
                    </div>
                    <div className="text-[11px] text-slate-500 font-medium mt-0.5">
                      {svc.desc}
                    </div>
                  </div>
                </button>
              );
            })}
          </div>
        </section>

        {/* 7. AI-POWERED HEALTH INSIGHTS */}
        <section className="bg-white rounded-3xl border border-slate-200 p-5 shadow-xs space-y-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Sparkles className="w-4 h-4 text-sky-600" />
              <h3 className="text-base font-black text-slate-900">
                CareFlow Health Insights
              </h3>
            </div>
            <span className="text-[11px] font-bold text-sky-700 bg-sky-50 px-2 py-0.5 rounded-md">
              AI-assisted non-diagnostic
            </span>
          </div>

          <div className="space-y-2.5">
            {healthInsights.map((insight) => (
              <div
                key={insight.id}
                className="p-3.5 bg-slate-50 border border-slate-200 rounded-2xl flex items-start justify-between gap-3"
              >
                <div>
                  <h4 className="text-xs sm:text-sm font-bold text-slate-900">
                    {insight.title}
                  </h4>
                  <p className="text-xs text-slate-600 mt-0.5 leading-relaxed">
                    {insight.description}
                  </p>
                </div>
                <button
                  onClick={() => speakText(insight.description, selectedLanguage)}
                  className="p-2 text-slate-400 hover:text-sky-700 rounded-xl hover:bg-slate-200/50 transition cursor-pointer flex-shrink-0"
                  title="Read aloud"
                >
                  <Volume2 className="w-4 h-4" />
                </button>
              </div>
            ))}
          </div>
        </section>

        {/* 8. RECENT MEDICAL RECORDS */}
        <section className="bg-white rounded-3xl border border-slate-200 p-5 shadow-xs space-y-3">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-black text-slate-900">
              Recent Medical Records
            </h3>
            <button
              onClick={() => onNavigate('DOCUMENT_SCAN')}
              className="text-xs font-bold text-sky-600 hover:text-sky-800 cursor-pointer"
            >
              Scan New Record →
            </button>
          </div>

          <div className="space-y-2">
            {recentRecords.map((rec) => (
              <button
                key={rec.id}
                onClick={() => setSelectedRecordDetail(rec)}
                className="w-full text-left p-3.5 bg-slate-50 hover:bg-slate-100 border border-slate-200 rounded-2xl flex items-center justify-between transition cursor-pointer active:scale-98"
              >
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-xs font-bold text-slate-900">
                      {rec.recordType}: {rec.department}
                    </span>
                    <span className="text-[10px] text-slate-400">
                      {rec.date}
                    </span>
                  </div>
                  <p className="text-xs text-slate-600 truncate max-w-md mt-0.5">
                    {rec.summary}
                  </p>
                </div>
                <ChevronRight className="w-4 h-4 text-slate-400" />
              </button>
            ))}
          </div>
        </section>

        {/* 9. RECENT ACTIVITY TIMELINE */}
        <section className="bg-white rounded-3xl border border-slate-200 p-5 shadow-xs space-y-3">
          <h3 className="text-base font-black text-slate-900">
            Recent Kiosk & Hospital Activity
          </h3>

          <div className="space-y-3">
            {recentActivities.map((act, i) => (
              <div key={act.id} className="flex items-start gap-3">
                <div className="w-2.5 h-2.5 rounded-full bg-sky-600 mt-1.5 flex-shrink-0" />
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between">
                    <span className="text-xs font-bold text-slate-900">
                      {act.title}
                    </span>
                    <span className="text-[10px] text-slate-400">
                      {act.timestamp}
                    </span>
                  </div>
                  <p className="text-xs text-slate-500 truncate">
                    {act.subtitle}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* 10. NEED IN-PERSON STAFF HELP CARD */}
        <section className="bg-slate-900 text-white rounded-3xl p-5 shadow-md flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="p-3 bg-white/10 rounded-2xl">
              <HelpCircle className="w-6 h-6 text-sky-300" />
            </div>
            <div>
              <h4 className="text-base font-black text-white">
                Need In-Person or Wheelchair Help?
              </h4>
              <p className="text-xs text-slate-300">
                A hospital care representative or escort will come to this kiosk
              </p>
            </div>
          </div>

          <button
            onClick={onRequestStaffHelp}
            className="px-4 py-2.5 bg-sky-600 hover:bg-sky-500 active:scale-95 text-white font-bold text-xs sm:text-sm rounded-xl shadow transition cursor-pointer self-stretch sm:self-center text-center"
          >
            Request Staff Help
          </button>
        </section>
      </main>

      {/* NOTIFICATIONS DIALOG MODAL */}
      {showNotificationsModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl border border-slate-200 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div className="flex items-center gap-2">
                <Bell className="w-5 h-5 text-sky-600" />
                <h3 className="text-base font-black text-slate-900">
                  Hospital Notifications
                </h3>
              </div>
              <button
                onClick={() => setShowNotificationsModal(false)}
                className="p-1.5 text-slate-400 hover:text-slate-600 rounded-lg cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-2.5 max-h-[60vh] overflow-y-auto pr-1">
              {notifications.map((notif) => (
                <div
                  key={notif.id}
                  onClick={() => onMarkNotificationRead(notif.id)}
                  className={`p-3.5 rounded-2xl border transition cursor-pointer ${
                    !notif.isRead
                      ? 'bg-sky-50 border-sky-300 text-sky-950'
                      : 'bg-slate-50 border-slate-200 text-slate-700'
                  }`}
                >
                  <div className="flex items-center justify-between mb-1">
                    <span className="font-bold text-xs">{notif.title}</span>
                    <span className="text-[10px] text-slate-400">{notif.timestamp}</span>
                  </div>
                  <p className="text-xs leading-relaxed">{notif.message}</p>
                </div>
              ))}
            </div>

            <button
              onClick={() => setShowNotificationsModal(false)}
              className="w-full py-2.5 bg-slate-900 text-white font-bold text-xs rounded-xl cursor-pointer"
            >
              Done
            </button>
          </div>
        </div>
      )}

      {/* HOSPITAL DIRECTIONS / QUEUE DETAILS MODAL */}
      {showDirectionsModal && activeVisit && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl border border-slate-200 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div className="flex items-center gap-2">
                <MapPin className="w-5 h-5 text-sky-600" />
                <h3 className="text-base font-black text-slate-900">
                  Hospital Floor & Directions
                </h3>
              </div>
              <button
                onClick={() => setShowDirectionsModal(false)}
                className="p-1.5 text-slate-400 hover:text-slate-600 rounded-lg cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3 text-xs text-slate-700">
              <div className="p-3 bg-sky-50 rounded-2xl border border-sky-200">
                <div className="font-black text-sky-900 text-sm">{activeVisit.department}</div>
                <div className="text-slate-600 font-bold">{activeVisit.doctorName}</div>
                <div className="text-sky-700 font-mono mt-1 font-bold">{activeVisit.roomNumber}</div>
              </div>

              <div className="space-y-2 p-3 bg-slate-50 rounded-2xl border border-slate-200">
                <strong className="text-slate-900 block font-bold">Directions from Main Kiosk Atrium:</strong>
                <ol className="list-decimal pl-4 space-y-1 text-slate-600">
                  <li>Proceed to Elevator Core B or the central staircase.</li>
                  <li>Go to 1st Floor.</li>
                  <li>Exit right down the General Medicine clinical corridor.</li>
                  <li>Room 104 is on the left; please take a seat in Waiting Area B.</li>
                </ol>
              </div>
            </div>

            <button
              onClick={() => setShowDirectionsModal(false)}
              className="w-full py-2.5 bg-sky-600 hover:bg-sky-700 text-white font-bold text-xs rounded-xl cursor-pointer"
            >
              Close Directions
            </button>
          </div>
        </div>
      )}

      {/* RECORD DETAIL MODAL */}
      {selectedRecordDetail && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-3xl max-w-lg w-full p-6 shadow-2xl border border-slate-200 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <div>
                <span className="text-[10px] font-black uppercase text-sky-600 bg-sky-50 px-2 py-0.5 rounded">
                  {selectedRecordDetail.recordType}
                </span>
                <h3 className="text-base font-black text-slate-900 mt-1">
                  {selectedRecordDetail.department}
                </h3>
              </div>
              <button
                onClick={() => setSelectedRecordDetail(null)}
                className="p-1.5 text-slate-400 hover:text-slate-600 rounded-lg cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <div className="flex justify-between text-slate-500">
                <span>Facility/Clinician: <strong>{selectedRecordDetail.doctorOrFacility}</strong></span>
                <span>Date: <strong>{selectedRecordDetail.date}</strong></span>
              </div>
              <div className="p-4 bg-slate-50 border border-slate-200 rounded-2xl text-slate-800 leading-relaxed font-medium">
                {selectedRecordDetail.summary}
              </div>
            </div>

            <div className="flex gap-2">
              <button
                onClick={() => {
                  setSelectedRecordDetail(null);
                  onNavigate('DOCUMENT_SCAN');
                }}
                className="flex-1 py-2.5 bg-sky-600 hover:bg-sky-700 text-white font-bold text-xs rounded-xl cursor-pointer"
              >
                View in Document Scanner
              </button>
              <button
                onClick={() => setSelectedRecordDetail(null)}
                className="px-4 py-2.5 bg-slate-200 text-slate-800 font-bold text-xs rounded-xl cursor-pointer"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* SERVICE BOOKING MODAL */}
      {serviceBookingModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl border border-slate-200 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-black text-slate-900">
                {serviceBookingModal}
              </h3>
              <button
                onClick={() => setServiceBookingModal(null)}
                className="p-1.5 text-slate-400 hover:text-slate-600 rounded-lg cursor-pointer"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <p className="text-slate-600">
                Confirm your request for <strong>{serviceBookingModal}</strong>. Our hospital coordinator will process your token and dispatch instructions.
              </p>
              <input
                type="text"
                value={bookingNote}
                onChange={(e) => setBookingNote(e.target.value)}
                placeholder="Optional special instructions (e.g. morning slot preferred)..."
                className="w-full p-3 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 focus:outline-none focus:ring-2 focus:ring-sky-500"
              />
            </div>

            <div className="flex gap-2">
              <button
                onClick={() => {
                  onBookServiceAction(serviceBookingModal, bookingNote);
                  setServiceBookingModal(null);
                  setBookingNote('');
                }}
                className="flex-1 py-2.5 bg-sky-600 hover:bg-sky-700 text-white font-bold text-xs rounded-xl cursor-pointer"
              >
                Confirm Request
              </button>
              <button
                onClick={() => setServiceBookingModal(null)}
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
