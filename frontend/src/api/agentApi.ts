import { apiRequest } from './apiClient';

export type Agent = {
  id: string;
  workspaceId: string;
  name: string;
  description?: string;
  model: string;
  status: string;
};

export type AskAgentResponse = {
  conversationId: string;
  answer: string;
};

export function listAgents(workspaceId: string): Promise<Agent[]> {
  return apiRequest<Agent[]>(`/api/workspaces/${workspaceId}/agents`);
}

export function createAgent(
  workspaceId: string,
  payload: { name: string; description: string; systemPrompt: string; model?: string }
): Promise<Agent> {
  return apiRequest<Agent>(`/api/workspaces/${workspaceId}/agents`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function askAgent(
  agentId: string,
  payload: { conversationId?: string; question: string }
): Promise<AskAgentResponse> {
  return apiRequest<AskAgentResponse>(`/api/agents/${agentId}/ask`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}
