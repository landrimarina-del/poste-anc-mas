import { useNavigate, useParams } from 'react-router-dom';

export function PracticeDetailPlaceholderPage() {
  const { practiceId } = useParams();
  const navigate = useNavigate();

  return (
    <section className="panel">
      <h2>Dettaglio pratica {practiceId}</h2>
      <p>Funzione non inclusa in Sprint 0. Il dettaglio completo sara implementato negli sprint successivi.</p>
      <button type="button" className="btn btn-outline" onClick={() => navigate('/pratiche')}>
        Torna alla lista pratiche
      </button>
    </section>
  );
}
