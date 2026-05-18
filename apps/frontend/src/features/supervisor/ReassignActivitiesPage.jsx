import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { supervisionTasksApi } from '../../core/api/supervisionTasksApi';

const initialFilters = {
  practiceNumber: '',
  assignmentDate: '',
  owner: '',
  assignee: ''
};

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

export function ReassignActivitiesPage() {
  const [tasks, setTasks] = useState([]);
  const [filtersDraft, setFiltersDraft] = useState(initialFilters);
  const [filtersApplied, setFiltersApplied] = useState(initialFilters);
  const [assigneeDraftByTaskId, setAssigneeDraftByTaskId] = useState({});

  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [infoMessage, setInfoMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');
  const [reassigningTaskId, setReassigningTaskId] = useState('');

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

  const onChangeAssigneeDraft = (taskId, value) => {
    setAssigneeDraftByTaskId((prev) => ({ ...prev, [taskId]: value }));
  };

  const onReassignGroup = async (taskId) => {
    if (!taskId || reassigningTaskId) {
      return;
    }

    setReassigningTaskId(taskId);
    setErrorMessage('');
    setSuccessMessage('');

    try {
      await supervisionTasksApi.reassignGroup(taskId);
      setSuccessMessage('Riassegnazione a Gruppo Operatore ANC completata.');
      await loadTasks(filtersApplied);
    } catch (error) {
      setErrorMessage(error?.message ?? 'Errore durante la riassegnazione a gruppo.');
    } finally {
      setReassigningTaskId('');
    }
  };

  const onReassignUser = async (taskId) => {
    if (!taskId || reassigningTaskId) {
      return;
    }

    const username = String(assigneeDraftByTaskId[taskId] ?? '').trim();
    if (!username) {
      setErrorMessage('Indicare un utente destinatario per la riassegnazione specifica.');
      setSuccessMessage('');
      return;
    }

    setReassigningTaskId(taskId);
    setErrorMessage('');
    setSuccessMessage('');

    try {
      await supervisionTasksApi.reassignUser(taskId, username);
      setSuccessMessage(`Riassegnazione completata verso utente ${username}.`);
      await loadTasks(filtersApplied);
    } catch (error) {
      setErrorMessage(error?.message ?? 'Errore durante la riassegnazione a utente.');
    } finally {
      setReassigningTaskId('');
    }
  };

  return (
    <section className="panel">
      <h2>Riassegna Attivita</h2>
      <p className="panel-note">Sprint 7: supervisione task ANC con azioni di riassegnazione a gruppo o utente.</p>

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
            Owner
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
            <input
              type="text"
              name="assignee"
              value={filtersDraft.assignee}
              onChange={onChangeFilter}
              placeholder="Es. operatore.destinatario"
            />
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
        <div className="panel-note">Task supervisione trovati: {tasks.length}</div>
      </div>

      {infoMessage ? <div className="info-box">{infoMessage}</div> : null}
      {successMessage ? <div className="api-success-box">{successMessage}</div> : null}
      {errorMessage ? <div className="api-error-box">{errorMessage}</div> : null}

      <div className="table-wrapper">
        <table className="practices-table">
          <thead>
            <tr>
              <th>Attivita</th>
              <th>Pratica N.</th>
              <th>Data Assegnazione</th>
              <th>Owner</th>
              <th>Assegnatario</th>
              <th>Stato task</th>
              <th>Azione</th>
            </tr>
          </thead>
          <tbody>
            {tasks.length === 0 ? (
              <tr>
                <td className="empty-row" colSpan={7}>
                  {loading ? 'Caricamento task supervisione in corso...' : 'Nessun task disponibile con i filtri impostati.'}
                </td>
              </tr>
            ) : (
              tasks.map((task) => {
                const taskId = task.taskId;
                const isReassigning = reassigningTaskId === taskId;

                return (
                  <tr key={taskId || `${task.practiceId}-${task.assignmentDate}`}>
                    <td>{task.activityLabel ?? 'Task ANC'}</td>
                    <td>
                      {task.practiceId ? (
                        <Link className="table-link" to={`/pratiche/${task.practiceId}`}>
                          {task.practiceNumber ?? '-'}
                        </Link>
                      ) : (
                        task.practiceNumber ?? '-'
                      )}
                    </td>
                    <td>{formatDateTime(task.assignmentDate)}</td>
                    <td>{task.owner ?? '-'}</td>
                    <td>{task.assignee ?? '-'}</td>
                    <td>{task.taskState ?? '-'}</td>
                    <td>
                      <div className="reassign-actions">
                        <button
                          type="button"
                          className="btn btn-outline btn-small"
                          onClick={() => onReassignGroup(taskId)}
                          disabled={!taskId || Boolean(reassigningTaskId)}
                        >
                          {isReassigning ? 'IN CORSO...' : 'RIASSEGNA A GRUPPO'}
                        </button>

                        <div className="inline-user-reassign">
                          <input
                            type="text"
                            aria-label={`Utente riassegnazione ${taskId}`}
                            placeholder="utente.destinatario"
                            value={assigneeDraftByTaskId[taskId] ?? ''}
                            onChange={(event) => onChangeAssigneeDraft(taskId, event.target.value)}
                            disabled={!taskId || Boolean(reassigningTaskId)}
                          />
                          <button
                            type="button"
                            className="btn btn-primary btn-small"
                            onClick={() => onReassignUser(taskId)}
                            disabled={!taskId || Boolean(reassigningTaskId)}
                          >
                            RIASSEGNA A UTENTE
                          </button>
                        </div>
                      </div>
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
