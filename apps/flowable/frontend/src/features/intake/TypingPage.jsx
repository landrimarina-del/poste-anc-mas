import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { attachmentsApi } from '../../core/api/attachmentsApi';
import { intakeApi } from '../../core/api/intakeApi';
import { practicesApi } from '../../core/api/practicesApi';

const previewSizes = {
  small: 360,
  medium: 520,
  large: 680
};

const documentTypeOptions = [
  { value: 'VERBALE', label: 'Verbale di denuncia' },
  { value: 'CARTA', label: 'Carta' }
];

const yesNoOptions = [
  { value: 'SI', label: 'Si' },
  { value: 'NO', label: 'No' }
];

const formalKoReasonOptions = [
  { value: 'INTESTAZIONE', label: 'Intestazione' },
  { value: 'FIRME', label: 'Firme' },
  { value: 'TIMBRO', label: 'Timbro' },
  { value: 'DICHIARAZIONE', label: 'Dichiarazione' },
  { value: 'CARTA_PI', label: 'Carta Poste Italiane' }
];

const checklistEmptyForm = {
  documentPresent: '',
  legibility: '',
  formalSuitability: '',
  clientDataConsistency: '',
  cardNumberCheckEnabled: false,
  cardNumberMatch: '',
  formalKoReasons: [],
  internalNotes: ''
};

function normalizeDocumentType(value) {
  if (!value || typeof value !== 'string') {
    return '';
  }

  const upper = value.toUpperCase();
  if (upper.includes('VERBALE')) {
    return 'VERBALE';
  }
  if (upper.includes('CARTA')) {
    return 'CARTA';
  }
  return '';
}

