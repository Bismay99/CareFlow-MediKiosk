import React, { useState } from 'react';
import {
  Calendar,
  Clock,
  MapPin,
  Plus,
  ArrowLeft,
  CheckCircle,
  X,
  Stethoscope,
  Video,
} from 'lucide-react';
import { HospitalAppointment } from '../types';

interface AppointmentsScreenProps {
  appointments: HospitalAppointment[];
  onBackClick: () => void;
  onBookNewAppointment: (doctor: string, dept: string, date: string, room: string) => void;
}

export const AppointmentsScreen: React.FC<AppointmentsScreenProps> = ({
  appointments,
  onBackClick,
  onBookNewAppointment,
}) => {
  const [activeTab, setActiveTab] = useState<'UPCOMING' | 'PAST'>('UPCOMING');
  const [showModal, setShowModal] = useState(false);

  const [formDoctor, setFormDoctor] = useState('Dr. Rajesh Mehta, MD');
  const [formDept, setFormDept] = useState('General Medicine');
  const [formDate, setFormDate] = useState('Tomorrow, 10:30 AM');
  const [formRoom, setFormRoom] = useState('Room 104 - 1st Floor');

  const filtered = appointments.filter((apt) => {
    if (activeTab === 'UPCOMING') {
      return apt.status !== 'Completed' && apt.status !== 'Cancelled';
    } else {
      return apt.status === 'Completed' || apt.status === 'Cancelled';
    }
  });

  const handleBook = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formDoctor.trim() || !formDate.trim()) return;
    onBookNewAppointment(formDoctor, formDept, formDate, formRoom);
    setShowModal(false);
  };

  return (
    <div className="min-h-screen bg-slate-50 p-4 sm:p-6 pb-24">
      <div className="max-w-2xl mx-auto space-y-4">
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
                My Appointments
              </h2>
              <p className="text-xs text-slate-500">
                Scheduled hospital clinic visits & consultations
              </p>
            </div>
          </div>

          <button
            onClick={() => setShowModal(true)}
            className="flex items-center gap-1.5 px-3.5 py-2 bg-sky-600 hover:bg-sky-700 text-white rounded-xl text-xs font-bold shadow-sm transition active:scale-95 cursor-pointer"
          >
            <Plus className="w-4 h-4" />
            <span>Book New</span>
          </button>
        </div>

        {/* Tabs */}
        <div className="flex bg-slate-200/70 p-1 rounded-2xl">
          <button
            onClick={() => setActiveTab('UPCOMING')}
            className={`flex-1 py-2 text-xs font-bold rounded-xl transition cursor-pointer ${
              activeTab === 'UPCOMING'
                ? 'bg-white text-slate-900 shadow-sm'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Upcoming Consultations
          </button>
          <button
            onClick={() => setActiveTab('PAST')}
            className={`flex-1 py-2 text-xs font-bold rounded-xl transition cursor-pointer ${
              activeTab === 'PAST'
                ? 'bg-white text-slate-900 shadow-sm'
                : 'text-slate-600 hover:text-slate-900'
            }`}
          >
            Past History
          </button>
        </div>

        {/* List of Appointments */}
        <div className="space-y-3">
          {filtered.length > 0 ? (
            filtered.map((apt) => (
              <div
                key={apt.id}
                className="bg-white rounded-3xl border border-slate-200 p-5 shadow-sm space-y-3 hover:border-sky-300 transition"
              >
                <div className="flex items-start justify-between gap-2 border-b border-slate-100 pb-3">
                  <div>
                    <span className="text-[10px] font-black uppercase tracking-wider text-sky-700 bg-sky-50 px-2 py-0.5 rounded">
                      {apt.department}
                    </span>
                    <h3 className="text-base font-black text-slate-900 mt-1">
                      {apt.doctorName}
                    </h3>
                    <p className="text-xs text-slate-500 font-medium">
                      {apt.specialty}
                    </p>
                  </div>

                  <span
                    className={`px-2.5 py-1 rounded-full text-[10px] font-bold ${
                      apt.status === 'Checked In' || apt.status === 'Calling Now'
                        ? 'bg-amber-100 text-amber-900 animate-pulse'
                        : apt.status === 'Completed'
                        ? 'bg-emerald-100 text-emerald-900'
                        : 'bg-sky-100 text-sky-800'
                    }`}
                  >
                    {apt.status}
                  </span>
                </div>

                <div className="flex flex-wrap items-center justify-between gap-2 text-xs text-slate-600">
                  <div className="flex items-center gap-1.5 font-bold text-slate-900">
                    <Clock className="w-4 h-4 text-sky-600" />
                    <span>{apt.date} • {apt.time}</span>
                  </div>

                  <div className="flex items-center gap-1.5 font-medium text-slate-600">
                    <MapPin className="w-4 h-4 text-slate-400" />
                    <span>{apt.room}</span>
                  </div>
                </div>
              </div>
            ))
          ) : (
            <div className="p-12 text-center text-slate-400 bg-white rounded-3xl border border-slate-200">
              No appointments found in this view.
            </div>
          )}
        </div>
      </div>

      {/* Book New Appointment Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-3xl max-w-md w-full p-6 shadow-2xl border border-slate-200 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-black text-slate-900">
                Book Hospital Appointment
              </h3>
              <button
                onClick={() => setShowModal(false)}
                className="p-1 text-slate-400 hover:text-slate-600"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleBook} className="space-y-3 text-xs">
              <div>
                <label className="font-bold text-slate-600 block mb-1">Doctor Name</label>
                <input
                  type="text"
                  value={formDoctor}
                  onChange={(e) => setFormDoctor(e.target.value)}
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-900 font-bold"
                />
              </div>

              <div>
                <label className="font-bold text-slate-600 block mb-1">Department</label>
                <select
                  value={formDept}
                  onChange={(e) => setFormDept(e.target.value)}
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-900"
                >
                  <option>General Medicine</option>
                  <option>Cardiology Wing</option>
                  <option>Orthopedics & Joint Clinic</option>
                  <option>Obstetrics & Maternal OPD</option>
                  <option>Endocrinology & Diabetology</option>
                </select>
              </div>

              <div>
                <label className="font-bold text-slate-600 block mb-1">Date & Time</label>
                <input
                  type="text"
                  value={formDate}
                  onChange={(e) => setFormDate(e.target.value)}
                  placeholder="e.g. Tomorrow, 10:30 AM"
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-900"
                />
              </div>

              <div>
                <label className="font-bold text-slate-600 block mb-1">Room / Location</label>
                <input
                  type="text"
                  value={formRoom}
                  onChange={(e) => setFormRoom(e.target.value)}
                  placeholder="e.g. Room 104 - 1st Floor"
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl text-slate-900"
                />
              </div>

              <div className="flex gap-2 pt-2">
                <button
                  type="submit"
                  className="flex-1 py-2.5 bg-sky-600 hover:bg-sky-700 text-white font-bold text-xs rounded-xl cursor-pointer"
                >
                  Confirm Appointment
                </button>
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2.5 bg-slate-200 text-slate-800 font-bold text-xs rounded-xl cursor-pointer"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
