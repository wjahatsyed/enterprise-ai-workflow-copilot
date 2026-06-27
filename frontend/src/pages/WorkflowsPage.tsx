import { FormEvent, useEffect, useMemo, useState } from 'react';
import { createWorkflow, getWorkflowRun, listWorkflows, startWorkflowRun, type Workflow, type WorkflowRun } from '../api/workflowApi';
import Card from '../components/Card';

const defaultWorkflowJson = JSON.stringify({
  name: 'Approval-backed action',
  description: 'Demo workflow with a manual approval step',
  steps: [
    {
      name: 'Manager approval',
      type: 'HUMAN_APPROVAL',
      configJson: '{}'
    }
  ]
}, null, 2);

export default function WorkflowsPage() {
  const [workflows, setWorkflows] = useState<Workflow[]>([]);
  const [workflowJson, setWorkflowJson] = useState(defaultWorkflowJson);
  const [selectedWorkflowId, setSelectedWorkflowId] = useState('');
  const [inputJson, setInputJson] = useState('{"demo":true}');
  const [runId, setRunId] = useState('');
  const [run, setRun] = useState<WorkflowRun | null>(null);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const workspaceId = localStorage.getItem('workspaceId') ?? '';

  const selectedWorkflow = useMemo(
    () => workflows.find((workflow) => workflow.id === selectedWorkflowId),
    [workflows, selectedWorkflowId]
  );

  async function loadWorkflows() {
    if (!workspaceId) {
      return;
    }
    setError('');
    try {
      const loaded = await listWorkflows(workspaceId);
      setWorkflows(loaded);
      setSelectedWorkflowId((current) => current || loaded[0]?.id || '');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load workflows');
    }
  }

  useEffect(() => {
    void loadWorkflows();
  }, [workspaceId]);

  async function handleCreate(event: FormEvent) {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      const payload = JSON.parse(workflowJson) as { name: string; description: string; steps: Workflow['steps'] };
      await createWorkflow(workspaceId, payload);
      setMessage('Workflow created.');
      await loadWorkflows();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Invalid workflow JSON');
    }
  }

  async function handleStart() {
    setError('');
    setMessage('');
    try {
      const startedRun = await startWorkflowRun(selectedWorkflowId, inputJson);
      setRun(startedRun);
      setRunId(startedRun.id);
      setMessage(`Run started: ${startedRun.status}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to start workflow');
    }
  }

  async function handleFetchRun() {
    if (!runId) {
      return;
    }
    setError('');
    try {
      setRun(await getWorkflowRun(runId));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch run');
    }
  }

  return (
    <div className="twoColumnPage">
      <Card title="Create workflow" eyebrow="JSON definition">
        <form className="formStack" onSubmit={handleCreate}>
          <textarea rows={14} value={workflowJson} onChange={(event) => setWorkflowJson(event.target.value)} />
          <button type="submit" disabled={!workspaceId}>Create workflow</button>
        </form>
      </Card>
      <div className="pageStack">
        <Card title="Workflows" actions={<button className="secondaryButton" type="button" onClick={loadWorkflows}>Refresh</button>}>
          {error && <p className="errorText">{error}</p>}
          {message && <p className="successText">{message}</p>}
          <div className="formStack">
            <label htmlFor="workflowSelect">Workflow</label>
            <select id="workflowSelect" value={selectedWorkflowId} onChange={(event) => setSelectedWorkflowId(event.target.value)}>
              <option value="">Select workflow</option>
              {workflows.map((workflow) => (
                <option key={workflow.id} value={workflow.id}>{workflow.name}</option>
              ))}
            </select>
            {selectedWorkflow && <p className="mutedText">{selectedWorkflow.status} · {selectedWorkflow.steps.length} steps</p>}
            <label htmlFor="inputJson">Run input JSON</label>
            <textarea id="inputJson" rows={5} value={inputJson} onChange={(event) => setInputJson(event.target.value)} />
            <button type="button" disabled={!selectedWorkflowId} onClick={handleStart}>Start run</button>
          </div>
        </Card>
        <Card title="Run status">
          <div className="inlineForm">
            <input value={runId} onChange={(event) => setRunId(event.target.value)} placeholder="Workflow run ID" />
            <button className="secondaryButton" type="button" onClick={handleFetchRun}>Fetch</button>
          </div>
          {run ? (
            <pre className="jsonPreview">{JSON.stringify(run, null, 2)}</pre>
          ) : (
            <p className="mutedText">Start or fetch a run to inspect status.</p>
          )}
        </Card>
      </div>
    </div>
  );
}
