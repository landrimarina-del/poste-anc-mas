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

function pickValue(...values) {
  for (const value of values) {
    if (value === null || value === undefined) continue;
    if (typeof value === 'string' && value.trim() === '') continue;
    return value;
  }
  return '-';
}

/**
 * Props:
 *   practiceId         : string
 *   attachments        : array
 *   activeAttachmentId : string
 *   onAttachmentChange : (id: string) => void
 *   onTypingConfirmed  : (type: string) => void
 *   practiceDetail     : object
 *   disabled           : boolean
 */
export function ClassificazioneStep({
  practiceId,
  attachments,
  activeAttachmentId,
  onAttachmentChange,
  onTypingConfirmed,
  practiceDetail,
  disabled
}) {
  const [selectedType, setSelectedType] = useState('');
  const [showDialog,   setShowDialog]   = useState(false);
  const [confirming,   setConfirming]   = useState(false);
  const [error,        setError]        = useState('');

  const previewUrl = activeAttachmentId
    ? attachmentsApi.previewUrl(activeAttachmentId)
    : '';
  const downloadUrl = activeAttachmentId
    ? attachmentsApi.downloadUrl(activeAttachmentId)
    : '';

  const activeAttachment = attachments.find(
    (a) => String(a.attachmentId ?? a.id) === String(activeAttachmentId)
  );
  const isUnavailable = activeAttachment?.ingestionStatus === 'FAILED'
    || activeAttachment?.ingestion_status === 'FAILED';

  const header = practiceDetail?.header ?? {};
  const client = practiceDetail?.client ?? {};
  const legacyClient = practiceDetail?.cliente ?? practiceDetail?.clientData ?? {};
  const blockedCard = practiceDetail?.blockedCard ?? {};

  const tipoCarta = blockedCard.cardType ?? blockedCard.tipoCarta ?? '-';
  const numeroCarta = blockedCard.cardNumberMasked ?? blockedCard.cardNumber ?? header.cardNumber ?? '-';
  const intestazioneCarta = blockedCard.cardHolder
    ?? blockedCard.intestatario
    ?? [client.lastName ?? client.cognome, client.firstName ?? client.nome].filter(Boolean).join(' ')
    ?? '-';

  const clienteNome = pickValue(client.firstName, client.nome, legacyClient.firstName, legacyClient.nome, legacyClient.NOME);
  const clienteCognome = pickValue(client.lastName, client.cognome, legacyClient.lastName, legacyClient.cognome, legacyClient.COGNOME);
  const clienteSesso = pickValue(client.gender, client.sesso, legacyClient.gender, legacyClient.sesso, legacyClient.SESSO);
  const clienteCodiceFiscale = pickValue(
    client.fiscalCode,
    client.codiceFiscale,
    legacyClient.fiscalCode,
    legacyClient.codiceFiscale,
    legacyClient.CODICEFISCALE,
    header.cfCliente,
    header.codiceFiscale
  );
  const clienteDataNascita = pickValue(client.birthDate, client.dataNascita, legacyClient.birthDate, legacyClient.dataNascita);
  const clienteComuneNascita = pickValue(client.birthCity, client.comuneNascita, legacyClient.birthCity, legacyClient.comuneNascita);
  const clienteProvinciaNascita = pickValue(client.birthProvince, client.provinciaNascita, legacyClient.birthProvince, legacyClient.provinciaNascita);
  const clienteNazioneNascita = pickValue(client.birthCountry, client.nazioneNascita, legacyClient.birthCountry, legacyClient.nazioneNascita);
  const clienteTelefono = pickValue(client.phone, client.telephone, client.telefono, legacyClient.phone, legacyClient.telephone, legacyClient.telefono);
  const clienteCellulare = pickValue(client.mobilePhone, client.mobile, client.cellulare, legacyClient.mobilePhone, legacyClient.mobile, legacyClient.cellulare);

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
    <div className="verifica-wrapper">
      <div className="verifica-content verifica-content-grid-2x2">
        <div className="summary-card verifica-data-card verifica-area-top-left">
          <h4>Dati Carta Bloccata</h4>
          <dl>
            <div>
              <dt>Tipo Carta</dt>
              <dd>{tipoCarta || '-'}</dd>
            </div>
            <div>
              <dt>Numero Carta</dt>
              <dd>{numeroCarta || '-'}</dd>
            </div>
            <div>
              <dt>Intestazione Carta</dt>
              <dd>{intestazioneCarta || '-'}</dd>
            </div>
          </dl>
        </div>

        <div className="summary-card verifica-data-card verifica-area-top-right">
          <h4>Dati Cliente</h4>
          <div className="verifica-client-rows">
            <dl className="verifica-client-row verifica-client-row-3">
              <div className="verifica-client-field"><dt>Nome</dt><dd>{clienteNome}</dd></div>
              <div className="verifica-client-field"><dt>Cognome</dt><dd>{clienteCognome}</dd></div>
              <div className="verifica-client-field"><dt>Sesso</dt><dd>{clienteSesso}</dd></div>
            </dl>
            <dl className="verifica-client-row verifica-client-row-3">
              <div className="verifica-client-field"><dt>Codice Fiscale</dt><dd>{clienteCodiceFiscale}</dd></div>
              <div className="verifica-client-field"><dt>Data di nascita</dt><dd>{clienteDataNascita}</dd></div>
              <div className="verifica-client-field"><dt>Comune di nascita</dt><dd>{clienteComuneNascita}</dd></div>
            </dl>
            <dl className="verifica-client-row verifica-client-row-3">
              <div className="verifica-client-field"><dt>Provincia di nascita</dt><dd>{clienteProvinciaNascita}</dd></div>
              <div className="verifica-client-field"><dt>Nazione di nascita</dt><dd>{clienteNazioneNascita}</dd></div>
              <div className="verifica-client-field"><dt>Telefono</dt><dd>{clienteTelefono}</dd></div>
            </dl>
            <dl className="verifica-client-row verifica-client-row-1">
              <div className="verifica-client-field"><dt>Cellulare</dt><dd>{clienteCellulare}</dd></div>
            </dl>
          </div>
        </div>

        <div className="summary-card verifica-data-card verifica-area-bottom-right">
          <h4>Contenuti Documento</h4>
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

          {downloadUrl ? (
            <a
              className="btn btn-outline btn-small"
              href={downloadUrl}
              target="_blank"
              rel="noreferrer"
              style={{ marginBottom: 12 }}
            >
              Download documento
            </a>
          ) : null}

          {previewUrl && !isUnavailable ? (
            <div className="viewer-frame-shell" style={{ height: 500 }}>
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

        <div className="checklist-form-section verifica-area-bottom-left classificazione-control-card">
          <h4>Controllo Documento</h4>

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
