import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { useAuth } from '../auth/AuthContext';
import { PhaseProvider } from '../../core/PhaseContext';
import { workflowTechnicalApi } from '../../core/api/workflowTechnicalApi';
import { supervisionDashboardApi } from '../../core/api/supervisionDashboardApi';
import { tasksApi } from '../../core/api/tasksApi';

const operatoreTabs = [
  { to: '/home', label: 'Home' },
  { to: '/attivita', label: 'Attivita' },
  { to: '/pratiche', label: 'Pratiche' }
];

const supervisoreTabs = [
  { to: '/home', label: 'Home' },
  { to: '/riassegna-attivita', label: 'Riassegna Attivita' },
  { to: '/segnalazioni-supervisore', label: 'Segnalazioni' },
  { to: '/pratiche', label: 'Pratiche' }
];

export function AppShell() {
  const { user, isSupervisore, logout } = useAuth();
  const navigate = useNavigate();
  const [workflowReadiness, setWorkflowReadiness] = useState({ status: 'loading', label: 'WORKFLOW: CHECK...' });
  const [headerCounters, setHeaderCounters] = useState({ activities: 0, activePractices: 0, closedPractices: 0 });

  const tabs = isSupervisore ? supervisoreTabs : operatoreTabs;

  const onLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  useEffect(() => {
    let isCancelled = false;

    const loadWorkflowReadiness = async () => {
      try {
        const details = await workflowTechnicalApi.readiness();
        if (isCancelled) {
          return;
        }

        const isReady = Boolean(details?.engineActive) && Boolean(details?.placeholderProcessDeployed);
        setWorkflowReadiness({
          status: isReady ? 'ok' : 'warning',
          label: isReady ? 'WORKFLOW: ATTIVO' : 'WORKFLOW: IN INIZIALIZZAZIONE'
        });
      } catch {
        if (!isCancelled) {
          setWorkflowReadiness({ status: 'error', label: 'WORKFLOW: NON DISPONIBILE' });
        }
      }
    };

    void loadWorkflowReadiness();

    return () => {
      isCancelled = true;
    };
  }, []);

  useEffect(() => {
    let isCancelled = false;

    if (!isSupervisore) {
      const loadOperatorCounters = async () => {
        try {
          const response = await tasksApi.counters();
          if (isCancelled) return;
          setHeaderCounters({
            activities: response?.activities ?? 0,
            activePractices: response?.activePractices ?? 0,
            closedPractices: response?.closedPractices ?? 0
          });
        } catch {
          if (!isCancelled) setHeaderCounters({ activities: 0, activePractices: 0, closedPractices: 0 });
        }
      };
      void loadOperatorCounters();
      return () => { isCancelled = true; };
    }

    const loadCounters = async () => {
      try {
        const response = await supervisionDashboardApi.counters();
        if (isCancelled) {
          return;
        }

        setHeaderCounters({
          activities: response?.values?.activities ?? 0,
          activePractices: response?.values?.activePractices ?? 0,
          closedPractices: response?.values?.closedPractices ?? 0
        });
      } catch {
        if (!isCancelled) {
          setHeaderCounters({ activities: 0, activePractices: 0, closedPractices: 0 });
        }
      }
    };

    void loadCounters();

    return () => {
      isCancelled = true;
    };
  }, [isSupervisore]);

  return (
    <PhaseProvider>
    <div className="app-shell">
      <header className="top-header">
        <div className="brand-area">
          <img src="/poste-logo.svg" alt="Logo Poste Italiane" className="brand-logo" />
          <div className="brand-title">Scrivania Digitale ANC</div>
        </div>

        <nav className="tabs-nav" aria-label="Navigazione principale">
          {tabs.map((tab) => (
            <NavLink
              key={tab.to}
              to={tab.to}
              className={({ isActive }) => `tab-link ${isActive ? 'active' : ''}`}
            >
              {tab.label}
            </NavLink>
          ))}
        </nav>

        <div className="header-right">
          <div className="counter-box">
            <span className="counter-value">{headerCounters.activities}</span>
            <span className="counter-label">Attivita</span>
          </div>
          <div className="counter-box">
            <span className="counter-value">{headerCounters.activePractices}</span>
            <span className="counter-label">Pratiche Attive</span>
          </div>
          <div className="counter-box">
            <span className="counter-value">{headerCounters.closedPractices}</span>
            <span className="counter-label">Pratiche Chiuse</span>
          </div>
          <div className="user-panel">
            <div className="user-name">{user?.fullName ?? user?.username}</div>
            <button type="button" className="btn btn-outline" onClick={onLogout}>
              LOGOUT
            </button>
          </div>
        </div>
      </header>

      <main className="content-area">
        <Outlet />
      </main>
    </div>
    </PhaseProvider>
  );
}
