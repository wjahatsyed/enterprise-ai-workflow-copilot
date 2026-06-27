import { FormEvent, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login } from '../api/authApi';
import { setToken } from '../api/apiClient';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await login(email);
      setToken(response.accessToken);
      localStorage.setItem('currentUser', JSON.stringify(response));
      navigate('/');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Login failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="loginPage">
      <section className="loginPanel">
        <div>
          <p className="eyebrow">Demo access</p>
          <h1>Enterprise AI Workflow Copilot</h1>
          <p className="mutedText">
            Sign in with an existing app user email to manage documents, agents,
            workflows, and approvals.
          </p>
        </div>
        <form onSubmit={handleSubmit} className="formStack">
          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            placeholder="wajahat@example.com"
            required
          />
          {error && <p className="errorText">{error}</p>}
          <button type="submit" disabled={loading}>
            {loading ? 'Signing in...' : 'Login'}
          </button>
        </form>
      </section>
    </main>
  );
}
