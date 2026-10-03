import React from 'react';
import { Home, Scan, Calendar, User } from 'lucide-react';
import { Screen } from '../types';

interface KioskBottomBarProps {
  currentScreen: Screen;
  onScreenSelect: (screen: Screen) => void;
  onAppointmentsClick: () => void;
}

export const KioskBottomBar: React.FC<KioskBottomBarProps> = ({
  currentScreen,
  onScreenSelect,
  onAppointmentsClick,
}) => {
  const navItems = [
    { screen: 'WELCOME' as Screen, label: 'Home', icon: Home },
    { screen: 'DOCUMENT_SCAN' as Screen, label: 'Scan Docs', icon: Scan },
    { screen: 'APPOINTMENTS' as Screen, label: 'Appointments', icon: Calendar, onClick: onAppointmentsClick },
    { screen: 'PROFILE' as Screen, label: 'Profile', icon: User },
  ];

  return (
    <nav className="fixed bottom-0 left-0 right-0 z-40 bg-white/95 backdrop-blur border-t border-slate-200 px-4 py-2 shadow-lg">
      <div className="max-w-md mx-auto flex items-center justify-around">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = currentScreen === item.screen;
          return (
            <button
              key={item.label}
              onClick={() => (item.onClick ? item.onClick() : onScreenSelect(item.screen))}
              className={`flex flex-col items-center gap-1 py-1 px-3 rounded-xl transition cursor-pointer active:scale-95 ${
                isActive
                  ? 'text-sky-700 font-bold'
                  : 'text-slate-500 hover:text-slate-800'
              }`}
            >
              <div
                className={`p-1.5 rounded-xl transition ${
                  isActive ? 'bg-sky-100 text-sky-700' : 'bg-transparent'
                }`}
              >
                <Icon className="w-5 h-5" />
              </div>
              <span className="text-[11px] leading-none tracking-tight">
                {item.label}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
