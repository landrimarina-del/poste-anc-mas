import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { tasksApi } from '../../core/api/tasksApi';

const initialFilters = {
  practiceNumber: '',
  taskState: '',
  assignedToMe: false
};

const MAX_SAVED_FILTERS = 5;

function formatDate(value) {
  if (!value) return '-';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  const DD = String(d.getDate()).padStart(2, '0');
  const MM = String(d.getMonth() + 1).padStart(2, '0');
  const YYYY = d.getFullYear();
  return `${DD}/${MM}/${YYYY}`;
}

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
  const [savedFilters, setSavedFilters] = useState([]);
  const [loadingFilters, setLoadingFilters] = useState(false);
  const [savingFilter, setSavingFilter] = useState(false);
  const [selectedFilterId, setSelectedFilterId] = useState(null);

  const loadSavedFilters = useCallback(async () => {
    setLoadingFilters(true);
    try {
      const result = await tasksApi.getSavedFilters();
      const items = Array.isArray(result) ? result : Array.isArray(result?.items) ? result.items : [];
      setSavedFilters(items.slice(0, MAX_SAVED_FILTERS));
    } catch {
      setSavedFilters([]);
    } finally {
      setLoadingFilters(false);
    }
  }, []);

  const loadActivities = useCallback(async (filters = filtersApplied) => {
    setLoading(true);
    setErrorMessage('');

    try {
      const { assignedToMe, ...restFilters } = filters;
      const result = await tasksApi.list({
        ...restFilters,
        ...(assignedToMe ? { assignedToMe: true } : {})
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

  useEffect(() => {
    void loadSavedFilters();
  }, [loadSavedFilters]);

  const onChangeFilter = (event) => {
    const { name, value, type, checked } = event.target;
    setFiltersDraft((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
  };

  const onChangeAssignedToMe = (event) => {
    const checked = event.target.checked;
    setFiltersDraft((prev) => ({ ...prev, assignedToMe: checked }));
    setFiltersApplied((prev) => ({ ...prev, assignedToMe: checked }));
  };

  const onApplyFilters = () => {
    setFiltersApplied(filtersDraft);
  };

  const onApplyAndSaveFilters = async () => {
    setSavingFilter(true);
    try {
      const filterJson = JSON.stringify(filtersDraft);
      await tasksApi.saveFilter(null, filterJson);
      await loadSavedFilters();
      setFiltersApplied(filtersDraft);
    } catch (error) {
      setErrorMessage(error?.message ?? 'Errore durante il salvataggio del filtro');
    } finally {
      setSavingFilter(false);
    }
  };

  const onApplySavedFilter = (savedFilter) => {
    try {
      const parsed = typeof savedFilter.filterJson === 'string'
        ? JSON.parse(savedFilter.filterJson)
        : (savedFilter.filterJson ?? {});
      const merged = { ...initialFilters, ...parsed };
      setFiltersDraft(merged);
      setFiltersApplied(merged);
      setSelectedFilterId(savedFilter.id ?? savedFilter.filterId ?? null);
    } catch {
      setErrorMessage('Filtro salvato non valido');
    }
  };

  const onDeleteSavedFilter = async (id) => {
    try {
      await tasksApi.deleteFilter(id);
      if (selectedFilterId === id) setSelectedFilterId(null);
      await loadSavedFilters();
    } catch (error) {
      setErrorMessage(error?.message ?? 'Errore durante l\'eliminazione del filtro');
    }
  };

  const onResetFilters = () => {
    setFiltersDraft(initialFilters);
    setFiltersApplied(initialFilters);
    setSelectedFilterId(null);
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

        <label className="filter-checkbox-label">
          <input
            type="checkbox"
            name="assignedToMe"
            checked={filtersDraft.assignedToMe}
            onChange={onChangeAssignedToMe}
            disabled={loading || Boolean(acceptingTaskId)}
          />
          Visualizza le attività a me assegnate
        </label>

        <div className="filters-actions">
          <button type="button" className="btn btn-primary btn-small" onClick={onApplyFilters} disabled={loading || Boolean(acceptingTaskId)}>
            APPLICA FILTRI
          </button>
          <button type="button" className="btn btn-outline btn-small" onClick={onApplyAndSaveFilters} disabled={loading || Boolean(acceptingTaskId) || savingFilter}>
            {savingFilter ? 'SALVATAGGIO...' : 'APPLICA E SALVA FILTRI'}
          </button>
          <button type="button" className="btn btn-outline btn-small" onClick={onResetFilters} disabled={loading || Boolean(acceptingTaskId)}>
            AZZERA FILTRI
          </button>
          <button type="button" className="btn btn-outline btn-small" onClick={onRefresh} disabled={loading || Boolean(acceptingTaskId)}>
            {loading ? 'CARICAMENTO...' : 'AGGIORNA'}
          </button>
        </div>
      </div>

      {/* Sezione Filtri Salvati */}
      {(savedFilters.length > 0 || loadingFilters) && (
        <div className="saved-filters-box">
          <div className="saved-filters-header">
            <span className="saved-filters-title">Ultimi Filtri Salvati</span>
            {loadingFilters && <span className="panel-note">Caricamento...</span>}
          </div>
          <div className="table-wrapper">
            <table className="practices-table saved-filters-table">
              <thead>
                <tr>
                  <th>Nome Filtro</th>
                  <th>Pratica N.</th>
                  <th>Stato</th>
                  <th>Attività a me</th>
                  <th>Data</th>
                  <th>Azioni</th>
                </tr>
              </thead>
              <tbody>
                {savedFilters.map((sf) => {
                  const sfId = sf.id ?? sf.filterId;
                  const isSelected = selectedFilterId === sfId;
                  let parsed = {};
                  try { parsed = typeof sf.filterJson === 'string' ? JSON.parse(sf.filterJson) : (sf.filterJson ?? {}); } catch { /* skip */ }
                  return (
                    <tr
                      key={sfId ?? sf.filterName}
                      className={`saved-filter-row${isSelected ? ' selected' : ''}`}
                    >
                      <td>{sf.filterName ?? 'Filtro senza nome'}</td>
                      <td>{parsed.practiceNumber || '-'}</td>
                      <td>{parsed.taskState || '-'}</td>
                      <td>{parsed.assignedToMe ? 'Sì' : 'No'}</td>
                      <td>{formatDate(sf.createdAt)}</td>
                      <td>
                        <div className="row-actions">
                          <button
                            type="button"
                            className="btn btn-primary btn-small"
                            onClick={() => onApplySavedFilter(sf)}
                          >
                            APPLICA
                          </button>
                          <button
                            type="button"
                            className="btn btn-outline btn-small"
                            onClick={() => onDeleteSavedFilter(sfId)}
                            aria-label="Elimina filtro"
                          >
                            ×
                          </button>
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

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
              <th>SLA</th>
              <th>Azione</th>
            </tr>
          </thead>
          <tbody>
            {!hasRows ? (
              <tr>
                <td className="empty-row" colSpan={8}>
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
                      {task.slaStatus === 'SCADUTO'
                        ? <span className="badge-sla badge-sla-scaduto" title={task.slaDueDate ? `Scaduto il ${formatDateTime(task.slaDueDate)}` : 'SLA scaduto'}>SCADUTO</span>
                        : task.slaStatus === 'IN_TEMPO'
                          ? <span className="badge-sla badge-sla-in-tempo" title={task.slaDueDate ? `Scadenza ${formatDateTime(task.slaDueDate)}` : 'In tempo'}>IN TEMPO</span>
                          : <span className="badge-sla badge-sla-nd">—</span>
                      }
                    </td>
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
