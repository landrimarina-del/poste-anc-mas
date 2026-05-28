import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { practicesApi } from '../../core/api/practicesApi';

function formatDateTime(value) {
  if (!value) {
    return '-';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return new Intl.DateTimeFormat('it-IT', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  }).format(date);
}

function formatStateDateTime(value) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  const HH = String(date.getHours()).padStart(2, '0');
  const mm = String(date.getMinutes()).padStart(2, '0');
  const DD = String(date.getDate()).padStart(2, '0');
  const MM = String(date.getMonth() + 1).padStart(2, '0');
  const YYYY = date.getFullYear();
  return `${HH}:${mm} ${DD}/${MM}/${YYYY}`;
}

const detailTabs = [
  { key: 'summary', label: 'Riepilogo' },
  { key: 'history', label: 'Cronologia' },
  { key: 'states', label: 'Stati' },
  { key: 'relatedActions', label: 'Azioni Correlate' }
];

function formatResidenceAddress(client) {
  const address = client?.residenceAddress ?? {};
  const pieces = [
    address.street ?? client?.residenceStreet,
    address.streetNumber ?? client?.residenceStreetNumber,
    address.city ?? client?.residenceCity,
    address.province ?? client?.residenceProvince,
    address.postalCode ?? client?.residencePostalCode
  ]
    .map((item) => String(item ?? '').trim())
    .filter(Boolean);

  return pieces.length ? pieces.join(', ') : '-';
}

const PPEZ_DESCRIPTIONS = {
  PPEZ026: 'Verbale Denuncia non leggibile',
  PPEZ027: 'Intestazione mancante nel Verbale Denuncia',
  PPEZ028: 'Firme mancanti nel Verbale Denuncia',
  PPEZ029: 'Intestazione non conforme al Timbro nel Verbale Denuncia',
  PPEZ030: 'Dichiarazione non conforme alle firme nel Verbale Denuncia',
  PPEZ031: 'Mancata descrizione emissione Carta da Poste Italiane',
  PPEZ032: 'Incoerenza dati Denunciante con dati Cliente',
  PPEZ033: 'Incorenza numero Carta nel Verbale Denuncia',
  PPEZ034: 'Carta non leggibile',
  PPEZ035: 'Carta non tagliata o incorenza numero Carta'
};

function formatSdOutcome(value) {
  if (!value) return '-';
  const v = value.toUpperCase();
  if (v === 'APPROVATA' || v === 'OK') return 'OK';
  if (v === 'RESPINTA' || v === 'KO') return 'NOK';
  return value;
}

