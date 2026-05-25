import { beforeEach, describe, expect, it, vi } from 'vitest';
import { signalsApi } from './signalsApi';

function jsonResponse(payload, status = 200) {
  return {
    ok: status >= 200 && status < 300,
    status,
    headers: {
      get: () => 'application/json'
    },
    json: async () => payload
  };
}

describe('signalsApi', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    vi.stubGlobal('fetch', vi.fn());
  });

  it('invoca GET /api/v1/signals con querystring filtri e normalizza lista', async () => {
    fetch.mockResolvedValue(
      jsonResponse({
        resultCode: 0,
        details: {
          items: [
            {
              signalId: 'SIG-100',
              practiceId: 'PR-01',
              practiceNumber: 'ANC-2026-0001',
              state: 'IN_CODA'
            }
          ]
        }
      })
    );

    const result = await signalsApi.list({ state: 'IN_CODA', operator: 'operatore.anc' });

    expect(fetch).toHaveBeenCalledWith(
      '/api/v1/signals?state=IN_CODA&operator=operatore.anc',
      expect.objectContaining({ method: 'GET', credentials: 'include' })
    );
    expect(result).toHaveLength(1);
    expect(result[0]).toMatchObject({
      id: 'SIG-100',
      practiceId: 'PR-01',
      practiceNumber: 'ANC-2026-0001',
      state: 'IN_CODA'
    });
  });

  it('invoca POST /api/v1/signals per invio segnalazione', async () => {
    fetch.mockResolvedValue(
      jsonResponse({
        resultCode: 0,
        details: {
          signalId: 'SIG-200',
          practiceId: 'PR-02',
          practiceNumber: 'ANC-2026-0002',
          state: 'IN_CODA'
        }
      })
    );

    const payload = {
      practiceId: 2,
      subject: 'Anomalia rilevata',
      description: 'Dettaglio anomalia'
    };

    const result = await signalsApi.create(payload);

    expect(fetch).toHaveBeenCalledWith(
      '/api/v1/signals',
      expect.objectContaining({
        method: 'POST',
        credentials: 'include',
        body: JSON.stringify(payload)
      })
    );
    expect(result.id).toBe('SIG-200');
  });

  it('invoca forward verso endpoint stub sinergia', async () => {
    fetch.mockResolvedValue(
      jsonResponse({
        resultCode: 0,
        details: {
          signalId: 'SIG-300',
          sinergiaTicketId: 'PIX-7788'
        }
      })
    );

    const result = await signalsApi.forwardToSinergia('SIG-300');

    expect(fetch).toHaveBeenCalledWith(
      '/api/v1/signals/SIG-300/forward-sinergia',
      expect.objectContaining({ method: 'POST', credentials: 'include' })
    );
    expect(result.sinergiaTicketId).toBe('PIX-7788');
  });

  it('invoca reassign con targetType USER e username/reason secondo contratto backend', async () => {
    fetch.mockResolvedValue(
      jsonResponse({
        resultCode: 0,
        details: {
          signalId: 'SIG-301',
          state: 'IN_CODA'
        }
      })
    );

    await signalsApi.reassign('SIG-301', {
      targetType: 'USER',
      username: 'operatore.destinatario',
      reason: 'Ribilanciamento carico',
      targetValue: 'campo-da-non-inviare'
    });

    expect(fetch).toHaveBeenCalledWith(
      '/api/v1/signals/SIG-301/reassign',
      expect.objectContaining({
        method: 'POST',
        credentials: 'include',
        body: JSON.stringify({
          targetType: 'USER',
          username: 'operatore.destinatario',
          reason: 'Ribilanciamento carico'
        })
      })
    );
  });
});
