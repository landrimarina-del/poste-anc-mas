// HomePage.jsx business: versione unica consolidata
import React, { useEffect, useState, useRef } from "react";
import { useAuth } from '../../app/auth/AuthContext';
import * as favoritesApi from "../../api/favoritesApi";
import { supervisionDashboardApi } from "../../core/api/supervisionDashboardApi";

function normalizeFavorites(response) {
  const list = Array.isArray(response) ? response : Array.isArray(response?.items) ? response.items : [];
  return list.map((item) => ({
    id: String(item?.favoriteId ?? item?.id ?? item?.linkId ?? ''),
    label: String(item?.titolo ?? item?.label ?? item?.title ?? 'Link preferito'),
    url: String(item?.url ?? item?.href ?? '#'),
    tipo: String(item?.tipo ?? 'INTERNO').toUpperCase()
  })).filter((item) => item.id);
}

function FavoriteLinksSection() {
  const [favorites, setFavorites] = useState([]);
  const [loadingFavorites, setLoadingFavorites] = useState(true);
  const [favoriteError, setFavoriteError] = useState('');
  const [favoriteInfo, setFavoriteInfo] = useState('');
  const [favoriteForm, setFavoriteForm] = useState({ label: '', url: '', tipo: 'INTERNO' });
  const [editingFavoriteId, setEditingFavoriteId] = useState('');
  const [savingFavorite, setSavingFavorite] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const modalRef = useRef();

  const loadFavorites = async () => {
    setLoadingFavorites(true);
    setFavoriteError('');
    try {
      const response = await favoritesApi.list();
      setFavorites(normalizeFavorites(response));
    } catch (error) {
      setFavorites([]);
      setFavoriteError(error?.message ?? 'Errore nel caricamento dei link favoriti.');
    } finally {
      setLoadingFavorites(false);
    }
  };

  useEffect(() => {
    void loadFavorites();
  }, []);

  const openModal = () => {
    setShowModal(true);
    setFavoriteForm({ label: '', url: '', tipo: 'INTERNO' });
    setEditingFavoriteId('');
    setFavoriteError('');
    setFavoriteInfo('');
    setTimeout(() => {
      if (modalRef.current) {
        modalRef.current.focus();
      }
    }, 100);
  };

  const closeModal = () => {
    setShowModal(false);
    setFavoriteForm({ label: '', url: '', tipo: 'INTERNO' });
    setEditingFavoriteId('');
    setFavoriteError('');
    setFavoriteInfo('');
  };

  const onEdit = (item) => {
    setEditingFavoriteId(item.id);
    setFavoriteForm({ label: item.label, url: item.url, tipo: item.tipo });
    setFavoriteError('');
    setFavoriteInfo('');
    setShowModal(true);
    setTimeout(() => {
      if (modalRef.current) {
        modalRef.current.focus();
      }
    }, 100);
  };

  const onSubmit = async (event) => {
    event.preventDefault();
    setFavoriteError('');
    setFavoriteInfo('');

    const label = favoriteForm.label.trim();
    const rawUrl = favoriteForm.url.trim();
    const url = rawUrl.startsWith('/') || /^https?:\/\//i.test(rawUrl)
      ? rawUrl
      : `https://${rawUrl}`;
    const tipo = favoriteForm.tipo;
    if (!label || !rawUrl || !tipo) {
      setFavoriteError('Compilare nome link, URL e tipo.');
      return;
    }

    setSavingFavorite(true);
    try {
      if (editingFavoriteId) {
        await favoritesApi.update(editingFavoriteId, { titolo: label, url, tipo });
        setFavoriteInfo('Link preferito aggiornato.');
      } else {
        await favoritesApi.create({ titolo: label, url, tipo });
        setFavoriteInfo('Link preferito aggiunto.');
      }
      closeModal();
      await loadFavorites();
    } catch (error) {
      setFavoriteError(error?.message ?? 'Errore durante il salvataggio del link preferito.');
    } finally {
      setSavingFavorite(false);
    }
  };

  const onDelete = async (favoriteId) => {
    setFavoriteError('');
    setFavoriteInfo('');
    setSavingFavorite(true);
    try {
      await favoritesApi.remove(favoriteId);
      setFavoriteInfo('Link preferito eliminato.');
      if (editingFavoriteId === favoriteId) {
        closeModal();
      }
      await loadFavorites();
    } catch (error) {
      setFavoriteError(error?.message ?? 'Errore durante l\'eliminazione del link preferito.');
    } finally {
      setSavingFavorite(false);
    }
  };

  return (
    <section className="favorites-section" aria-label="Link favoriti">
      <div className="favorites-header">
        <h3>Link Favoriti</h3>
        <button type="button" className="btn btn-outline btn-small" onClick={loadFavorites} disabled={loadingFavorites || savingFavorite}>
          {loadingFavorites ? 'Caricamento...' : 'Aggiorna'}
        </button>
        <button type="button" className="btn btn-primary btn-small" onClick={openModal} style={{ marginLeft: 8 }}>
          + Aggiungi nuovo link
        </button>
      </div>

      {favoriteError ? <div className="api-error-box">{favoriteError}</div> : null}
      {favoriteInfo ? <div className="info-box">{favoriteInfo}</div> : null}

      {/* Modal per aggiunta/modifica link */}
      {showModal && (
        <div className="modal-backdrop" tabIndex={-1} style={{ position: 'fixed', zIndex: 1000, top: 0, left: 0, width: '100vw', height: '100vh', background: 'rgba(0,0,0,0.2)' }} onClick={closeModal}>
          <div
            className="modal-content"
            style={{ background: '#fff', maxWidth: 400, margin: '10vh auto', padding: 24, borderRadius: 8, position: 'relative' }}
            onClick={e => e.stopPropagation()}
          >
            <h4>{editingFavoriteId ? 'Modifica Link Preferito' : 'Aggiungi Nuovo Link'}</h4>
            <form className="favorites-form" onSubmit={onSubmit}>
              <label>
                Titolo Link
                <input
                  ref={modalRef}
                  type="text"
                  value={favoriteForm.label}
                  onChange={(event) => setFavoriteForm((prev) => ({ ...prev, label: event.target.value }))}
                  placeholder="Es. Lista Pratiche"
                  disabled={savingFavorite}
                  required
                />
              </label>
              <label>
                Link (URL)
                <input
                  type="text"
                  value={favoriteForm.url}
                  onChange={(event) => setFavoriteForm((prev) => ({ ...prev, url: event.target.value }))}
                  placeholder="Es. /pratiche"
                  disabled={savingFavorite}
                  required
                />
              </label>
              <label>
                Tipo Link
                <select
                  value={favoriteForm.tipo}
                  onChange={(event) => setFavoriteForm((prev) => ({ ...prev, tipo: event.target.value }))}
                  disabled={savingFavorite}
                  required
                >
                  <option value="INTERNO">INTERNO</option>
                  <option value="ESTERNO">ESTERNO</option>
                  <option value="LEGACY">LEGACY</option>
                </select>
              </label>
              <div className="favorites-actions" style={{ marginTop: 16 }}>
                <button type="submit" className="btn btn-primary btn-small" disabled={savingFavorite}>
                  {savingFavorite ? 'SALVATAGGIO...' : editingFavoriteId ? 'SALVA' : 'AGGIUNGI'}
                </button>
                <button type="button" className="btn btn-outline btn-small" onClick={closeModal} disabled={savingFavorite} style={{ marginLeft: 8 }}>
                  Annulla
                </button>
              </div>
              {favoriteError ? <div className="api-error-box">{favoriteError}</div> : null}
            </form>
          </div>
        </div>
      )}

      <div className="table-wrapper">
        <table className="practices-table favorites-table">
          <thead>
            <tr>
              <th>Titolo</th>
              <th>URL</th>
              <th>Tipo</th>
              <th>Azione</th>
            </tr>
          </thead>
          <tbody>
            {favorites.length === 0 ? (
              <tr>
                <td className="empty-row" colSpan={4}>
                  {loadingFavorites ? 'Caricamento link favoriti...' : 'Nessun link presente'}
                </td>
              </tr>
            ) : (
              favorites.map((item) => (
                <tr key={item.id}>
                  <td>{item.label}</td>
                  <td>{item.url}</td>
                  <td>{item.tipo}</td>
                  <td>
                    <div className="row-actions">
                      <button type="button" className="btn btn-outline btn-small" onClick={() => onEdit(item)} disabled={savingFavorite}>
                        MODIFICA
                      </button>
                      <button type="button" className="btn btn-outline btn-small" onClick={() => onDelete(item.id)} disabled={savingFavorite}>
                        ELIMINA
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}

export function HomePage() {
  const { isSupervisore, user } = useAuth();
  const [selectedMonth, setSelectedMonth] = useState(() => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
  });
  const [refreshKey, setRefreshKey] = useState(0);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');
  const [infoMessage, setInfoMessage] = useState('');
  const [dashboardData, setDashboardData] = useState({
    counters: { activities: 0, activePractices: 0, closedPractices: 0 },
    dailyOpened: [],
    dailyWorked: [],
    byState: []
  });

  useEffect(() => {
    let isCancelled = false;
    if (!isSupervisore) {
      return () => {
        isCancelled = true;
      };
    }
    const loadDashboard = async () => {
      setLoading(true);
      setErrorMessage('');
      setInfoMessage('');
      try {
        const month = selectedMonth;
        const [countersResponse, dailyOpenedResponse, dailyWorkedResponse, byStateResponse] = await Promise.all([
          supervisionDashboardApi.counters(),
          supervisionDashboardApi.dailyOpened(month),
          supervisionDashboardApi.dailyWorked(month),
          supervisionDashboardApi.byState(month)
        ]);
        if (isCancelled) return;
        setDashboardData({
          counters: countersResponse?.values ?? { activities: 0, activePractices: 0, closedPractices: 0 },
          dailyOpened: dailyOpenedResponse?.items ?? [],
          dailyWorked: dailyWorkedResponse?.items ?? [],
          byState: byStateResponse?.items ?? []
        });
        const isFallback = Boolean(countersResponse?.fallbackMode)
          || Boolean(dailyOpenedResponse?.fallbackMode)
          || Boolean(dailyWorkedResponse?.fallbackMode)
          || Boolean(byStateResponse?.fallbackMode);
        if (isFallback) {
          setInfoMessage('Endpoint dashboard supervisore non disponibile: visualizzazione in modalita fallback compatibile.');
        }
      } catch (error) {
        if (!isCancelled) {
          setErrorMessage(error?.message ?? 'Errore durante il caricamento della dashboard supervisore.');
          setDashboardData({
            counters: { activities: 0, activePractices: 0, closedPractices: 0 },
            dailyOpened: [],
            dailyWorked: [],
            byState: []
          });
        }
      } finally {
        if (!isCancelled) {
          setLoading(false);
        }
      }
    };
    void loadDashboard();
    return () => {
      isCancelled = true;
    };
  }, [isSupervisore, selectedMonth, refreshKey]);

  const onChangeMonth = (event) => {
    setSelectedMonth(event.target.value);
  };
  const onRefresh = () => {
    setRefreshKey((k) => k + 1);
  };

  if (isSupervisore) {
    return (
      <section className="panel">
        <div className="supervisor-home-header">
          <div>
            <h2>Welcome Page Supervisore</h2>
            <p className="panel-note">Sprint 8: monitoraggio volumi e stati pratiche ANC.</p>
          </div>
          <div className="month-filter-box">
            <label htmlFor="dashboard-month">Mese (YYYY-MM)</label>
            <input
              id="dashboard-month"
              type="month"
              value={selectedMonth}
              onChange={onChangeMonth}
              max="2099-12"
            />
            <button type="button" className="btn btn-outline btn-small" onClick={onRefresh} disabled={loading}>
              {loading ? 'Caricamento...' : 'Aggiorna'}
            </button>
          </div>
        </div>
        <div className="home-counters-grid" aria-label="Contatori supervisore">
          <article className="home-counter-card">
            <h3>Attivita</h3>
            <p>{dashboardData.counters.activities}</p>
          </article>
          <article className="home-counter-card">
            <h3>Pratiche Attive</h3>
            <p>{dashboardData.counters.activePractices}</p>
          </article>
          <article className="home-counter-card">
            <h3>Pratiche Chiuse</h3>
            <p>{dashboardData.counters.closedPractices}</p>
          </article>
        </div>
        {infoMessage ? <div className="info-box">{infoMessage}</div> : null}
        {errorMessage ? <div className="api-error-box">{errorMessage}</div> : null}
        <div className="home-charts-grid">
          <article className="home-chart-card">
            <h3>Pratiche Giornaliere</h3>
            {loading ? <div className="chart-empty">Caricamento grafico...</div> : <DailyOpenedChart items={dashboardData.dailyOpened} />}
          </article>
          <article className="home-chart-card">
            <h3>Pratiche Giornaliere Lavorate (OK/KO)</h3>
            {loading ? <div className="chart-empty">Caricamento grafico...</div> : <DailyWorkedChart items={dashboardData.dailyWorked} />}
          </article>
          <article className="home-chart-card">
            <h3>Pratiche per Stato</h3>
            {loading ? <div className="chart-empty">Caricamento grafico...</div> : <ByStateChart items={dashboardData.byState} />}
          </article>
        </div>
        <FavoriteLinksSection />
      </section>
    );
  }

  // Operatore
  return (
    <section className="panel">
      <h2>Welcome Page {isSupervisore ? 'Supervisore' : 'Specialista'}</h2>
      <p>
        Benvenuto {user?.fullName ?? user?.username}. Questa e' la home placeholder di Sprint 0,
        pronta per ospitare i contenuti dei prossimi sprint.
      </p>
      <div className="placeholder-grid">
        <article className="placeholder-card">
          <h3>Dashboard</h3>
          <p>Contenuto non disponibile in Sprint 0.</p>
        </article>
        <article className="placeholder-card">
          <h3>Navigazione</h3>
          <p>Tab attivi in base al profilo autenticato.</p>
        </article>
      </div>
      <FavoriteLinksSection />
    </section>
  );
}

function maxValue(items, accessor) {
  return items.reduce((acc, item) => Math.max(acc, accessor(item)), 0);
}

function shouldShowDailyLabel(index) {
  return index % 5 === 0;
}

function ChartWithAxes({ max, ariaLabel, children }) {
  return (
    <div className="chart-with-axes" aria-label={ariaLabel}>
      <div className="chart-y-axis">
        <span>{max}</span>
        <span>{Math.round(max / 2)}</span>
        <span>0</span>
      </div>
      <div className="chart-bars">
        {children}
      </div>
    </div>
  );
}

function DailyWorkedLegend() {
  return (
    <div className="chart-legend">
      <span className="chart-legend-item"><span className="chart-legend-swatch chart-segment-ok" />OK</span>
      <span className="chart-legend-item"><span className="chart-legend-swatch chart-segment-ko" />KO</span>
    </div>
  );
}

function DailyOpenedChart({ items }) {
  const max = Math.max(1, maxValue(items, (item) => item.count));

  if (!items.length) {
    return <div className="chart-empty">Nessun dato per il mese selezionato.</div>;
  }

  return (
    <ChartWithAxes max={max} ariaLabel="Istogramma pratiche giornaliere">
      {items.map((item, index) => {
        const percentage = Math.max(4, Math.round((item.count / max) * 100));
        const showDayLabel = shouldShowDailyLabel(index);
        return (
            <div key={`opened-${item.day}`} className="chart-bar-item" title={`Giorno ${item.day}: ${item.count}`}>
            <div className="chart-bar-single" style={{ height: `${percentage}%` }} />
            <div className={`chart-bar-label${showDayLabel ? '' : ' chart-bar-label-hidden'}`}>{item.day}</div>
          </div>
        );
      })}
    </ChartWithAxes>
  );
}

function DailyWorkedChart({ items }) {
  const max = Math.max(1, maxValue(items, (item) => item.ok + item.ko));

  if (!items.length) {
    return (
      <>
        <DailyWorkedLegend />
        <div className="chart-empty">Nessun dato per il mese selezionato.</div>
      </>
    );
  }

  return (
    <>
      <DailyWorkedLegend />
      <ChartWithAxes max={max} ariaLabel="Istogramma pratiche giornaliere lavorate">
        {items.map((item, index) => {
          const total = item.ok + item.ko;
          const totalHeight = Math.max(4, Math.round((total / max) * 100));
          const okHeight = total > 0 ? Math.round((item.ok / total) * totalHeight) : 0;
          const koHeight = Math.max(0, totalHeight - okHeight);
          const showDayLabel = shouldShowDailyLabel(index);

          return (
            <div key={`worked-${item.day}`} className="chart-bar-item" title={`Giorno ${item.day}: OK ${item.ok}, KO ${item.ko}`}>
              <div className="chart-bar-stacked" style={{ height: `${totalHeight}%` }}>
                <div className="chart-segment-ok" style={{ height: `${okHeight}%` }} />
                <div className="chart-segment-ko" style={{ height: `${koHeight}%` }} />
              </div>
              <div className={`chart-bar-label${showDayLabel ? '' : ' chart-bar-label-hidden'}`}>{item.day}</div>
            </div>
          );
        })}
      </ChartWithAxes>
    </>
  );
}

function ByStateChart({ items }) {
  const max = Math.max(1, maxValue(items, (item) => item.count));

  if (!items.length) {
    return <div className="chart-empty">Nessun dato disponibile.</div>;
  }

  return (
    <ChartWithAxes max={max} ariaLabel="Istogramma pratiche per stato">
      {items.map((item) => {
        const percentage = Math.max(4, Math.round((item.count / max) * 100));
        return (
          <div key={`state-${item.state}`} className="chart-bar-item" title={`${item.state}: ${item.count}`}>
            <div className="chart-bar-single chart-bar-state" style={{ height: `${percentage}%` }} />
            <div className="chart-bar-label chart-state-label">{item.state}</div>
          </div>
        );
      })}
    </ChartWithAxes>
  );
}
