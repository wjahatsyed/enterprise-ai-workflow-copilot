import { FormEvent, useEffect, useState } from 'react';
import { createAgent, listAgents, type Agent } from '../api/agentApi';
import Card from '../components/Card';

export default function AgentsPage() {
  const [agents, setAgents] = useState<Agent[]>([]);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [systemPrompt, setSystemPrompt] = useState('You are a precise enterprise workflow assistant.');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const workspaceId = localStorage.getItem('workspaceId') ?? '';

  async function loadAgents() {
    if (!workspaceId) {
      return;
    }
    setError('');
    try {
      setAgents(await listAgents(workspaceId));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load agents');
    }
  }

  useEffect(() => {
    void loadAgents();
  }, [workspaceId]);

  async function handleCreate(event: FormEvent) {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      await createAgent(workspaceId, { name, description, systemPrompt });
      setName('');
      setDescription('');
      setMessage('Agent created.');
      await loadAgents();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create agent');
    }
  }

  return (
    <div className="twoColumnPage">
      <Card title="Create agent" eyebrow="AI agents">
        <form className="formStack" onSubmit={handleCreate}>
          <label htmlFor="agentName">Name</label>
          <input id="agentName" value={name} onChange={(event) => setName(event.target.value)} required />
          <label htmlFor="agentDescription">Description</label>
          <input id="agentDescription" value={description} onChange={(event) => setDescription(event.target.value)} />
          <label htmlFor="systemPrompt">System prompt</label>
          <textarea id="systemPrompt" rows={8} value={systemPrompt} onChange={(event) => setSystemPrompt(event.target.value)} required />
          <button type="submit" disabled={!workspaceId}>Create agent</button>
        </form>
      </Card>
      <Card title="Agents" actions={<button className="secondaryButton" type="button" onClick={loadAgents}>Refresh</button>}>
        {error && <p className="errorText">{error}</p>}
        {message && <p className="successText">{message}</p>}
        <div className="tableList">
          {agents.map((agent) => (
            <article key={agent.id} className="listRow">
              <div>
                <strong>{agent.name}</strong>
                <span>{agent.status} · {agent.model}</span>
              </div>
              <code>{agent.id}</code>
            </article>
          ))}
          {agents.length === 0 && <p className="mutedText">No agents loaded.</p>}
        </div>
      </Card>
    </div>
  );
}
