import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { authApi } from '../../core/api/authApi';

const AuthContext = createContext(null);

function hasRole(user, roleCode) {
  return Array.isArray(user?.roles) && user.roles.includes(roleCode);
}

function hasAnyRole(user, roleCodes) {
  return roleCodes.some((roleCode) => hasRole(user, roleCode));
}

export function AuthProvider({ children }) {
  const [status, setStatus] = useState('loading');
  const [user, setUser] = useState(null);

  const refreshUser = useCallback(async () => {
    try {
      const me = await authApi.me();
      setUser(me);
      setStatus('authenticated');
      return me;
    } catch {
      setUser(null);
      setStatus('anonymous');
      return null;
    }
  }, []);

  useEffect(() => {
    refreshUser();
  }, [refreshUser]);

  const login = useCallback(async (username, password) => {
    const logged = await authApi.login(username, password);
    setUser(logged);
    setStatus('authenticated');
    return logged;
  }, []);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } finally {
      setUser(null);
      setStatus('anonymous');
    }
  }, []);

  const value = useMemo(
    () => ({
      status,
      user,
      isAuthenticated: status === 'authenticated',
      isOperatore: hasAnyRole(user, ['OPERATORE', 'OPERATORE_ANC']),
      isSupervisore: hasAnyRole(user, ['SUPERVISORE', 'SUPERVISORE_ANC']),
      login,
      logout,
      refreshUser
    }),
    [status, user, login, logout, refreshUser]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth deve essere usato dentro AuthProvider');
  }
  return context;
}
