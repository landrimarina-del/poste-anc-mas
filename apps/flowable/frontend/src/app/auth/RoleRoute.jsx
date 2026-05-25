import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

export function RoleRoute({ allowedRoles, children }) {
  const { user } = useAuth();
  const roles = user?.roles ?? [];

  if (allowedRoles.some((role) => roles.includes(role))) {
    return children;
  }

  return <Navigate to="/forbidden" replace />;
}
