// Componente "Verifica Documento" — sezione 2 del task lavorazione (Sprint 12 — S12-FE-2)
// Ref: GAP-UI.md §2.5 — Verifica Documenti
// Layout 2 colonne: col-sx (dati + checklist) | col-dx (allegati/viewer)

import { useEffect, useState } from 'react';
import { attachmentsApi } from '../../core/api/attachmentsApi';
import { intakeApi } from '../../core/api/intakeApi';

const yesNoOptions = [
  { value: 'SI', label: 'Si' },
  { value: 'NO', label: 'No' }
];

const formalKoReasonOptions = [
  { value: 'INTESTAZIONE', label: 'Intestazione' },
  { value: 'FIRME',        label: 'Firme' },
  { value: 'TIMBRO',       label: 'Timbro' },
  { value: 'DICHIARAZIONE', label: 'Dichiarazione' },
  { value: 'CARTA_PI',     label: 'Carta Poste Italiane' }
];

/**
 * Props:
 *   practiceDetail     : object — risposta completa GET /practices/{id}
 *   confirmedType      : string (VERBALE | CARTA)
 *   attachments        : array
 *   activeAttachmentId : string
 *   onAttachmentChange : (id: string) => void
 *   checklistForm      : object
 *   onChecklistChange  : (field, value) => void
 *   onToggleKoReason   : (reason) => void
 *   checklistStatus    : string
 *   checklistOutcome   : string
 *   checklistLoading   : boolean
 *   checklistSaving    : boolean
 *   canSaveChecklist   : boolean
 *   isCardChecklist    : boolean
 */
