import { httpClient } from './httpClient';

export const intakeApi = {
  confirmTyping: (practiceId, documentType) =>
    httpClient.request(`/api/v1/practices/${practiceId}/intake/typing`, {
      method: 'POST',
      body: JSON.stringify({ documentType })
    }),
  getChecklist: (practiceId) =>
    httpClient.request(`/api/v1/practices/${practiceId}/intake/checklist`),
  saveChecklist: (practiceId, payload) =>
    httpClient.request(`/api/v1/practices/${practiceId}/intake/checklist`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    }),
  closePractice: (practiceId) =>
    httpClient.request(`/api/v1/practices/${practiceId}/intake/close`, {
      method: 'POST',
      body: JSON.stringify({})
    }),
  editChecklist: (practiceId) =>
    httpClient.request(`/api/v1/practices/${practiceId}/intake/checklist/edit`, {
      method: 'POST',
      body: JSON.stringify({})
    }),
  getChecklistHelp: (practiceId, itemId) =>
    httpClient.request(`/api/v1/practices/${practiceId}/intake/checklist/help/${encodeURIComponent(itemId)}`),
  // Sprint 13: causali KO
  getCausali: (practiceId, categoria) =>
    httpClient.request(`/api/v1/practices/${practiceId}/intake/checklist/causali?categoria=${encodeURIComponent(categoria)}`),
  // Sprint 13: note di lavorazione
  getNotes: (practiceId) =>
    httpClient.request(`/api/v1/practices/${practiceId}/notes`),
  addNote: (practiceId, testo) =>
    httpClient.request(`/api/v1/practices/${practiceId}/notes`, {
      method: 'POST',
      body: JSON.stringify({ testo })
    }),
  upsertNote: (practiceId, testo) =>
    httpClient.request(`/api/v1/practices/${practiceId}/notes`, {
      method: 'PUT',
      body: JSON.stringify({ testo })
    })
};
