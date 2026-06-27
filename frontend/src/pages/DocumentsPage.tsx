import { FormEvent, useEffect, useState } from 'react';
import { createDocument, embedDocument, listDocuments, type DocumentSummary } from '../api/documentApi';
import Card from '../components/Card';

export default function DocumentsPage() {
  const [documents, setDocuments] = useState<DocumentSummary[]>([]);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const workspaceId = localStorage.getItem('workspaceId') ?? '';

  async function loadDocuments() {
    if (!workspaceId) {
      return;
    }
    setError('');
    try {
      setDocuments(await listDocuments(workspaceId));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load documents');
    }
  }

  useEffect(() => {
    void loadDocuments();
  }, [workspaceId]);

  async function handleCreate(event: FormEvent) {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      await createDocument(workspaceId, { title, content });
      setTitle('');
      setContent('');
      setMessage('Document created.');
      await loadDocuments();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create document');
    }
  }

  async function handleEmbed(documentId: string) {
    setError('');
    setMessage('');
    try {
      const result = await embedDocument(documentId);
      setMessage(result);
      await loadDocuments();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to embed document');
    }
  }

  return (
    <div className="twoColumnPage">
      <Card title="Create document" eyebrow="Knowledge base">
        <form className="formStack" onSubmit={handleCreate}>
          <label htmlFor="title">Title</label>
          <input id="title" value={title} onChange={(event) => setTitle(event.target.value)} required />
          <label htmlFor="content">Content</label>
          <textarea id="content" rows={10} value={content} onChange={(event) => setContent(event.target.value)} required />
          <button type="submit" disabled={!workspaceId}>Create document</button>
        </form>
        {!workspaceId && <p className="mutedText">Set a workspace ID first.</p>}
      </Card>
      <Card title="Documents" actions={<button className="secondaryButton" type="button" onClick={loadDocuments}>Refresh</button>}>
        {error && <p className="errorText">{error}</p>}
        {message && <p className="successText">{message}</p>}
        <div className="tableList">
          {documents.map((document) => (
            <article key={document.id} className="listRow">
              <div>
                <strong>{document.title}</strong>
                <span>{document.status} · {document.chunkCount} chunks</span>
              </div>
              <button className="secondaryButton" type="button" onClick={() => handleEmbed(document.id)}>
                Embed
              </button>
            </article>
          ))}
          {documents.length === 0 && <p className="mutedText">No documents loaded.</p>}
        </div>
      </Card>
    </div>
  );
}
