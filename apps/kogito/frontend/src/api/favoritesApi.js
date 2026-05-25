// Mock API favoriti per POC GAP002

let favorites = [
  { id: '1', titolo: 'Lista Pratiche', url: '/pratiche', tipo: 'INTERNO' },
  { id: '2', titolo: 'Portale Poste', url: 'https://www.poste.it', tipo: 'ESTERNO' }
];

export async function list() {
  return { items: favorites };
}

export async function create({ titolo, url, tipo }) {
  const id = String(Date.now());
  favorites.push({ id, titolo, url, tipo });
  return { id, titolo, url, tipo };
}

export async function update(id, { titolo, url, tipo }) {
  favorites = favorites.map(f => f.id === id ? { id, titolo, url, tipo } : f);
  return { id, titolo, url, tipo };
}

export async function remove(id) {
  favorites = favorites.filter(f => f.id !== id);
  return { id };
}
