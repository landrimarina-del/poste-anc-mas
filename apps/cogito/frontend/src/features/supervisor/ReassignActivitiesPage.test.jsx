import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ReassignActivitiesPage } from './ReassignActivitiesPage';

const mocks = vi.hoisted(() => ({
  list: vi.fn(),
  reassignGroup: vi.fn(),
  reassignUser: vi.fn()
}));

vi.mock('../../core/api/supervisionTasksApi', () => ({
  supervisionTasksApi: {
    list: mocks.list,
    reassignGroup: mocks.reassignGroup,
    reassignUser: mocks.reassignUser
  }
}));

function renderPage() {
  return render(
    <MemoryRouter>
      <ReassignActivitiesPage />
    </MemoryRouter>
  );
}

describe('ReassignActivitiesPage Sprint 7', () => {
  afterEach(() => {
    cleanup();
    vi.clearAllMocks();
  });

  beforeEach(() => {
    mocks.list.mockResolvedValue({
      items: [
        {
          taskId: 'task-1',
          practiceId: 'practice-1',
          practiceNumber: 'ANC-2026-0001',
          assignmentDate: '2026-05-15T09:30:00Z',
          owner: 'owner.anc',
          assignee: 'operatore.anc',
          taskState: 'IN_CARICO',
          activityLabel: 'Verifica Documento'
        }
      ],
      fallbackMode: false
    });
    mocks.reassignGroup.mockResolvedValue({});
    mocks.reassignUser.mockResolvedValue({});
  });

  it('carica task supervisione con filtri Sprint 7 disponibili', async () => {
    renderPage();

    await waitFor(() => {
      expect(mocks.list).toHaveBeenCalledWith({
        practiceNumber: '',
        assignmentDate: '',
        owner: '',
        assignee: ''
      });
    });

    expect(screen.getByLabelText('Pratica N°')).toBeInTheDocument();
    expect(screen.getByLabelText('Data Assegnazione')).toBeInTheDocument();
    expect(screen.getByLabelText('Owner')).toBeInTheDocument();
    expect(screen.getByLabelText('Assegnatario')).toBeInTheDocument();
  });

  it('applica filtri e invia payload corretto al backend', async () => {
    renderPage();

    await waitFor(() => {
      expect(mocks.list).toHaveBeenCalledTimes(1);
    });
    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Applica filtri' })).toBeEnabled();
    });

    mocks.list.mockClear();

    fireEvent.change(screen.getByLabelText('Pratica N°'), {
      target: { value: 'ANC-2026-0099' }
    });
    fireEvent.change(screen.getByLabelText('Data Assegnazione'), {
      target: { value: '2026-05-15' }
    });
    fireEvent.change(screen.getByLabelText('Owner'), {
      target: { value: 'owner.specialista' }
    });
    fireEvent.change(screen.getByLabelText('Assegnatario'), {
      target: { value: 'utente.destinatario' }
    });

    fireEvent.click(screen.getByRole('button', { name: 'Applica filtri' }));

    await waitFor(() => {
      expect(mocks.list).toHaveBeenCalledWith({
        practiceNumber: 'ANC-2026-0099',
        assignmentDate: '2026-05-15',
        owner: 'owner.specialista',
        assignee: 'utente.destinatario'
      });
    });
  });

  it('esegue riassegnazione a gruppo operatore ANC', async () => {
    renderPage();

    expect(await screen.findByRole('link', { name: 'ANC-2026-0001' })).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'RIASSEGNA A GRUPPO' }));

    await waitFor(() => {
      expect(mocks.reassignGroup).toHaveBeenCalledWith('task-1');
      expect(screen.getByText('Riassegnazione a Gruppo Operatore ANC completata.')).toBeInTheDocument();
    });
  });

  it('valida utente obbligatorio prima della riassegnazione specifica', async () => {
    renderPage();

    expect(await screen.findByRole('link', { name: 'ANC-2026-0001' })).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'RIASSEGNA A UTENTE' }));

    expect(
      screen.getByText('Indicare un utente destinatario per la riassegnazione specifica.')
    ).toBeInTheDocument();
    expect(mocks.reassignUser).not.toHaveBeenCalled();
  });
});
