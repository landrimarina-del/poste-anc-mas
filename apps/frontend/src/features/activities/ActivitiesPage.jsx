import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { tasksApi } from '../../core/api/tasksApi';

const initialFilters = {
  practiceNumber: '',
  taskState: ''
};

const taskStateOptions = [
  { value: '', label: 'Tutti' },
  { value: 'IN_CODA', label: 'In coda' },
  { value: 'IN_CARICO', label: 'In carico' }
];

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

function getItemsFromResponse(result) {
  if (Array.isArray(result)) {
    return result;
  }
  if (Array.isArray(result?.items)) {
    return result.items;
  }
  return [];
}

export function ActivitiesPage() {
  const navigate = useNavigate();
  const [activities, setActivities] = useState([]);
  const [filtersDraft, setFiltersDraft] = useState(initialFilters);
  const [filtersApplied, setFiltersApplied] = useState(initialFilters);
  const [loading, setLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState('');
  const [acceptingTaskId, setAcceptingTaskId] = useState(null);

  const loadActivities = useCallback(async (filters = filtersApplied) => {
    setLoading(true);
    setErrorMessage('');

    try {
      const result = await tasksApi.list({
        ...filters
      });

      setActivities(getItemsFromResponse(result));
    } catch (error) {
      setActivities([]);
      setErrorMessage(error?.message ?? 'Errore durante il caricamento della lista attivita');
    } finally {
      setLoading(false);
    }
  }, [filtersApplied]);

  useEffect(() => {
    loadActivities(filtersApplied);
  }, [filtersApplied, loadActivities]);

  const onChangeFilter = (event) => {
    const { name, value } = event.target;
    setFiltersDraft((prev) => ({ ...prev, [name]: value }));
  };

  const onApplyFilters = () => {
    setFiltersApplied(filtersDraft);
  };

  const onResetFilters = () => {
    setFiltersDraft(initialFilters);
    setFiltersApplied(initialFilters);
  };

  const onRefresh = () => {
    loadActivities(filtersApplied);
  };

  const onAcceptTask = async (task) => {
    if (!task?.taskId || acceptingTaskId) {
      return;
    }

    setAcceptingTaskId(task.taskId);
    setErrorMessage('');

    try {
      const accepted = await tasksApi.accept(task.taskId);
      const acceptedTask = accepted?.task ?? accepted;
      const targetPracticeId = acceptedTask?.practiceId ?? task.practiceId;

      // Aggiornamento immediato UI: presa in carico task e pratica in lavorazione.
      setActivities((prev) =>
        prev.map((item) => {
          if (item.taskId !== task.taskId) {
            return item;
          }

          return {
            ...item,
            taskState: acceptedTask?.taskState ?? 'IN_CARICO',
            ownerUser: acceptedTask?.ownerUser ?? acceptedTask?.ownerUsername ?? item.ownerUser,
            practiceState: acceptedTask?.practiceState ?? 'IN_LAVORAZIONE'
          };
        })
      );

      await loadActivities(filtersApplied);

      if (targetPracticeId) {
        navigate(`/attivita/${task.taskId}/tipizzazione?practiceId=${encodeURIComponent(targetPracticeId)}`);
      }
    } catch (error) {
      setErrorMessage(error?.message ?? 'Errore durante la presa in carico del task');
    } finally {
      setAcceptingTaskId(null);
    }
  };

  const hasRows = activities.length > 0;

  const availableTaskStateValues = useMemo(
    () => taskStateOptions.map((option) => option.value),
    []
  );

  useEffect(() => {
    if (availableTaskStateValues.includes(filtersDraft.taskState)) {
      return;
    }

    setFiltersDraft((prev) => ({ ...prev, taskState: '' }));
  }, [availableTaskStateValues, filtersDraft.taskState]);

  useEffect(() => {
    if (availableTaskStateValues.includes(filtersApplied.taskState)) {
      return;
    }

    setFiltersApplied((prev) => ({ ...prev, taskState: '' }));
  }, [availableTaskStateValues, filtersApplied.taskState]);

  return (
    <section className="panel">
      <h2>Lista Attivita Operatore</h2>
      <p className="panel-note">Sprint 3: filtri essenziali e presa in carico task (ACCETTA).</p>

      <div className="filters-box" aria-label="Filtri lista attivita">
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
            Stato task
            <select name="taskState" value={filtersDraft.taskState} onChange={onChangeFilter}>
              {taskStateOptions.map((option) => (
                <option key={option.value || 'ALL'} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>
        </div>

        <div className="filters-actions">
          <button type="button" className="btn btn-primary btn-small" onClick={onApplyFilters} disabled={loading || Boolean(acceptingTaskId)}>
            Applica filtri
          </button>
          <button type="button" className="btn btn-outline btn-small" onClick={onResetFilters} disabled={loading || Boolean(acceptingTaskId)}>
            Cancella filtri
          </button>
          <button type="button" className="btn btn-outline btn-small" onClick={onRefresh} disabled={loading || Boolean(acceptingTaskId)}>
            {loading ? 'Caricamento...' : 'Aggiorna'}
          </button>
        </div>
      </div>

      <div className="practices-toolbar">
        <div className="panel-note">Task trovati: {activities.length}</div>
      </div>

      {errorMessage ? <div className="api-error-box">{errorMessage}</div> : null}

      <div className="table-wrapper">
        <table className="practices-table">
          <thead>
            <tr>
              <th>Attivita</th>
              <th>Pratica N.</th>
              <th>Stato task</th>
              <th>Stato pratica</th>
              <th>Assegnatario</th>
              <th>Data creazione</th>
              <th>Azione</th>
            </tr>
          </thead>
          <tbody>
            {!hasRows ? (
              <tr>
                <td className="empty-row" colSpan={7}>
                  {loading ? 'Caricamento lista attivita in corso...' : 'Nessuna attivita disponibile con i filtri impostati.'}
                </td>
              </tr>
            ) : (
              activities.map((task) => {
                const canAccept = task.taskState === 'IN_CODA';
                const canOpenTyping = task.taskState === 'IN_CARICO' && Boolean(task.practiceId);
                const isAccepting = acceptingTaskId === task.taskId;
                return (
                  <tr key={task.taskId ?? `${task.practiceId}-${task.createdAt}`}>
                    <td>{task.activityLabel ?? task.taskName ?? 'Task ANC'}</td>
                    <td>
                      {task.practiceId ? (
                        <Link className="table-link" to={`/pratiche/${task.practiceId}`}>
                          {task.practiceNumber ?? '-'}
                        </Link>
                      ) : (
                        task.practiceNumber ?? '-'
                      )}
                    </td>
                    <td>{task.taskState ?? '-'}</td>
                    <td>{task.practiceState ?? '-'}</td>
                    <td>{task.ownerUser ?? task.ownerUsername ?? '-'}</td>
                    <td>{formatDateTime(task.createdAt)}</td>
                    <td>
                      <div className="row-actions">
                        <button
                          type="button"
                          className="btn btn-primary btn-small"
                          disabled={!canAccept || Boolean(acceptingTaskId)}
                          onClick={() => onAcceptTask(task)}
                        >
                          {isAccepting ? 'ACCETTA IN CORSO...' : 'ACCETTA'}
                        </button>
                        <button
                          type="button"
                          className="btn btn-outline btn-small"
                          disabled={!canOpenTyping || Boolean(acceptingTaskId)}
                          onClick={() => navigate(`/attivita/${task.taskId}/tipizzazione?practiceId=${encodeURIComponent(task.practiceId)}`)}
                        >
                          APRI
                        </button>
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
