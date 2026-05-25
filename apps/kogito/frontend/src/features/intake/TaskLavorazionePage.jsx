// Pagina principale task lavorazione (Sprint 12)

const FASI = [
  { id: 'RACCOLTA_INPUT', label: 'Raccolta input' },
  { id: 'LAVORAZIONE',    label: 'Lavorazione' },
  { id: 'CHIUSURA_PRATICA', label: 'Chiusura Pratica' },
];
// Sostituisce TypingPage con layout sidebar + milestone bar + step navigation
// Ref: GAP-US-03, GAP-US-04, GAP-US-06 | GAP-UX.md §4 §6 §8 | GAP-UI.md §2.4

import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { attachmentsApi } from '../../core/api/attachmentsApi';
import { intakeApi } from '../../core/api/intakeApi';
import { practicesApi } from '../../core/api/practicesApi';
import { tasksApi } from '../../core/api/tasksApi';
import { usePhase } from '../../core/PhaseContext';
import { ClassificazioneStep } from './ClassificazioneStep';
import { VerificaDocumentiStep } from './VerificaDocumentiStep';
import { WorkflowSidebar } from './WorkflowSidebar';

// ─── Costanti ───────────────────────────────────────────────────────────────

const checklistEmptyForm = {
  documentPresent: '',
  legibility: '',
  formalSuitability: '',
  clientDataConsistency: '',
  cardNumberCheckEnabled: false,
  cardNumberMatch: '',
  formalKoReasons: [],
  internalNotes: '',
  finalNotePractice: '',
  cardNotes: {
    legibility: '',
    formalSuitability: ''
  },
  verbaleNotes: {
    legibility: '',
    formalSuitability: '',
    clientDataConsistency: '',
    cardNumberMatch: ''
  },
  // Sprint 13: campi KO opzionali
  codiceCausaleIdCarta: null,
  codiceCausaleIdVerbale: null
};

const verbaleNoteKeys = ['legibility', 'formalSuitability', 'clientDataConsistency', 'cardNumberMatch'];
const cardNoteKeys = ['legibility', 'formalSuitability'];

// ─── Helper functions (allineate con TypingPage) ────────────────────────────

function normalizeDocumentType(value) {
  if (!value || typeof value !== 'string') return '';
  const upper = value.toUpperCase();
  if (upper.includes('VERBALE')) return 'VERBALE';
  if (upper.includes('CARTA'))   return 'CARTA';
  return '';
}

function normalizeYesNo(value) {
  if (typeof value === 'boolean') return value ? 'SI' : 'NO';
  if (typeof value !== 'string')  return '';
  const u = value.toUpperCase();
  if (u === 'SI' || u === 'YES' || u === 'TRUE')  return 'SI';
  if (u === 'NO' || u === 'FALSE')                 return 'NO';
  return '';
}

function normalizeOutcome(value) {
  if (typeof value !== 'string') return '';
  const u = value.toUpperCase();
  if (u.includes('APPROVATA')) return 'APPROVATA';
  if (u.includes('RESPINTA'))  return 'RESPINTA';
  return '';
}

function normalizeKoReasonList(value) {
  if (!Array.isArray(value)) return [];
  const allowed = new Set(['INTESTAZIONE', 'FIRME', 'TIMBRO', 'DICHIARAZIONE', 'CARTA_PI']);
  return value.map((i) => String(i ?? '').toUpperCase()).filter((i) => allowed.has(i));
}

function toBooleanOrNull(value) {
  if (value === 'SI') return true;
  if (value === 'NO') return false;
  return null;
}

function formatDateTime(value) {
  if (!value) return '-';
  const date = value instanceof Date ? value : new Date(value);
  if (Number.isNaN(date.getTime())) return '-';
  return new Intl.DateTimeFormat('it-IT', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }).format(date);
}

// Sprint 13: formattazione data nota (HH:mm DD/MM/YYYY)
function formatNoteDate(dateStr) {
  if (!dateStr) return '';
  try {
    const d = new Date(dateStr);
    const hh = String(d.getHours()).padStart(2, '0');
    const mm = String(d.getMinutes()).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    const mo = String(d.getMonth() + 1).padStart(2, '0');
    const yyyy = d.getFullYear();
    return `${hh}:${mm} ${dd}/${mo}/${yyyy}`;
  } catch {
    return dateStr;
  }
}

function normalizeVerbaleNotes(raw) {
  const normalized = {
    legibility: '',
    formalSuitability: '',
    clientDataConsistency: '',
    cardNumberMatch: ''
  };
  if (!raw || typeof raw !== 'object' || Array.isArray(raw)) return normalized;
  for (const key of verbaleNoteKeys) {
    normalized[key] = typeof raw[key] === 'string' ? raw[key] : '';
  }
  return normalized;
}

function normalizeCardNotes(raw) {
  const normalized = {
    legibility: '',
    formalSuitability: ''
  };
  if (!raw || typeof raw !== 'object' || Array.isArray(raw)) return normalized;
  for (const key of cardNoteKeys) {
    normalized[key] = typeof raw[key] === 'string' ? raw[key] : '';
  }
  return normalized;
}

function pickFirstString(...values) {
  for (const value of values) {
    if (typeof value !== 'string') continue;
    return value;
  }
  return '';
}

