import { Navigate, createBrowserRouter } from 'react-router-dom';
import { ProtectedRoute } from './auth/ProtectedRoute';
import { GuestRoute } from './auth/GuestRoute';
import { RoleRoute } from './auth/RoleRoute';
import { AppShell } from './shell/AppShell';
import { LoginPage } from '../features/auth/LoginPage';
import { HomePage } from '../features/home/HomePage';
import { ActivitiesPage } from '../features/activities/ActivitiesPage';
import { TypingPage } from '../features/intake/TypingPage';
import { PracticesPage } from '../features/practices/PracticesPage';
import { PracticeDetailPage } from '../features/practices/PracticeDetailPage';
import { ReassignActivitiesPage } from '../features/supervisor/ReassignActivitiesPage';
import { ForbiddenPage } from '../features/shared/ForbiddenPage';
import { SignalsDashboardPage } from '../features/signals/SignalsDashboardPage';

export const router = createBrowserRouter([
  {
    path: '/login',
    element: (
      <GuestRoute>
        <LoginPage />
      </GuestRoute>
    )
  },
  {
    path: '/',
    element: (
      <ProtectedRoute>
        <AppShell />
      </ProtectedRoute>
    ),
    children: [
      {
        index: true,
        element: <Navigate to="/home" replace />
      },
      {
        path: 'home',
        element: <HomePage />
      },
      {
        path: 'attivita',
        element: (
          <RoleRoute allowedRoles={['OPERATORE', 'OPERATORE_ANC']}>
            <ActivitiesPage />
          </RoleRoute>
        )
      },
      {
        path: 'attivita/:taskId/tipizzazione',
        element: (
          <RoleRoute allowedRoles={['OPERATORE', 'OPERATORE_ANC']}>
            <TypingPage />
          </RoleRoute>
        )
      },
      {
        path: 'pratiche',
        element: <PracticesPage />
      },
      {
        path: 'pratiche/:practiceId',
        element: <PracticeDetailPage />
      },
      {
        path: 'riassegna-attivita',
        element: (
          <RoleRoute allowedRoles={['SUPERVISORE', 'SUPERVISORE_ANC']}>
            <ReassignActivitiesPage />
          </RoleRoute>
        )
      },
      {
        path: 'segnalazioni',
        element: (
          <RoleRoute allowedRoles={['OPERATORE', 'OPERATORE_ANC', 'SUPERVISORE', 'SUPERVISORE_ANC']}>
            <SignalsDashboardPage />
          </RoleRoute>
        )
      },
      {
        path: 'forbidden',
        element: <ForbiddenPage />
      }
    ]
  },
  {
    path: '*',
    element: <Navigate to="/" replace />
  }
]);
