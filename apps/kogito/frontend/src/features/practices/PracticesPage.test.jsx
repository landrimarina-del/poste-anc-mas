import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { PracticesPage } from './PracticesPage';

const mocks = vi.hoisted(() => ({
  list: vi.fn(),
  exportExcel: vi.fn()
}));

vi.mock('../../core/api/practicesApi', () => ({
  practicesApi: {
    list: mocks.list,
    exportExcel: mocks.exportExcel
  }
}));

describe('PracticesPage Sprint 10', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mocks.list.mockResolvedValue({
      page: 0,
      size: 10,
      total: 1,
      items: [
        {
          practiceId: 101,
          practiceNumber: 'ANC-2026-0001',
          requestId: 'REQ-1',
          idWorkItem: 'WI-1',
          state: 'APERTA'
        }
      ]
    });
    mocks.exportExcel.mockResolvedValue(new Blob(['excel']));

    if (!URL.createObjectURL) {
      Object.defineProperty(URL, 'createObjectURL', {
        writable: true,
        configurable: true,
        value: () => 'blob:excel-url'
      });
    }

    if (!URL.revokeObjectURL) {
      Object.defineProperty(URL, 'revokeObjectURL', {
        writable: true,
        configurable: true,
        value: () => {}
      });
    }

    vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:excel-url');
    vi.spyOn(URL, 'revokeObjectURL').mockImplementation(() => {});
  });

  it('mostra pulsante export excel e richiama endpoint export', async () => {
    render(
      <MemoryRouter>
        <PracticesPage />
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText('ANC-2026-0001')).toBeInTheDocument();
    });

    fireEvent.click(screen.getByRole('button', { name: 'EXPORT EXCEL' }));

    await waitFor(() => {
      expect(mocks.exportExcel).toHaveBeenCalled();
      expect(screen.getByText('Export Excel completato con successo.')).toBeInTheDocument();
    });
  });
});
