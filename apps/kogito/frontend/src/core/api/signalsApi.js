import { httpClient } from './httpClient';

function buildQueryString(params = {}) {
  const search = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value === undefined || value === null || value === '') {
      return;
    }
    search.set(key, String(value));
  });

  const query = search.toString();
  return query ? `?${query}` : '';
}

function normalizeSignal(item) {
  return {
    id: item?.id ?? item?.signalId ?? item?.ticketId ?? '',
    practiceId: item?.practiceId ?? item?.practice?.id ?? '',
    practiceNumber: item?.practiceNumber ?? item?.practice?.practiceNumber ?? '-',
    state: item?.state ?? item?.status ?? 'IN_CODA',
    operator: item?.operator ?? item?.ownerUsername ?? item?.owner ?? item?.assignee ?? '',
    groupName: item?.groupName ?? '',
    createdAt: item?.createdAt ?? item?.createdDate ?? item?.insertedAt ?? '',
    updatedAt: item?.updatedAt ?? item?.lastUpdatedAt ?? item?.modifiedAt ?? '',
    title: item?.title ?? item?.subject ?? '-',
    description: item?.description ?? item?.note ?? '',
    sinergiaTicketId: item?.sinergiaTicketId ?? item?.externalTicketId ?? '',
    activityLabel: item?.activityLabel ?? '',
    acceptedAt: item?.acceptedAt ?? ''
  };
}

function normalizeSignalList(response) {
  if (Array.isArray(response)) {
    return response.map(normalizeSignal);
  }

  if (Array.isArray(response?.items)) {
    return response.items.map(normalizeSignal);
  }

  return [];
}

function buildCreateRequest(payload = {}) {
  return {
    practiceId: payload.practiceId,
    subject: payload.subject,
    description: payload.description
  };
}

function buildReassignRequest(payload = {}) {
  const request = {
    targetType: payload.targetType
  };

  if (payload.targetType === 'USER' && payload.username) {
    request.username = payload.username;
  }

  if (payload.reason) {
    request.reason = payload.reason;
  }

  return request;
}

export const signalsApi = {
  operators: async () => {
    const response = await httpClient.request('/api/v1/signals/operators', {
      method: 'GET'
    });
    if (Array.isArray(response)) return response;
    if (Array.isArray(response?.items)) return response.items;
    return [];
  },

  create: async (payload) => {
    const requestPayload = buildCreateRequest(payload);
    const response = await httpClient.request('/api/v1/signals', {
      method: 'POST',
      body: JSON.stringify(requestPayload)
    });

    return normalizeSignal(response ?? payload);
  },

  my: async (params) => {
    const response = await httpClient.request(`/api/v1/signals/me${buildQueryString(params)}`, {
      method: 'GET'
    });

    return normalizeSignalList(response);
  },

  list: async (params) => {
    const response = await httpClient.request(`/api/v1/signals${buildQueryString(params)}`, {
      method: 'GET'
    });

    return normalizeSignalList(response);
  },

  reassign: async (signalId, payload) => {
    const requestPayload = buildReassignRequest(payload);
    const response = await httpClient.request(`/api/v1/signals/${signalId}/reassign`, {
      method: 'POST',
      body: JSON.stringify(requestPayload)
    });

    return normalizeSignal(response ?? {});
  },

  forwardToSinergia: async (signalId) => {
    const response = await httpClient.request(`/api/v1/signals/${signalId}/forward-sinergia`, {
      method: 'POST'
    });

    return normalizeSignal(response ?? {});
  }
};