function formatFileSize(bytes) {
  if (!bytes) return '-';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

const CHECKLIST_ROWS = {
  CARTA: [
    { key: 'legibility',        title: 'La Carta risulta leggibile?',                    koCode: 'PPEZ034', required: 'SI' },
    { key: 'formalSuitability', title: 'La Carta è idonea al controllo formale?',        koCode: 'PPEZ035', required: 'SI' }
  ],
  VERBALE: [
    { key: 'legibility',                      title: 'Verbale Denuncia leggibile?',                       koCode: 'PPEZ026', required: 'SI' },
    { key: 'intestazioneOk',                   title: 'Intestazione presente?',                            koCode: 'PPEZ027', required: 'SI' },
    { key: 'firmeOk',                          title: 'Firme presenti?',                                   koCode: 'PPEZ028', required: 'SI' },
    { key: 'intestazioneConformeAlTimbroOk',   title: 'Intestazione conforme al Timbro?',                  koCode: 'PPEZ029', required: 'NO' },
    { key: 'dichiarazioneConformeAlleFirmeOk', title: 'Dichiarazione conforme alle firme?',                koCode: 'PPEZ030', required: 'NO' },
    { key: 'cartaPosteItalianeOk',             title: 'Carta emessa da Poste Italiane?',                   koCode: 'PPEZ031', required: 'NO' },
    { key: 'clientDataConsistency',            title: 'Coerenza dati Denunciante con dati Cliente?',       koCode: 'PPEZ032', required: 'SI' },
    { key: 'cardNumberMatch',                  title: 'Numero Carta coerente con Verbale?',                koCode: 'PPEZ033', required: 'NO' }
  ]
};

const DETAIL_SIDEBAR_ITEMS = [
  { id: 'DATI_PRATICA',    label: 'Dati Pratica',     icon: 'fa-briefcase',      section: 1 },
  { id: 'DATI_LAV',        label: 'Dati Lavorazione', icon: 'fa-check-square-o', section: 2 },
  { id: 'ESITO',           label: 'Esito',             icon: 'fa-address-card-o', section: 3 }
];

const EVENT_DESCRIPTIONS = {
  PRACTICE_OPENED:            'Aperta in SD',
  STATE_CHANGED:              'Stato cambiato',
  PRACTICE_CLOSE_REQUESTED:   'Chiusa da operatore',
  PRACTICE_FINALIZED_BPM:     'Chiusa da sistema esterno',
  CHECKLIST_SAVED:            'Pratica modificata',
  CHECKLIST_REOPENED:         'Pratica modificata',
  CHECKLIST_HELP_VIEWED:      'Help checklist visualizzato',
  DOCUMENT_TYPED:             'Documento tipizzato',
  TASK_ACCEPTED:              'Presa in carico',
  TASK_REASSIGNED:            'Task riassegnato',
  BPM_OUTCOME_ACK_RECEIVED:   'Chiusa da sistema esterno',
  ATTACHMENT_INGESTED:        'Allegato acquisito',
  SIGNAL_CREATED:             'Segnalazione creata',
  SIGNAL_TAKEN:               'Segnalazione presa in carico',
  SIGNAL_FORWARDED_SINERGIA:  'Segnalazione inoltrata a Sinergia',
  SIGNAL_REASSIGNED:          'Segnalazione riassegnata',
  FAVORITE_CREATED:           'Preferito creato',
  FAVORITE_UPDATED:           'Preferito aggiornato',
  FAVORITE_DELETED:           'Preferito eliminato',
};

export function PracticeDetailPage() {
  const { practiceId } = useParams();
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState('summary');
  const [riepilogoSection, setRiepilogoSection] = useState(1);
  const [sidebarCollapsed, setSidebarCollapsed] = useState(false);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [detail, setDetail] = useState(null);
  const [history, setHistory] = useState([]);
  const [states, setStates] = useState([]);
  const [relatedActions, setRelatedActions] = useState([]);
  const [showResidenceAddress, setShowResidenceAddress] = useState(false);
  const [attachments, setAttachments] = useState([]);
  const [activeAttachmentId, setActiveAttachmentId] = useState('');

  const loadDetail = useCallback(async () => {
    if (!practiceId) {
      return;
    }

    setLoading(true);
    setErrorMessage('');

    try {
      const [detailResponse, historyResponse, statesResponse, relatedActionsResponse, attachmentsResponse] = await Promise.all([
        practicesApi.detail(practiceId),
        practicesApi.history(practiceId),
        practicesApi.states(practiceId),
        practicesApi.relatedActions(practiceId),
        practicesApi.attachments(practiceId).catch(() => [])
      ]);

      setDetail(detailResponse);
      setHistory(Array.isArray(historyResponse) ? historyResponse : []);
      setStates(Array.isArray(statesResponse) ? statesResponse : []);
      setRelatedActions(Array.isArray(relatedActionsResponse) ? relatedActionsResponse : []);
      setAttachments(Array.isArray(attachmentsResponse) ? attachmentsResponse : []);
    } catch (error) {
      setErrorMessage(error?.message ?? 'Errore durante il caricamento del dettaglio pratica');
      setDetail(null);
      setHistory([]);
      setStates([]);
      setRelatedActions([]);
      setAttachments([]);
    } finally {
      setLoading(false);
    }
  }, [practiceId]);

  useEffect(() => {
    loadDetail();
  }, [loadDetail]);

  useEffect(() => {
    if (attachments.length > 0 && !activeAttachmentId) {
      setActiveAttachmentId(String(attachments[0].attachmentId));
    }
  }, [attachments, activeAttachmentId]);

  const header = detail?.header ?? {};
  const client = detail?.client ?? {};
  const blockedCard = detail?.blockedCard ?? {};
  const koCodes = (() => {
    try { return JSON.parse(header.koCodesJson ?? '[]'); } catch { return []; }
  })();

  return (
    <section className="panel">
      <div className="detail-header">
        <div>
          <h2>Dettaglio pratica {header.practiceNumber ?? practiceId}</h2>

        </div>

        <div className="detail-header-actions">
          <button
            type="button"
            className="btn btn-primary btn-small"
            onClick={() =>
              navigate(
                `/segnalazioni?practiceId=${encodeURIComponent(practiceId ?? '')}&practiceNumber=${encodeURIComponent(
                  header.practiceNumber ?? ''
                )}`
              )
            }
          >
            INVIA SEGNALAZIONE
          </button>
          <button type="button" className="btn btn-outline btn-small" onClick={loadDetail} disabled={loading}>
            {loading ? 'Caricamento...' : 'Aggiorna'}
          </button>
          <button type="button" className="btn btn-outline btn-small" onClick={() => navigate('/pratiche')}>
            Torna alla lista
          </button>
        </div>
      </div>

      {errorMessage ? <div className="api-error-box">{errorMessage}</div> : null}

      <div className="detail-tabs" role="tablist" aria-label="Sezioni dettaglio pratica">
        {detailTabs.map((tab) => (
          <button
            key={tab.key}
            type="button"
            role="tab"
            className={`detail-tab ${activeTab === tab.key ? 'active' : ''}`}
            aria-selected={activeTab === tab.key}
            onClick={() => setActiveTab(tab.key)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {activeTab === 'summary' ? (
        <div className="detail-section" role="tabpanel">

          {/* Barra avanzamento processo */}
          {(() => {
            const FASI_PRATICA = [
              { id: 'APERTA', label: 'Apertura' },
              { id: 'IN_LAVORAZIONE', label: 'Lavorazione' },
              { id: 'CHIUSURA', label: 'Chiusura' }
            ];
            const stateToIdx = (s) => {
              if (!s || s === 'APERTA') return 0;
              if (s === 'IN_LAVORAZIONE' || s === 'IN_ATTESA_CONFERMA_BPM') return 1;
              return 2;
            };
            const idx = stateToIdx(header.state);
            const pct = Math.round((idx / (FASI_PRATICA.length - 1)) * 100);
            return (
              <section className="progress-banner" aria-label="Avanzamento processo">
                <div className="progress-steps">
                  {FASI_PRATICA.map((f, i) => (
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

          {/* Layout sidebar + contenuto */}
          <div className="workflow-page-layout" style={{ marginTop: 12 }}>

            {/* Sidebar inline con label personalizzate */}
            <nav
              className={`workflow-sidebar ${sidebarCollapsed ? 'extra-narrow' : 'narrow'}`}
              aria-label="Navigazione dettaglio"
            >
              <button
                type="button"
                className="workflow-sidebar-toggle"
                onClick={() => setSidebarCollapsed((p) => !p)}
                aria-label={sidebarCollapsed ? 'Espandi sidebar' : 'Comprimi sidebar'}
              >
                <i
                  className={`fa ${sidebarCollapsed ? 'fa-angle-double-right' : 'fa-angle-double-left'}`}
                  aria-hidden="true"
                />
              </button>
              <ul className="workflow-sidebar-list">
                {DETAIL_SIDEBAR_ITEMS.map((item) => (
                  <li key={item.id}>
                    <button
                      type="button"
                      className={['workflow-sidebar-item', riepilogoSection === item.section ? 'active' : ''].filter(Boolean).join(' ')}
                      onClick={() => setRiepilogoSection(item.section)}
                      title={item.label}
                    >
                      <i className={`fa ${item.icon} workflow-sidebar-icon`} aria-hidden="true" />
                      {!sidebarCollapsed && <span className="workflow-sidebar-label">{item.label}</span>}
                    </button>
                  </li>
                ))}
              </ul>
            </nav>

            {/* Area contenuto read-only */}
            <div className="workflow-content-area" style={{ padding: '16px' }}>

              {/* Sezione 1 — Dati Pratica (layout orizzontale 3 colonne) */}
              {riepilogoSection === 1 ? (
                <article className="summary-card">
                  <h4>Dati Pratica</h4>
                  <dl style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '12px 24px' }}>
                    <div><dt>Pratica N.</dt><dd>{header.practiceNumber ?? '-'}</dd></div>
                    <div><dt>Stato</dt><dd>{header.state ?? '-'}</dd></div>
                    <div><dt>Esito SD</dt><dd>{formatSdOutcome(header.sdOutcome)}</dd></div>
                    <div><dt>Data apertura</dt><dd>{formatDateTime(header.openedAt)}</dd></div>
                    <div><dt>Ultima modifica</dt><dd>{formatDateTime(header.lastModifiedAt)}</dd></div>
                    <div><dt>Data chiusura</dt><dd>{formatDateTime(header.closedAt)}</dd></div>
                    <div><dt>Codice cliente</dt><dd>{client.customerCode ?? '-'}</dd></div>
                    <div><dt>Codice fiscale</dt><dd>{client.fiscalCode ?? '-'}</dd></div>
                  </dl>
                </article>
              ) : null}

              {/* Sezione 2 — Dati Lavorazione */}
              {riepilogoSection === 2 ? (
                <>
                  <div className="verifica-content verifica-content-grid-2x2">
                    <article className="summary-card verifica-area-top-left">
                      <h4>Dati Carta Bloccata</h4>
                      <dl>
                        <div><dt>Tipo Carta</dt><dd>{blockedCard.cardType ?? '-'}</dd></div>
                        <div><dt>Numero Carta</dt><dd>{blockedCard.cardNumberMasked ?? '-'}</dd></div>
                        <div><dt>Intestazione Carta</dt><dd>{blockedCard.cardHolder ?? '-'}</dd></div>
                      </dl>
                    </article>
                    <article className="summary-card verifica-area-top-right">
                      <h4>Dati Cliente</h4>
                      <dl style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px 16px' }}>
                        <div><dt>Nome</dt><dd>{client.firstName ?? '-'}</dd></div>
                        <div><dt>Cognome</dt><dd>{client.lastName ?? '-'}</dd></div>
                        <div><dt>Sesso</dt><dd>{client.gender ?? '-'}</dd></div>
                        <div><dt>Codice Fiscale</dt><dd>{client.fiscalCode ?? '-'}</dd></div>
                        <div><dt>Data di nascita</dt><dd>{client.birthDate ? String(client.birthDate) : '-'}</dd></div>
                        <div><dt>Comune di nascita</dt><dd>{client.birthCity ?? '-'}</dd></div>
                        <div><dt>Provincia di nascita</dt><dd>{client.birthProvince ?? '-'}</dd></div>
                        <div><dt>Nazione di nascita</dt><dd>{client.birthCountry ?? '-'}</dd></div>
                        <div><dt>Telefono</dt><dd>{client.phone ?? '-'}</dd></div>
                        <div><dt>Cellulare</dt><dd>{client.mobilePhone ?? '-'}</dd></div>
                      </dl>
                      <div className="detail-collapse-block" style={{ marginTop: 12 }}>
                        <button
                          type="button"
                          className="btn btn-outline btn-small"
                          onClick={() => setShowResidenceAddress((prev) => !prev)}
                        >
                          {showResidenceAddress ? 'Nascondi indirizzo residenza' : 'Mostra indirizzo residenza'}
                        </button>
                        {showResidenceAddress ? (
                          <div className="detail-collapse-content">
                            <div className="panel-note">{formatResidenceAddress(client)}</div>
                          </div>
                        ) : null}
                      </div>
                    </article>
                  <article className="summary-card verifica-area-bottom-left" style={{ minWidth: 0 }}>
                    <h4>Controllo Carta</h4>
                    {/* Tipo Documento + Stato */}
                    <div className="checklist-meta-row" style={{ marginBottom: 12, alignItems: 'center' }}>
                      <div style={{ border: '1px solid var(--gray-200)', borderRadius: 8, padding: '8px 12px', background: '#fff' }}>
                        <div style={{ fontSize: '0.78rem', color: 'var(--gray-500)', marginBottom: 2 }}>Tipo confermato</div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                          <strong>{header.documentType ?? '-'}</strong>
                          {header.documentType ? (
                            <span style={{
                              fontSize: '0.72rem', fontWeight: 700, padding: '2px 8px',
                              background: '#e8f4ff', color: '#005eb8', borderRadius: 10, border: '1px solid #b8d9f5'
                            }}>confermato</span>
                          ) : null}
                        </div>
                      </div>
                      {header.sdOutcome ? (
                        <span className="status-badge status-badge-draft" style={{ marginLeft: 12 }}>
                          Stato: {['CHIUSA_OK', 'CHIUSA_KO'].includes(header.state) ? 'COMPLETATA' : 'BOZZA'}
                        </span>
                      ) : null}
                    </div>
                    {/* Checklist table */}
                    {header.documentType && (CHECKLIST_ROWS[header.documentType] ?? []).length > 0 ? (
                      <div className="verbale-checklist-table-wrapper">
                        <table className="verbale-checklist-table">
                          <thead>
                            <tr>
                              <th scope="col">Richiesto</th>
                              <th scope="col">Descrizione</th>
                              <th scope="col">Conforme</th>
                              <th scope="col">Note</th>
                            </tr>
                          </thead>
                          <tbody>
                            {(CHECKLIST_ROWS[header.documentType] ?? []).map((row) => {
                              const conforme = header.sdOutcome
                                ? (koCodes.includes(row.koCode) ? 'NO' : 'SI')
                                : '-';
                              return (
                                <tr key={row.key}>
                                  <td><strong>{row.required}</strong></td>
                                  <td>{row.title}</td>
                                  <td>
                                    <strong style={{ color: conforme === 'NO' ? '#c0392b' : conforme === 'SI' ? '#1a7a46' : '' }}>
                                      {conforme}
                                    </strong>
                                  </td>
                                  <td>-</td>
                                </tr>
                              );
                            })}
                          </tbody>
                        </table>
                      </div>
                    ) : null}
                    {/* Esito Controllo */}
                    <div className={`summary-card verbale-esito-causale-card${formatSdOutcome(header.sdOutcome) === 'OK' ? ' outcome-card-ok' : formatSdOutcome(header.sdOutcome) === 'NOK' ? ' outcome-card-ko' : ''}`} style={{ marginTop: 12 }}>
                      <dl>
                        <div><dt>Esito Controllo</dt><dd>{formatSdOutcome(header.sdOutcome) === '-' ? '-' : formatSdOutcome(header.sdOutcome) === 'OK' ? 'OK' : 'KO'}</dd></div>
                        <div>
                          <dt>Causali KO</dt>
                          <dd>
                            {koCodes.length > 0 ? (
                              <ul style={{ margin: 0, paddingLeft: '1.2rem' }}>
                                {koCodes.map((code) => (
                                  <li key={code}>{PPEZ_DESCRIPTIONS[code] || code}</li>
                                ))}
                              </ul>
                            ) : '-'}
                          </dd>
                        </div>
                      </dl>
                    </div>
                  </article>
                  {/* Contenuti Carta */}
                  <article className="summary-card verifica-area-bottom-right" style={{ minWidth: 0 }}>
                    <h4>Contenuti Carta</h4>
                    {activeAttachmentId ? (
                      <a
                        className="btn btn-outline btn-small"
                        href={`/api/v1/attachments/${activeAttachmentId}/download`}
                        target="_blank"
                        rel="noreferrer"
                        style={{ marginBottom: 12, display: 'inline-block' }}
                      >
                        <i className="fa fa-download" aria-hidden="true" />{' '}Download documento
                      </a>
                    ) : null}
                    {attachments.length > 1 ? (
                      <div className="viewer-attachment-select" style={{ marginBottom: 12 }}>
                        <label htmlFor="detail-attachSel">Scelta documento</label>
                        <select
                          id="detail-attachSel"
                          value={activeAttachmentId}
                          onChange={(e) => setActiveAttachmentId(e.target.value)}
                        >
                          {attachments.map((att) => (
                            <option key={att.attachmentId} value={String(att.attachmentId)}>
                              {att.fileName ?? `Allegato ${att.attachmentId}`}
                            </option>
                          ))}
                        </select>
                      </div>
                    ) : null}
                    {attachments.length > 0 ? (
                      activeAttachmentId ? (
                        <div className="viewer-frame-shell" style={{ height: 500 }}>
                          <iframe
                            title="Anteprima allegato"
                            src={`/api/v1/attachments/${activeAttachmentId}/preview`}
                            className="viewer-frame"
                          />
                        </div>
                      ) : (
                        <div className="panel-note">Selezionare un allegato per visualizzare il contenuto.</div>
                      )
                    ) : (
                      <div className="panel-note">Nessun allegato disponibile sulla pratica.</div>
                    )}
                  </article>
                  </div>
                </>
              ) : null}

              {/* Sezione 3 — Esito */}
              {riepilogoSection === 3 ? (
                <>
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
                    <article className="summary-card">
                      <h4>Pratica</h4>
                      <dl>
                        <div><dt>Numero</dt><dd>{header.practiceNumber ?? '-'}</dd></div>
                        <div><dt>Stato</dt><dd>{header.state ?? '-'}</dd></div>
                      </dl>
                    </article>
                    <article className={`summary-card ${header.sdOutcome === 'APPROVATA' ? 'outcome-card-ok' : header.sdOutcome === 'RESPINTA' ? 'outcome-card-ko' : ''}`}>
                      <h4>Esito checklist</h4>
                      <dl>
                        <div><dt>Tipo documento</dt><dd>{header.documentType ?? '-'}</dd></div>
                        <div><dt>Esito SD</dt><dd>{formatSdOutcome(header.sdOutcome)}</dd></div>
                        <div>
                          <dt>Causali KO</dt>
                          <dd>
                            {koCodes.length > 0 ? (
                              <ul style={{ margin: 0, paddingLeft: '1.2rem' }}>
                                {koCodes.map((code) => (
                                  <li key={code}>{PPEZ_DESCRIPTIONS[code] || code}</li>
                                ))}
                              </ul>
                            ) : '-'}
                          </dd>
                        </div>
                      </dl>
                    </article>
                  </div>
                  <article className="summary-card" style={{ marginTop: 12 }}>
                    <h4>Note di Lavorazione</h4>
                    <textarea
                      readOnly
                      value=""
                      placeholder="Nessuna nota disponibile"
                      rows={4}
                      style={{ width: '100%', marginTop: 4, boxSizing: 'border-box', resize: 'none', background: '#f9f9f9', cursor: 'default' }}
                    />
                  </article>
                </>
              ) : null}

            </div>
          </div>
        </div>
      ) : null}

      {activeTab === 'history' ? (
        <div className="detail-section" role="tabpanel">
          <h3>Cronologia</h3>
          <div className="table-wrapper">
            <table className="practices-table">
              <thead>
                <tr>
                  <th>Data evento</th>
                  <th>Operatore</th>
                  <th>Descrizione evento</th>
                </tr>
              </thead>
              <tbody>
                {history.length === 0 ? (
                  <tr>
                    <td className="empty-row" colSpan={3}>
                      {loading ? 'Caricamento cronologia...' : 'Nessun evento disponibile.'}
                    </td>
                  </tr>
                ) : (() => {
                  const seen = new Set();
                  return history
                    .filter((item) => {
                      const k = item.eventId != null
                        ? String(item.eventId)
                        : `${item.eventType}|${item.occurredAt}|${item.actor ?? ''}`;
                      if (seen.has(k)) return false;
                      seen.add(k);
                      return true;
                    })
                    .map((item) => (
                      <tr key={item.eventId ?? `${item.eventType}-${item.occurredAt}`}>
                        <td>{formatDateTime(item.occurredAt)}</td>
                        <td>{item.actor ?? '-'}</td>
                        <td>{EVENT_DESCRIPTIONS[item.eventType] ?? item.eventType ?? '-'}</td>
                      </tr>
                    ));
                })()}
              </tbody>
            </table>
          </div>
        </div>
      ) : null}

      {activeTab === 'states' ? (
        <div className="detail-section" role="tabpanel">
          <h3>Stati</h3>
          <div className="table-wrapper">
            <table className="practices-table">
              <thead>
                <tr>
                  <th>Stato</th>
                  <th>Data stato</th>
                  <th>Operatore</th>
                </tr>
              </thead>
              <tbody>
                {states.length === 0 ? (
                  <tr>
                    <td className="empty-row" colSpan={3}>
                      {loading ? 'Caricamento stati...' : 'Nessuna transizione disponibile.'}
                    </td>
                  </tr>
                ) : (
                  states.map((item) => (
                    <tr key={item.transitionId ?? `${item.toState}-${item.transitionedAt}`}>
                      <td>{item.toState ?? item.stato ?? '-'}</td>
                      <td>{formatDateTime(item.transitionedAt ?? item.dataOra ?? item.at)}</td>
                      <td>{item.attore ?? item.actor ?? '-'}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      ) : null}

      {activeTab === 'relatedActions' ? (
        <div className="detail-section" role="tabpanel">
          <h3>Azioni Correlate</h3>
          <div className="table-wrapper">
            <table className="practices-table">
              <thead>
                <tr>
                  <th>Data/Ora</th>
                  <th>Azione</th>
                  <th>Stato</th>
                  <th>Attore</th>
                  <th>Note</th>
                </tr>
              </thead>
              <tbody>
                {relatedActions.length === 0 ? (
                  <tr>
                    <td className="empty-row" colSpan={5}>
                      {loading ? 'Caricamento azioni correlate...' : 'Nessuna azione correlata disponibile.'}
                    </td>
                  </tr>
                ) : (
                  relatedActions.map((item, index) => (
                    <tr key={item.actionId ?? `${item.actionType}-${item.createdAt}-${index}`}>
                      <td>{formatDateTime(item.createdAt ?? item.occurredAt ?? item.at)}</td>
                      <td>{item.actionType ?? item.action ?? '-'}</td>
                      <td>{item.status ?? '-'}</td>
                      <td>{item.actor ?? item.performedBy ?? '-'}</td>
                      <td>{item.note ?? item.description ?? '-'}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>
      ) : null}
    </section>
  );
}
