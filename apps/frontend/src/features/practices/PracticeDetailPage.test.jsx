import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { PracticeDetailPage } from './PracticeDetailPage';

const mocks = vi.hoisted(() => ({
  detail: vi.fn(),
  history: vi.fn(),
  states: vi.fn(),
  relatedActions: vi.fn()
}));

vi.mock('../../core/api/practicesApi', () => ({
  practicesApi: {
    detail: mocks.detail,
    history: mocks.history,
    states: mocks.states,
    relatedActions: mocks.relatedActions
  }
}));

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/pratiche/101']}>
      <Routes>
        <Route path="/pratiche/:practiceId" element={<PracticeDetailPage />} />
      </Routes>
    </MemoryRouter>
  );
}

describe('PracticeDetailPage Sprint 10', () => {
  beforeEach(() => {
    vi.clearAllMocks();

    mocks.detail.mockResolvedValue({
      header: { practiceNumber: 'ANC-2026-0001' },
      client: {
        firstName: 'Mario',
        lastName: 'Rossi',
        fiscalCode: 'RSSMRA80A01H501U',
        residenceAddress: {
          street: 'Via Roma',
          streetNumber: '10',
          city: 'Roma',
          province: 'RM',
          postalCode: '00100'
        }
      },
      blockedCard: {}
    });
    mocks.history.mockResolvedValue([]);
    mocks.states.mockResolvedValue([]);
    mocks.relatedActions.mockResolvedValue([
      {
        actionId: 'ACT-1',
        actionType: 'CHIUSURA_PRATICA',
        status: 'COMPLETATA',
        actor: 'op.rossi',
        createdAt: '2026-05-10T10:00:00Z'
      }
    ]);
  });

  it('mostra tab Azioni Correlate con dati da API', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByText(/Dettaglio pratica ANC-2026-0001/i)).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('tab', { name: 'Azioni Correlate' }));

    await waitFor(() => {
      expect(screen.getByText('CHIUSURA_PRATICA')).toBeInTheDocument();
      expect(screen.getByText('COMPLETATA')).toBeInTheDocument();
    });
  });

  it('gestisce espandi/comprimi indirizzo residenza in riepilogo', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Espandi indirizzo residenza' })).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: 'Espandi indirizzo residenza' }));
    expect(screen.getByText(/Via Roma, 10, Roma, RM, 00100/i)).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Comprimi indirizzo residenza' }));
    expect(screen.queryByText(/Via Roma, 10, Roma, RM, 00100/i)).not.toBeInTheDocument();
  });
});
