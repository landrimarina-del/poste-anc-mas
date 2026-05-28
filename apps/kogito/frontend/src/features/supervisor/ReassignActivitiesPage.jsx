import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { supervisionTasksApi } from '../../core/api/supervisionTasksApi';

const GROUP_OPTIONS = [
  { value: '', label: 'Tutti' },
  { value: 'GRUPPO_OPERATORE_ANC', label: 'Gruppo Operatori ANC' },
  { value: 'GRUPPO_SUPERVISORE_ANC', label: 'Gruppo Supervisori ANC' },
];

const initialFilters = {
  practiceNumber: '',
  assignmentDate: '',
  owner: '',
  assigneeGroup: ''
};

function formatDateTime(value) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return new Intl.DateTimeFormat('it-IT', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit'
  }).format(date);
}

export function ReassignActivitiesPage() {
  const [tasks, setTasks] = useState([]);
  const [filtersDraft, setFiltersDraft] = useState(initialFilters);
  const [filtersApplied, setFiltersApplied] = useState(initialFilters);
  const [selectedTaskIds, setSelectedTaskIds] = useState([]);
  const [reassignType, setReassignType] = useState('GRUPPO');
  const [targetUser, setTargetUser] = useState('');

  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [infoMessage, setInfoMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [reassigning, setReassigning] = useState(false);
  const [reassigningTaskId, setReassigningTaskId] = useState(null);
  const [operators, setOperators] = useState([]);

  const loadTasks = useCallback(async (filters = filtersApplied) => {
    setLoading(true);
    setErrorMessage('');

    try {
      const response = await supervisionTasksApi.list(filters);
      setTasks(response?.items ?? []);

      if (response?.fallbackMode) {
        setInfoMessage('Endpoint supervisione non disponibile: visualizzazione in modalita fallback compatibile.');
      } else {
        setInfoMessage('');
      }
    } catch (error) {
      setTasks([]);
      setErrorMessage(error?.message ?? 'Errore durante il caricamento dei task di supervisione.');
      setInfoMessage('');
    } finally {
      setLoading(false);
    }
  }, [filtersApplied]);

  useEffect(() => {
    loadTasks(filtersApplied);
  }, [filtersApplied, loadTasks]);

  useEffect(() => {
    supervisionTasksApi.listOperators()
      .then((result) => {
        const list = Array.isArray(result) ? result : Array.isArray(result?.items) ? result.items : [];
        setOperators(list);
        if (list.length > 0) setTargetUser(list[0]);
      })
      .catch(() => setOperators([]));
  }, []);

  const onChangeFilter = (event) => {
    const { name, value } = event.target;
    setFiltersDraft((prev) => ({ ...prev, [name]: value }));
  };

  const onApplyFilters = () => {
    setSuccessMessage('');
    setFiltersApplied(filtersDraft);
  };

  const onResetFilters = () => {
    setSuccessMessage('');
    setFiltersDraft(initialFilters);
    setFiltersApplied(initialFilters);
  };

  const onRefresh = () => {
    setSuccessMessage('');
    loadTasks(filtersApplied);
  };

  const onToggleSelectTask = (taskId) => {
    setSelectedTaskIds((prev) =>
      prev.includes(taskId) ? prev.filter((id) => id !== taskId) : [...prev, taskId]
    );
  };

  const onToggleSelectAll = () => {
    if (selectedTaskIds.length === tasks.length) {
      setSelectedTaskIds([]);
    } else {
      setSelectedTaskIds(tasks.map((t) => t.taskId).filter(Boolean));
    }
  };

  const onReassign = async () => {
    if (selectedTaskIds.length === 0 || reassigning) return;
    if (reassignType === 'UTENTE' && !targetUser.trim()) {
      setErrorMessage('Indicare un utente destinatario per la riassegnazione.');
      return;
    }
    setReassigning(true);
    setErrorMessage('');
    setSuccessMessage('');
    const errors = [];
    for (const taskId of selectedTaskIds) {
      try {
        if (reassignType === 'GRUPPO') {
          await supervisionTasksApi.reassignGroup(taskId);
        } else {
          await supervisionTasksApi.reassignUser(taskId, targetUser.trim());
        }
      } catch (err) {
        errors.push(`${taskId}: ${err?.message ?? 'Errore'}`);
      }
    }
    setReassigning(false);
    setSelectedTaskIds([]);
    if (errors.length) {
      setErrorMessage(`Riassegnazione completata con errori: ${errors.join('; ')}`);
    } else {
      setSuccessMessage(
        reassignType === 'GRUPPO'
          ? `${selectedTaskIds.length} task riassegnati al Gruppo Operatore ANC.`
          : `${selectedTaskIds.length} task riassegnati all'utente ${targetUser.trim()}.`
      );
    }
    await loadTasks(filtersApplied);
  };

  const allSelected = tasks.length > 0 && selectedTaskIds.length === tasks.length;

  return (
    <section className="panel">
      <h2>Riassegna Attività</h2>

      {/* Box Dettagli riassegnazione */}
      <div className="box-poste reassign-detail-box">
        <h3 className="box-poste-title">Dettagli riassegnazione</h3>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '8px' }}>
          <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
            <input
              type="radio"
              name="reassignType"
              value="GRUPPO"
              checked={reassignType === 'GRUPPO'}
              onChange={() => setReassignType('GRUPPO')}
              disabled={reassigning}
            />
            Riassegna al Gruppo Operatore
          </label>
          <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
            <input
              type="radio"
              name="reassignType"
              value="UTENTE"
              checked={reassignType === 'UTENTE'}
              onChange={() => setReassignType('UTENTE')}
              disabled={reassigning}
            />
            Riassegna a Utente
          </label>
          {reassignType === 'UTENTE' && (
            <div style={{ marginTop: '4px', marginLeft: '24px' }}>
              <label style={{ display: 'flex', flexDirection: 'column', gap: '4px', fontWeight: 500 }}>
                Operatore destinatario
                <select
                  value={targetUser}
                  onChange={(e) => setTargetUser(e.target.value)}
                  disabled={reassigning || operators.length === 0}
                  style={{ minWidth: '220px' }}
                >
                  {operators.length === 0 && <option value="">Caricamento...</option>}
                  {operators.map((op) => (
                    <option key={op} value={op}>{op}</option>
                  ))}
                </select>
              </label>
            </div>
          )}
        </div>
      </div>

      <div className="filters-box" aria-label="Filtri riassegna attivita">
        <div className="filters-grid">
          <label>
            Pratica N°
            <input
              type="text"
              name="practiceNumber"
              value={filtersDraft.practiceNumber}
              onChange={onChangeFilter}
              placeholder="Es. ANC-2026-0001"
            />
          </label>

          <label>
            Data Assegnazione
            <input
              type="date"
              name="assignmentDate"
              value={filtersDraft.assignmentDate}
              onChange={onChangeFilter}
            />
          </label>

          <label>
            Utente in carico
            <input
              type="text"
              name="owner"
              value={filtersDraft.owner}
              onChange={onChangeFilter}
              placeholder="Es. operatore.anc"
            />
          </label>

          <label>
            Assegnatario
            <select
              name="assigneeGroup"
              value={filtersDraft.assigneeGroup}
              onChange={onChangeFilter}
            >
              {GROUP_OPTIONS.map((o) => (
                <option key={o.value} value={o.value}>{o.label}</option>
              ))}
            </select>
          </label>
        </div>

        <div className="filters-actions">
          <button
            type="button"
            className="btn btn-primary btn-small"
            onClick={onApplyFilters}
            disabled={loading || Boolean(reassigningTaskId)}
          >
            Applica filtri
          </button>
          <button
            type="button"
            className="btn btn-outline btn-small"
            onClick={onResetFilters}
            disabled={loading || Boolean(reassigningTaskId)}
          >
            Cancella filtri
          </button>
          <button
            type="button"
            className="btn btn-outline btn-small"
            onClick={onRefresh}
            disabled={loading || Boolean(reassigningTaskId)}
          >
            {loading ? 'Caricamento...' : 'Aggiorna'}
          </button>
        </div>
      </div>

      <div className="practices-toolbar">
        <div className="panel-note">Task trovati: {tasks.length} — Selezionati: {selectedTaskIds.length}</div>
        <button
          type="button"
          className="btn btn-primary"
          onClick={onReassign}
          disabled={selectedTaskIds.length === 0 || reassigning}
        >
          {reassigning ? 'RIASSEGNAZIONE IN CORSO...' : 'RIASSEGNA'}
        </button>
      </div>

      {infoMessage ? <div className="info-box">{infoMessage}</div> : null}
      {successMessage ? <div className="api-success-box">{successMessage}</div> : null}
      {errorMessage ? <div className="api-error-box">{errorMessage}</div> : null}

      <div className="table-wrapper">
        <table className="practices-table">
          <thead>
            <tr>
              <th>
                <input
                  type="checkbox"
                  aria-label="Seleziona tutti"
                  checked={allSelected}
                  onChange={onToggleSelectAll}
                  disabled={tasks.length === 0 || reassigning}
                />
              </th>
              <th>Pratica N.</th>
              <th>Attività</th>
              <th>Assegnatario</th>
              <th>Utente in carico</th>
              <th>Data Assegnazione</th>
              <th>Data Presa in carico</th>
              <th>Stato Pratica</th>
            </tr>
          </thead>
          <tbody>
            {tasks.length === 0 ? (
              <tr>
                <td className="empty-row" colSpan={8}>
                  {loading ? 'Caricamento task supervisione in corso...' : 'Nessun task disponibile con i filtri impostati.'}
                </td>
              </tr>
            ) : (
              tasks.map((task) => {
                const taskId = task.taskId;
                const isSelected = selectedTaskIds.includes(taskId);

                return (
                  <tr
                    key={taskId || `${task.practiceId}-${task.assignmentDate}`}
                    className={isSelected ? 'row-selected' : ''}
                  >
                    <td>
                      <input
                        type="checkbox"
                        aria-label={`Seleziona task ${taskId}`}
                        checked={isSelected}
                        onChange={() => onToggleSelectTask(taskId)}
                        disabled={!taskId || reassigning}
                      />
                    </td>
                    <td>
                      {task.practiceId ? (
                        <Link className="table-link" to={`/pratiche/${task.practiceId}`}>
                          {task.practiceNumber ?? '-'}
                        </Link>
                      ) : (
                        task.practiceNumber ?? '-'
                      )}
                    </td>
                    <td>{task.activityLabel ?? task.activitylabel ?? 'Task ANC'}</td>
                    <td>{task.groupName ?? '-'}</td>
                    <td>{task.owner ?? task.ownerUsername ?? '-'}</td>
                    <td>{formatDateTime(task.assignmentDate)}</td>
                    <td>{formatDateTime(task.acceptedAt ?? task.accepted_at)}</td>
                    <td>
                      {task.taskState === 'IN_CARICO'
                        ? <span className="badge-stato badge-stato-in-carico" title="In lavorazione">✓</span>
                        : task.taskState === 'IN_CODA'
                          ? <span className="badge-stato badge-stato-in-coda" title="In coda">✓</span>
                          : <span className="badge-stato" title={task.taskState ?? '-'}>—</span>
                      }
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}
