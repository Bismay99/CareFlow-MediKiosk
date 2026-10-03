import React, { useState, useEffect, useRef } from 'react';
import { Mic, MicOff, Send, X, Volume2, Sparkles, AlertCircle } from 'lucide-react';
import { VoiceAssistantState, Language } from '../types';
import { createSpeechRecognizer, speakText, stopSpeaking } from '../utils/speech';

interface VoiceAssistantModalProps {
  isOpen: boolean;
  state: VoiceAssistantState;
  statusText: string;
  transcript: string;
  response: string;
  language: Language;
  onClose: () => void;
  onSendCommand: (query: string) => void;
  isAccessibilityMode: boolean;
}

export const VoiceAssistantModal: React.FC<VoiceAssistantModalProps> = ({
  isOpen,
  state,
  statusText,
  transcript,
  response,
  language,
  onClose,
  onSendCommand,
  isAccessibilityMode,
}) => {
  const [inputText, setInputText] = useState('');
  const [isListening, setIsListening] = useState(false);
  const [currentTranscript, setCurrentTranscript] = useState(transcript);
  const recognizerRef = useRef<any>(null);

  useEffect(() => {
    setCurrentTranscript(transcript);
  }, [transcript]);

  useEffect(() => {
    if (!isOpen) {
      stopListening();
      stopSpeaking();
    }
  }, [isOpen]);

  const startListening = () => {
    stopSpeaking();
    const recognizer = createSpeechRecognizer(
      language,
      (text, isFinal) => {
        setCurrentTranscript(text);
        if (isFinal) {
          setIsListening(false);
          onSendCommand(text);
        }
      },
      (err) => {
        console.warn('Speech recognition error:', err);
        setIsListening(false);
      }
    );

    if (recognizer) {
      recognizerRef.current = recognizer;
      try {
        recognizer.start();
        setIsListening(true);
      } catch (e) {
        console.error('Error starting recognition:', e);
      }
    } else {
      alert('Speech recognition is not supported in this browser. You can type your query below.');
    }
  };

  const stopListening = () => {
    if (recognizerRef.current) {
      try {
        recognizerRef.current.stop();
      } catch (e) {
        // Ignore
      }
      recognizerRef.current = null;
    }
    setIsListening(false);
  };

  const handleSubmit = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    const q = inputText.trim() || currentTranscript.trim();
    if (q) {
      stopListening();
      onSendCommand(q);
      setInputText('');
    }
  };

  if (!isOpen) return null;

  const quickPrompts = [
    'Where is my queue token?',
    'Show upcoming appointments',
    'How do I reach Room 104?',
    'I need wheelchair staff assistance',
    'Open document scanner',
  ];

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/70 backdrop-blur-sm animate-fade-in">
      <div className="w-full max-w-lg bg-white rounded-3xl shadow-2xl border border-slate-200 overflow-hidden flex flex-col">
        {/* Header */}
        <div className="p-4 bg-gradient-to-r from-sky-700 to-indigo-800 text-white flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            <div className="p-2 bg-white/20 rounded-xl">
              <Sparkles className="w-5 h-5 text-sky-200" />
            </div>
            <div>
              <h3 className="font-bold text-base leading-tight">
                CareFlow AI Voice Assistant
              </h3>
              <p className="text-xs text-sky-200">
                Hospital guidance, queue tracking & clinical check-in
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-2 hover:bg-white/20 rounded-xl transition cursor-pointer text-white/80 hover:text-white"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Assistant Content Body */}
        <div className="p-6 space-y-5">
          {/* Animated Center Orb & Mic Status */}
          <div className="flex flex-col items-center justify-center py-4 text-center">
            <button
              onClick={isListening ? stopListening : startListening}
              className={`relative p-6 rounded-full transition-all duration-300 active:scale-95 cursor-pointer shadow-lg ${
                isListening
                  ? 'bg-rose-500 text-white ring-8 ring-rose-200 animate-pulse'
                  : state === 'PROCESSING'
                  ? 'bg-amber-500 text-white ring-8 ring-amber-200 animate-spin'
                  : 'bg-sky-600 hover:bg-sky-700 text-white ring-4 ring-sky-100'
              }`}
            >
              {isListening ? (
                <Mic className="w-8 h-8" />
              ) : (
                <Mic className="w-8 h-8" />
              )}
            </button>

            <span className="mt-4 text-xs font-bold uppercase tracking-wider text-slate-500">
              {isListening
                ? '🎙 Listening... Speak clearly now'
                : state === 'PROCESSING'
                ? 'Thinking & executing...'
                : statusText || 'Tap microphone to speak'}
            </span>
          </div>

          {/* Transcript / Input text */}
          {(currentTranscript || isListening) && (
            <div className="p-3.5 bg-slate-50 border border-slate-200 rounded-2xl">
              <span className="text-[10px] font-black uppercase text-slate-400 block mb-1">
                You asked:
              </span>
              <p className="text-sm font-semibold text-slate-800">
                "{currentTranscript || 'Listening...'}"
              </p>
            </div>
          )}

          {/* Response Box */}
          {response && (
            <div className="p-4 bg-sky-50 border border-sky-200 rounded-2xl space-y-2 animate-fade-in">
              <div className="flex items-center justify-between">
                <span className="text-xs font-black uppercase tracking-wider text-sky-800 flex items-center gap-1.5">
                  <Sparkles className="w-3.5 h-3.5 text-sky-600" />
                  CareFlow Response
                </span>
                <button
                  onClick={() => speakText(response, language)}
                  className="p-1.5 text-sky-700 hover:bg-sky-200/60 rounded-lg transition cursor-pointer"
                  title="Speak again"
                >
                  <Volume2 className="w-4 h-4" />
                </button>
              </div>
              <p className="text-sm text-slate-900 leading-relaxed font-medium">
                {response}
              </p>
            </div>
          )}

          {/* Quick Prompts */}
          <div>
            <span className="text-[11px] font-bold text-slate-400 uppercase tracking-wider block mb-2">
              Common Questions:
            </span>
            <div className="flex flex-wrap gap-1.5">
              {quickPrompts.map((prompt) => (
                <button
                  key={prompt}
                  onClick={() => {
                    setCurrentTranscript(prompt);
                    onSendCommand(prompt);
                  }}
                  className="text-xs font-medium px-3 py-1.5 bg-slate-100 hover:bg-sky-50 hover:text-sky-700 hover:border-sky-300 border border-slate-200 rounded-xl transition cursor-pointer"
                >
                  {prompt}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Text Input Footer */}
        <div className="p-4 bg-slate-50 border-t border-slate-200">
          <form onSubmit={handleSubmit} className="flex items-center gap-2">
            <input
              type="text"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder="Or type a question (e.g. queue wait time)..."
              className="flex-1 bg-white border border-slate-300 rounded-xl px-3.5 py-2.5 text-sm text-slate-800 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
            <button
              type="submit"
              disabled={!inputText.trim()}
              className="p-2.5 bg-sky-600 hover:bg-sky-700 disabled:opacity-50 text-white rounded-xl transition cursor-pointer flex-shrink-0"
            >
              <Send className="w-4 h-4" />
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
