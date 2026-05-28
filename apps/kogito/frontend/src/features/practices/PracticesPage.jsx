import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { practicesApi } from '../../core/api/practicesApi';

const DEFAULT_PAGE_SIZE = 10;

const initialFilters = {
  practiceNumber: '',
  state: '',
  openedFrom: '',
  openedTo: '',
  closedFrom: '',
  closedTo: '',
  lastModifiedFrom: '',
  lastModifiedTo: '',
  sdOutcome: ''
};

const sortableColumns = {
  practiceNumber: 'Pratica N.',
  openedAt: 'Data Apertura',
  lastModifiedAt: 'Data Ultima Modifica',
  closedAt: 'Data Chiusura',
  sdOutcome: 'Esito SD',
  state: 'Stato'
};

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

function formatSdOutcome(value) {
  if (!value) return '-';
  const v = value.toUpperCase();
  if (v === 'APPROVATA' || v === 'OK') return 'OK';
  if (v === 'RESPINTA' || v === 'KO') return 'NOK';
  return value;
}

export function PracticesPage() {
  const [practices, setPractices] = useState([]);
  const [pagination, setPagination] = useState({ page: 0, size: DEFAULT_PAGE_SIZE, total: 0 });
  const [filtersDraft, setFiltersDraft] = useState(initialFilters);
  const [filtersApplied, setFiltersApplied] = useState(initialFilters);
  const [sortBy, setSortBy] = useState({ field: 'openedAt', direction: 'desc' });
  const [loading, setLoading] = useState(true);
  const [exporting, setExporting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [infoMessage, setInfoMessage] = useState('');

  const loadPractices = useCallback(async (nextPage = 0, nextFilters = filtersApplied, nextSort = sortBy) => {
    setLoading(true);
    setErrorMessage('');

    try {
      const result = await practicesApi.list({
        page: nextPage,
        size: pagination.size,
        sort: `${nextSort.field},${nextSort.direction}`,
        ...nextFilters
      });

      if (Array.isArray(result)) {
        setPractices(result);
        setPagination((prev) => ({ ...prev, page: nextPage, total: result.length }));
      } else {
        setPractices(Array.isArray(result?.items) ? result.items : []);
        setPagination((prev) => ({
          ...prev,
          page: typeof result?.page === 'number' ? result.page : nextPage,
          size: typeof result?.size === 'number' ? result.size : prev.size,
          total: typeof result?.total === 'number' ? result.total : 0
        }));
      }
    } catch (error) {
      setErrorMessage(error?.message ?? 'Errore durante il caricamento pratiche');
    } finally {
      setLoading(false);
    }
  }, [filtersApplied, pagination.size, sortBy]);

  useEffect(() => {
    loadPractices(0, filtersApplied, sortBy);
  }, [filtersApplied, sortBy, loadPractices]);

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

  const onToggleSort = (field) => {
    setSortBy((prev) => {
      if (prev.field === field) {
        return {
          field,
          direction: prev.direction === 'asc' ? 'desc' : 'asc'
        };
      }
      return {
        field,
        direction: 'asc'
      };
    });
  };

  const totalPages = Math.max(1, Math.ceil(pagination.total / pagination.size));
  const currentPage = pagination.page + 1;

  const onMovePage = (targetPage) => {
    if (targetPage < 0 || targetPage >= totalPages || targetPage === pagination.page) {
      return;
    }
    loadPractices(targetPage, filtersApplied, sortBy);
  };

  const renderSortLabel = (field, label) => {
    if (sortBy.field !== field) {
      return `${label} [asc/desc]`;
    }
    return `${label} [${sortBy.direction}]`;
  };

  const hasRows = practices.length > 0;

  const onRefresh = () => {
    loadPractices(pagination.page, filtersApplied, sortBy);
  };

  const onExportExcel = async () => {
    setErrorMessage('');
    setInfoMessage('');
    setExporting(true);

    try {
      const blob = await practicesApi.exportExcel({
        page: 0,
        size: 1000,
        sort: `${sortBy.field},${sortBy.direction}`,
        ...filtersApplied
      });

      const fileName = `pratiche-anc-${new Date().toISOString().slice(0, 10)}.xlsx`;
      const objectUrl = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = objectUrl;
      link.download = fileName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(objectUrl);
      setInfoMessage('Export Excel completato con successo.');
    } catch (error) {
      setErrorMessage(error?.message ?? 'Errore durante l\'export Excel della lista pratiche.');
    } finally {
      setExporting(false);
    }
  };

  const isFirstPage = pagination.page <= 0;
  const isLastPage = pagination.page >= totalPages - 1;

  const statusOptions = ['', 'APERTA', 'IN_LAVORAZIONE', 'IN_ATTESA_CONFERMA_BPM', 'CHIUSA_OK', 'CHIUSA_KO'];
  const outcomeOptions = ['', 'OK', 'KO'];

  const renderHeaderButton = (field) => (
    <button
      type="button"
      className="table-sort-btn"
      onClick={() => onToggleSort(field)}
      title={`Ordina per ${sortableColumns[field]}`}
    >
      {renderSortLabel(field, sortableColumns[field])}
    </button>
  );

  return (
    <section className="panel">
      <div className="practices-toolbar">
        <h2 style={{ margin: 0 }}>Pratiche</h2>
      </div>

      <div className="filters-box" aria-label="Filtri pratiche">
        <div className="filters-grid">
          {/* Riga 1 col 1: Cerca */}
          <label>
            Cerca Pratiche
            <div className="filter-search-row">
              <input
                type="text"
                name="practiceNumber"
                value={filtersDraft.practiceNumber}
                onChange={onChangeFilter}
                placeholder="Es. ANC-2026-0001"
                onKeyDown={(e) => e.key === 'Enter' && onApplyFilters()}
              />
            </div>
          </label>

          {/* Riga 1 col 2: Stato */}
          <label>
            Stato
            <select name="state" value={filtersDraft.state} onChange={onChangeFilter}>
              {statusOptions.map((value) => (
                <option key={value || 'ALL'} value={value}>{value || 'Qualsiasi'}</option>
              ))}
            </select>
          </label>

          {/* Riga 1 col 3: Data Chiusura range */}
          <label>
            Data Chiusura
            <div className="filter-date-range">
              <input type="date" name="closedFrom" value={filtersDraft.closedFrom} onChange={onChangeFilter} />
              <span className="filter-date-sep">-</span>
              <input type="date" name="closedTo" value={filtersDraft.closedTo} onChange={onChangeFilter} />
            </div>
          </label>

          {/* Riga 2 col 1: Data Apertura range */}
          <label>
            Data Apertura
            <div className="filter-date-range">
              <input type="date" name="openedFrom" value={filtersDraft.openedFrom} onChange={onChangeFilter} />
              <span className="filter-date-sep">-</span>
              <input type="date" name="openedTo" value={filtersDraft.openedTo} onChange={onChangeFilter} />
            </div>
          </label>

          {/* Riga 2 col 2: Data Ultima Modifica range */}
          <label>
            Data Ultima Modifica
            <div className="filter-date-range">
              <input type="date" name="lastModifiedFrom" value={filtersDraft.lastModifiedFrom} onChange={onChangeFilter} />
              <span className="filter-date-sep">-</span>
              <input type="date" name="lastModifiedTo" value={filtersDraft.lastModifiedTo} onChange={onChangeFilter} />
            </div>
          </label>

          {/* Riga 2 col 3: Esito SD */}
          <label>
            Esito SD
            <select name="sdOutcome" value={filtersDraft.sdOutcome} onChange={onChangeFilter}>
              {outcomeOptions.map((value) => (
                <option key={value || 'ALL'} value={value}>{value || 'Qualsiasi'}</option>
              ))}
            </select>
          </label>
        </div>

        <div className="filters-actions">
          <span className="panel-note" style={{ marginRight: 'auto', alignSelf: 'center' }}>Totale: {pagination.total}</span>
          <button type="button" className="btn btn-primary btn-small" onClick={onApplyFilters} disabled={loading}>
            Applica Filtri
          </button>
          <button type="button" className="btn btn-outline btn-small" onClick={onResetFilters} disabled={loading}>
            Azzera Filtri
          </button>
          <button type="button" className="btn btn-outline btn-small" onClick={onRefresh} disabled={loading}>
            {loading ? 'Aggiorna...' : 'Aggiorna'}
          </button>
          <button type="button" className="btn btn-outline btn-small" onClick={onExportExcel} disabled={loading || exporting}>
            {exporting ? 'Export...' : 'Esporta Excel'}
          </button>
        </div>
      </div>

      {infoMessage ? <div className="info-box">{infoMessage}</div> : null}
      {errorMessage ? <div className="api-error-box">{errorMessage}</div> : null}

      <div className="table-wrapper">
        <table className="practices-table">
          <thead>
            <tr>
              <th>{renderHeaderButton('practiceNumber')}</th>
              <th>Codice Fiscale</th>
              <th>Codice Cliente</th>
              <th>{renderHeaderButton('openedAt')}</th>
              <th>{renderHeaderButton('lastModifiedAt')}</th>
              <th>{renderHeaderButton('closedAt')}</th>
              <th>Data Inserimento Richiesta</th>
              <th>{renderHeaderButton('sdOutcome')}</th>
              <th>Operatore</th>
              <th>{renderHeaderButton('state')}</th>
              <th>Segnalazioni</th>
            </tr>
          </thead>
          <tbody>
            {!hasRows ? (
              <tr>
                <td className="empty-row" colSpan={11}>
                  {loading ? 'Caricamento pratiche in corso...' : 'Nessuna pratica disponibile con i filtri impostati.'}
                </td>
              </tr>
            ) : (
              practices.map((practice) => (
                <tr key={practice.practiceId ?? `${practice.requestId}-${practice.idWorkItem}`}>
                  <td>
                    <Link className="table-link" to={`/pratiche/${practice.practiceId}`}>
                      {practice.practiceNumber ?? '-'}
                    </Link>
                  </td>
                  <td>{practice.codiceFiscale ?? practice.fiscalCode ?? '-'}</td>
                  <td>{practice.codiceCliente ?? '-'}</td>
                  <td>{formatDateTime(practice.openedAt)}</td>
                  <td>{formatDateTime(practice.lastModifiedAt ?? practice.updatedAt)}</td>
                  <td>{formatDateTime(practice.closedAt)}</td>
                  <td>{formatDateTime(practice.dataInserimentoRichiesta ?? practice.requestInsertedAt)}</td>
                  <td>{formatSdOutcome(practice.sdOutcome)}</td>
                  <td>{practice.operatore ?? practice.ownerUser ?? '-'}</td>
                  <td>{practice.state ?? '-'}</td>
                  <td>
                    {practice.segnalazioniCount > 0
                      ? <span className="badge-warning" title={`${practice.segnalazioniCount} segnalazione/i aperte`}>🔔 {practice.segnalazioniCount}</span>
                      : <span style={{ color: 'var(--gray-400)' }}>—</span>}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      <div className="pagination-bar" aria-label="Paginazione pratiche">
        <span>
          Pagina {currentPage} di {totalPages}
        </span>
        <div className="pagination-actions">
          <button type="button" className="btn btn-outline btn-small" onClick={() => onMovePage(0)} disabled={loading || isFirstPage}>
            {'<<'}
          </button>
          <button
            type="button"
            className="btn btn-outline btn-small"
            onClick={() => onMovePage(pagination.page - 1)}
            disabled={loading || isFirstPage}
          >
            {'<'}
          </button>
          <button
            type="button"
            className="btn btn-outline btn-small"
            onClick={() => onMovePage(pagination.page + 1)}
            disabled={loading || isLastPage}
          >
            {'>'}
          </button>
          <button
            type="button"
            className="btn btn-outline btn-small"
            onClick={() => onMovePage(totalPages - 1)}
            disabled={loading || isLastPage}
          >
            {'>>'}
          </button>
        </div>
      </div>
    </section>
  );
}