function extractInternalNotesEnvelope(rawNotes) {
  const emptyEnvelope = {
    legacy: '',
    finalNotePractice: '',
    cardKoNote: '',
    cardNotes: normalizeCardNotes(null),
    verbaleNotes: normalizeVerbaleNotes(null)
  };

  if (typeof rawNotes !== 'string') return emptyEnvelope;
  const trimmed = rawNotes.trim();
  if (!trimmed) return emptyEnvelope;

  try {
    const parsed = JSON.parse(trimmed);
    const isEnvelope = parsed && typeof parsed === 'object' && !Array.isArray(parsed)
      && (Object.prototype.hasOwnProperty.call(parsed, 'legacy')
        || Object.prototype.hasOwnProperty.call(parsed, 'verbaleNotes')
        || Object.prototype.hasOwnProperty.call(parsed, 'cardNotes'));

    if (!isEnvelope) {
      return {
        legacy: trimmed,
        finalNotePractice: '',
        cardKoNote: '',
        cardNotes: normalizeCardNotes(null),
        verbaleNotes: normalizeVerbaleNotes(null)
      };
    }

    return {
      legacy: typeof parsed.legacy === 'string' ? parsed.legacy : '',
      finalNotePractice: typeof parsed.finalNotePractice === 'string'
        ? parsed.finalNotePractice
        : (typeof parsed.finalNote === 'string' ? parsed.finalNote : ''),
      cardKoNote: typeof parsed.cardKoNote === 'string' ? parsed.cardKoNote : '',
      cardNotes: normalizeCardNotes(parsed.cardNotes),
      verbaleNotes: normalizeVerbaleNotes(parsed.verbaleNotes)
    };
  } catch {
    // Fallback legacy: stringa non JSON valida
    return {
      legacy: trimmed,
      finalNotePractice: '',
      cardKoNote: '',
      cardNotes: normalizeCardNotes(null),
      verbaleNotes: normalizeVerbaleNotes(null)
    };
  }
}

function mapChecklistToForm(detail) {
  if (!detail || typeof detail !== 'object') {
    return {
      form: { ...checklistEmptyForm },
      status: 'NON_INIZIATA',
      draftSaved: false,
      outcome: '',
      isEditable: true
    };
  }
  const payload = detail.checklist ?? detail;
  const parsedInternalNotes = extractInternalNotesEnvelope(payload.internalNotes);
  const legacyVerbaleNotes = parsedInternalNotes.verbaleNotes;
  const legacyCardNotes = parsedInternalNotes.cardNotes;
  const sharedCausaleId = payload.codiceCausaleId ?? payload.causaleId ?? null;
  const causaleCarta = payload.codiceCausaleIdCarta ?? sharedCausaleId;
  const causaleVerbale = payload.codiceCausaleIdVerbale ?? sharedCausaleId;
  const form = {
    documentPresent:       normalizeYesNo(payload.documentPresent ?? payload.cardPresent),
    legibility:            normalizeYesNo(payload.legibility ?? payload.readabilityOk),
    formalSuitability:     normalizeYesNo(payload.formalSuitability ?? payload.formalOk),
    clientDataConsistency: normalizeYesNo(payload.clientDataConsistency ?? payload.customerDataOk),
    cardNumberCheckEnabled: Boolean(payload.cardNumberCheckEnabled ?? payload.cardNumberMatchRequired),
    cardNumberMatch:       normalizeYesNo(payload.cardNumberMatch ?? payload.cardNumberMatchOk),
    formalKoReasons:       normalizeKoReasonList(payload.formalKoReasons ?? payload.koReasons),
    internalNotes:         pickFirstString(parsedInternalNotes.cardKoNote, parsedInternalNotes.legacy),
    finalNotePractice:     pickFirstString(
      payload.finalNotePractice,
      parsedInternalNotes.finalNotePractice,
      parsedInternalNotes.legacy
    ),
    cardNotes: normalizeCardNotes({
      legibility: pickFirstString(
        payload.noteLegibility,
        payload.cardNotes?.legibility,
        legacyCardNotes.legibility
      ),
      formalSuitability: pickFirstString(
        payload.noteFormalSuitability,
        payload.cardNotes?.formalSuitability,
        legacyCardNotes.formalSuitability
      )
    }),
    verbaleNotes: normalizeVerbaleNotes({
      legibility: pickFirstString(
        payload.noteLegibility,
        payload.verbaleNotes?.legibility,
        legacyVerbaleNotes.legibility
      ),
      formalSuitability: pickFirstString(
        payload.noteFormalSuitability,
        payload.verbaleNotes?.formalSuitability,
        legacyVerbaleNotes.formalSuitability
      ),
      clientDataConsistency: pickFirstString(
        payload.noteClientDataConsistency,
        payload.verbaleNotes?.clientDataConsistency,
        legacyVerbaleNotes.clientDataConsistency
      ),
      cardNumberMatch: pickFirstString(
        payload.noteCardNumberMatch,
        payload.verbaleNotes?.cardNumberMatch,
        legacyVerbaleNotes.cardNumberMatch
      )
    }),
    codiceCausaleIdCarta: causaleCarta !== null && causaleCarta !== undefined ? String(causaleCarta) : null,
    codiceCausaleIdVerbale: causaleVerbale !== null && causaleVerbale !== undefined ? String(causaleVerbale) : null
  };
  return {
    form,
    status:    payload.status ?? payload.stato ?? 'NON_INIZIATA',
    draftSaved: Boolean(payload.draftSaved ?? false),
    outcome:   normalizeOutcome(payload.outcome ?? payload.esito),
    isEditable: payload.isEditable !== false
  };
}

