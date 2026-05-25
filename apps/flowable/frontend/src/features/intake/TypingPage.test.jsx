import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { TypingPage } from './TypingPage';

const mocks = vi.hoisted(() => ({
  listByPractice: vi.fn(),
  confirmTyping: vi.fn(),
  detail: vi.fn(),
  getChecklist: vi.fn(),
  saveChecklist: vi.fn(),
  closePractice: vi.fn(),
  editChecklist: vi.fn()
}));

vi.mock('../../core/api/attachmentsApi', () => ({
  attachmentsApi: {
    listByPractice: mocks.listByPractice,
    previewUrl: (id) => `/api/v1/attachments/${id}/preview`,
    downloadUrl: (id) => `/api/v1/attachments/${id}/download`
  }
}));

vi.mock('../../core/api/intakeApi', () => ({
  intakeApi: {
    confirmTyping: mocks.confirmTyping,
    getChecklist: mocks.getChecklist,
    saveChecklist: mocks.saveChecklist,
    closePractice: mocks.closePractice,
    editChecklist: mocks.editChecklist
  }
}));

vi.mock('../../core/api/practicesApi', () => ({
  practicesApi: {
    detail: mocks.detail
  }
}));

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/attivita/task-1/tipizzazione?practiceId=practice-1']}>
      <Routes>
        <Route path="/attivita/:taskId/tipizzazione" element={<TypingPage />} />
      </Routes>
    </MemoryRouter>
  );
}

beforeEach(() => {
  mocks.detail.mockResolvedValue({
    header: {
      practiceNumber: 'ANC-2026-0001'
    }
  });

  mocks.listByPractice.mockResolvedValue([
    {
      attachmentId: 'att-1',
      fileName: 'verbale.pdf'
    }
  ]);

  mocks.confirmTyping.mockResolvedValue({});
  mocks.getChecklist.mockResolvedValue({
    status: 'BOZZA',
    checklistDescription: 'Descrizione checklist da backend',
    draftSaved: true,
    outcome: 'APPROVATA'
  });
  mocks.saveChecklist.mockResolvedValue({});
  mocks.closePractice.mockResolvedValue({});
  mocks.editChecklist.mockResolvedValue({});
});

describe('TypingPage Sprint 4', () => {
  it('render viewer e switch dimensione preview', async () => {
    const { container } = renderPage();

    await waitFor(() => {
      expect(screen.getByTitle('Anteprima allegato')).toBeInTheDocument();
    });

    const shell = container.querySelector('.viewer-frame-shell');
    expect(shell).toBeTruthy();
    expect(shell.style.height).toBe('520px');

    fireEvent.click(screen.getByRole('button', { name: 'Grande' }));
    expect(shell.style.height).toBe('680px');

    fireEvent.click(screen.getByRole('button', { name: 'Piccolo' }));
    expect(shell.style.height).toBe('360px');
  });

  it('consente comprimi/espandi preview allegato', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByTitle('Anteprima allegato')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: 'Comprimi preview' }));
    expect(screen.queryByTitle('Anteprima allegato')).not.toBeInTheDocument();
    expect(screen.getByText(/Preview allegato compressa/i)).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Espandi preview' }));
    expect(screen.getByTitle('Anteprima allegato')).toBeInTheDocument();
  });

  it('mostra fallback download quando allegato presente', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByRole('link', { name: 'Download' })).toBeInTheDocument();
    });

    expect(screen.getByRole('link', { name: 'Download' })).toHaveAttribute(
      'href',
      '/api/v1/attachments/att-1/download'
    );
  });

  it('conferma tipizzazione e blocca controllo tipo documento', async () => {
    renderPage();

    await waitFor(() => {
      expect(screen.getByLabelText('Tipo documento')).toBeInTheDocument();
    });

    fireEvent.change(screen.getByLabelText('Tipo documento'), {
      target: { value: 'VERBALE' }
    });

    fireEvent.click(screen.getByRole('button', { name: 'CONFERMA' }));

    await waitFor(() => {
      expect(mocks.confirmTyping).toHaveBeenCalledWith('practice-1', 'VERBALE');
      expect(screen.getByText('Verbale di denuncia (confermato)')).toBeInTheDocument();
    });

    await waitFor(() => {
      expect(mocks.getChecklist).toHaveBeenCalledWith('practice-1');
      expect(screen.getByText('Descrizione checklist da backend')).toBeInTheDocument();
    });
  });

  it('mostra box informativo quando preview fallisce', async () => {
    mocks.listByPractice.mockRejectedValueOnce(
      new Error('Errore tecnico durante il caricamento della tipizzazione documento.')
    );

    renderPage();

    await waitFor(() => {
      expect(
        screen.getByText('Viewer non disponibile: usare Download e proseguire con la tipizzazione.')
      ).toBeInTheDocument();
    });
  });
});
