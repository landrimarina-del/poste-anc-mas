import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../app/auth/AuthContext';

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const from = location.state?.from?.pathname || '/home';


  const onSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setErrorMessage('');

    try {
      // Ricordami: logica da implementare se necessario
      await login(username, password);
      navigate(from, { replace: true });
    } catch (error) {
      setErrorMessage(error.message || 'Login non riuscito');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="login-page">
      <div className="login-card">
        <img src="/poste-logo.svg" alt="Logo Poste Italiane" className="poste-logo" />
        <h1 className="login-title">Scrivania Digitale ANC</h1>
        <p className="login-subtitle">Accesso area riservata</p>

        <form onSubmit={onSubmit} className="login-form">
          <label htmlFor="username">Nome utente</label>
          <input
            id="username"
            type="text"
            autoComplete="username"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            required
          />

          <label htmlFor="password">Password</label>
          <input
            id="password"
            type="password"
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />

          <div className="login-options-row" style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 }}>
            <label className="remember-me-label" style={{ display: 'flex', alignItems: 'center', gap: 8, margin: 0 }}>
              Ricordami su questo computer
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={e => setRememberMe(e.target.checked)}
                style={{ marginLeft: 4 }}
              />
            </label>
            <a href="#" className="forgot-link" style={{ fontSize: 14 }} onClick={e => { e.preventDefault(); alert('Funzionalità non disponibile nella POC.'); }}>Password dimenticata?</a>
          </div>

          {errorMessage ? <div className="form-error">{errorMessage}</div> : null}

          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? 'ACCESSO IN CORSO...' : 'ACCEDI'}
          </button>
        </form>
      </div>
    </div>
  );
}