function buildCardInternalNotesPayload(form) {
  const cardNotes = normalizeCardNotes(form?.cardNotes);
  const hasCardNotes = cardNoteKeys.some((key) => String(cardNotes[key] ?? '').trim() !== '');
  const cardKoNote = String(form?.internalNotes ?? '').trim();

  if (!hasCardNotes) {
    return cardKoNote;
  }

  return JSON.stringify({
    legacy: cardKoNote,
    cardKoNote,
    cardNotes
  });
}

function buildChecklistSavePayload(form, documentType) {
  if (documentType === 'CARTA') {
    const hasAutoKo = form.documentPresent === 'NO';
    const legibilityOk = toBooleanOrNull(form.legibility);
    const formalOk = toBooleanOrNull(form.formalSuitability);
    const cardConformityOk = hasAutoKo
      ? null
      : (legibilityOk === false || formalOk === false)
        ? false
        : (legibilityOk === true && formalOk === true)
          ? true
          : null;

    return {
      // Bug2 fix: backend si aspetta cardPresent (non documentPresent) per CARTA
      cardPresent:           hasAutoKo ? false : true,
      cardConformityOk,
      koReasons:             [],
      internalNotes:         buildCardInternalNotesPayload(form),
      // Sprint 13: causale KO opzionale CARTA
      codiceCausaleId:       form.codiceCausaleIdCarta ? Number(form.codiceCausaleIdCarta) : null
    };
  }
  const hasAutoKo = form.documentPresent === 'NO';
  return {
    documentPresent:       toBooleanOrNull(form.documentPresent),
    readabilityOk:         hasAutoKo ? false : toBooleanOrNull(form.legibility),
    formalOk:              hasAutoKo ? false : toBooleanOrNull(form.formalSuitability),
    customerDataOk:        hasAutoKo ? false : toBooleanOrNull(form.clientDataConsistency),
    cardNumberMatchRequired: Boolean(form.cardNumberCheckEnabled),
    cardNumberMatchOk:     form.cardNumberCheckEnabled
      ? (hasAutoKo ? false : toBooleanOrNull(form.cardNumberMatch))
      : null,
    koReasons:    form.formalSuitability === 'NO' ? form.formalKoReasons : [],
    noteLegibility: String(form?.verbaleNotes?.legibility ?? '').trim(),
    noteFormalSuitability: String(form?.verbaleNotes?.formalSuitability ?? '').trim(),
    noteClientDataConsistency: String(form?.verbaleNotes?.clientDataConsistency ?? '').trim(),
    noteCardNumberMatch: String(form?.verbaleNotes?.cardNumberMatch ?? '').trim(),
    finalNotePractice: String(form.finalNotePractice ?? '').trim(),
    internalNotes: String(form.internalNotes ?? '').trim(),
    // Sprint 13: causale KO opzionale VERBALE
    codiceCausaleId: form.codiceCausaleIdVerbale ? Number(form.codiceCausaleIdVerbale) : null
  };
}

function getChecklistValidationError(form, documentType) {
  if (documentType === 'CARTA') {
    if (!form.legibility || !form.formalSuitability) {
      return 'Compilare tutte le verifiche obbligatorie della checklist CARTA.';
    }
    return '';
  }
  if (!form.documentPresent) return 'Indicare se il documento è presente.';
  if (form.documentPresent === 'NO') return '';
  if (!form.legibility || !form.formalSuitability || !form.clientDataConsistency) {
    return 'Completare i controlli obbligatori della checklist.';
  }
  if (form.cardNumberCheckEnabled && !form.cardNumberMatch) {
    return 'Compilare la corrispondenza numero carta oppure disattivare il controllo facoltativo.';
  }
  if (form.formalSuitability === 'NO' && form.formalKoReasons.length === 0) {
    return 'Se idoneità formale è NO, selezionare almeno una causale KO formale.';
  }
  return '';
}

function extractConfirmedType(detail) {
  return normalizeDocumentType(
    detail?.header?.documentType
      ?? detail?.header?.typedDocumentType
      ?? detail?.intake?.documentType
      ?? detail?.documentType
  );
}

function extractPracticeState(detail) {
  return String(detail?.header?.state ?? detail?.state ?? '').toUpperCase();
}

function deriveFase(detail) {
  // Tenta prima il campo diretto
  const fase = detail?.fase;
  if (fase) return String(fase).toUpperCase();
  // Derivazione dallo stato pratica
  const state = extractPracticeState(detail);
  if (state.includes('CHIUSA')) return 'CHIUSURA_PRATICA';
  if (state.includes('LAVORAZIONE') || state.includes('ATTESA_CONFERMA')) return 'LAVORAZIONE';
  return 'RACCOLTA_INPUT';
}

// ─── Componente ─────────────────────────────────────────────────────────────

