import { useState, useCallback, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { signalsApi } from '../../core/api/signalsApi';

const TABS = [
  { key: 'riassegna', label: 'Riassegna Segnalazioni' },
  { key: 'mie', label: 'Visualizza le mie Segnalazioni' },
  { key: 'tutte', label: 'Segnalazioni' },
];

const STATE_OPTIONS = [
  { value: '', label: 'Tutti' },
  { value: 'IN_CODA', label: 'In Coda' },
  { value: 'IN_LAVORAZIONE', label: 'In Lavorazione' },
];

const GROUP_OPTIONS = [
  { value: '', label: 'Tutti' },
  { value: 'GRUPPO_OPERATORE_ANC', label: 'Gruppo Operatori ANC' },
  { value: 'GRUPPO_SUPERVISORE_ANC', label: 'Gruppo Supervisori ANC' },
];

const initialFilters = {
  id: '',
  practiceNumber: '',
  activityLabel: '',
  assigneeGroup: '',
  operator: '',
  fromDate: '',
  toDate: '',
  state: '',
};

function formatDateTime(value) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('it-IT', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  }).format(date);
}

function StateBadge({ state }) {
  if (state === 'IN_LAVORAZIONE') {
    return <span className="badge-stato badge-stato-in-carico" title="In Lavorazione">✓</span>;
  }
  if (state === 'IN_CODA') {
    return <span className="badge-stato badge-stato-in-coda" title="In Coda">✓</span>;
  }
  return <span className="badge-stato" title={state ?? '-'}>—</span>;
}

function ReassignSignalModal({ signal, onClose, onConfirm, working }) {
  const [targetType, setTargetType] = useState('');
  const [username, setUsername] = useState('');
  const [operators, setOperators] = useState([]);
  const [loadingOps, setLoadingOps] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setLoadingOps(true);
    signalsApi.operators()
      .then((data) => { if (!cancelled) setOperators(data); })
      .catch(() => {})
      .finally(() => { if (!cancelled) setLoadingOps(false); });
    return () => { cancelled = true; };
  }, []);

  const canSubmit = targetType !== '' && !working;

  const handleConfirm = () => {
    if (!canSubmit) return;
    onConfirm({ targetType, username: targetType === 'USER' ? username.trim() : undefined });
  };

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="modal-title">
      <div className="modal-dialog">
        <div className="modal-header-bar" id="modal-title">
          Dettagli riassegnazione
        </div>
        <div className="modal-body">
          <p className="modal-signal-info">
            Segnalazione <strong>#{signal.id}</strong> — Pratica <strong>{signal.practiceNumber}</strong>
          </p>
          <fieldset className="modal-fieldset">
            <legend>Scegli Tipologia di Riassegnazione *</legend>
            <label className="modal-radio-label">
              <input
                type="radio"
                name="targetType"
                value="GROUP"
                checked={targetType === 'GROUP'}
                onChange={() => { setTargetType('GROUP'); setUsername(''); }}
              />
              {' '}Riassegna Attività a Gruppo
            </label>
            <label className="modal-radio-label">
              <input
                type="radio"
                name="targetType"
                value="USER"
                checked={targetType === 'USER'}
                onChange={() => setTargetType('USER')}
              />
              {' '}Riassegna Attività a Utenti
            </label>
            {targetType === 'USER' && (
              <select
                className="modal-username-input"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                disabled={loadingOps}
              >
                <option value="">{loadingOps ? 'Caricamento...' : '-- seleziona utente --'}</option>
                {operators.map((op) => (
                  <option key={op.username} value={op.username}>
                    {op.fullName} ({op.username})
                  </option>
                ))}
              </select>
            )}
            <label className="modal-radio-label">
              <input
                type="radio"
                name="targetType"
                value="ME"
                checked={targetType === 'ME'}
                onChange={() => { setTargetType('ME'); setUsername(''); }}
              />
              {' '}Riassegna Attività a me
            </label>
          </fieldset>
        </div>
        <div className="modal-footer">
          <button type="button" className="btn btn-outline btn-small" onClick={onClose} disabled={working}>
            Annulla
          </button>
          <button
            type="button"
            className="btn btn-primary btn-small"
            onClick={handleConfirm}
            disabled={!canSubmit || (targetType === 'USER' && !username.trim())}
          >
            {working ? 'Riassegnazione...' : 'ASSEGNA'}
          </button>
        </div>
      </div>
    </div>
  );
}

