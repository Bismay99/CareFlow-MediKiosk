import { Language } from '../types';

let currentUtterance: SpeechSynthesisUtterance | null = null;

export function speakText(text: string, language?: Language, enabled: boolean = true) {
  if (!enabled || !text.trim() || typeof window === 'undefined' || !window.speechSynthesis) return;

  stopSpeaking();

  const utterance = new SpeechSynthesisUtterance(text);
  const langCode = language?.code || 'en';

  const langMap: Record<string, string> = {
    en: 'en-IN',
    hi: 'hi-IN',
    bn: 'bn-IN',
    ta: 'ta-IN',
    te: 'te-IN',
    mr: 'mr-IN'
  };

  utterance.lang = langMap[langCode] || 'en-US';
  utterance.rate = 0.95;
  utterance.pitch = 1.0;

  currentUtterance = utterance;
  window.speechSynthesis.speak(utterance);
}

export function stopSpeaking() {
  if (typeof window !== 'undefined' && window.speechSynthesis) {
    window.speechSynthesis.cancel();
    currentUtterance = null;
  }
}

// Browser speech recognition wrapper
export function createSpeechRecognizer(
  language: Language,
  onResult: (text: string, isFinal: boolean) => void,
  onError: (err: any) => void
) {
  if (typeof window === 'undefined') return null;

  const SpeechRecognition =
    (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;

  if (!SpeechRecognition) {
    return null;
  }

  const recognition = new SpeechRecognition();
  const langCode = language.code || 'en';
  const langMap: Record<string, string> = {
    en: 'en-IN',
    hi: 'hi-IN',
    bn: 'bn-IN',
    ta: 'ta-IN',
    te: 'te-IN',
    mr: 'mr-IN'
  };

  recognition.lang = langMap[langCode] || 'en-US';
  recognition.interimResults = true;
  recognition.continuous = false;

  recognition.onresult = (event: any) => {
    let interim = '';
    let final = '';

    for (let i = event.resultIndex; i < event.results.length; ++i) {
      if (event.results[i].isFinal) {
        final += event.results[i][0].transcript;
      } else {
        interim += event.results[i][0].transcript;
      }
    }

    if (final) {
      onResult(final.trim(), true);
    } else if (interim) {
      onResult(interim.trim(), false);
    }
  };

  recognition.onerror = (err: any) => {
    onError(err);
  };

  return recognition;
}
