import { MemoryRouter } from 'react-router-dom';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { SignalsDashboardPage } from './SignalsDashboardPage';

const useAuthMock = vi.hoisted(() => vi.fn());
const signalsApiMock = vi.hoisted(() => ({
  my: vi.fn(),
  list: vi.fn(),
  create: vi.fn(),
  reassign: vi.fn(),
  forwardToSinergia: vi.fn()
}));

vi.mock('../../app/auth/AuthContext', () => ({
  useAuth: useAuthMock
}));

vi.mock('../../core/api/signalsApi', () => ({
  signalsApi: signalsApiMock
}));

describe('SignalsDashboardPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    signalsApiMock.my.mockResolvedValue([]);
    signalsApiMock.list.mockResolvedValue([]);
    signalsApiMock.create.mockResolvedValue({ id: 'SIG-900' });
    signalsApiMock.reassign.mockResolvedValue({});
    signalsApiMock.forwardToSinergia.mockResolvedValue({});
  });

  afterEach(() => {
    cleanup();
    vi.restoreAllMocks();
  });

  it('precompila il contesto pratica e invia segnalazione in vista operatore', async () => {
    useAuthMock.mockReturnValue({ isSupervisore: false });

    render(
      <MemoryRouter initialEntries={['/segnalazioni?practiceId=900&practiceNumber=ANC-2026-0900']}>
        <SignalsDashboardPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Dashboard Segnalazioni')).toBeInTheDocument();
    expect(screen.getByDisplayValue('900')).toBeInTheDocument();
    expect(screen.getByDisplayValue('ANC-2026-0900')).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('Titolo'), { target: { value: 'Test invio' } });
    fireEvent.change(screen.getByLabelText('Descrizione'), { target: { value: 'Dettaglio test' } });
    fireEvent.click(screen.getByRole('button', { name: 'INVIA SEGNALAZIONE' }));

    await waitFor(() => {
      expect(signalsApiMock.create).toHaveBeenCalledWith(
        expect.objectContaining({
          practiceId: 900,
          subject: 'Test invio',
          description: 'Dettaglio test'
        })
      );
    });

    expect(screen.queryByText('Vista globale supervisore')).not.toBeInTheDocument();
  });

  it('mostra vista globale e invoca lista filtrata per supervisore', async () => {
    useAuthMock.mockReturnValue({ isSupervisore: true });

    render(
      <MemoryRouter initialEntries={['/segnalazioni']}>
        <SignalsDashboardPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Vista globale supervisore')).toBeInTheDocument();

    fireEvent.change(screen.getByLabelText('ID segnalazione'), { target: { value: 'SIG-12' } });
    fireEvent.change(screen.getByLabelText('Stato'), { target: { value: 'IN_CODA' } });
    fireEvent.click(screen.getByRole('button', { name: 'Applica filtri' }));

    await waitFor(() => {
      expect(signalsApiMock.list).toHaveBeenCalledWith(
        expect.objectContaining({ signalId: 'SIG-12', state: 'IN_CODA' })
      );
    });
  });

  it('invia payload reassign con targetType USER e username/reason', async () => {
    useAuthMock.mockReturnValue({ isSupervisore: true });
    signalsApiMock.list.mockResolvedValue([
      {
        id: 'SIG-44',
        state: 'IN_CODA',
        practiceNumber: 'ANC-2026-0044',
        operator: 'operatore.anc'
      }
    ]);

    const promptMock = vi.spyOn(window, 'prompt');
    promptMock
      .mockReturnValueOnce('USER')
      .mockReturnValueOnce('operatore.destinatario')
      .mockReturnValueOnce('Ribilanciamento carico');

    render(
      <MemoryRouter initialEntries={['/segnalazioni']}>
        <SignalsDashboardPage />
      </MemoryRouter>
    );

    expect(await screen.findByText('Vista globale supervisore')).toBeInTheDocument();
    expect(await screen.findByText('ANC-2026-0044')).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'RIASSEGNA' }));

    await waitFor(() => {
      expect(signalsApiMock.reassign).toHaveBeenCalledWith('SIG-44', {
        targetType: 'USER',
        username: 'operatore.destinatario',
        reason: 'Ribilanciamento carico'
      });
    });
  });
});
