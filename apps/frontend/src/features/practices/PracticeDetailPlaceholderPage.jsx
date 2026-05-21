import { useNavigate, useParams } from 'react-router-dom';

export function PracticeDetailPlaceholderPage() {
  const { practiceId } = useParams();
  const navigate = useNavigate();

  return (
    <section className="panel">
      <h2>Dettaglio pratica {practiceId}</h2>

      <button type="button" className="btn btn-outline" onClick={() => navigate('/pratiche')}>
        Torna alla lista pratiche
      </button>
    </section>
  );
}
