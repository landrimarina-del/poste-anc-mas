import { useCallback, useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useAuth } from '../../app/auth/AuthContext';
import { signalsApi } from '../../core/api/signalsApi';

// Demo data per tab attive/storico
const DEMO_ATTIVE = [
  { id: 'SIG-001', practiceNumber: 'ANC-2026-0001', state: 'IN_LAVORAZIONE', operator: 'operatore.anc', title: 'Anomalia login', sinergiaTicketId: '-', createdAt: '2026-05-10T09:00', updatedAt: '2026-05-10T10:00' },
  { id: 'SIG-002', practiceNumber: 'ANC-2026-0002', state: 'IN_CODA', operator: 'operatore.anc', title: 'Errore invio', sinergiaTicketId: '-', createdAt: '2026-05-11T09:00', updatedAt: '2026-05-11T10:00' }
];
const DEMO_STORICO = [
  { id: 'SIG-003', practiceNumber: 'ANC-2026-0003', state: 'CHIUSO', operator: 'operatore.anc', title: 'Test chiusura', sinergiaTicketId: 'TCK-123', createdAt: '2026-04-10T09:00', updatedAt: '2026-04-11T10:00' }
];

const initialCreateForm = {
  practiceId: '',
  practiceNumber: '',
  subject: '',
  description: ''
};

const initialGlobalFilters = {
  signalId: '',
  state: '',
  operator: '',
  fromDate: '',
  toDate: ''
};

const signalStateOptions = ['', 'IN_CODA', 'IN_LAVORAZIONE', 'CHIUSO'];

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

function buildCreatePayload(form) {
  return {
    practiceId: Number.parseInt(form.practiceId.trim(), 10),
    subject: form.subject.trim(),
    description: form.description.trim()
  };
}

