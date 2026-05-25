import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

export function GuestRoute({ children }) {
  const { isAuthenticated, status } = useAuth();

  if (status === 'loading') {
    return <div className="page-loader">Caricamento sessione...</div>;
  }

  if (isAuthenticated) {
    return <Navigate to="/home" replace />;
  }

  return children;
}