function FiltersBar({ filters, onChange, onApply, onClear, onRefresh, onExit, showOperator = true }) {
  return (
    <div className="filters-box" aria-label="Filtri segnalazioni">
      <div className="filters-grid">
        <label>
          Pratica N.
          <input
            type="text"
            name="practiceNumber"
            value={filters.practiceNumber}
            onChange={onChange}
            placeholder="es. PRAT-2026-001"
          />
        </label>
        <label>
          Id Segnalazione
          <input
            type="text"
            name="id"
            value={filters.id}
            onChange={onChange}
            placeholder="es. 42"
          />
        </label>
        <label>
          Attività Segnalazione
          <input
            type="text"
            name="activityLabel"
            value={filters.activityLabel}
            onChange={onChange}
            placeholder="es. Attivazione Nuova Carta"
          />
        </label>
        {showOperator && (
          <label>
            Assegnatario
            <select name="assigneeGroup" value={filters.assigneeGroup} onChange={onChange}>
              {GROUP_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>
          </label>
        )}
        {showOperator && (
          <label>
            Utente in carico
            <input
              type="text"
              name="operator"
              value={filters.operator}
              onChange={onChange}
              placeholder="username"
            />
          </label>
        )}
        <label>
          Stato
          <select name="state" value={filters.state} onChange={onChange}>
            {STATE_OPTIONS.map((o) => (
              <option key={o.value} value={o.value}>{o.label}</option>
            ))}
          </select>
        </label>
        <label>
          Data Creazione Da
          <input
            type="date"
            name="fromDate"
            value={filters.fromDate}
            onChange={onChange}
          />
        </label>
        <label>
          Data Creazione A
          <input
            type="date"
            name="toDate"
            value={filters.toDate}
            onChange={onChange}
          />
        </label>
      </div>
      <div className="filters-actions">
        <button type="button" className="btn btn-primary btn-small" onClick={onApply}>
          CERCA
        </button>
        <button type="button" className="btn btn-outline btn-small" onClick={onRefresh}>
          AGGIORNA
        </button>
        <button type="button" className="btn btn-outline btn-small" onClick={onClear}>
          CANCELLA FILTRI
        </button>
        <button type="button" className="btn btn-outline btn-small" onClick={onExit}>
          ESCI
        </button>
      </div>
    </div>
  );
}

function SignalsTable({ rows, loading, canReassign, onSignalClick }) {
  return (
    <div className="table-wrapper">
      <table className="practices-table">
        <thead>
          <tr>
            <th>Id Segnalazione</th>
            <th>Pratica N.</th>
            <th>Attività</th>
            <th>Assegnatario</th>
            <th>Utente in carico</th>
            <th>Data Presa in Carico</th>
            <th>Data Creazione</th>
            <th>Stato</th>
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr>
              <td className="empty-row" colSpan={8}>
                {loading ? 'Caricamento segnalazioni...' : 'Nessuna segnalazione trovata.'}
              </td>
            </tr>
          ) : (
            rows.map((item) => (
              <tr key={item.id || `${item.practiceId}-${item.createdAt}`}>
                <td>
                  {canReassign && item.state !== 'CHIUSO' ? (
                    <button
                      type="button"
                      className="table-link btn-link"
                      onClick={() => onSignalClick(item)}
                    >
                      {item.id || '-'}
                    </button>
                  ) : (
                    item.id || '-'
                  )}
                </td>
                <td>
                  {item.practiceId ? (
                    <Link className="table-link" to={`/pratiche/${item.practiceId}`}>
                      {item.practiceNumber}
                    </Link>
                  ) : (
                    item.practiceNumber
                  )}
                </td>
                <td>{item.activityLabel || item.title || '-'}</td>
                <td>{item.groupName || '-'}</td>
                <td>{item.operator || '-'}</td>
                <td>{formatDateTime(item.acceptedAt)}</td>
                <td>{formatDateTime(item.createdAt)}</td>
                <td><StateBadge state={item.state} /></td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}

export function SupervisionSignalsPage() {
  const navigate = useNavigate();

  const [activeTab, setActiveTab] = useState('riassegna');
  const [filtersDraft, setFiltersDraft] = useState(initialFilters);
  const [filtersApplied, setFiltersApplied] = useState(initialFilters);

  const [signals, setSignals] = useState([]);
  const [mySignals, setMySignals] = useState([]);
  const [loading, setLoading] = useState(false);

  const [reassignTarget, setReassignTarget] = useState(null);
  const [reassigning, setReassigning] = useState(false);

  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const buildApiParams = useCallback((filters) => {
    const p = {};
    if (filters.id) p.id = filters.id;
    if (filters.practiceNumber) p.practiceNumber = filters.practiceNumber;
    if (filters.activityLabel) p.activityLabel = filters.activityLabel;
    if (filters.assigneeGroup) p.assigneeGroup = filters.assigneeGroup;
    if (filters.operator) p.operator = filters.operator;
    if (filters.state) p.state = filters.state;
    if (filters.fromDate) p.fromDate = filters.fromDate;
    if (filters.toDate) p.toDate = filters.toDate;
    return p;
  }, []);

  const loadSignals = useCallback(async (filters) => {
    setLoading(true);
    setErrorMessage('');
    try {
      const data = await signalsApi.list(buildApiParams(filters));
      setSignals(Array.isArray(data) ? data : []);
    } catch (e) {
      setErrorMessage(e?.message ?? 'Errore nel caricamento delle segnalazioni.');
      setSignals([]);
    } finally {
      setLoading(false);
    }
  }, [buildApiParams]);

  const loadMySignals = useCallback(async (filters) => {
    setLoading(true);
    setErrorMessage('');
    try {
      const data = await signalsApi.my(buildApiParams(filters));
      setMySignals(Array.isArray(data) ? data : []);
    } catch (e) {
      setErrorMessage(e?.message ?? 'Errore nel caricamento delle segnalazioni personali.');
      setMySignals([]);
    } finally {
      setLoading(false);
    }
  }, [buildApiParams]);

  useEffect(() => {
    if (activeTab === 'mie') {
      loadMySignals(filtersApplied);
    } else {
      loadSignals(filtersApplied);
    }
  }, [activeTab, filtersApplied, loadSignals, loadMySignals]);

  const onChangeFilter = (e) => {
    const { name, value } = e.target;
    setFiltersDraft((prev) => ({ ...prev, [name]: value }));
  };

  const onApplyFilters = () => {
    setFiltersApplied({ ...filtersDraft });
  };

  const onClearFilters = () => {
    setFiltersDraft(initialFilters);
    setFiltersApplied(initialFilters);
  };

  const onRefresh = () => {
    if (activeTab === 'mie') {
      loadMySignals(filtersApplied);
    } else {
      loadSignals(filtersApplied);
    }
  };

  const onExit = () => navigate('/home');

  const onSignalClick = (signal) => {
    setSuccessMessage('');
    setErrorMessage('');
    setReassignTarget(signal);
  };

  const onConfirmReassign = async ({ targetType, username }) => {
    if (!reassignTarget) return;
    setReassigning(true);
    setErrorMessage('');
    try {
      await signalsApi.reassign(reassignTarget.id, { targetType, username });
      setSuccessMessage(`Segnalazione #${reassignTarget.id} riassegnata con successo.`);
      setReassignTarget(null);
      await loadSignals(filtersApplied);
    } catch (e) {
      setErrorMessage(e?.message ?? 'Errore durante la riassegnazione.');
    } finally {
      setReassigning(false);
    }
  };

  const currentRows = activeTab === 'mie' ? mySignals : signals;

  return (
    <div className="page-container">
      <h2>Segnalazioni Supervisore</h2>

      {errorMessage && (
        <div className="alert alert-error" role="alert">{errorMessage}</div>
      )}
      {successMessage && (
        <div className="alert alert-success" role="status">{successMessage}</div>
      )}

      <nav className="page-tab-bar" role="tablist" aria-label="Sezioni segnalazioni">
        {TABS.map((tab) => (
          <button
            key={tab.key}
            type="button"
            role="tab"
            aria-selected={activeTab === tab.key}
            className={`page-tab-btn ${activeTab === tab.key ? 'active' : ''}`}
            onClick={() => {
              setActiveTab(tab.key);
              setFiltersDraft(initialFilters);
              setFiltersApplied(initialFilters);
              setErrorMessage('');
              setSuccessMessage('');
            }}
          >
            {tab.label}
          </button>
        ))}
      </nav>

      <div role="tabpanel">
        <FiltersBar
          filters={filtersDraft}
          onChange={onChangeFilter}
          onApply={onApplyFilters}
          onClear={onClearFilters}
          onRefresh={onRefresh}
          onExit={onExit}
          showOperator={activeTab !== 'mie'}
        />

        <div className="table-panel">
          <div className="table-panel-header">
            <span className="panel-note">Totale: {currentRows.length}</span>
          </div>
          <SignalsTable
            rows={currentRows}
            loading={loading}
            canReassign={activeTab === 'riassegna'}
            onSignalClick={onSignalClick}
          />
        </div>
      </div>

      {reassignTarget && (
        <ReassignSignalModal
          signal={reassignTarget}
          onClose={() => setReassignTarget(null)}
          onConfirm={onConfirmReassign}
          working={reassigning}
        />
      )}
    </div>
  );
}