function SignalsTable({ title, rows, emptyMessage, loading, canManage, onForward, onReassign }) {
  return (
    <article className="signals-block">
      <div className="signals-block-header">
        <h3>{title}</h3>
        <span className="panel-note">Totale: {rows.length}</span>
      </div>

      <div className="table-wrapper">
        <table className="practices-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Pratica N.</th>
              <th>Stato</th>
              <th>Operatore</th>
              <th>Titolo</th>
              <th>Ticket Sinergia</th>
              <th>Creata il</th>
              <th>Ultima modifica</th>
              <th>Azione</th>
            </tr>
          </thead>
          <tbody>
            {rows.length === 0 ? (
              <tr>
                <td className="empty-row" colSpan={9}>
                  {loading ? 'Caricamento segnalazioni...' : emptyMessage}
                </td>
              </tr>
            ) : (
              rows.map((item) => (
                <tr key={item.id || `${item.practiceId}-${item.createdAt}`}>
                  <td>{item.id || '-'}</td>
                  <td>{item.practiceNumber || '-'}</td>
                  <td>{item.state || '-'}</td>
                  <td>{item.operator || '-'}</td>
                  <td>{item.title || '-'}</td>
                  <td>{item.sinergiaTicketId || '-'}</td>
                  <td>{formatDateTime(item.createdAt)}</td>
                  <td>{formatDateTime(item.updatedAt)}</td>
                  <td>
                    <div className="signals-actions-cell">
                      <button
                        type="button"
                        className="btn btn-outline btn-small"
                        onClick={() => onForward(item.id)}
                        disabled={!item.id}
                      >
                        FORWARD SINERGIA
                      </button>
                      {canManage ? (
                        <button
                          type="button"
                          className="btn btn-primary btn-small"
                          onClick={() => onReassign(item.id)}
                          disabled={!item.id || item.state === 'CHIUSO'}
                        >
                          RIASSEGNA
                        </button>
                      ) : null}
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </article>
  );
}

export function SignalsDashboardPage() {
  const { isSupervisore } = useAuth();
  const [searchParams] = useSearchParams();

  const [createForm, setCreateForm] = useState(initialCreateForm);
  const [globalFiltersDraft, setGlobalFiltersDraft] = useState(initialGlobalFilters);
  const [globalFiltersApplied, setGlobalFiltersApplied] = useState(initialGlobalFilters);

  const [loadingMySignals, setLoadingMySignals] = useState(true);
  const [loadingGlobalSignals, setLoadingGlobalSignals] = useState(false);
  const [submittingCreate, setSubmittingCreate] = useState(false);
  const [workingSignalId, setWorkingSignalId] = useState('');

  const [mySignals, setMySignals] = useState([]);
  const [globalSignals, setGlobalSignals] = useState([]);
  // Stato tab demo
  const [activeTab, setActiveTab] = useState('attive'); // 'attive' | 'storico'
  const [demoFilters, setDemoFilters] = useState({ state: '', operator: '', signalId: '' });

  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    const practiceId = searchParams.get('practiceId') ?? '';
    const practiceNumber = searchParams.get('practiceNumber') ?? '';

    if (!practiceId && !practiceNumber) {
      return;
    }

    setCreateForm((prev) => ({
      ...prev,
      practiceId: practiceId || prev.practiceId,
      practiceNumber: practiceNumber || prev.practiceNumber
    }));
  }, [searchParams]);

  const loadMySignals = useCallback(async () => {
    setLoadingMySignals(true);
    setErrorMessage('');

    try {
      const response = await signalsApi.my();
      setMySignals(Array.isArray(response) ? response : []);
    } catch (error) {
      setMySignals([]);
      setErrorMessage(error?.message ?? 'Errore durante il caricamento di Le Mie Segnalazioni.');
    } finally {
      setLoadingMySignals(false);
    }
  }, []);

  const loadGlobalSignals = useCallback(async (filters = globalFiltersApplied) => {
    if (!isSupervisore) {
      return;
    }

    setLoadingGlobalSignals(true);
    setErrorMessage('');

    try {
      const response = await signalsApi.list(filters);
      setGlobalSignals(Array.isArray(response) ? response : []);
    } catch (error) {
      setGlobalSignals([]);
      setErrorMessage(error?.message ?? 'Errore durante il caricamento della vista globale segnalazioni.');
    } finally {
      setLoadingGlobalSignals(false);
    }
  }, [globalFiltersApplied, isSupervisore]);

  useEffect(() => {
    loadMySignals();
  }, [loadMySignals]);

  useEffect(() => {
    loadGlobalSignals(globalFiltersApplied);
  }, [globalFiltersApplied, loadGlobalSignals]);

  const onChangeCreateField = (event) => {
    const { name, value } = event.target;
    setCreateForm((prev) => ({ ...prev, [name]: value }));
  };

  const onSubmitCreate = async (event) => {
    event.preventDefault();

    const practiceIdNormalized = createForm.practiceId.trim();

    if (!practiceIdNormalized) {
      setErrorMessage('Il campo Practice ID e\' obbligatorio per inviare una segnalazione.');
      setSuccessMessage('');
      return;
    }

    if (!/^\d+$/.test(practiceIdNormalized)) {
      setErrorMessage('Il campo Practice ID deve contenere un valore numerico intero.');
      setSuccessMessage('');
      return;
    }

    if (!createForm.subject.trim()) {
      setErrorMessage('Il titolo della segnalazione e\' obbligatorio.');
      setSuccessMessage('');
      return;
    }

    setSubmittingCreate(true);
    setErrorMessage('');
    setSuccessMessage('');

    try {
      const created = await signalsApi.create(buildCreatePayload(createForm));
      setSuccessMessage(`Segnalazione ${created?.id ?? ''} inviata correttamente.`.trim());
      setCreateForm((prev) => ({ ...prev, subject: '', description: '' }));
      await loadMySignals();
      await loadGlobalSignals(globalFiltersApplied);
    } catch (error) {
      setErrorMessage(error?.message ?? 'Errore durante l\'invio della segnalazione.');
    } finally {
      setSubmittingCreate(false);
    }
  };

  const onForwardToSinergia = async (signalId) => {
    if (!signalId || workingSignalId) {
      return;
    }

    setWorkingSignalId(signalId);
    setErrorMessage('');
    setSuccessMessage('');

    try {
      const forwarded = await signalsApi.forwardToSinergia(signalId);
      const ticketInfo = forwarded?.sinergiaTicketId
        ? ` Ticket: ${forwarded.sinergiaTicketId}.`
        : '';
      setSuccessMessage(`Forward verso stub Sinergia completato.${ticketInfo}`);
      await loadMySignals();
      await loadGlobalSignals(globalFiltersApplied);
    } catch (error) {
      setErrorMessage(error?.message ?? 'Errore durante il forward verso Sinergia.');
    } finally {
      setWorkingSignalId('');
    }
  };

  const onReassignSignal = async (signalId) => {
    if (!signalId || workingSignalId || !isSupervisore) {
      return;
    }

    const targetType = window.prompt('Tipo riassegnazione: USER, GROUP o ME', 'USER');
    if (!targetType) {
      return;
    }

    const upperTargetType = targetType.toUpperCase();

    if (!['USER', 'GROUP', 'ME'].includes(upperTargetType)) {
      setErrorMessage('Tipo riassegnazione non valido. Valori ammessi: USER, GROUP, ME.');
      setSuccessMessage('');
      return;
    }

    let username;

    if (upperTargetType === 'USER') {
      const operator = window.prompt('Inserisci username operatore destinatario', 'operatore.anc');
      if (!operator?.trim()) {
        return;
      }
      username = operator.trim();
    }

    const reasonRaw = window.prompt('Motivazione riassegnazione (opzionale)', '');
    const reason = (reasonRaw ?? '').trim();

    const reassignPayload = {
      targetType: upperTargetType
    };

    if (upperTargetType === 'USER') {
      reassignPayload.username = username;
    }

    if (reason) {
      reassignPayload.reason = reason;
    }

    setWorkingSignalId(signalId);
    setErrorMessage('');
    setSuccessMessage('');

    try {
      await signalsApi.reassign(signalId, reassignPayload);

      setSuccessMessage('Riassegnazione segnalazione completata.');
      await loadMySignals();
      await loadGlobalSignals(globalFiltersApplied);
    } catch (error) {
      setErrorMessage(error?.message ?? 'Errore durante la riassegnazione segnalazione.');
    } finally {
      setWorkingSignalId('');
    }
  };

  const onChangeGlobalFilter = (event) => {
    const { name, value } = event.target;
    setGlobalFiltersDraft((prev) => ({ ...prev, [name]: value }));
  };

  const onApplyGlobalFilters = () => {
    setGlobalFiltersApplied(globalFiltersDraft);
  };

  const onResetGlobalFilters = () => {
    setGlobalFiltersDraft(initialGlobalFilters);
    setGlobalFiltersApplied(initialGlobalFilters);
  };

  return (
    <section className="panel">
      <h2>Dashboard Segnalazioni</h2>
      <p className="panel-note">
        Sprint 9 (EPIC E10): invio segnalazioni ANC, vista Le Mie Segnalazioni e gestione supervisore.<br />
        <b>Nota:</b> i dati delle tabelle sottostanti sono dimostrativi e non sincronizzati con il sistema esterno.
      </p>

      {successMessage ? <div className="api-success-box">{successMessage}</div> : null}
      {errorMessage ? <div className="api-error-box">{errorMessage}</div> : null}

      <article className="signals-block">
        <h3>Invio segnalazione da contesto pratica</h3>
        <form className="signals-form-grid" onSubmit={onSubmitCreate}>
          <label>
            Practice ID
            <input
              type="text"
              inputMode="numeric"
              name="practiceId"
              value={createForm.practiceId}
              onChange={onChangeCreateField}
              placeholder="Es. 123456"
            />
          </label>

          <label>
            Pratica N°
            <input
              type="text"
              name="practiceNumber"
              value={createForm.practiceNumber}
              onChange={onChangeCreateField}
              placeholder="Es. ANC-2026-0001"
            />
          </label>

          <label>
            Titolo
            <input
              type="text"
              name="subject"
              value={createForm.subject}
              onChange={onChangeCreateField}
              placeholder="Sintesi anomalia"
            />
          </label>

          <label className="signals-form-description">
            Descrizione
            <textarea
              name="description"
              rows={3}
              value={createForm.description}
              onChange={onChangeCreateField}
              placeholder="Dettaglio operativo della segnalazione"
            />
          </label>

          <div className="signals-form-actions">
            <button
              type="submit"
              className="btn btn-primary"
              disabled={submittingCreate || Boolean(workingSignalId)}
            >
              {submittingCreate ? 'INVIO IN CORSO...' : 'INVIA SEGNALAZIONE'}
            </button>
          </div>
        </form>
      </article>

      {/* TAB DEMO */}
      <div className="signals-tabs-demo">
        <button
          type="button"
          className={activeTab === 'attive' ? 'tab-btn active' : 'tab-btn'}
          onClick={() => setActiveTab('attive')}
        >Visualizza Le Segnalazioni Attive</button>
        <button
          type="button"
          className={activeTab === 'storico' ? 'tab-btn active' : 'tab-btn'}
          onClick={() => setActiveTab('storico')}
        >Visualizza Segnalazioni (Storico)</button>
      </div>

      <div className="filters-box" aria-label="Filtri tab demo segnalazioni">
        <div className="filters-grid">
          <label>
            ID segnalazione
            <input
              type="text"
              name="signalId"
              value={demoFilters.signalId}
              onChange={e => setDemoFilters(f => ({ ...f, signalId: e.target.value }))}
              placeholder="Es. SIG-0001"
            />
          </label>
          <label>
            Stato
            <select name="state" value={demoFilters.state} onChange={e => setDemoFilters(f => ({ ...f, state: e.target.value }))}>
              <option value="">Tutti</option>
              <option value="IN_CODA">IN_CODA</option>
              <option value="IN_LAVORAZIONE">IN_LAVORAZIONE</option>
              <option value="CHIUSO">CHIUSO</option>
            </select>
          </label>
          <label>
            Operatore
            <input
              type="text"
              name="operator"
              value={demoFilters.operator}
              onChange={e => setDemoFilters(f => ({ ...f, operator: e.target.value }))}
              placeholder="Es. operatore.anc"
            />
          </label>
        </div>
        <div className="filters-actions">
          <button type="button" className="btn btn-primary btn-small" onClick={() => setDemoFilters({ ...demoFilters })}>AGGIORNA</button>
          <button type="button" className="btn btn-outline btn-small" onClick={() => setDemoFilters({ state: '', operator: '', signalId: '' })}>CANCELLA FILTRI</button>
        </div>
      </div>

      {/* Tabella demo */}
      <SignalsTable
        title={activeTab === 'attive' ? 'Segnalazioni Attive (DEMO)' : 'Segnalazioni Storico (DEMO)'}
        rows={
          (activeTab === 'attive' ? DEMO_ATTIVE : DEMO_STORICO)
            .filter(row =>
              (!demoFilters.signalId || row.id.includes(demoFilters.signalId)) &&
              (!demoFilters.state || row.state === demoFilters.state) &&
              (!demoFilters.operator || row.operator.includes(demoFilters.operator))
            )
        }
        loading={false}
        emptyMessage="Nessuna segnalazione presente."
        canManage={false}
        onForward={() => {}}
        onReassign={() => {}}
      />
    </section>
  );
}
