import React, { useState, useRef } from 'react';
import {
  Camera,
  Upload,
  FileText,
  Plus,
  Trash2,
  Edit2,
  ArrowRight,
  Sparkles,
  CheckCircle,
  AlertCircle,
  X,
  RefreshCw,
} from 'lucide-react';
import { ExtractedDocument, MedicationItem, LabValueItem } from '../types';
import { IntakeStepProgressHeader } from '../components/IntakeStepProgressHeader';

interface DocumentScanScreenProps {
  scannedImageUrl: string | null;
  isScanning: boolean;
  extractedDoc: ExtractedDocument;
  onImageCaptured: (dataUrl: string) => void;
  onLoadSampleDocument: () => void;
  onUpdateDiagnosis: (diagnosis: string) => void;
  onAddMedication: (item: MedicationItem) => void;
  onUpdateMedication: (index: number, item: MedicationItem) => void;
  onDeleteMedication: (index: number) => void;
  onAddLabValue: (item: LabValueItem) => void;
  onUpdateLabValue: (index: number, item: LabValueItem) => void;
  onDeleteLabValue: (index: number) => void;
  onProceedToSummary: () => void;
}

export const DocumentScanScreen: React.FC<DocumentScanScreenProps> = ({
  scannedImageUrl,
  isScanning,
  extractedDoc,
  onImageCaptured,
  onLoadSampleDocument,
  onUpdateDiagnosis,
  onAddMedication,
  onUpdateMedication,
  onDeleteMedication,
  onAddLabValue,
  onUpdateLabValue,
  onDeleteLabValue,
  onProceedToSummary,
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [editingMedIndex, setEditingMedIndex] = useState<number | null>(null);
  const [editingLabIndex, setEditingLabIndex] = useState<number | null>(null);

  const [medModalOpen, setMedModalOpen] = useState(false);
  const [medForm, setMedForm] = useState<MedicationItem>({
    name: '',
    dosage: '40 mg',
    frequency: 'OD (Morning)',
    instructions: 'Take after breakfast with water.',
  });

  const [labModalOpen, setLabModalOpen] = useState(false);
  const [labForm, setLabForm] = useState<LabValueItem>({
    testName: '',
    value: '',
    unit: 'mg/dL',
    referenceRange: '< 200',
    status: 'Normal',
  });

  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onload = (event) => {
        if (event.target?.result) {
          onImageCaptured(event.target.result as string);
        }
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSaveMed = () => {
    if (!medForm.name.trim()) return;
    if (editingMedIndex !== null) {
      onUpdateMedication(editingMedIndex, medForm);
    } else {
      onAddMedication(medForm);
    }
    setMedModalOpen(false);
    setEditingMedIndex(null);
    setMedForm({ name: '', dosage: '40 mg', frequency: 'OD (Morning)', instructions: 'Take after breakfast' });
  };

  const handleSaveLab = () => {
    if (!labForm.testName.trim()) return;
    if (editingLabIndex !== null) {
      onUpdateLabValue(editingLabIndex, labForm);
    } else {
      onAddLabValue(labForm);
    }
    setLabModalOpen(false);
    setEditingLabIndex(null);
    setLabForm({ testName: '', value: '', unit: 'mg/dL', referenceRange: '< 200', status: 'Normal' });
  };

  return (
    <div className="min-h-screen bg-slate-50 p-4 sm:p-6 pb-24">
      <div className="max-w-3xl mx-auto space-y-4">
        {/* Step 3 Progress Bar */}
        <IntakeStepProgressHeader
          step={3}
          totalSteps={4}
          stepTitle="Document OCR & Prescription Extraction"
        />

        {/* Capture / Upload Box */}
        <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-sm space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-slate-100 pb-3">
            <div>
              <h2 className="text-xl font-black text-slate-900">
                Scan Medical Records
              </h2>
              <p className="text-xs text-slate-500 mt-0.5">
                Upload or capture past hospital prescriptions, lab reports, or discharge slips.
              </p>
            </div>

            {/* Load Sample Prescription Button */}
            <button
              onClick={onLoadSampleDocument}
              className="px-3.5 py-2 bg-sky-50 hover:bg-sky-100 text-sky-800 border border-sky-200 rounded-xl text-xs font-bold transition active:scale-95 cursor-pointer flex items-center gap-1.5 self-start sm:self-auto"
            >
              <FileText className="w-4 h-4 text-sky-600" />
              <span>Load Sample Apollo Prescription</span>
            </button>
          </div>

          {/* Action Row */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <input
              type="file"
              ref={fileInputRef}
              accept="image/*,.pdf"
              onChange={handleFileUpload}
              className="hidden"
            />

            <button
              onClick={() => fileInputRef.current?.click()}
              className="p-4 rounded-2xl border-2 border-dashed border-sky-300 hover:border-sky-500 bg-sky-50/50 hover:bg-sky-50 text-slate-800 flex items-center justify-center gap-3 transition cursor-pointer active:scale-98"
            >
              <Upload className="w-5 h-5 text-sky-600" />
              <div className="text-left">
                <div className="text-xs font-black">Upload Prescription Image</div>
                <div className="text-[11px] text-slate-500">JPG, PNG, PDF up to 20MB</div>
              </div>
            </button>

            <button
              onClick={() => fileInputRef.current?.click()}
              className="p-4 rounded-2xl border-2 border-dashed border-slate-300 hover:border-slate-400 bg-slate-50 hover:bg-slate-100 text-slate-800 flex items-center justify-center gap-3 transition cursor-pointer active:scale-98"
            >
              <Camera className="w-5 h-5 text-slate-600" />
              <div className="text-left">
                <div className="text-xs font-black">Capture via Kiosk Camera</div>
                <div className="text-[11px] text-slate-500">Document scanning lens</div>
              </div>
            </button>
          </div>

          {/* Scanned Document Preview if active */}
          {scannedImageUrl && (
            <div className="p-3 bg-slate-50 border border-slate-200 rounded-2xl space-y-2">
              <span className="text-[10px] font-black uppercase tracking-wider text-slate-400 block">
                Uploaded Document Preview:
              </span>
              <div className="max-h-48 overflow-hidden rounded-xl border border-slate-200 bg-white flex items-center justify-center">
                <img
                  src={scannedImageUrl}
                  alt="Scanned Prescription"
                  className="max-h-48 object-contain"
                />
              </div>
            </div>
          )}

          {isScanning && (
            <div className="p-4 bg-sky-50 border border-sky-200 rounded-2xl flex items-center gap-3 animate-pulse">
              <RefreshCw className="w-5 h-5 text-sky-600 animate-spin" />
              <div className="text-xs font-bold text-sky-900">
                Gemini Multimodal OCR is extracting medications, dosages, and lab tables...
              </div>
            </div>
          )}
        </div>

        {/* Structured OCR Results Container */}
        <div className="bg-white rounded-3xl border border-slate-200 p-6 shadow-sm space-y-6">
          <div className="flex items-center justify-between border-b border-slate-100 pb-3">
            <div className="flex items-center gap-2">
              <Sparkles className="w-5 h-5 text-sky-600" />
              <h3 className="text-lg font-black text-slate-900">
                Verified Extracted Clinical Data
              </h3>
            </div>
            <span className="text-xs text-slate-500 font-medium">
              Review and edit prior to summary
            </span>
          </div>

          {/* Diagnosis */}
          <div>
            <label className="block text-xs font-black uppercase tracking-wider text-slate-500 mb-1.5">
              Extracted Primary Diagnosis
            </label>
            <input
              type="text"
              value={extractedDoc.diagnosis}
              onChange={(e) => onUpdateDiagnosis(e.target.value)}
              placeholder="e.g. Essential Hypertension Stage 1, Hyperlipidemia"
              className="w-full p-3 bg-slate-50 border border-slate-200 rounded-2xl text-sm font-bold text-slate-900 focus:bg-white focus:outline-none focus:ring-2 focus:ring-sky-500"
            />
          </div>

          {/* Medications Table */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <div>
                <h4 className="text-sm font-black text-slate-900">
                  Extracted Medications ({extractedDoc.medications.length})
                </h4>
                <p className="text-xs text-slate-500">
                  Prescription dosages and patient food directions
                </p>
              </div>

              <button
                type="button"
                onClick={() => {
                  setEditingMedIndex(null);
                  setMedForm({ name: '', dosage: '40 mg', frequency: 'OD (Morning)', instructions: 'Take after breakfast' });
                  setMedModalOpen(true);
                }}
                className="flex items-center gap-1.5 px-3 py-1.5 bg-sky-600 hover:bg-sky-700 text-white rounded-xl text-xs font-bold transition active:scale-95 cursor-pointer"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>Add Medication</span>
              </button>
            </div>

            <div className="space-y-2">
              {extractedDoc.medications.map((med, idx) => (
                <div
                  key={idx}
                  className="p-3 bg-slate-50 border border-slate-200 rounded-2xl flex items-center justify-between gap-3 text-xs"
                >
                  <div className="min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-slate-900 text-sm">
                        {med.name}
                      </span>
                      <span className="text-[10px] font-bold text-sky-700 bg-sky-100 px-2 py-0.5 rounded">
                        {med.dosage}
                      </span>
                      <span className="text-[10px] text-slate-500 font-mono">
                        {med.frequency}
                      </span>
                    </div>
                    <p className="text-slate-600 mt-0.5 truncate">
                      {med.instructions}
                    </p>
                  </div>

                  <div className="flex items-center gap-1 flex-shrink-0">
                    <button
                      onClick={() => {
                        setEditingMedIndex(idx);
                        setMedForm({ ...med });
                        setMedModalOpen(true);
                      }}
                      className="p-2 text-slate-500 hover:text-sky-700 rounded-xl hover:bg-slate-200/50 transition cursor-pointer"
                      title="Edit"
                    >
                      <Edit2 className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => onDeleteMedication(idx)}
                      className="p-2 text-rose-500 hover:text-rose-700 rounded-xl hover:bg-rose-100/50 transition cursor-pointer"
                      title="Delete"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Lab Values Table */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <div>
                <h4 className="text-sm font-black text-slate-900">
                  Extracted Lab Profile & Investigations ({extractedDoc.labValues.length})
                </h4>
                <p className="text-xs text-slate-500">
                  Blood chemistry, pathology metrics & reference ranges
                </p>
              </div>

              <button
                type="button"
                onClick={() => {
                  setEditingLabIndex(null);
                  setLabForm({ testName: '', value: '', unit: 'mg/dL', referenceRange: '< 200', status: 'Normal' });
                  setLabModalOpen(true);
                }}
                className="flex items-center gap-1.5 px-3 py-1.5 bg-sky-600 hover:bg-sky-700 text-white rounded-xl text-xs font-bold transition active:scale-95 cursor-pointer"
              >
                <Plus className="w-3.5 h-3.5" />
                <span>Add Lab Test</span>
              </button>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
              {extractedDoc.labValues.map((lab, idx) => (
                <div
                  key={idx}
                  className="p-3 bg-slate-50 border border-slate-200 rounded-2xl flex items-center justify-between gap-2 text-xs"
                >
                  <div>
                    <div className="flex items-center gap-2">
                      <span className="font-bold text-slate-900">
                        {lab.testName}
                      </span>
                      <span
                        className={`text-[10px] font-bold px-1.5 py-0.5 rounded ${
                          lab.status === 'High'
                            ? 'bg-rose-100 text-rose-700'
                            : lab.status === 'Low'
                            ? 'bg-amber-100 text-amber-800'
                            : 'bg-emerald-100 text-emerald-800'
                        }`}
                      >
                        {lab.status}
                      </span>
                    </div>
                    <div className="text-slate-700 font-mono mt-0.5">
                      <strong>{lab.value}</strong> {lab.unit} <span className="text-slate-400 font-normal">({lab.referenceRange})</span>
                    </div>
                  </div>

                  <div className="flex items-center gap-1 flex-shrink-0">
                    <button
                      onClick={() => {
                        setEditingLabIndex(idx);
                        setLabForm({ ...lab });
                        setLabModalOpen(true);
                      }}
                      className="p-1.5 text-slate-500 hover:text-sky-700 rounded-lg cursor-pointer"
                      title="Edit"
                    >
                      <Edit2 className="w-3.5 h-3.5" />
                    </button>
                    <button
                      onClick={() => onDeleteLabValue(idx)}
                      className="p-1.5 text-rose-500 hover:text-rose-700 rounded-lg cursor-pointer"
                      title="Delete"
                    >
                      <Trash2 className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Proceed to Step 4 Button */}
        <button
          onClick={onProceedToSummary}
          className="w-full py-4 bg-slate-900 hover:bg-slate-800 text-white font-black text-sm rounded-2xl shadow-md transition flex items-center justify-center gap-2 cursor-pointer active:scale-98"
        >
          <span>Proceed to Step 4: Synthesize Clinical Summary</span>
          <ArrowRight className="w-4 h-4" />
        </button>
      </div>

      {/* Medication Add/Edit Modal */}
      {medModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-3xl p-6 max-w-md w-full shadow-2xl border border-slate-200 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-black text-slate-900">
                {editingMedIndex !== null ? 'Edit Medication' : 'Add Medication'}
              </h3>
              <button onClick={() => setMedModalOpen(false)} className="p-1 text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <label className="font-bold text-slate-600 block mb-1">Medicine Name</label>
                <input
                  type="text"
                  value={medForm.name}
                  onChange={(e) => setMedForm({ ...medForm, name: e.target.value })}
                  placeholder="e.g. Telmisartan"
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl"
                />
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="font-bold text-slate-600 block mb-1">Dosage</label>
                  <input
                    type="text"
                    value={medForm.dosage}
                    onChange={(e) => setMedForm({ ...medForm, dosage: e.target.value })}
                    placeholder="40 mg"
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl"
                  />
                </div>
                <div>
                  <label className="font-bold text-slate-600 block mb-1">Frequency</label>
                  <input
                    type="text"
                    value={medForm.frequency}
                    onChange={(e) => setMedForm({ ...medForm, frequency: e.target.value })}
                    placeholder="OD (Morning)"
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl"
                  />
                </div>
              </div>
              <div>
                <label className="font-bold text-slate-600 block mb-1">Instructions</label>
                <input
                  type="text"
                  value={medForm.instructions}
                  onChange={(e) => setMedForm({ ...medForm, instructions: e.target.value })}
                  placeholder="Take after breakfast with water"
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl"
                />
              </div>
            </div>

            <div className="flex gap-2 pt-2">
              <button
                onClick={handleSaveMed}
                className="flex-1 py-2.5 bg-sky-600 hover:bg-sky-700 text-white font-bold text-xs rounded-xl"
              >
                Save
              </button>
              <button
                onClick={() => setMedModalOpen(false)}
                className="px-4 py-2.5 bg-slate-200 text-slate-800 font-bold text-xs rounded-xl"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Lab Value Add/Edit Modal */}
      {labModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/60 backdrop-blur-sm animate-fade-in">
          <div className="bg-white rounded-3xl p-6 max-w-md w-full shadow-2xl border border-slate-200 space-y-4">
            <div className="flex items-center justify-between border-b border-slate-100 pb-3">
              <h3 className="text-base font-black text-slate-900">
                {editingLabIndex !== null ? 'Edit Lab Investigation' : 'Add Lab Investigation'}
              </h3>
              <button onClick={() => setLabModalOpen(false)} className="p-1 text-slate-400 hover:text-slate-600">
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <label className="font-bold text-slate-600 block mb-1">Test Name</label>
                <input
                  type="text"
                  value={labForm.testName}
                  onChange={(e) => setLabForm({ ...labForm, testName: e.target.value })}
                  placeholder="e.g. Total Cholesterol"
                  className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl"
                />
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="font-bold text-slate-600 block mb-1">Value</label>
                  <input
                    type="text"
                    value={labForm.value}
                    onChange={(e) => setLabForm({ ...labForm, value: e.target.value })}
                    placeholder="228"
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl"
                  />
                </div>
                <div>
                  <label className="font-bold text-slate-600 block mb-1">Unit</label>
                  <input
                    type="text"
                    value={labForm.unit}
                    onChange={(e) => setLabForm({ ...labForm, unit: e.target.value })}
                    placeholder="mg/dL"
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl"
                  />
                </div>
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div>
                  <label className="font-bold text-slate-600 block mb-1">Reference Range</label>
                  <input
                    type="text"
                    value={labForm.referenceRange}
                    onChange={(e) => setLabForm({ ...labForm, referenceRange: e.target.value })}
                    placeholder="< 200"
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl"
                  />
                </div>
                <div>
                  <label className="font-bold text-slate-600 block mb-1">Status</label>
                  <select
                    value={labForm.status}
                    onChange={(e) => setLabForm({ ...labForm, status: e.target.value as any })}
                    className="w-full p-2.5 bg-slate-50 border border-slate-200 rounded-xl"
                  >
                    <option value="Normal">Normal</option>
                    <option value="High">High</option>
                    <option value="Low">Low</option>
                  </select>
                </div>
              </div>
            </div>

            <div className="flex gap-2 pt-2">
              <button
                onClick={handleSaveLab}
                className="flex-1 py-2.5 bg-sky-600 hover:bg-sky-700 text-white font-bold text-xs rounded-xl"
              >
                Save
              </button>
              <button
                onClick={() => setLabModalOpen(false)}
                className="px-4 py-2.5 bg-slate-200 text-slate-800 font-bold text-xs rounded-xl"
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
