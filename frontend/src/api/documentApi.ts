import { apiRequest } from './apiClient';

export type DocumentSummary = {
  id: string;
  workspaceId: string;
  title: string;
  sourceType: string;
  status: string;
  chunkCount: number;
};

export function listDocuments(workspaceId: string): Promise<DocumentSummary[]> {
  return apiRequest<DocumentSummary[]>(`/api/workspaces/${workspaceId}/documents`);
}

export function createDocument(
  workspaceId: string,
  payload: { title: string; content: string }
): Promise<DocumentSummary> {
  return apiRequest<DocumentSummary>(`/api/workspaces/${workspaceId}/documents`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function embedDocument(documentId: string): Promise<string> {
  return apiRequest<string>(`/api/documents/${documentId}/embeddings`, {
    method: 'POST'
  });
}
