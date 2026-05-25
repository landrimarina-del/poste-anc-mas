// Mock API dashboard supervisore per POC GAP002

export async function counters() {
  return { values: { activities: 12, activePractices: 7, closedPractices: 5 } };
}

export async function dailyOpened(month) {
  return { items: [] };
}

export async function dailyWorked(month) {
  return { items: [] };
}

export async function byState(month) {
  return { items: [] };
}
