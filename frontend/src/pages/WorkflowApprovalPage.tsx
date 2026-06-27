import { useState } from 'react';
import { approveRun, getApproval, rejectRun, type Approval } from '../api/workflowApi';
import Card from '../components/Card';

export default function WorkflowApprovalPage() {
  const [runId, setRunId] = useState('');
  const [actor, setActor] = useState('demo-admin@example.com');
  const [reason, setReason] = useState('Rejected from dashboard demo.');
  const [approval, setApproval] = useState<Approval | null>(null);
  const [error, setError] = useState('');

  async function handleFetch() {
    setError('');
    try {
      setApproval(await getApproval(runId));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch approval');
    }
  }

  async function handleApprove() {
    setError('');
    try {
      setApproval(await approveRun(runId, actor));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to approve run');
    }
  }

  async function handleReject() {
    setError('');
    try {
      setApproval(await rejectRun(runId, actor, reason));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to reject run');
    }
  }

  return (
    <div className="pageStack">
      <Card title="Workflow approval" eyebrow="Human-in-the-loop">
        <div className="formStack">
          <label htmlFor="runId">Workflow run ID</label>
          <input id="runId" value={runId} onChange={(event) => setRunId(event.target.value)} placeholder="Paste workflow run UUID" />
          <label htmlFor="actor">Reviewer</label>
          <input id="actor" value={actor} onChange={(event) => setActor(event.target.value)} />
          <label htmlFor="reason">Reject reason</label>
          <input id="reason" value={reason} onChange={(event) => setReason(event.target.value)} />
          <div className="buttonRow">
            <button className="secondaryButton" type="button" onClick={handleFetch} disabled={!runId}>Fetch approval</button>
            <button type="button" onClick={handleApprove} disabled={!runId}>Approve</button>
            <button className="dangerButton" type="button" onClick={handleReject} disabled={!runId}>Reject</button>
          </div>
        </div>
      </Card>
      <Card title="Approval state">
        {error && <p className="errorText">{error}</p>}
        {approval ? (
          <pre className="jsonPreview">{JSON.stringify(approval, null, 2)}</pre>
        ) : (
          <p className="mutedText">Fetch a workflow approval to review its current state.</p>
        )}
      </Card>
    </div>
  );
}
