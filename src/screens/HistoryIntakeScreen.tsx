import React, { useState, useEffect, useRef } from 'react';
import {
  Send,
  Mic,
  Volume2,
  ArrowRight,
  Sparkles,
  Bot,
  User,
  Activity,
  CheckCircle2,
} from 'lucide-react';
import { ChatMessage, Language, SocratesIntake } from '../types';
import { IntakeStepProgressHeader, UrgentRedFlagAlertBanner } from '../components/IntakeStepProgressHeader';
import { createSpeechRecognizer, speakText } from '../utils/speech';

interface HistoryIntakeScreenProps {
  chatMessages: ChatMessage[];
  quickReplies: string[];
  isAiThinking: boolean;
  socratesIntake: SocratesIntake;
  selectedLanguage: Language;
  isRedFlagDetected: boolean;
  redFlagMessage: string;
  onAlertStaff: () => void;
  onDismissRedFlag: () => void;
  onSendMessage: (text: string) => void;
  onReadAloud: (text: string) => void;
  onProceedToDocumentScan: () => void;
}

export const HistoryIntakeScreen: React.FC<HistoryIntakeScreenProps> = ({
  chatMessages,
  quickReplies,
  isAiThinking,
  socratesIntake,
  selectedLanguage,
  isRedFlagDetected,
  redFlagMessage,
  onAlertStaff,
  onDismissRedFlag,
  onSendMessage,
  onReadAloud,
  onProceedToDocumentScan,
}) => {
  const [inputText, setInputText] = useState('');
  const [isRecording, setIsRecording] = useState(false);
  const chatEndRef = useRef<HTMLDivElement>(null);
  const recognizerRef = useRef<any>(null);

  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatMessages, isAiThinking]);

  const handleSubmit = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!inputText.trim() || isAiThinking) return;
    onSendMessage(inputText.trim());
    setInputText('');
  };

  const handleVoiceInput = () => {
    if (isRecording) {
      recognizerRef.current?.stop();
      setIsRecording(false);
      return;
    }

    const recognizer = createSpeechRecognizer(
      selectedLanguage,
      (text, isFinal) => {
        if (isFinal && text.trim()) {
          setIsRecording(false);
          onSendMessage(text.trim());
        } else {
          setInputText(text);
        }
      },
      () => {
        setIsRecording(false);
      }
    );

    if (recognizer) {
      recognizerRef.current = recognizer;
      try {
        recognizer.start();
        setIsRecording(true);
      } catch {
        setIsRecording(false);
      }
    } else {
      alert('Speech recognition not available. Please type or use quick replies.');
    }
  };

  // Completed SOCRATES fields count
  const completedFields = [
    socratesIntake.chiefComplaint,
    socratesIntake.site,
    socratesIntake.onset,
    socratesIntake.character,
    socratesIntake.radiation,
    socratesIntake.associations,
    socratesIntake.timing,
    socratesIntake.exacerbatingRelieving,
    socratesIntake.severity,
    socratesIntake.pastMedicalHistory,
    socratesIntake.currentMedications || socratesIntake.allergies,
  ].filter(Boolean).length;

  return (
    <div className="min-h-screen bg-slate-50 p-4 sm:p-6 pb-24 flex flex-col">
      <div className="max-w-2xl mx-auto w-full flex-1 flex flex-col space-y-4">
        {/* Step 2 Progress Bar */}
        <IntakeStepProgressHeader
          step={2}
          totalSteps={4}
          stepTitle="Conversational History Intake (SOCRATES)"
        />

        {/* Urgent Red Flag Banner if acute symptoms detected */}
        {isRedFlagDetected && (
          <UrgentRedFlagAlertBanner
            message={redFlagMessage}
            onCallStaff={onAlertStaff}
            onDismiss={onDismissRedFlag}
          />
        )}

        {/* SOCRATES Status Tracker Bar */}
        <div className="bg-white rounded-2xl border border-slate-200 p-3 shadow-xs flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="p-1.5 bg-sky-100 text-sky-700 rounded-lg">
              <Activity className="w-4 h-4" />
            </div>
            <span className="text-xs font-bold text-slate-800">
              Clinical Intake Progress
            </span>
          </div>
          <div className="flex items-center gap-2">
            <span className="text-xs font-black text-sky-700">
              {completedFields} / 11 Details Captured
            </span>
            <div className="w-16 bg-slate-200 h-2 rounded-full overflow-hidden">
              <div
                className="bg-sky-600 h-full rounded-full transition-all duration-300"
                style={{ width: `${(completedFields / 11) * 100}%` }}
              />
            </div>
          </div>
        </div>

        {/* Chat History Box */}
        <div className="bg-white rounded-3xl border border-slate-200 p-4 sm:p-6 shadow-sm flex-1 flex flex-col justify-between overflow-hidden min-h-[420px]">
          <div className="space-y-4 overflow-y-auto pr-1 flex-1 max-h-[50vh]">
            {chatMessages.map((msg) => {
              const isAi = msg.sender === 'AI';
              return (
                <div
                  key={msg.id}
                  className={`flex items-start gap-2.5 ${
                    isAi ? 'justify-start' : 'justify-end'
                  }`}
                >
                  {isAi && (
                    <div className="w-8 h-8 rounded-full bg-sky-600 text-white flex items-center justify-center flex-shrink-0 mt-0.5">
                      <Bot className="w-4 h-4" />
                    </div>
                  )}

                  <div
                    className={`max-w-[82%] p-4 rounded-3xl text-sm leading-relaxed shadow-xs ${
                      isAi
                        ? 'bg-slate-50 border border-slate-200 text-slate-900 rounded-tl-xs'
                        : 'bg-sky-600 text-white rounded-tr-xs'
                    }`}
                  >
                    <div className="flex items-start justify-between gap-3">
                      <p className="whitespace-pre-line font-medium">{msg.text}</p>
                      {isAi && (
                        <button
                          onClick={() => onReadAloud(msg.text)}
                          className="p-1 text-slate-400 hover:text-sky-700 rounded-md transition cursor-pointer flex-shrink-0"
                          title="Read question aloud"
                        >
                          <Volume2 className="w-4 h-4" />
                        </button>
                      )}
                    </div>
                  </div>

                  {!isAi && (
                    <div className="w-8 h-8 rounded-full bg-slate-800 text-white flex items-center justify-center flex-shrink-0 mt-0.5">
                      <User className="w-4 h-4" />
                    </div>
                  )}
                </div>
              );
            })}

            {isAiThinking && (
              <div className="flex items-center gap-2 text-slate-400 text-xs font-semibold p-2">
                <div className="w-2 h-2 rounded-full bg-sky-600 animate-ping" />
                <span>MediKiosk AI is analyzing response...</span>
              </div>
            )}

            <div ref={chatEndRef} />
          </div>

          {/* Quick Replies Options */}
          {quickReplies.length > 0 && !isAiThinking && (
            <div className="pt-3 border-t border-slate-100">
              <span className="text-[10px] font-black uppercase tracking-wider text-slate-400 block mb-1.5">
                Suggested Tap Replies:
              </span>
              <div className="flex flex-wrap gap-1.5">
                {quickReplies.map((reply) => (
                  <button
                    key={reply}
                    onClick={() => onSendMessage(reply)}
                    className="px-3.5 py-2 bg-sky-50 hover:bg-sky-100 text-sky-800 border border-sky-200 rounded-2xl text-xs font-bold transition active:scale-95 cursor-pointer shadow-2xs"
                  >
                    {reply}
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Input Controls */}
          <div className="pt-3 mt-3 border-t border-slate-100 flex items-center gap-2">
            <button
              type="button"
              onClick={handleVoiceInput}
              className={`p-3 rounded-2xl transition cursor-pointer active:scale-95 flex-shrink-0 ${
                isRecording
                  ? 'bg-rose-600 text-white animate-pulse'
                  : 'bg-slate-100 hover:bg-slate-200 text-slate-700'
              }`}
              title={isRecording ? 'Listening... Tap to stop' : 'Voice Speak'}
            >
              <Mic className="w-5 h-5" />
            </button>

            <form onSubmit={handleSubmit} className="flex-1 flex items-center gap-2">
              <input
                type="text"
                value={inputText}
                onChange={(e) => setInputText(e.target.value)}
                placeholder="Type your reply or tap microphone..."
                className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-2xl text-sm text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-sky-500"
              />
              <button
                type="submit"
                disabled={!inputText.trim() || isAiThinking}
                className="p-3 bg-sky-600 hover:bg-sky-700 disabled:opacity-50 text-white rounded-2xl transition cursor-pointer active:scale-95 flex-shrink-0"
              >
                <Send className="w-5 h-5" />
              </button>
            </form>
          </div>
        </div>

        {/* Proceed to Step 3 Button */}
        <button
          onClick={onProceedToDocumentScan}
          className="w-full py-4 bg-slate-900 hover:bg-slate-800 text-white font-black text-sm rounded-2xl shadow-md transition flex items-center justify-center gap-2 cursor-pointer active:scale-98"
        >
          <span>Proceed to Step 3: Document & Prescription Scan</span>
          <ArrowRight className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
};
