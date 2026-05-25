import { httpClient } from './httpClient';

export const attachmentsApi = {
  listByPractice: (practiceId) =>
    httpClient.request(`/api/v1/practices/${practiceId}/attachments`, {
      method: 'GET'
    }),
  previewUrl: (attachmentId) => `/api/v1/attachments/${attachmentId}/preview`,
  downloadUrl: (attachmentId) => `/api/v1/attachments/${attachmentId}/download`
};
