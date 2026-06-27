import { FormEvent, useEffect, useState } from 'react';
import { askAgent, listAgents, type Agent } from '../api/agentApi';
import Card from '../components/Card';

export default function AgentChatPage() {
  const [agents, setAgents] = useState<Agent[]>([]);
  const [agentId, setAgentId] = useState('');
  const [conversationId, setConversationId] = useState('');
  const [question, setQuestion] = useState('');
  const [answer, setAnswer] = useState('');
  const [error, setError] = useState('');
  const workspaceId = localStorage.getItem('workspaceId') ?? '';

  useEffect(() => {
    async function load() {
      if (!workspaceId) {
        return;
      }
      try {
        const loadedAgents = await listAgents(workspaceId);
        setAgents(loadedAgents);
        setAgentId((current) => current || loadedAgents[0]?.id || '');
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load agents');
      }
    }

    void load();
  }, [workspaceId]);

  async function handleAsk(event: FormEvent) {
    event.preventDefault();
    setError('');
    setAnswer('');
    try {
      const response = await askAgent(agentId, {
        question,
        conversationId: conversationId || undefined
      });
      setConversationId(response.conversationId);
      setAnswer(response.answer);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to ask agent');
    }
  }

  return (
    <div className="pageStack">
      <Card title="Agent chat" eyebrow="Ask a workspace agent">
        <form className="formStack" onSubmit={handleAsk}>
          <label htmlFor="agentSelect">Agent</label>
          <select id="agentSelect" value={agentId} onChange={(event) => setAgentId(event.target.value)} required>
            <option value="">Select agent</option>
            {agents.map((agent) => (
              <option key={agent.id} value={agent.id}>{agent.name}</option>
            ))}
          </select>
          <label htmlFor="conversationId">Conversation ID</label>
          <input id="conversationId" value={conversationId} onChange={(event) => setConversationId(event.target.value)} placeholder="Optional" />
          <label htmlFor="question">Question</label>
          <textarea id="question" rows={5} value={question} onChange={(event) => setQuestion(event.target.value)} required />
          <button type="submit" disabled={!agentId}>Ask agent</button>
        </form>
      </Card>
      <Card title="Answer">
        {error && <p className="errorText">{error}</p>}
        {answer ? <p className="answerBox">{answer}</p> : <p className="mutedText">Ask a question to see the agent response.</p>}
      </Card>
    </div>
  );
}
