// Componente sidebar navigazione workflow lavorazione (Sprint 12 — S12-FE-1)
// Ref: GAP-UX.md §4.1 — Sidebar Navigazione | GAP-UI.md §2.4

import { useEffect, useState } from 'react';

const SIDEBAR_ITEMS = [
  { id: 'DATI_PRATICA',       label: 'Dati Pratica',       icon: 'fa-briefcase',       section: 1 },
  { id: 'VERIFICA_DOCUMENTO', label: 'Verifica Documento', icon: 'fa-check-square-o',  section: 2 },
  { id: 'RIEPILOGO',          label: 'Riepilogo',          icon: 'fa-address-card-o',  section: 3 }
];

const LS_KEY = 'workflow-sidebar-collapsed';

/**
 * Props:
 *   activeSection   : number (1 | 2 | 3)
 *   onSectionChange : (section: number) => void
 *   sidebarState    : { currentStep, steps: [{ id, label, enabled, completed }] }
 */
export function WorkflowSidebar({ activeSection, onSectionChange, sidebarState }) {
  const [collapsed, setCollapsed] = useState(() => {
    try {
      return localStorage.getItem(LS_KEY) === 'true';
    } catch {
      return false;
    }
  });

  useEffect(() => {
    try {
      localStorage.setItem(LS_KEY, String(collapsed));
    } catch {
      // ignora errori localStorage
    }
  }, [collapsed]);

  function getStepMeta(item) {
    const step = sidebarState?.steps?.find((s) => s.id === item.id);
    return {
      enabled:   step?.enabled   ?? item.section <= 2,
      completed: step?.completed ?? false
    };
  }

  return (
    <nav
      className={`workflow-sidebar ${collapsed ? 'extra-narrow' : 'narrow'}`}
      aria-label="Navigazione workflow"
    >
      <button
        type="button"
        className="workflow-sidebar-toggle"
        onClick={() => setCollapsed((prev) => !prev)}
        aria-label={collapsed ? 'Espandi sidebar' : 'Comprimi sidebar'}
        title={collapsed ? 'Espandi sidebar' : 'Comprimi sidebar'}
      >
        <i
          className={`fa ${collapsed ? 'fa-angle-double-right' : 'fa-angle-double-left'}`}
          aria-hidden="true"
        />
      </button>

      <ul className="workflow-sidebar-list">
        {SIDEBAR_ITEMS.map((item) => {
          const { enabled, completed } = getStepMeta(item);
          const isActive   = activeSection === item.section;
          const isDisabled = !enabled;

          return (
            <li key={item.id}>
              <button
                type="button"
                className={[
                  'workflow-sidebar-item',
                  isActive   ? 'active'    : '',
                  isDisabled ? 'disabled'  : '',
                  completed  ? 'completed' : ''
                ]
                  .filter(Boolean)
                  .join(' ')}
                onClick={() => {
                  if (!isDisabled) onSectionChange(item.section);
                }}
                disabled={isDisabled}
                aria-current={isActive ? 'step' : undefined}
                title={item.label}
                style={isDisabled ? { cursor: 'default' } : undefined}
              >
                <i
                  className={`fa ${item.icon} workflow-sidebar-icon`}
                  aria-hidden="true"
                />
                {!collapsed && (
                  <span className="workflow-sidebar-label">{item.label}</span>
                )}
              </button>
            </li>
          );
        })}
      </ul>
    </nav>
  );
}