export function VerificaDocumentiStep({
  practiceId,
  practiceDetail,
  confirmedType,
  attachments,
  activeAttachmentId,
  onAttachmentChange,
  checklistForm,
  onChecklistChange,
  onToggleKoReason,
  checklistStatus,
  checklistOutcome,
  checklistLoading,
  checklistSaving,
  canSaveChecklist,
  isCardChecklist
}) {
  const [allegatiVisible, setAllegatiVisible] = useState(true);
  const [sezioneVisible,  setSezioneVisible]  = useState(true);

  const header      = practiceDetail?.header      ?? {};
  const client      = practiceDetail?.client      ?? {};
  const blockedCard = practiceDetail?.blockedCard ?? {};

  const previewUrl = activeAttachmentId
    ? attachmentsApi.previewUrl(activeAttachmentId)
    : '';

  const isConformityDisabled = checklistForm?.documentPresent === 'NO';

  const isBusy = checklistLoading || checklistSaving;

  // Sprint 13: stato causali e toggle aree KO
  const [causaliCarta,   setCausaliCarta]   = useState([]);
  const [causaliVerbale, setCausaliVerbale] = useState([]);
  const [koCartaOpen,    setKoCartaOpen]    = useState(true);
  const [koVerbaleOpen,  setKoVerbaleOpen]  = useState({
    legibility: true,
    formalSuitability: true,
    clientDataConsistency: true,
    cardNumberMatch: true
  });

  // Sprint 13: carica causali CARTA quando conformità diventa NO
  useEffect(() => {
    if (!isCardChecklist || checklistForm?.formalSuitability !== 'NO' || !practiceId || causaliCarta.length > 0) return;
    intakeApi.getCausali(practiceId, 'CARTA')
      .then((r) => setCausaliCarta(Array.isArray(r) ? r : []))
      .catch(() => {});
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [checklistForm?.formalSuitability, isCardChecklist, practiceId]);

  // Sprint 13: carica causali VERBALE quando almeno un item diventa NO
  useEffect(() => {
    if (isCardChecklist || !practiceId || causaliVerbale.length > 0) return;
    const anyKo = checklistForm?.legibility === 'NO'
      || checklistForm?.formalSuitability === 'NO'
      || checklistForm?.clientDataConsistency === 'NO'
      || checklistForm?.cardNumberMatch === 'NO';
    if (!anyKo) return;
    intakeApi.getCausali(practiceId, 'VERBALE')
      .then((r) => setCausaliVerbale(Array.isArray(r) ? r : []))
      .catch(() => {});
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [
    checklistForm?.legibility,
    checklistForm?.formalSuitability,
    checklistForm?.clientDataConsistency,
    checklistForm?.cardNumberMatch,
    isCardChecklist,
    practiceId
  ]);

  // Sprint 13: helper area KO VERBALE per-item
  function renderVerbaleKoArea(fieldKey, label) {
    if (checklistForm[fieldKey] !== 'NO' || isConformityDisabled) return null;
    const isOpen = koVerbaleOpen[fieldKey];
    return (
      <div className="ko-area-expandable">
        <button
          type="button"
          className="ko-area-toggle"
          onClick={() => setKoVerbaleOpen((v) => ({ ...v, [fieldKey]: !v[fieldKey] }))}
        >
          {isOpen ? '\u25b2' : '\u25bc'} {label}
        </button>
        {isOpen ? (
          <div className="ko-area-body">
            <label htmlFor={`vd-nota-ko-${fieldKey}`}>
              Note aggiuntive
              <textarea
                id={`vd-nota-ko-${fieldKey}`}
                value={checklistForm.internalNotes}
                onChange={(e) => onChecklistChange('internalNotes', e.target.value)}
                placeholder="Inserisci note opzionali..."
                maxLength={500}
                rows={3}
                disabled={isBusy || !canSaveChecklist}
              />
            </label>
            <label htmlFor={`vd-causale-ko-${fieldKey}`}>
              Causale KO (opzionale)
              <select
                id={`vd-causale-ko-${fieldKey}`}
                value={checklistForm.codiceCausaleIdVerbale ?? ''}
                onChange={(e) => onChecklistChange('codiceCausaleIdVerbale', e.target.value || null)}
                disabled={isBusy || !canSaveChecklist}
              >
                <option value="">-- Seleziona causale --</option>
                {causaliVerbale.map((c) => (
                  <option key={c.id} value={c.id}>{c.descrizione}</option>
                ))}
              </select>
            </label>
          </div>
        ) : null}
      </div>
    );
  }

  return (
    <div className="verifica-wrapper">
      {/* Button bar */}
      <div className="verifica-button-bar">
        <button
          type="button"
          className="btn btn-outline btn-small"
          onClick={() => setAllegatiVisible((v) => !v)}
        >
          <i
            className={`fa ${allegatiVisible ? 'fa-eye-slash' : 'fa-eye'}`}
            aria-hidden="true"
          />
          {' '}{allegatiVisible ? 'nascondi allegati' : 'mostra allegati'}
        </button>
        <button
          type="button"
          className="btn btn-outline btn-small"
          onClick={() => setSezioneVisible((v) => !v)}
        >
          {sezioneVisible ? 'nascondi sezione' : 'mostra sezione'}
        </button>
      </div>

      {sezioneVisible ? (
        <div className={`verifica-content ${allegatiVisible ? '' : 'verifica-content-full'}`}>

          {/* ===== Colonna sinistra ===== */}
          <div className="verifica-left">

            {/* Dati Cliente — read-only */}
            <div className="summary-card verifica-data-card">
              <h4>Dati Cliente</h4>
              <dl>
                <div>
                  <dt>Cognome e Nome</dt>
                  <dd>
                    {[client.lastName, client.firstName].filter(Boolean).join(' ') ||
                      client.fullName ||
                      '-'}
                  </dd>
                </div>
                <div>
                  <dt>Codice Fiscale</dt>
                  <dd>{client.fiscalCode ?? client.codiceFiscale ?? '-'}</dd>
                </div>
                <div>
                  <dt>Data di nascita</dt>
                  <dd>{client.birthDate ?? '-'}</dd>
                </div>
              </dl>
            </div>

            {/* Dati Carta — read-only */}
            <div className="summary-card verifica-data-card">
              <h4>Dati Carta</h4>
              <dl>
                <div>
                  <dt>Numero carta</dt>
                  <dd>{blockedCard.cardNumberMasked ?? blockedCard.cardNumber ?? header.cardNumber ?? '-'}</dd>
                </div>
                <div>
                  <dt>Tipo carta</dt>
                  <dd>{blockedCard.cardType ?? '-'}</dd>
                </div>
                <div>
                  <dt>Intestatario</dt>
                  <dd>{blockedCard.cardHolder ?? '-'}</dd>
                </div>
              </dl>
            </div>

            {/* Tipo documento — read-only */}
            <div className="summary-card verifica-data-card">
              <h4>Tipo Documento</h4>
              <dl>
                <div>
                  <dt>Tipo confermato</dt>
                  <dd>
                    {confirmedType}{' '}
                    <span className="status-badge status-badge-draft">confermato</span>
                  </dd>
                </div>
              </dl>
            </div>

            {/* Checklist */}
            <div className="checklist-form-section">
              <h4>Checklist {isCardChecklist ? 'Carta' : 'Verbale'}</h4>

              <div className="checklist-meta-row">
                <span className="status-badge">
                  Stato: {checklistStatus || 'NON_INIZIATA'}
                </span>
              </div>

              {checklistLoading ? (
                <div className="panel-note">Caricamento checklist...</div>
              ) : (
                <div className="checklist-fields-grid">
                  {isCardChecklist ? (
                    <>
                      <label htmlFor="vd-documentPresent">
                        Presenza carta
                        <select
                          id="vd-documentPresent"
                          value={checklistForm.documentPresent}
                          onChange={(e) => onChecklistChange('documentPresent', e.target.value)}
                          disabled={isBusy || !canSaveChecklist}
                        >
                          <option value="">Seleziona...</option>
                          {yesNoOptions.map((o) => (
                            <option key={o.value} value={o.value}>{o.label}</option>
                          ))}
                        </select>
                      </label>
                      <label htmlFor="vd-formalSuitability">
                        Conformità carta
                        <select
                          id="vd-formalSuitability"
                          value={checklistForm.formalSuitability}
                          onChange={(e) => onChecklistChange('formalSuitability', e.target.value)}
                          disabled={isConformityDisabled || isBusy || !canSaveChecklist}
                        >
                          <option value="">Seleziona...</option>
                          {yesNoOptions.map((o) => (
                            <option key={o.value} value={o.value}>{o.label}</option>
                          ))}
                        </select>
                      </label>
                    </>
                  ) : (
                    <>
                      <label htmlFor="vd-documentPresent">
                        Documento presente?
                        <select
                          id="vd-documentPresent"
                          value={checklistForm.documentPresent}
                          onChange={(e) => onChecklistChange('documentPresent', e.target.value)}
                          disabled={isBusy || !canSaveChecklist}
                        >
                          <option value="">Seleziona...</option>
                          {yesNoOptions.map((o) => (
                            <option key={o.value} value={o.value}>{o.label}</option>
                          ))}
                        </select>
                      </label>
                      <label htmlFor="vd-legibility">
                        Leggibilità
                        <select
                          id="vd-legibility"
                          value={checklistForm.legibility}
                          onChange={(e) => onChecklistChange('legibility', e.target.value)}
                          disabled={isConformityDisabled || isBusy || !canSaveChecklist}
                        >
                          <option value="">Seleziona...</option>
                          {yesNoOptions.map((o) => (
                            <option key={o.value} value={o.value}>{o.label}</option>
                          ))}
                        </select>
                      </label>
                      <label htmlFor="vd-formalSuitability">
                        Idoneità formale
                        <select
                          id="vd-formalSuitability"
                          value={checklistForm.formalSuitability}
                          onChange={(e) => onChecklistChange('formalSuitability', e.target.value)}
                          disabled={isConformityDisabled || isBusy || !canSaveChecklist}
                        >
                          <option value="">Seleziona...</option>
                          {yesNoOptions.map((o) => (
                            <option key={o.value} value={o.value}>{o.label}</option>
                          ))}
                        </select>
                      </label>
                      <label htmlFor="vd-clientDataConsistency">
                        Coerenza dati cliente
                        <select
                          id="vd-clientDataConsistency"
                          value={checklistForm.clientDataConsistency}
                          onChange={(e) => onChecklistChange('clientDataConsistency', e.target.value)}
                          disabled={isConformityDisabled || isBusy || !canSaveChecklist}
                        >
                          <option value="">Seleziona...</option>
                          {yesNoOptions.map((o) => (
                            <option key={o.value} value={o.value}>{o.label}</option>
                          ))}
                        </select>
                      </label>
                    </>
                  )}
                </div>
              )}

              {isConformityDisabled ? (
                <div className="info-box">
                  {isCardChecklist
                    ? 'Carta assente: esito KO automatico applicato.'
                    : 'Documento assente: KO automatico applicato e controlli conformità disabilitati.'}
                </div>
              ) : null}

              {/* Sprint 13 D13-FE-1: Area KO conformità CARTA */}
              {isCardChecklist && !isConformityDisabled && checklistForm.formalSuitability === 'NO' ? (
                <div className="ko-area-expandable">
                  <button
                    type="button"
                    className="ko-area-toggle"
                    onClick={() => setKoCartaOpen((v) => !v)}
                  >
                    {koCartaOpen ? '\u25b2' : '\u25bc'} Dettaglio KO conformità
                  </button>
                  {koCartaOpen ? (
                    <div className="ko-area-body">
                      <label htmlFor="vd-nota-ko-carta">
                        Note aggiuntive
                        <textarea
                          id="vd-nota-ko-carta"
                          value={checklistForm.internalNotes}
                          onChange={(e) => onChecklistChange('internalNotes', e.target.value)}
                          placeholder="Inserisci note opzionali..."
                          maxLength={500}
                          rows={3}
                          disabled={isBusy || !canSaveChecklist}
                        />
                      </label>
                      <label htmlFor="vd-causale-ko-carta">
                        Causale KO (opzionale)
                        <select
                          id="vd-causale-ko-carta"
                          value={checklistForm.codiceCausaleIdCarta ?? ''}
                          onChange={(e) => onChecklistChange('codiceCausaleIdCarta', e.target.value || null)}
                          disabled={isBusy || !canSaveChecklist}
                        >
                          <option value="">-- Seleziona causale --</option>
                          {causaliCarta.map((c) => (
                            <option key={c.id} value={c.id}>{c.descrizione}</option>
                          ))}
                        </select>
                      </label>
                    </div>
                  ) : null}
                </div>
              ) : null}

              {/* Sprint 13 D13-FE-2: Aree KO per-item VERBALE */}
              {!isCardChecklist && !isConformityDisabled ? (
                <>
                  {renderVerbaleKoArea('legibility', 'Leggibilità KO')}
                  {renderVerbaleKoArea('formalSuitability', 'Idoneità Formale KO')}
                  {renderVerbaleKoArea('clientDataConsistency', 'Coerenza Dati Cliente KO')}
                  {checklistForm.cardNumberCheckEnabled
                    ? renderVerbaleKoArea('cardNumberMatch', 'Corrispondenza Numero Carta KO')
                    : null}
                </>
              ) : null}

              {!isCardChecklist && checklistForm.formalSuitability === 'NO' ? (
                <div className="checklist-ko-group" role="group" aria-label="Causali KO formali">
                  <h5>Causali KO formali obbligatorie</h5>
                  <div className="checklist-ko-options">
                    {formalKoReasonOptions.map((item) => (
                      <label key={item.value} className="checklist-checkbox-inline">
                        <input
                          type="checkbox"
                          checked={checklistForm.formalKoReasons?.includes(item.value)}
                          onChange={() => onToggleKoReason(item.value)}
                          disabled={isBusy || !canSaveChecklist}
                        />
                        {item.label}
                      </label>
                    ))}
                  </div>
                </div>
              ) : null}

              {/* Riepilogo esito */}
              {checklistOutcome ? (
                <div
                  className={`outcome-card ${
                    checklistOutcome === 'APPROVATA' ? 'outcome-card-ok' : 'outcome-card-ko'
                  }`}
                  aria-live="polite"
                >
                  <h5>Riepilogo esito</h5>
                  <p>{checklistOutcome}</p>
                </div>
              ) : null}
            </div>
          </div>

          {/* ===== Colonna destra: allegati/viewer ===== */}
          {allegatiVisible ? (
            <div className="verifica-right">
              {attachments.length > 0 ? (
                <>
                  <div className="viewer-attachment-select">
                    <label htmlFor="verifica-attachSel">Allegato</label>
                    <select
                      id="verifica-attachSel"
                      value={activeAttachmentId}
                      onChange={(e) => onAttachmentChange(e.target.value)}
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

                  {previewUrl ? (
                    <div className="viewer-frame-shell" style={{ height: 500 }}>
                      <iframe
                        title="Anteprima allegato"
                        src={previewUrl}
                        className="viewer-frame"
                      />
                    </div>
                  ) : null}
                </>
              ) : (
                <div className="panel-note">Nessun allegato disponibile sulla pratica.</div>
              )}
            </div>
          ) : null}
        </div>
      ) : null}
    </div>
  );
}
