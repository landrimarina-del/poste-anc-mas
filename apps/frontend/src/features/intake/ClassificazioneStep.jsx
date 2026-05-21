// Componente classificazione/tipizzazione documento (Sprint 12 — S12-FE-3)
// Ref: GAP-UX.md §2.2 e §7.1 | GAP-UI.md §2.11
// AC-S6-07: dialog testo verbatim "ATTENZIONE" + "non sarà possibile modificare"

import { useState } from 'react';
import { attachmentsApi } from '../../core/api/attachmentsApi';
import { intakeApi } from '../../core/api/intakeApi';

const documentTypeOptions = [
  { value: 'VERBALE', label: 'Verbale di denuncia' },
  { value: 'CARTA',   label: 'Carta' }
];

function normalizeDocumentType(value) {
  if (!value || typeof value !== 'string') return '';
  const upper = value.toUpperCase();
  if (upper.includes('VERBALE')) return 'VERBALE';
  if (upper.includes('CARTA'))   return 'CARTA';
  return '';
}

function getTypeLabel(value) {
  return documentTypeOptions.find((o) => o.value === value)?.label ?? value;
}

/**
 * Props:
 *   practiceId         : string
 *   attachments        : array
 *   activeAttachmentId : string
 *   onAttachmentChange : (id: string) => void
 *   onTypingConfirmed  : (type: string) => void
 *   disabled           : boolean
 */
export function ClassificazioneStep({
  practiceId,
  attachments,
  activeAttachmentId,
  onAttachmentChange,
  onTypingConfirmed,
  disabled
}) {
  const [selectedType, setSelectedType] = useState('');
  const [showDialog,   setShowDialog]   = useState(false);
  const [confirming,   setConfirming]   = useState(false);
  const [error,        setError]        = useState('');

  const previewUrl = activeAttachmentId
    ? attachmentsApi.previewUrl(activeAttachmentId)
    : '';

  const activeAttachment = attachments.find(
    (a) => String(a.attachmentId ?? a.id) === String(activeAttachmentId)
  );
  const isUnavailable = activeAttachment?.ingestionStatus === 'FAILED'
    || activeAttachment?.ingestion_status === 'FAILED';

  const onClickConferma = () => {
    if (!selectedType) return;
    setError('');
    setShowDialog(true);
  };

  const onDialogAnnulla = () => {
    setShowDialog(false);
  };

  const onDialogConferma = async () => {
    setShowDialog(false);
    setConfirming(true);
    setError('');
    try {
      await intakeApi.confirmTyping(practiceId, selectedType);
      onTypingConfirmed(selectedType);
    } catch (err) {
      setError(err?.message ?? 'Impossibile confermare la tipizzazione.');
    } finally {
      setConfirming(false);
    }
  };

  return (
    <div className="classificazione-layout">
      {/* Colonna sinistra: dropdown tipo documento + bottone CONFERMA */}
      <div className="classificazione-left">
        <h3>Classificazione documento</h3>

        <div className="typing-form-block">
          <label htmlFor="classif-docType">Tipo documento</label>
          <select
            id="classif-docType"
            value={selectedType}
            onChange={(e) => setSelectedType(normalizeDocumentType(e.target.value))}
            disabled={disabled || confirming}
          >
            <option value="">Seleziona...</option>
            {documentTypeOptions.map((o) => (
              <option key={o.value} value={o.value}>{o.label}</option>
            ))}
          </select>

          <button
            type="button"
            className="btn btn-primary btn-squared"
            onClick={onClickConferma}
            disabled={!selectedType || disabled || confirming}
          >
            {confirming ? 'CONFERMA IN CORSO...' : 'CONFERMA'}
          </button>
        </div>

        {error ? <div className="api-error-box" style={{ marginTop: 10 }}>{error}</div> : null}
      </div>

      {/* Colonna destra: preview documento */}
      <div className="classificazione-right">
        {attachments.length > 0 ? (
          <div className="viewer-attachment-select">
            <label htmlFor="classif-attachSel">Allegato</label>
            <select
              id="classif-attachSel"
              value={activeAttachmentId}
              onChange={(e) => onAttachmentChange(e.target.value)}
              disabled={disabled}
            >
              {attachments.map((item) => {
                const val = String(item.attachmentId ?? item.id);
                return (
                  <option key={val} value={val}>
                    {item.fileName ?? item.name ?? `Allegato ${val}`}
                  </option>
                );
              })}
            </select>
          </div>
        ) : (
          <div className="panel-note">Nessun allegato disponibile sulla pratica.</div>
        )}

        {previewUrl && !isUnavailable ? (
          <div className="viewer-frame-shell" style={{ height: 440 }}>
            <iframe
              title="Anteprima documento"
              src={previewUrl}
              className="viewer-frame"
            />
          </div>
        ) : isUnavailable ? (
          <div className="attachment-unavailable">Documento non disponibile (acquisizione fallita).</div>
        ) : null}
      </div>

      {/* Dialog di conferma — AC-S6-07 testo verbatim */}
      {showDialog ? (
        <div
          className="confirm-dialog-overlay"
          role="dialog"
          aria-modal="true"
          aria-labelledby="confirmTypingTitle"
        >
          <div className="confirm-dialog">
            <h3 id="confirmTypingTitle">ATTENZIONE</h3>
            <p>
              E&apos; stato selezionato{' '}
              <strong>{getTypeLabel(selectedType)}</strong> come tipologia.
              Attenzione: non sarà possibile modificare il tipo documento in futuro.
              Confermare la selezione?
            </p>
            <div className="confirm-dialog-actions">
              <button
                type="button"
                className="btn btn-outline btn-squared"
                onClick={onDialogAnnulla}
              >
                ANNULLA
              </button>
              <button
                type="button"
                className="btn btn-primary btn-squared"
                onClick={onDialogConferma}
              >
                CONFERMA
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