export function TaskLavorazionePage() {
  const { taskId } = useParams();
  const [searchParams] = useSearchParams();
  const practiceId = searchParams.get('practiceId');
  const hasPracticeId = Boolean(practiceId);
  const { setFase: setGlobalFase } = usePhase();

  // Navigazione workflow
  const [activeSection, setActiveSection] = useState(2);
  const [backendSidebarState, setBackendSidebarState] = useState(null);

  // Dati pratica
  const [loading,        setLoading]        = useState(true);
  const [pageError,      setPageError]      = useState('');
  const [practiceNumber, setPracticeNumber] = useState('');
  const [practiceState,  setPracticeState]  = useState('');
  const [practiceDetail, setPracticeDetail] = useState(null);
  const [fase,           setFase]           = useState('LAVORAZIONE');

  // Allegati
  const [attachments,        setAttachments]        = useState([]);
  const [activeAttachmentId, setActiveAttachmentId] = useState('');

  // Tipizzazione
  const [confirmedType, setConfirmedType] = useState('');

  // Checklist
  const [checklistLoading,  setChecklistLoading]  = useState(false);
  const [checklistSaving,   setChecklistSaving]   = useState(false);
  const [checklistEditing,  setChecklistEditing]  = useState(false);
  const [practiceClosing,   setPracticeClosing]   = useState(false);
  const [checklistStatus,   setChecklistStatus]   = useState('NON_INIZIATA');
  const [checklistOutcome,  setChecklistOutcome]  = useState('');
  const [checklistEditable, setChecklistEditable] = useState(true);
  const [checklistForm,     setChecklistForm]     = useState(checklistEmptyForm);

  // Messaggi
  const [checklistError,    setChecklistError]    = useState('');
  const [checklistInfo,     setChecklistInfo]     = useState('');
  const [footerError,       setFooterError]       = useState('');

  // Sprint 13: note di lavorazione
  const [notes,        setNotes]        = useState([]);
  const [notesLoading, setNotesLoading] = useState(false);

  // Sprint 15: SLA
  const [taskSlaStatus, setTaskSlaStatus] = useState(null);
  const [notesError,   setNotesError]   = useState('');
  const [newNoteText,  setNewNoteText]  = useState('');
  const [noteSaving,   setNoteSaving]   = useState(false);

  // ─── Derived state ──────────────────────────────────────────────────────

  const isCardChecklist = confirmedType === 'CARTA';
  const isPracticeClosedForEditing = ['IN_ATTESA_CONFERMA_BPM', 'CHIUSA_OK', 'CHIUSA_KO'].includes(practiceState);
  const canSaveChecklist = !isPracticeClosedForEditing && checklistEditable;
  const esitoSD = checklistOutcome; // alias per chiarezza spec

  // Sidebar state calcolato: RIEPILOGO abilitato solo quando esitoSD è valorizzato
  const computedSidebarState = useMemo(() => {
    const riepEnabled = Boolean(esitoSD);
    const base = backendSidebarState ?? {
      currentStep: activeSection === 1 ? 'DATI_PRATICA'
        : activeSection === 3 ? 'RIEPILOGO'
        : 'VERIFICA_DOCUMENTO',
      steps: [
        { id: 'DATI_PRATICA',       label: 'Dati Pratica',       enabled: true,        completed: true },
        { id: 'VERIFICA_DOCUMENTO', label: 'Verifica Documento', enabled: true,        completed: riepEnabled },
        { id: 'RIEPILOGO',          label: 'Riepilogo',          enabled: riepEnabled, completed: false }
      ]
    };

    // Override enabled/completed in base alla logica locale (N-01/N-02)
    const steps = base.steps.map((step) => {
      if (step.id === 'RIEPILOGO') {
        return { ...step, enabled: riepEnabled };
      }
      if (step.id === 'VERIFICA_DOCUMENTO') {
        return { ...step, completed: riepEnabled };
      }
      return step;
    });

    return {
      ...base,
      currentStep: activeSection === 1 ? 'DATI_PRATICA'
        : activeSection === 3 ? 'RIEPILOGO'
        : 'VERIFICA_DOCUMENTO',
      steps
    };
  }, [backendSidebarState, esitoSD, activeSection]);

  // ─── Load checklist ─────────────────────────────────────────────────────

  const loadChecklist = useCallback(async () => {
    if (!practiceId || !confirmedType) return null;

    setChecklistLoading(true);
    setChecklistError('');
    try {
      const response = await intakeApi.getChecklist(practiceId);
      const mapped = mapChecklistToForm(response ?? {});

      if (confirmedType === 'CARTA' && !mapped.form.documentPresent) {
        mapped.form.documentPresent = 'SI';
      }

      setChecklistForm(mapped.form);
      setChecklistStatus(mapped.status);
      setChecklistOutcome(mapped.outcome);
      setChecklistEditable(mapped.isEditable);
      setChecklistInfo('');

      return mapped.outcome;
    } catch (err) {
      setChecklistError(err?.message ?? 'Errore nel caricamento della checklist.');
      return null;
    } finally {
      setChecklistLoading(false);
    }
  }, [practiceId, confirmedType]);

  // Sprint 13: caricamento note di lavorazione
  const loadNotes = useCallback(async () => {
    if (!practiceId) return;
    setNotesLoading(true);
    setNotesError('');
    try {
      const response = await intakeApi.getNotes(practiceId);
      setNotes(Array.isArray(response) ? response : []);
    } catch (err) {
      setNotesError(err?.message ?? 'Errore nel caricamento delle note.');
    } finally {
      setNotesLoading(false);
    }
  }, [practiceId]);

  // ─── Load page ──────────────────────────────────────────────────────────

  const loadPage = useCallback(async () => {
    if (!practiceId) {
      setPageError('Pratica non selezionata. Aprire la schermata dalla Lista Attività.');
      setLoading(false);
      return;
    }

    setLoading(true);
    setPageError('');

    try {
      // Caricamento parallelo: task details + pratica + allegati + checklist (per documentType)
      const [taskResponse, detailResponse, attachmentsResponse, checklistResponse] = await Promise.all([
        taskId ? tasksApi.getById(taskId).catch(() => null) : Promise.resolve(null),
        practicesApi.detail(practiceId),
        attachmentsApi.listByPractice(practiceId),
        intakeApi.getChecklist(practiceId).catch(() => null)
      ]);

      // Sidebar state dal backend (opzionale: se non disponibile usiamo il default)
      if (taskResponse?.sidebarState) {
        setBackendSidebarState(taskResponse.sidebarState);
      }

      // Sprint 15: SLA status
      setTaskSlaStatus(taskResponse?.slaStatus ?? null);

      // Bug1 fix: documentType viene dalla checklist response, non dalla practice detail
      const confirmedFromChecklist = normalizeDocumentType(
        checklistResponse?.documentType ?? checklistResponse?.checklist?.documentType
      );
      // documentType anche dal task response (campo aggiunto Sprint 12 fix)
      const confirmedFromTask = normalizeDocumentType(taskResponse?.documentType);
      const confirmed = extractConfirmedType(detailResponse) || confirmedFromTask || confirmedFromChecklist;
      const list = Array.isArray(attachmentsResponse)
        ? attachmentsResponse
        : (attachmentsResponse?.items ?? []);
      const first = list[0];

      const derivedFase = deriveFase(detailResponse);
      setPracticeNumber(detailResponse?.header?.practiceNumber ?? practiceId);
      setPracticeState(extractPracticeState(detailResponse));
      setPracticeDetail(detailResponse);
      setFase(derivedFase);
      setGlobalFase(derivedFase);
      setConfirmedType(confirmed);
      setAttachments(list);
      setActiveAttachmentId(String(first?.attachmentId ?? first?.id ?? ''));
    } catch (err) {
      setPageError(err?.message ?? 'Errore tecnico durante il caricamento.');
      setPracticeDetail(null);
    } finally {
      setLoading(false);
    }
  }, [practiceId, taskId]);

  useEffect(() => { loadPage(); }, [loadPage]);
  useEffect(() => { loadChecklist(); }, [loadChecklist]);

  // Reset fase globale all'uscita dalla pagina
  useEffect(() => () => { setGlobalFase(null); }, [setGlobalFase]);

  // Polling automatico quando pratica in attesa conferma BPM
  useEffect(() => {
    if (practiceState !== 'IN_ATTESA_CONFERMA_BPM') return undefined;
    const interval = setInterval(() => { loadPage(); }, 5000);
    return () => clearInterval(interval);
  }, [practiceState, loadPage]);

  // Sprint 13: carica note quando si arriva al riepilogo
  useEffect(() => {
    if (activeSection === 3) { loadNotes(); }
  }, [activeSection, loadNotes]);

  // ─── Handlers checklist ─────────────────────────────────────────────────

  const onChecklistChange = (field, value) => {
    setChecklistError('');
    setChecklistInfo('');
    setChecklistForm((current) => {
      const next = { ...current, [field]: value };

      if (field === 'verbaleNotes') {
        next.verbaleNotes = normalizeVerbaleNotes(value);
      }

      if (field === 'cardNotes') {
        next.cardNotes = normalizeCardNotes(value);
      }

      if (confirmedType === 'CARTA' && (field === 'legibility' || field === 'formalSuitability')) {
        next.documentPresent = 'SI';
      }

      if (field === 'documentPresent' && value === 'NO') {
        next.formalSuitability = 'NO';
        if (confirmedType === 'VERBALE') {
          next.legibility = 'NO';
          next.clientDataConsistency = 'NO';
          if (next.cardNumberCheckEnabled) next.cardNumberMatch = 'NO';
        }
      }
      if (field === 'documentPresent' && value === 'SI' && isCardChecklist && next.formalSuitability === 'NO') {
        next.formalSuitability = '';
      }
      if (field === 'cardNumberCheckEnabled' && !value) {
        next.cardNumberMatch = '';
      }
      if (field === 'formalSuitability' && value !== 'NO') {
        next.formalKoReasons = [];
      }
      return next;
    });
  };

  const onToggleKoReason = (reason) => {
    setChecklistError('');
    setChecklistForm((current) => {
      const already = current.formalKoReasons.includes(reason);
      return {
        ...current,
        formalKoReasons: already
          ? current.formalKoReasons.filter((r) => r !== reason)
          : [...current.formalKoReasons, reason]
      };
    });
  };

  // ─── Footer actions (S12-FE-5) ──────────────────────────────────────────

  // N-03/AC-S6-05: "SALVA E PROSEGUI" — visibile su section=2, disabled se esitoSD valorizzato
  const onSalvaEProsegui = async () => {
    setChecklistError('');
    setFooterError('');

    const validationError = getChecklistValidationError(checklistForm, confirmedType);
    if (validationError) {
      setChecklistError(validationError);
      return;
    }

    setChecklistSaving(true);
    try {
      await intakeApi.saveChecklist(
        practiceId,
        buildChecklistSavePayload(checklistForm, confirmedType)
      );
      const latestOutcome = await loadChecklist();
      setChecklistInfo('Checklist salvata.');
      if (latestOutcome) {
        setActiveSection(3);
      }
    } catch (err) {
      setChecklistError(err?.message ?? 'Errore tecnico durante il salvataggio checklist.');
    } finally {
      setChecklistSaving(false);
    }
  };

  // AC-S6-06: "CHIUDI PRATICA" — visibile solo su section=3
  const onChiudiPratica = async () => {
    setFooterError('');
    setPracticeClosing(true);
    try {
      await intakeApi.closePractice(practiceId);
      setPracticeState('IN_ATTESA_CONFERMA_BPM');
      setChecklistEditable(false);
      setChecklistInfo('Pratica chiusa. In attesa conferma BPM.');
    } catch (err) {
      setFooterError(err?.message ?? 'Impossibile chiudere la pratica.');
    } finally {
      setPracticeClosing(false);
    }
  };

  // N-05: "MODIFICA" — sempre visibile; resetta esitoSD e torna a sezione 2
  const onModifica = async () => {
    setChecklistError('');
    setFooterError('');
    setChecklistEditing(true);
    try {
      await intakeApi.editChecklist(practiceId);
      await loadChecklist();
      setChecklistInfo('Checklist riaperta in modifica.');
      setActiveSection(2);
    } catch (err) {
      setFooterError(err?.message ?? 'Impossibile riaprire la checklist in modifica.');
    } finally {
      setChecklistEditing(false);
    }
  };

  // Sprint 13: aggiunta nota di lavorazione
  const onAddNote = async () => {
    const testo = newNoteText.trim();
    if (!testo || !practiceId) return;
    setNoteSaving(true);
    setNotesError('');
    try {
      await intakeApi.addNote(practiceId, testo);
      setNewNoteText('');
      await loadNotes();
    } catch (err) {
      setNotesError(err?.message ?? "Errore nell'aggiunta della nota.");
    } finally {
      setNoteSaving(false);
    }
  };

  // ─── Typing confirmed callback ───────────────────────────────────────────

  const onTypingConfirmed = (type) => {
    setConfirmedType(type);
    setChecklistError('');
    setChecklistInfo('');
    setChecklistOutcome('');
    setChecklistStatus('NON_INIZIATA');
    // Evita residui della checklist precedente durante il cambio tipo.
    setChecklistForm({ ...checklistEmptyForm });
    setActiveSection(2);
    // Avvia caricamento checklist per il tipo appena confermato.
    // Viene anche coperto dall'useEffect dipendente da confirmedType.
    setTimeout(() => { loadChecklist(); }, 0);
  };

  // ─── Render: sezione Dati Pratica (section 1) ───────────────────────────

  function renderDatiPratica() {
    const header = practiceDetail?.header ?? {};
    const client = practiceDetail?.client ?? {};

    const dataAperturaRaw =
      header.openedAt
      ?? header.openingDate
      ?? header.dataApertura
      ?? header.createdAt
      ?? practiceDetail?.openedAt
      ?? practiceDetail?.openingDate
      ?? practiceDetail?.dataApertura
      ?? practiceDetail?.createdAt;

    const dataUltimaModificaRaw =
      header.lastModifiedAt
      ?? header.dataUltimaModifica
      ?? header.updatedAt
      ?? practiceDetail?.lastModifiedAt
      ?? practiceDetail?.dataUltimaModifica
      ?? practiceDetail?.updatedAt;

    const stato =
      header.state
      ?? header.stato
      ?? practiceDetail?.state
      ?? practiceDetail?.stato
      ?? '-';

    const codiceCliente =
      client.customerCode
      ?? client.codiceCliente
      ?? header.customerCode
      ?? header.codiceCliente
      ?? practiceDetail?.customerCode
      ?? practiceDetail?.codiceCliente
      ?? '-';

    const codiceFiscale =
      client.fiscalCode
      ?? client.codiceFiscale
      ?? header.fiscalCode
      ?? header.codiceFiscale
      ?? practiceDetail?.fiscalCode
      ?? practiceDetail?.codiceFiscale
      ?? '-';

    const canale =
      header.channel
      ?? header.canale
      ?? practiceDetail?.channel
      ?? practiceDetail?.canale
      ?? '-';

    return (
      <div>
        <h3>Dati Pratica</h3>
        <div className="summary-grid">
          <article className="summary-card">
            <h4>Dati Pratica</h4>
            <dl>
              <div><dt>Data Apertura</dt><dd>{formatDateTime(dataAperturaRaw)}</dd></div>
              <div><dt>Data Ultima Modifica</dt><dd>{formatDateTime(dataUltimaModificaRaw)}</dd></div>
              <div><dt>Stato</dt><dd>{stato}</dd></div>
              <div><dt>Codice Cliente</dt><dd>{codiceCliente}</dd></div>
              <div><dt>Codice Fiscale</dt><dd>{codiceFiscale}</dd></div>
              <div><dt>Canale</dt><dd>{canale}</dd></div>
            </dl>
          </article>
        </div>
      </div>
    );
  }

  // ─── Render: sezione Riepilogo (section 3) ──────────────────────────────

  function renderRiepilogo() {
    const header = practiceDetail?.header ?? {};
    return (
      <div>
        <h3>Riepilogo</h3>
        <div className="summary-grid">
          <article className="summary-card">
            <h4>Pratica</h4>
            <dl>
              <div><dt>Numero</dt><dd>{header.practiceNumber ?? practiceId ?? '-'}</dd></div>
              <div><dt>Stato</dt><dd>{practiceState || '-'}</dd></div>
            </dl>
          </article>
          <article className={`summary-card ${esitoSD === 'APPROVATA' ? 'outcome-card-ok' : esitoSD === 'RESPINTA' ? 'outcome-card-ko' : ''}`}>
            <h4>Esito checklist</h4>
            <dl>
              <div>
                <dt>Tipo documento</dt>
                <dd>{confirmedType || '-'}</dd>
              </div>
              <div>
                <dt>Esito SD</dt>
                <dd>{esitoSD || 'Non ancora disponibile'}</dd>
              </div>
              <div>
                <dt>Stato checklist</dt>
                <dd>{checklistStatus || 'NON_INIZIATA'}</dd>
              </div>
            </dl>
          </article>
        </div>

        {practiceState === 'IN_ATTESA_CONFERMA_BPM' ? (
          <div className="info-box" style={{ marginTop: 12 }}>
            Pratica in stato transitorio IN_ATTESA_CONFERMA_BPM. In attesa ack finale BPM.
          </div>
        ) : null}
        {practiceState === 'CHIUSA_OK' || practiceState === 'CHIUSA_KO' ? (
          <div className={`status-badge final-state-badge ${practiceState === 'CHIUSA_OK' ? 'final-state-badge-ok' : 'final-state-badge-ko'}`} style={{ marginTop: 12, display: 'inline-block' }}>
            Stato finale BPM: {practiceState}
          </div>
        ) : null}

        {/* Sprint 13 D13-FE-3: Note di Lavorazione */}
        <div className="summary-card" style={{ marginTop: 16 }}>
          <h4>Note di Lavorazione</h4>
          {notesLoading ? (
            <div className="panel-note">Caricamento note...</div>
          ) : null}
          {notesError ? (
            <div className="api-error-box">{notesError}</div>
          ) : null}
          {!notesLoading && notes.length === 0 ? (
            <div className="panel-note">Nessuna nota presente.</div>
          ) : null}
          {notes.length > 0 ? (
            <ul style={{ listStyle: 'none', padding: 0, margin: '8px 0' }}>
              {notes.map((n) => (
                <li key={n.id} style={{ borderBottom: '1px solid var(--gray-200)', padding: '6px 0' }}>
                  <div style={{ fontSize: 13, color: 'var(--gray-600)' }}>
                    {n.autore} &mdash; {formatNoteDate(n.createdAt)}
                  </div>
                  <div>{n.testo}</div>
                </li>
              ))}
            </ul>
          ) : null}
          <div style={{ marginTop: 12 }}>
            <label htmlFor="riepilogo-nota-input">Aggiungi nota</label>
            <textarea
              id="riepilogo-nota-input"
              value={newNoteText}
              onChange={(e) => setNewNoteText(e.target.value)}
              placeholder="Inserisci una nota..."
              rows={3}
              maxLength={1000}
              style={{ width: '100%', marginTop: 4, boxSizing: 'border-box' }}
              disabled={noteSaving}
            />
            <button
              type="button"
              className="btn btn-primary btn-small"
              onClick={onAddNote}
              disabled={noteSaving || !newNoteText.trim()}
              style={{ marginTop: 8 }}
            >
              {noteSaving ? 'Salvataggio...' : 'Aggiungi Nota'}
            </button>
          </div>
        </div>
      </div>
    );
  }

  // ─── Render principale ───────────────────────────────────────────────────

  const isBusy = loading || checklistSaving || checklistEditing || practiceClosing;

  return (
    <div className="workflow-page">
      {/* Header */}
      <div style={{ padding: '16px 16px 0', background: '#fff', borderBottom: '1px solid var(--gray-200)' }}>
        <div className="detail-header">
          <div>
            <h2 style={{ margin: 0 }}>Task Lavorazione — {practiceNumber || practiceId || '-'}</h2>
            {taskSlaStatus === 'SCADUTO' && (
              <span className="badge-sla badge-sla-scaduto" style={{ marginTop: 4, display: 'inline-block' }}>SLA SCADUTO</span>
            )}
            {taskSlaStatus === 'IN_TEMPO' && (
              <span className="badge-sla badge-sla-in-tempo" style={{ marginTop: 4, display: 'inline-block' }}>SLA IN TEMPO</span>
            )}
          </div>
          <div className="detail-header-actions">
            <button
              type="button"
              className="btn btn-outline btn-small"
              onClick={loadPage}
              disabled={loading}
            >
              {loading ? 'Caricamento...' : 'Aggiorna'}
            </button>
            <Link className="btn btn-outline btn-small" to="/attivita">
              Indietro
            </Link>
          </div>
        </div>
        {pageError ? <div className="api-error-box" style={{ margin: '8px 0' }}>{pageError}</div> : null}
      </div>

      {/* Barra avanzamento processo */}
      {fase && (() => {
        const idx = FASI.findIndex((f) => f.id === fase);
        const pct = idx < 0 ? 0 : Math.round((idx / (FASI.length - 1)) * 100);
        return (
          <section className="progress-banner" aria-label="Avanzamento processo">
            <div className="progress-steps">
              {FASI.map((f, i) => (
                <span
                  key={f.id}
                  className={i === idx ? 'progress-step-current' : i < idx ? 'progress-step-done' : ''}
                >
                  {f.label}
                </span>
              ))}
            </div>
            <div className="progress-line" style={{ width: `${pct}%` }} />
          </section>
        );
      })()}

      {/* Layout principale: sidebar + contenuto */}
      <div className="workflow-page-layout">

        {/* Sidebar navigazione — S12-FE-1 */}
        <WorkflowSidebar
          activeSection={activeSection}
          onSectionChange={setActiveSection}
          sidebarState={computedSidebarState}
        />

        {/* Area contenuto */}
        <div className="workflow-content-area">
          {loading ? (
            <div className="panel-note">Caricamento in corso...</div>
          ) : null}

          {!loading && activeSection === 2 && !hasPracticeId ? (
            <div className="api-error-box">
              Pratica non selezionata. Aprire il task dalla Lista Attivita per includere il riferimento pratica.
            </div>
          ) : null}

          {/* Sezione 1: Dati Pratica */}
          {!loading && activeSection === 1 ? renderDatiPratica() : null}

          {/* Sezione 2: Classificazione (se tipo non ancora confermato) — S12-FE-3 */}
          {!loading && activeSection === 2 && hasPracticeId && !confirmedType ? (
            <ClassificazioneStep
              practiceId={practiceId}
              practiceDetail={practiceDetail}
              attachments={attachments}
              activeAttachmentId={activeAttachmentId}
              onAttachmentChange={setActiveAttachmentId}
              onTypingConfirmed={onTypingConfirmed}
              disabled={isBusy || isPracticeClosedForEditing}
            />
          ) : null}

          {/* Sezione 2: Verifica Documento (se tipo confermato) — S12-FE-2 */}
          {!loading && activeSection === 2 && hasPracticeId && confirmedType ? (
            <>
              {checklistError ? <div className="api-error-box">{checklistError}</div> : null}
              {checklistInfo  ? <div className="info-box">{checklistInfo}</div>         : null}
              <VerificaDocumentiStep
                practiceId={practiceId}
                practiceDetail={practiceDetail}
                confirmedType={confirmedType}
                attachments={attachments}
                activeAttachmentId={activeAttachmentId}
                onAttachmentChange={setActiveAttachmentId}
                checklistForm={checklistForm}
                onChecklistChange={onChecklistChange}
                onToggleKoReason={onToggleKoReason}
                checklistStatus={checklistStatus}
                checklistOutcome={checklistOutcome}
                checklistLoading={checklistLoading}
                checklistSaving={checklistSaving}
                canSaveChecklist={canSaveChecklist}
                isCardChecklist={isCardChecklist}
              />
            </>
          ) : null}

          {/* Sezione 3: Riepilogo */}
          {!loading && activeSection === 3 ? renderRiepilogo() : null}
        </div>
      </div>

      {/* Footer azioni — S12-FE-5 */}
      <div className="workflow-footer">
        {footerError ? <span className="form-error">{footerError}</span> : null}

        {/* N-05/AC-S6-05: "SALVA E PROSEGUI" — visible section=2, disabled se esitoSD valorizzato */}
        {activeSection === 2 && confirmedType ? (
          <button
            type="button"
            className="btn btn-primary btn-squared"
            onClick={onSalvaEProsegui}
            disabled={Boolean(esitoSD) || isBusy}
          >
            {checklistSaving ? 'SALVATAGGIO...' : 'SALVA E PROSEGUI'}
          </button>
        ) : null}

        {/* N-04/AC-S6-06: "CHIUDI PRATICA" — visible solo section=3 */}
        {activeSection === 3 ? (
          <button
            type="button"
            className="btn btn-primary btn-squared"
            onClick={onChiudiPratica}
            disabled={isBusy || isPracticeClosedForEditing}
          >
            {practiceClosing ? 'CHIUSURA IN CORSO...' : 'CHIUDI PRATICA'}
          </button>
        ) : null}

        {/* "MODIFICA" — sempre visibile */}
        <button
          type="button"
          className="btn btn-outline btn-squared"
          onClick={onModifica}
          disabled={isBusy || !confirmedType}
        >
          {checklistEditing ? 'MODIFICA IN CORSO...' : 'MODIFICA'}
        </button>
      </div>
    </div>
  );
}
