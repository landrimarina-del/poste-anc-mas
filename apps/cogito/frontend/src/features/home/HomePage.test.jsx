import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import { HomePage } from './HomePage';

vi.mock('../../app/auth/AuthContext', () => ({
  useAuth: vi.fn()
}));

vi.mock('../../core/api/supervisionDashboardApi', () => ({
  supervisionDashboardApi: {
    counters: vi.fn(),
    dailyOpened: vi.fn(),
    dailyWorked: vi.fn(),
    byState: vi.fn()
  }
}));

vi.mock('../../core/api/favoritesApi', () => ({
  favoritesApi: {
    list: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    remove: vi.fn()
  }
}));

const { useAuth } = await import('../../app/auth/AuthContext');
const { supervisionDashboardApi } = await import('../../core/api/supervisionDashboardApi');
const { favoritesApi } = await import('../../core/api/favoritesApi');

describe('HomePage Sprint 8', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    favoritesApi.list.mockResolvedValue({ items: [] });
    favoritesApi.create.mockResolvedValue({});
    favoritesApi.update.mockResolvedValue({});
    favoritesApi.remove.mockResolvedValue({});
  });

  it('mantiene la home operatore senza dashboard supervisore', () => {
    useAuth.mockReturnValue({
      isSupervisore: false,
      user: { username: 'operatore.anc', fullName: 'Operatore ANC' }
    });

    render(<HomePage />);

    expect(screen.getByText(/Welcome Page Specialista/i)).toBeInTheDocument();
    expect(screen.queryByText(/Pratiche Giornaliere/i)).not.toBeInTheDocument();
    expect(screen.queryByLabelText(/Mese \(YYYY-MM\)/i)).not.toBeInTheDocument();
  });

  it('consente creazione link favoriti in home', async () => {
    useAuth.mockReturnValue({
      isSupervisore: false,
      user: { username: 'operatore.anc', fullName: 'Operatore ANC' }
    });

    render(<HomePage />);

    await waitFor(() => {
      expect(favoritesApi.list).toHaveBeenCalledTimes(1);
    });

    const [labelInput, urlInput] = screen.getAllByRole('textbox');
    fireEvent.change(labelInput, { target: { value: 'Lista pratiche' } });
    fireEvent.change(urlInput, { target: { value: '/pratiche' } });
    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'ESTERNO' } });

    fireEvent.click(screen.getByRole('button', { name: 'AGGIUNGI' }));

    await waitFor(() => {
      expect(favoritesApi.create).toHaveBeenCalledWith({ titolo: 'Lista pratiche', url: '/pratiche', tipo: 'ESTERNO' });
    });
  });

  it('mappa correttamente la lista favoriti da payload backend', async () => {
    useAuth.mockReturnValue({
      isSupervisore: false,
      user: { username: 'operatore.anc', fullName: 'Operatore ANC' }
    });

    favoritesApi.list.mockResolvedValue({
      items: [
        { favoriteId: 99, titolo: 'Portale Legacy', url: '/legacy', tipo: 'LEGACY' }
      ]
    });

    render(<HomePage />);

    await waitFor(() => {
      expect(screen.getByText('Portale Legacy')).toBeInTheDocument();
      expect(screen.getByText('/legacy')).toBeInTheDocument();
      expect(screen.getByRole('cell', { name: 'LEGACY' })).toBeInTheDocument();
    });
  });

  it('mostra dashboard supervisore con grafici e contatori', async () => {
    useAuth.mockReturnValue({
      isSupervisore: true,
      user: { username: 'supervisore.anc', fullName: 'Supervisore ANC' }
    });

    supervisionDashboardApi.counters.mockResolvedValue({
      values: { activities: 12, activePractices: 8, closedPractices: 30 },
      fallbackMode: false
    });

    supervisionDashboardApi.dailyOpened.mockResolvedValue({
      items: [
        { day: 1, count: 3 },
        { day: 2, count: 5 }
      ],
      fallbackMode: false
    });

    supervisionDashboardApi.dailyWorked.mockResolvedValue({
      items: [
        { day: 1, ok: 2, ko: 1 },
        { day: 2, ok: 3, ko: 1 }
      ],
      fallbackMode: false
    });

    supervisionDashboardApi.byState.mockResolvedValue({
      items: [
        { state: 'IN_LAVORAZIONE', count: 4 },
        { state: 'CHIUSA_OK', count: 10 }
      ],
      fallbackMode: false
    });

    render(<HomePage />);

    expect(screen.getByText(/Welcome Page Supervisore/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Mese \(YYYY-MM\)/i)).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getByText('12')).toBeInTheDocument();
      expect(screen.getByText('8')).toBeInTheDocument();
      expect(screen.getByText('30')).toBeInTheDocument();
    });

    expect(screen.getByRole('heading', { name: /^Pratiche Giornaliere$/i })).toBeInTheDocument();
    expect(screen.getByText(/Pratiche Giornaliere Lavorate/i)).toBeInTheDocument();
    expect(screen.getByText(/Pratiche per Stato/i)).toBeInTheDocument();
  });
});
