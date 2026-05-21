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
  return date.toLocaleString('it-IT');
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

export function PracticeDetailPage() {
  const { practiceId } = useParams();
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState('summary');
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [detail, setDetail] = useState(null);
  const [history, setHistory] = useState([]);
  const [states, setStates] = useState([]);
  const [relatedActions, setRelatedActions] = useState([]);
  const [showResidenceAddress, setShowResidenceAddress] = useState(false);

  const loadDetail = useCallback(async () => {
    if (!practiceId) {
      return;
    }

    setLoading(true);
    setErrorMessage('');

    try {
      const [detailResponse, historyResponse, statesResponse, relatedActionsResponse] = await Promise.all([
        practicesApi.detail(practiceId),
        practicesApi.history(practiceId),
        practicesApi.states(practiceId),
        practicesApi.relatedActions(practiceId)
      ]);

      setDetail(detailResponse);
      setHistory(Array.isArray(historyResponse) ? historyResponse : []);
      setStates(Array.isArray(statesResponse) ? statesResponse : []);
      setRelatedActions(Array.isArray(relatedActionsResponse) ? relatedActionsResponse : []);
    } catch (error) {
      setErrorMessage(error?.message ?? 'Errore durante il caricamento del dettaglio pratica');
      setDetail(null);
      setHistory([]);
      setStates([]);
      setRelatedActions([]);
    } finally {
      setLoading(false);
    }
  }, [practiceId]);

  useEffect(() => {
    loadDetail();
  }, [loadDetail]);

  const header = detail?.header ?? {};
  const client = detail?.client ?? {};
  const blockedCard = detail?.blockedCard ?? {};

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
          <h3>Riepilogo</h3>
          <div className="summary-grid">
            <article className="summary-card">
              <h4>Testata pratica</h4>
              <dl>
                <div><dt>Pratica N.</dt><dd>{header.practiceNumber ?? '-'}</dd></div>
                <div><dt>ID pratica</dt><dd>{header.practiceId ?? '-'}</dd></div>
                <div><dt>Request ID</dt><dd>{header.requestId ?? '-'}</dd></div>
                <div><dt>ID Work Item</dt><dd>{header.idWorkItem ?? '-'}</dd></div>
                <div><dt>Stato</dt><dd>{header.state ?? '-'}</dd></div>
                <div><dt>Esito SD</dt><dd>{header.sdOutcome ?? '-'}</dd></div>
                <div><dt>Data apertura</dt><dd>{formatDateTime(header.openedAt)}</dd></div>
                <div><dt>Ultima modifica</dt><dd>{formatDateTime(header.lastModifiedAt)}</dd></div>
                <div><dt>Data chiusura</dt><dd>{formatDateTime(header.closedAt)}</dd></div>
              </dl>
            </article>

            <article className="summary-card">
              <h4>Dati cliente</h4>
              <dl>
                <div><dt>Nome</dt><dd>{client.firstName ?? '-'}</dd></div>
                <div><dt>Cognome</dt><dd>{client.lastName ?? '-'}</dd></div>
                <div><dt>Codice fiscale</dt><dd>{client.fiscalCode ?? '-'}</dd></div>
                <div><dt>Codice cliente</dt><dd>{client.customerCode ?? '-'}</dd></div>
              </dl>

              <div className="detail-collapse-block">
                <button
                  type="button"
                  className="btn btn-outline btn-small"
                  onClick={() => setShowResidenceAddress((prev) => !prev)}
                >
                  {showResidenceAddress ? 'Comprimi indirizzo residenza' : 'Espandi indirizzo residenza'}
                </button>
                {showResidenceAddress ? (
                  <div className="detail-collapse-content">
                    <div className="panel-note">{formatResidenceAddress(client)}</div>
                  </div>
                ) : null}
              </div>
            </article>

            <article className="summary-card">
              <h4>Dati carta bloccata</h4>
              <dl>
                <div><dt>Numero carta</dt><dd>{blockedCard.cardNumberMasked ?? '-'}</dd></div>
                <div><dt>Tipologia carta</dt><dd>{blockedCard.cardType ?? '-'}</dd></div>
                <div><dt>Intestatario</dt><dd>{blockedCard.cardHolder ?? '-'}</dd></div>
              </dl>
            </article>
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
                  <th>Data/Ora</th>
                  <th>Evento</th>
                  <th>Attore</th>
                  <th>Correlation ID</th>
                  <th>Note</th>
                </tr>
              </thead>
              <tbody>
                {history.length === 0 ? (
                  <tr>
                    <td className="empty-row" colSpan={5}>
                      {loading ? 'Caricamento cronologia...' : 'Nessun evento disponibile.'}
                    </td>
                  </tr>
                ) : (
                  history.map((item) => (
                    <tr key={item.eventId ?? `${item.eventType}-${item.occurredAt}`}>
                      <td>{formatDateTime(item.occurredAt)}</td>
                      <td>{item.eventType ?? '-'}</td>
                      <td>{item.actor ?? '-'}</td>
                      <td>{item.correlationId ?? '-'}</td>
                      <td>{item.note ?? '-'}</td>
                    </tr>
                  ))
                )}
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
                  <th>Data/Ora</th>
                  <th>Attore</th>
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
                      <td>{formatStateDateTime(item.transitionedAt ?? item.dataOra ?? item.at)}</td>
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
