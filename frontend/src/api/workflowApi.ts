import { apiRequest } from './apiClient';

export type WorkflowStep = {
  id?: string;
  stepOrder?: number;
  name: string;
  type: string;
  configJson: string;
};

export type Workflow = {
  id: string;
  workspaceId: string;
  name: string;
  description?: string;
  status: string;
  steps: WorkflowStep[];
};

export type WorkflowRun = {
  id: string;
  workflowId: string;
  status: string;
  inputJson: string;
  outputJson?: string;
  stepRuns?: Array<{
    id: string;
    workflowStepId: string;
    stepName: string;
    stepType: string;
    status: string;
  }>;
};

export type Approval = {
  workflowRunId: string;
  approvalStatus: string;
  approvedBy?: string;
  approvedAt?: string;
  rejectionReason?: string;
};

export function listWorkflows(workspaceId: string): Promise<Workflow[]> {
  return apiRequest<Workflow[]>(`/api/workspaces/${workspaceId}/workflows`);
}

export function createWorkflow(
  workspaceId: string,
  payload: { name: string; description: string; steps: WorkflowStep[] }
): Promise<Workflow> {
  return apiRequest<Workflow>(`/api/workspaces/${workspaceId}/workflows`, {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function startWorkflowRun(
  workflowId: string,
  inputJson: string
): Promise<WorkflowRun> {
  return apiRequest<WorkflowRun>(`/api/workflows/${workflowId}/runs`, {
    method: 'POST',
    body: JSON.stringify({ inputJson })
  });
}

export function getWorkflowRun(runId: string): Promise<WorkflowRun> {
  return apiRequest<WorkflowRun>(`/api/workflow-runs/${runId}`);
}

export function getApproval(runId: string): Promise<Approval> {
  return apiRequest<Approval>(`/api/workflow-runs/${runId}/approval`);
}

export function approveRun(runId: string, approvedBy: string): Promise<Approval> {
  return apiRequest<Approval>(`/api/workflow-runs/${runId}/approve`, {
    method: 'POST',
    body: JSON.stringify({ approvedBy })
  });
}

export function rejectRun(
  runId: string,
  rejectedBy: string,
  reason: string
): Promise<Approval> {
  return apiRequest<Approval>(`/api/workflow-runs/${runId}/reject`, {
    method: 'POST',
    body: JSON.stringify({ rejectedBy, reason })
  });
}
