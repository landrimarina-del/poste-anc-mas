import { httpClient } from './httpClient';

export const workflowTechnicalApi = {
  readiness: () =>
    httpClient.request('/api/v1/technical/workflow/readiness', {
      method: 'GET'
    })
};