function extractItems(response) {
  if (Array.isArray(response)) {
    return response;
  }
  if (Array.isArray(response?.items)) {
    return response.items;
  }
  return [];
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

function getTypeLabel(documentType) {
  return documentTypeOptions.find((item) => item.value === documentType)?.label ?? '-';
}

function normalizeYesNo(value) {
  if (typeof value === 'boolean') {
    return value ? 'SI' : 'NO';
  }
  if (typeof value !== 'string') {
    return '';
  }

  const upper = value.toUpperCase();
  if (upper === 'SI' || upper === 'YES' || upper === 'TRUE') {
    return 'SI';
  }
  if (upper === 'NO' || upper === 'FALSE') {
    return 'NO';
  }
  return '';
}

function normalizeOutcome(value) {
  if (typeof value !== 'string') {
    return '';
  }
  const upper = value.toUpperCase();
  if (upper.includes('APPROVATA')) {
    return 'APPROVATA';
  }
  if (upper.includes('RESPINTA')) {
    return 'RESPINTA';
  }
  return '';
}

function toBooleanOrNull(value) {
  if (value === 'SI') {
    return true;
  }
  if (value === 'NO') {
    return false;
  }
  return null;
}

function normalizeKoReasonList(value) {
  if (!Array.isArray(value)) {
    return [];
  }
  const allowed = new Set(formalKoReasonOptions.map((item) => item.value));
  return value
    .map((item) => String(item ?? '').toUpperCase())
    .filter((item) => allowed.has(item));
}

function extractChecklistPayload(detail) {
  if (!detail || typeof detail !== 'object') {
    return null;
  }
  return detail.checklist ?? detail;
}

function mapChecklistToForm(detail) {
  const payload = extractChecklistPayload(detail) ?? {};
  const form = {
    documentPresent: normalizeYesNo(payload.documentPresent),
    legibility: normalizeYesNo(payload.legibility ?? payload.readabilityOk),
    formalSuitability: normalizeYesNo(payload.formalSuitability ?? payload.formalOk),
    clientDataConsistency: normalizeYesNo(payload.clientDataConsistency ?? payload.customerDataOk),
    cardNumberCheckEnabled: Boolean(payload.cardNumberCheckEnabled ?? payload.cardNumberMatchRequired),
    cardNumberMatch: normalizeYesNo(payload.cardNumberMatch ?? payload.cardNumberMatchOk),
    formalKoReasons: normalizeKoReasonList(payload.formalKoReasons ?? payload.koReasons),
    internalNotes: typeof payload.internalNotes === 'string' ? payload.internalNotes : ''
  };

  if (form.documentPresent === 'NO') {
    form.legibility = 'NO';
    form.formalSuitability = 'NO';
    form.clientDataConsistency = 'NO';
    if (form.cardNumberCheckEnabled) {
      form.cardNumberMatch = 'NO';
    }
  }

  return {
    form,
    status: payload.status ?? payload.stato ?? 'NON_INIZIATA',
    draftSaved: Boolean(payload.draftSaved ?? false),
    outcome: normalizeOutcome(payload.outcome ?? payload.esito),
    isEditable: payload.isEditable !== false,
    description: payload.description ?? payload.checklistDescription ?? payload.descrizione ?? ''
  };
}

function buildChecklistSavePayload(form, documentType) {
  if (documentType === 'CARTA') {
    const hasAutoKo = form.documentPresent === 'NO';
    return {
      documentPresent: toBooleanOrNull(form.documentPresent),
      readabilityOk: null,
      formalOk: hasAutoKo ? false : toBooleanOrNull(form.formalSuitability),
      customerDataOk: null,
      cardNumberMatchRequired: false,
      cardNumberMatchOk: null,
      koReasons: [],
      internalNotes: typeof form.internalNotes === 'string' ? form.internalNotes.trim() : ''
    };
  }

  const hasAutoKo = form.documentPresent === 'NO';
  const cardNumberMatchOk = form.cardNumberCheckEnabled
    ? (hasAutoKo ? false : toBooleanOrNull(form.cardNumberMatch))
    : null;

  return {
    documentPresent: toBooleanOrNull(form.documentPresent),
    readabilityOk: hasAutoKo ? false : toBooleanOrNull(form.legibility),
    formalOk: hasAutoKo ? false : toBooleanOrNull(form.formalSuitability),
    customerDataOk: hasAutoKo ? false : toBooleanOrNull(form.clientDataConsistency),
    cardNumberMatchRequired: Boolean(form.cardNumberCheckEnabled),
    cardNumberMatchOk,
    koReasons: form.formalSuitability === 'NO' ? form.formalKoReasons : [],
    internalNotes: typeof form.internalNotes === 'string' ? form.internalNotes.trim() : ''
  };
}

function getChecklistValidationError(form, documentType) {
  if (!form.documentPresent) {
    return 'Indicare se il documento e presente.';
  }

  if (form.documentPresent === 'NO') {
    return '';
  }

  if (documentType === 'CARTA') {
    if (!form.formalSuitability) {
      return 'Per carta presente, indicare la conformita carta.';
    }
    return '';
  }

  if (!form.legibility || !form.formalSuitability || !form.clientDataConsistency) {
    return 'Completare i controlli obbligatori della checklist.';
  }

  if (form.cardNumberCheckEnabled && !form.cardNumberMatch) {
    return 'Compilare la corrispondenza numero carta oppure disattivare il controllo facoltativo.';
  }

  if (form.formalSuitability === 'NO' && form.formalKoReasons.length === 0) {
    return 'Se idoneita formale e NO, selezionare almeno una causale KO formale.';
  }

  return '';
}

export function TypingPage() {
  const { taskId } = useParams();
  const [searchParams] = useSearchParams();
  const practiceId = searchParams.get('practiceId');

  const [loading, setLoading] = useState(true);
  const [practiceNumber, setPracticeNumber] = useState('');
  const [practiceState, setPracticeState] = useState('');
  const [attachments, setAttachments] = useState([]);
  const [activeAttachmentId, setActiveAttachmentId] = useState('');
  const [previewFailed, setPreviewFailed] = useState(false);
  // GAP-BLOCKER-001: stato dedicato per classificare la risposta del backend
  //   - ''               : allegato disponibile, iframe renderizzabile
  //   - 'not_available'  : 404 (ingestion_status != AVAILABLE)
  //   - 'technical'      : 5xx oppure body con resultCode=4004
  const [attachmentError, setAttachmentError] = useState('');
  const [previewSize, setPreviewSize] = useState('medium');
  const [previewCollapsed, setPreviewCollapsed] = useState(false);

  const [selectedType, setSelectedType] = useState('');
  const [confirmedType, setConfirmedType] = useState('');
  const [confirming, setConfirming] = useState(false);

  const [checklistLoading, setChecklistLoading] = useState(false);
  const [checklistSaving, setChecklistSaving] = useState(false);
  const [checklistEditing, setChecklistEditing] = useState(false);
  const [practiceClosing, setPracticeClosing] = useState(false);
  const [checklistStatus, setChecklistStatus] = useState('NON_INIZIATA');
  const [checklistDraftSaved, setChecklistDraftSaved] = useState(false);
  const [checklistOutcome, setChecklistOutcome] = useState('');
  const [checklistDescription, setChecklistDescription] = useState('');
  const [checklistEditable, setChecklistEditable] = useState(true);
  const [checklistForm, setChecklistForm] = useState(checklistEmptyForm);

  const [pageError, setPageError] = useState('');
  const [typingError, setTypingError] = useState('');
  const [typingInfo, setTypingInfo] = useState('');
  const [checklistError, setChecklistError] = useState('');
  const [checklistInfo, setChecklistInfo] = useState('');

  const activeAttachment = useMemo(
    () => attachments.find((item) => String(item.attachmentId ?? item.id) === String(activeAttachmentId)) ?? null,
    [attachments, activeAttachmentId]
  );

  const isLocked = Boolean(confirmedType);
  const isCardChecklist = confirmedType === 'CARTA';
  const isPracticeClosedForEditing = ['IN_ATTESA_CONFERMA_BPM', 'CHIUSA_OK', 'CHIUSA_KO'].includes(practiceState);
  const isFinalState = practiceState === 'CHIUSA_OK' || practiceState === 'CHIUSA_KO';
  const isConformityDisabled = checklistForm.documentPresent === 'NO';
  const canShowChecklist = isLocked && (confirmedType === 'VERBALE' || confirmedType === 'CARTA');
  const canSaveChecklist = !isPracticeClosedForEditing && checklistEditable;
  const canClosePractice =
    canShowChecklist
    && !isPracticeClosedForEditing
    && (checklistDraftSaved || checklistStatus !== 'NON_INIZIATA')
    && Boolean(checklistOutcome);
  const canEditNotes = !isCardChecklist && checklistOutcome === 'RESPINTA';

  const resetChecklistState = useCallback(() => {
    setChecklistForm(checklistEmptyForm);
    setChecklistStatus('NON_INIZIATA');
    setChecklistDraftSaved(false);
    setChecklistOutcome('');
    setChecklistDescription('');
    setChecklistEditable(true);
    setChecklistError('');
    setChecklistInfo('');
  }, []);

  const loadChecklist = useCallback(async () => {
    if (!practiceId || !isLocked || (confirmedType !== 'VERBALE' && confirmedType !== 'CARTA')) {
      resetChecklistState();
      return;
    }

    setChecklistLoading(true);
    setChecklistError('');
    try {
      const response = await intakeApi.getChecklist(practiceId);
      const mapped = mapChecklistToForm(response ?? {});

      setChecklistForm(mapped.form);
      setChecklistStatus(mapped.status);
      setChecklistDraftSaved(mapped.draftSaved || mapped.status === 'BOZZA');
      setChecklistOutcome(mapped.outcome);
      setChecklistDescription(typeof mapped.description === 'string' ? mapped.description : '');
      setChecklistEditable(mapped.isEditable);
      setChecklistInfo('');

      // C4.10: arricchisce la descrizione checklist con l'endpoint help dedicato
      const helpItemId = confirmedType === 'CARTA' ? 'CARDPRESENT' : 'DOCUMENTPRESENT';
      try {
        const helpResponse = await intakeApi.getChecklistHelp(practiceId, helpItemId);
        const helpText = helpResponse?.description || helpResponse?.title;
        if (helpText) {
          setChecklistDescription(helpText);
        }
      } catch {
        // fallback: mantieni la descrizione già impostata dalla risposta checklist
      }
    } catch (error) {
      const errorMessage = error?.message ?? 'Errore tecnico nel caricamento della checklist.';
      resetChecklistState();
      setChecklistError(errorMessage);
    } finally {
      setChecklistLoading(false);
    }
  }, [practiceId, isLocked, confirmedType, resetChecklistState]);

  const loadPage = useCallback(async () => {
    if (!practiceId) {
      setPageError('Pratica non selezionata. Aprire la schermata dalla Lista Attivita.');
      setLoading(false);
      return;
    }

    setLoading(true);
    setPageError('');

    try {
      const [detailResponse, attachmentsResponse] = await Promise.all([
        practicesApi.detail(practiceId),
        attachmentsApi.listByPractice(practiceId)
      ]);

      const confirmed = extractConfirmedType(detailResponse);
      const list = extractItems(attachmentsResponse);
      const firstAttachment = list[0];

      setPracticeNumber(detailResponse?.header?.practiceNumber ?? practiceId);
      setPracticeState(extractPracticeState(detailResponse));
      setConfirmedType(confirmed);
      setSelectedType((current) => current || confirmed);
      setAttachments(list);
      setActiveAttachmentId(String(firstAttachment?.attachmentId ?? firstAttachment?.id ?? ''));
      setPreviewFailed(false);
      setAttachmentError('');
    } catch (error) {
      setPracticeNumber(practiceId);
      setPracticeState('');
      setAttachments([]);
      setActiveAttachmentId('');
      setPageError(error?.message ?? 'Errore tecnico durante il caricamento della tipizzazione documento.');
    } finally {
      setLoading(false);
    }
  }, [practiceId]);

  useEffect(() => {
    loadPage();
  }, [loadPage]);

  useEffect(() => {
    loadChecklist();
  }, [loadChecklist]);

  // ISS-S6-02: polling automatico ack BPM quando la pratica è IN_ATTESA_CONFERMA_BPM
  useEffect(() => {
    if (practiceState !== 'IN_ATTESA_CONFERMA_BPM') {
      return undefined;
    }
    const pollInterval = setInterval(() => {
      loadPage();
    }, 5000);
    return () => clearInterval(pollInterval);
  }, [practiceState, loadPage]);

  const onConfirmTyping = async () => {
    setTypingError('');
    setTypingInfo('');

    if (!selectedType) {
      setTypingError('Selezionare il tipo documento prima di confermare.');
      return;
    }

    if (isLocked) {
      if (selectedType === confirmedType) {
        setTypingInfo('Tipizzazione gia confermata in precedenza: nessuna variazione applicata.');
      } else {
        setTypingError('Tipo documento gia confermato in modo irreversibile per questa pratica.');
      }
      return;
    }

    setConfirming(true);
    try {
      await intakeApi.confirmTyping(practiceId, selectedType);
      setConfirmedType(selectedType);
      setTypingInfo('Tipizzazione confermata. Milestone successive abilitate (placeholder Sprint 4).');
      if (selectedType !== 'VERBALE' && selectedType !== 'CARTA') {
        resetChecklistState();
      }
    } catch (error) {
      setTypingError(error?.message ?? 'Impossibile confermare la tipizzazione.');
    } finally {
      setConfirming(false);
    }
  };

  const onChecklistFieldChange = (field, value) => {
    setChecklistError('');
    setChecklistInfo('');
    setChecklistForm((current) => {
      const next = { ...current, [field]: value };

      if (field === 'documentPresent' && value === 'NO') {
        next.formalSuitability = 'NO';
        if (confirmedType === 'VERBALE') {
          next.legibility = 'NO';
          next.clientDataConsistency = 'NO';
          if (next.cardNumberCheckEnabled) {
            next.cardNumberMatch = 'NO';
          }
        }
      }

      if (field === 'documentPresent' && value === 'SI' && confirmedType === 'CARTA' && next.formalSuitability === 'NO') {
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
    setChecklistInfo('');
    setChecklistForm((current) => {
      const alreadySelected = current.formalKoReasons.includes(reason);
      return {
        ...current,
        formalKoReasons: alreadySelected
          ? current.formalKoReasons.filter((item) => item !== reason)
          : [...current.formalKoReasons, reason]
      };
    });
  };

  const onSaveChecklist = async () => {
    setChecklistError('');
    setChecklistInfo('');

    const validationError = getChecklistValidationError(checklistForm, confirmedType);
    if (validationError) {
      setChecklistError(validationError);
      return;
    }

    setChecklistSaving(true);
    try {
      await intakeApi.saveChecklist(practiceId, buildChecklistSavePayload(checklistForm, confirmedType));
      setChecklistInfo('Checklist salvata in bozza. Riepilogo esito aggiornato.');
      await loadChecklist();
    } catch (error) {
      setChecklistError(error?.message ?? 'Errore tecnico durante il salvataggio checklist.');
    } finally {
      setChecklistSaving(false);
    }
  };

  const onEditChecklist = async () => {
    setChecklistError('');
    setChecklistInfo('');
    setChecklistEditing(true);

    try {
      await intakeApi.editChecklist(practiceId);
      setChecklistInfo('Checklist riaperta in modifica.');
      await loadChecklist();
    } catch (error) {
      setChecklistError(error?.message ?? 'Impossibile riaprire la checklist in modifica.');
    } finally {
      setChecklistEditing(false);
    }
  };

  const onClosePractice = async () => {
    setChecklistError('');
    setChecklistInfo('');
    setPracticeClosing(true);

    try {
      await intakeApi.closePractice(practiceId);
      setPracticeState('IN_ATTESA_CONFERMA_BPM');
      setChecklistEditable(false);
      setChecklistInfo('Pratica chiusa con esito checklist: stato aggiornato a IN_ATTESA_CONFERMA_BPM.');
    } catch (error) {
      setChecklistError(error?.message ?? 'Impossibile chiudere la pratica.');
    } finally {
      setPracticeClosing(false);
    }
  };

  const previewUrl = activeAttachment ? attachmentsApi.previewUrl(activeAttachment.attachmentId ?? activeAttachment.id) : '';
  const downloadUrl = activeAttachment ? attachmentsApi.downloadUrl(activeAttachment.attachmentId ?? activeAttachment.id) : '';

  // GAP-BLOCKER-001: il backend ora serve sempre i bytes da MinIO e risponde 404
  // (ApiResponse.error) quando ingestion_status != AVAILABLE. Verifichiamo lo stato
  // dell'endpoint prima di montare l'iframe per poter mostrare un messaggio coerente.
  useEffect(() => {
    if (!previewUrl) {
      setAttachmentError('');
      return undefined;
    }

    let cancelled = false;
    const controller = new AbortController();

    (async () => {
      try {
        const response = await fetch(previewUrl, {
          method: 'GET',
          credentials: 'include',
          headers: { Accept: 'application/pdf, application/json, */*' },
          signal: controller.signal
        });

        if (cancelled) {
          return;
        }

        if (response.ok) {
          setAttachmentError('');
          setPreviewFailed(false);
          return;
        }

        if (response.status === 404) {
          // Tenta lettura body per distinguere resultCode tecnico (4004) da
          // semplice "non disponibile".
          let resultCode = null;
          try {
            const contentType = response.headers.get('content-type') ?? '';
            if (contentType.includes('application/json')) {
              const payload = await response.json();
              resultCode = payload?.resultCode ?? null;
            }
          } catch (_parseError) {
            // ignoriamo, classificheremo come not_available
          }
          if (cancelled) {
            return;
          }
          setAttachmentError(resultCode === 4004 ? 'technical' : 'not_available');
          return;
        }

        if (response.status >= 500) {
          setAttachmentError('technical');
          return;
        }

        // Altri status non attesi: trattiamo come errore tecnico.
        setAttachmentError('technical');
      } catch (error) {
        if (cancelled || error?.name === 'AbortError') {
          return;
        }
        // In ambienti di test fetch puo non essere mockato: in tal caso non
        // alteriamo lo stato e lasciamo che l'iframe tenti comunque il rendering.
      }
    })();

    return () => {
      cancelled = true;
      controller.abort();
    };
  }, [previewUrl]);

  const viewerHeight = previewSizes[previewSize] ?? previewSizes.medium;

  return (
    <section className="panel">
      <div className="detail-header">
        <div>
          <h2>Tipizzazione documento</h2>
          <p className="panel-note">Task accettato - pratica {practiceNumber || practiceId || '-'}.</p>
        </div>

        <div className="detail-header-actions">
          <button type="button" className="btn btn-outline btn-small" onClick={loadPage} disabled={loading || confirming}>
            {loading ? 'Caricamento...' : 'Aggiorna'}
          </button>
          <Link className="btn btn-outline btn-small" to="/attivita">
            Indietro
          </Link>
        </div>
      </div>

      {pageError ? <div className="api-error-box">{pageError}</div> : null}

      <div className="typing-layout">
        <aside className="typing-left">
          <h3>Raccolta input</h3>

          <div className="typing-form-block">
            <label htmlFor="documentType">Tipo documento</label>
            {isLocked ? (
              <div id="documentType" className="readonly-field" aria-live="polite">
                {getTypeLabel(confirmedType)} (confermato)
              </div>
            ) : (
              <select
                id="documentType"
                value={selectedType}
                onChange={(event) => setSelectedType(normalizeDocumentType(event.target.value))}
                disabled={loading || confirming || isPracticeClosedForEditing}
              >
                <option value="">Seleziona...</option>
                {documentTypeOptions.map((item) => (
                  <option key={item.value} value={item.value}>
                    {item.label}
                  </option>
                ))}
              </select>
            )}

            <button
              type="button"
              className="btn btn-primary"
              onClick={onConfirmTyping}
              disabled={loading || confirming || !practiceId || isPracticeClosedForEditing}
            >
              {confirming ? 'CONFERMA IN CORSO...' : 'CONFERMA'}
            </button>
          </div>

          {typingError ? <div className="api-error-box">{typingError}</div> : null}
          {typingInfo ? <div className="info-box">{typingInfo}</div> : null}

          {practiceState === 'IN_ATTESA_CONFERMA_BPM' ? (
            <div className="info-box">Pratica in stato transitorio IN_ATTESA_CONFERMA_BPM. In attesa ack finale BPM.</div>
          ) : null}

          {isFinalState ? (
            <div className={`status-badge final-state-badge ${practiceState === 'CHIUSA_OK' ? 'final-state-badge-ok' : 'final-state-badge-ko'}`}>
              Stato finale BPM: {practiceState}
            </div>
          ) : null}

          {(previewFailed || pageError) ? (
            <div className="info-box" role="status">
              Viewer non disponibile: usare Download e proseguire con la tipizzazione.
            </div>
          ) : null}

          <div className="milestone-placeholder">
            <h4>Milestone Verifica Documento</h4>
            <p>
              {!isLocked
                ? 'Non abilitata: confermare prima la tipizzazione.'
                : confirmedType === 'VERBALE'
                  ? 'Compilare la checklist e salvare in bozza per aggiornare il riepilogo esito.'
                  : confirmedType === 'CARTA'
                    ? 'Compilare la checklist carta minima e salvare per ottenere il riepilogo esito.'
                    : 'Checklist non disponibile: tipizzazione corrente non compatibile.'}
            </p>
          </div>

          {canShowChecklist ? (
            <div className="checklist-form-section" aria-label={isCardChecklist ? 'Checklist carta' : 'Checklist verbale'}>
              <h4>{isCardChecklist ? 'Checklist Carta' : 'Checklist Verbale'}</h4>
              <div className="panel-note">
                {checklistDescription
                  || (isCardChecklist
                    ? 'Verifica presenza e conformita minima carta secondo regole ANC.'
                    : 'Verifica completezza, leggibilita e coerenza documento per la pratica ANC.')}
              </div>

              {checklistLoading ? <div className="panel-note">Caricamento checklist...</div> : null}

              {checklistError ? <div className="api-error-box">{checklistError}</div> : null}
              {checklistInfo ? <div className="info-box">{checklistInfo}</div> : null}

              <div className="checklist-meta-row">
                <span className="status-badge">Stato: {checklistStatus || 'NON_INIZIATA'}</span>
                {checklistDraftSaved ? <span className="status-badge status-badge-draft">Bozza salvata</span> : null}
              </div>

              <div className="checklist-fields-grid">
                {isCardChecklist ? (
                  <>
                    <label htmlFor="documentPresent">
                      Presenza carta
                      <select
                        id="documentPresent"
                        value={checklistForm.documentPresent}
                        onChange={(event) => onChecklistFieldChange('documentPresent', event.target.value)}
                        disabled={checklistLoading || checklistSaving || checklistEditing || practiceClosing || !canSaveChecklist}
                      >
                        <option value="">Seleziona...</option>
                        {yesNoOptions.map((item) => (
                          <option key={item.value} value={item.value}>
                            {item.label}
                          </option>
                        ))}
                      </select>
                    </label>

                    <label htmlFor="formalSuitability">
                      Conformita carta
                      <select
                        id="formalSuitability"
                        value={checklistForm.formalSuitability}
                        onChange={(event) => onChecklistFieldChange('formalSuitability', event.target.value)}
                        disabled={isConformityDisabled || checklistLoading || checklistSaving || checklistEditing || practiceClosing || !canSaveChecklist}
                      >
                        <option value="">Seleziona...</option>
                        {yesNoOptions.map((item) => (
                          <option key={item.value} value={item.value}>
                            {item.label}
                          </option>
                        ))}
                      </select>
                    </label>
                  </>
                ) : (
                  <>
                    <label htmlFor="documentPresent">
                      Documento presente?
                      <select
                        id="documentPresent"
                        value={checklistForm.documentPresent}
                        onChange={(event) => onChecklistFieldChange('documentPresent', event.target.value)}
                        disabled={checklistLoading || checklistSaving || checklistEditing || practiceClosing || !canSaveChecklist}
                      >
                        <option value="">Seleziona...</option>
                        {yesNoOptions.map((item) => (
                          <option key={item.value} value={item.value}>
                            {item.label}
                          </option>
                        ))}
                      </select>
                    </label>

                    <label htmlFor="legibility">
                      Leggibilita
                      <select
                        id="legibility"
                        value={checklistForm.legibility}
                        onChange={(event) => onChecklistFieldChange('legibility', event.target.value)}
                        disabled={isConformityDisabled || checklistLoading || checklistSaving || checklistEditing || practiceClosing || !canSaveChecklist}
                      >
                        <option value="">Seleziona...</option>
                        {yesNoOptions.map((item) => (
                          <option key={item.value} value={item.value}>
                            {item.label}
                          </option>
                        ))}
                      </select>
                    </label>

                    <label htmlFor="formalSuitability">
                      Idoneita formale
                      <select
                        id="formalSuitability"
                        value={checklistForm.formalSuitability}
                        onChange={(event) => onChecklistFieldChange('formalSuitability', event.target.value)}
                        disabled={isConformityDisabled || checklistLoading || checklistSaving || checklistEditing || practiceClosing || !canSaveChecklist}
                      >
                        <option value="">Seleziona...</option>
                        {yesNoOptions.map((item) => (
                          <option key={item.value} value={item.value}>
                            {item.label}
                          </option>
                        ))}
                      </select>
                    </label>

                    <label htmlFor="clientDataConsistency">
                      Coerenza dati cliente
                      <select
                        id="clientDataConsistency"
                        value={checklistForm.clientDataConsistency}
                        onChange={(event) => onChecklistFieldChange('clientDataConsistency', event.target.value)}
                        disabled={isConformityDisabled || checklistLoading || checklistSaving || checklistEditing || practiceClosing || !canSaveChecklist}
                      >
                        <option value="">Seleziona...</option>
                        {yesNoOptions.map((item) => (
                          <option key={item.value} value={item.value}>
                            {item.label}
                          </option>
                        ))}
                      </select>
                    </label>
                  </>
                )}
              </div>

              {isConformityDisabled ? (
                <div className="info-box">
                  {isCardChecklist
                    ? 'Carta assente: esito KO automatico applicato e conformita carta disabilitata.'
                    : 'Documento assente: KO automatico applicato e controlli conformita disabilitati.'}
                </div>
              ) : null}

              {!isCardChecklist ? (
                <label className="checklist-toggle" htmlFor="cardNumberCheckEnabled">
                  <input
                    id="cardNumberCheckEnabled"
                    type="checkbox"
                    checked={checklistForm.cardNumberCheckEnabled}
                    onChange={(event) => onChecklistFieldChange('cardNumberCheckEnabled', event.target.checked)}
                    disabled={checklistLoading || checklistSaving || checklistEditing || practiceClosing || !canSaveChecklist}
                  />
                  Corrispondenza numero carta (facoltativa)
                </label>
              ) : null}

              {!isCardChecklist && checklistForm.cardNumberCheckEnabled ? (
                <label htmlFor="cardNumberMatch">
                  Corrispondenza numero carta
                  <select
                    id="cardNumberMatch"
                    value={checklistForm.cardNumberMatch}
                    onChange={(event) => onChecklistFieldChange('cardNumberMatch', event.target.value)}
                    disabled={isConformityDisabled || checklistLoading || checklistSaving || checklistEditing || practiceClosing || !canSaveChecklist}
                  >
                    <option value="">Seleziona...</option>
                    {yesNoOptions.map((item) => (
                      <option key={item.value} value={item.value}>
                        {item.label}
                      </option>
                    ))}
                  </select>
                </label>
              ) : null}

              {!isCardChecklist && checklistForm.formalSuitability === 'NO' ? (
                <div className="checklist-ko-group" role="group" aria-label="Causali KO formali">
                  <h5>Causali KO formali obbligatorie</h5>
                  <div className="checklist-ko-options">
                    {formalKoReasonOptions.map((item) => (
                      <label key={item.value} className="checklist-checkbox-inline">
                        <input
                          type="checkbox"
                          checked={checklistForm.formalKoReasons.includes(item.value)}
                          onChange={() => onToggleKoReason(item.value)}
                          disabled={checklistLoading || checklistSaving || checklistEditing || practiceClosing || !canSaveChecklist}
                        />
                        {item.label}
                      </label>
                    ))}
                  </div>
                </div>
              ) : null}

              <div className="checklist-actions-row">
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={onSaveChecklist}
                  disabled={checklistLoading || checklistSaving || checklistEditing || practiceClosing || !canSaveChecklist}
                >
                  {checklistSaving ? 'SALVATAGGIO...' : 'SALVA E PROSEGUI'}
                </button>
                <button
                  type="button"
                  className="btn btn-outline"
                  onClick={onEditChecklist}
                  disabled={checklistLoading || checklistSaving || checklistEditing || practiceClosing || isPracticeClosedForEditing}
                >
                  {checklistEditing ? 'MODIFICA IN CORSO...' : 'MODIFICA'}
                </button>
                {canClosePractice ? (
                  <button
                    type="button"
                    className="btn btn-primary"
                    onClick={onClosePractice}
                    disabled={checklistLoading || checklistSaving || checklistEditing || practiceClosing}
                  >
                    {practiceClosing ? 'CHIUSURA IN CORSO...' : 'CHIUDI PRATICA'}
                  </button>
                ) : null}
              </div>

              <div
                className={`outcome-card ${checklistOutcome === 'APPROVATA' ? 'outcome-card-ok' : checklistOutcome === 'RESPINTA' ? 'outcome-card-ko' : ''}`}
                aria-live="polite"
              >
                <h5>Riepilogo esito</h5>
                <p>
                  {checklistOutcome
                    ? checklistOutcome
                    : 'Esito non ancora disponibile. Salvare la checklist per il calcolo automatico.'}
                </p>
              </div>

              {canEditNotes ? (
                <label htmlFor="internalNotes">
                  Note interne
                  <textarea
                    id="internalNotes"
                    value={checklistForm.internalNotes}
                    onChange={(event) => onChecklistFieldChange('internalNotes', event.target.value)}
                    rows={4}
                    disabled={checklistLoading || checklistSaving || checklistEditing || practiceClosing || !canSaveChecklist}
                  />
                </label>
              ) : null}
            </div>
          ) : null}
        </aside>

        <section className="typing-right" aria-label="Viewer allegato">
          <div className="viewer-toolbar">
            <span>Dimensione preview</span>
            <div className="viewer-size-actions" role="group" aria-label="Controlli dimensione viewer">
              <button
                type="button"
                className={`btn btn-outline btn-small ${previewSize === 'small' ? 'active-size' : ''}`}
                onClick={() => setPreviewSize('small')}
                disabled={loading}
              >
                Piccolo
              </button>
              <button
                type="button"
                className={`btn btn-outline btn-small ${previewSize === 'medium' ? 'active-size' : ''}`}
                onClick={() => setPreviewSize('medium')}
                disabled={loading}
              >
                Medio
              </button>
              <button
                type="button"
                className={`btn btn-outline btn-small ${previewSize === 'large' ? 'active-size' : ''}`}
                onClick={() => setPreviewSize('large')}
                disabled={loading}
              >
                Grande
              </button>
              <button
                type="button"
                className="btn btn-outline btn-small"
                onClick={() => setPreviewCollapsed((prev) => !prev)}
                disabled={loading}
              >
                {previewCollapsed ? 'Espandi preview' : 'Comprimi preview'}
              </button>
            </div>
          </div>

          {attachments.length > 0 ? (
            <div className="viewer-attachment-select">
              <label htmlFor="attachmentSelector">Allegato</label>
              <select
                id="attachmentSelector"
                value={activeAttachmentId}
                onChange={(event) => {
                  setActiveAttachmentId(event.target.value);
                  setPreviewFailed(false);
                  setAttachmentError('');
                }}
                disabled={loading}
              >
                {attachments.map((item) => {
                  const value = String(item.attachmentId ?? item.id);
                  return (
                    <option key={value} value={value}>
                      {item.fileName ?? item.name ?? `Allegato ${value}`}
                    </option>
                  );
                })}
              </select>
            </div>
          ) : (
            <div className="panel-note">Nessun allegato disponibile sulla pratica.</div>
          )}

          {downloadUrl && attachmentError !== 'not_available' ? (
            <a className="btn btn-outline" href={downloadUrl} target="_blank" rel="noreferrer">
              Download
            </a>
          ) : null}

          {previewCollapsed ? (
            <div className="panel-note">Preview allegato compressa. Usare "Espandi preview" per visualizzarla.</div>
          ) : (
            <div className="viewer-frame-shell" style={{ height: `${viewerHeight}px` }}>
              {attachmentError === 'not_available' ? (
                <div className="api-error-box">Allegato non disponibile</div>
              ) : attachmentError === 'technical' ? (
                <div className="api-error-box">Errore tecnico nel recupero dell&apos;allegato.</div>
              ) : previewUrl ? (
                <iframe
                  title="Anteprima allegato"
                  src={previewUrl}
                  className="viewer-frame"
                  onError={() => setPreviewFailed(true)}
                />
              ) : (
                <div className="empty-row">Anteprima non disponibile.</div>
              )}
            </div>
          )}
        </section>
      </div>

      <div className="panel-note">Task: {taskId || '-'} | Pratica: {practiceId || '-'}</div>
    </section>
  );
}
