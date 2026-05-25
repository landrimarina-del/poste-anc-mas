// Mock useAuth per POC GAP002
import { useMemo } from 'react';

// Restituisce sempre un utente demo e ruolo selezionabile via localStorage ("supervisore"/"operatore")
export function useAuth() {
  // Per demo: cambia ruolo da console con localStorage.setItem('anc-role', 'supervisore')
  const role = localStorage.getItem('anc-role') || 'operatore';
  const user = useMemo(() => ({
    username: role === 'supervisore' ? 'supervisor.demo' : 'operator.demo',
    fullName: role === 'supervisore' ? 'Demo Supervisore' : 'Demo Operatore',
    roles: [role],
  }), [role]);
  return {
    user,
    isSupervisore: role === 'supervisore',
    isOperatore: role === 'operatore',
    logout: () => localStorage.removeItem('anc-role'),
  };
}
