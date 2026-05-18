import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ActivitiesPage } from './ActivitiesPage';

const mocks = vi.hoisted(() => ({
  list: vi.fn(),
  accept: vi.fn()
}));

vi.mock('../../core/api/tasksApi', () => ({
  tasksApi: {
    list: mocks.list,
    accept: mocks.accept
  }
}));

function renderPage() {
  return render(
    <MemoryRouter>
      <ActivitiesPage />
    </MemoryRouter>
  );
}

describe('ActivitiesPage taskState filter', () => {
  beforeEach(() => {
    mocks.list.mockResolvedValue([]);
    mocks.accept.mockReset();
  });

  it('mostra solo stati task supportati dal backend nel filtro', async () => {
    renderPage();

    await waitFor(() => {
      expect(mocks.list).toHaveBeenCalled();
    });

    expect(screen.getByRole('option', { name: 'Tutti' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'In coda' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'In carico' })).toBeInTheDocument();
    expect(screen.queryByRole('option', { name: 'COMPLETATO' })).not.toBeInTheDocument();
  });

  it('invia nel payload solo il valore backend supportato selezionato', async () => {
    renderPage();

    await waitFor(() => {
      expect(mocks.list).toHaveBeenCalledWith({ practiceNumber: '', taskState: '' });
    });

    mocks.list.mockClear();

    fireEvent.change(screen.getByRole('combobox', { name: 'Stato task' }), {
      target: { value: 'IN_CARICO' }
    });
    fireEvent.click(screen.getByRole('button', { name: 'Applica filtri' }));

    await waitFor(() => {
      expect(mocks.list).toHaveBeenCalledWith({ practiceNumber: '', taskState: 'IN_CARICO' });
    });
  });
});
