# Enterprise AI Workflow Copilot

[![CI](https://github.com/wjahatsyed/enterprise-ai-workflow-copilot/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/wjahatsyed/enterprise-ai-workflow-copilot/actions/workflows/ci.yml)

Enterprise AI Workflow Copilot is a multi-tenant platform designed to streamline complex business processes using AI agents and automated workflows. It provides a robust framework for managing documents, building a knowledge base with OpenAI embeddings, and orchestrating AI-driven actions with human-in-the-loop approvals.

## 🚀 Key Features

- **Multi-tenancy**: Strong isolation between different organizations (tenants).
- **Document Knowledge Base**: Ingest and process documents into searchable chunks.
- **OpenAI Embeddings**: Leverage state-of-the-art embeddings for semantic search and AI context.
- **AI Agents**: Create and interact with workspace-specific AI agents.
- **Workflow Orchestration**: Define multi-step workflows with automated and manual steps.
- **Human-in-the-Loop**: Integrated approval framework for critical workflow steps.
- **Action Framework**: Extensible system for AI agents to perform real-world actions.
- **Outbox Pattern**: Reliable domain event publishing for eventual consistency.

## 🛠 Tech Stack

- **Backend**: Java 25, Spring Boot 4.1.0
- **Database**: PostgreSQL 17
- **Caching/Messaging**: Redis 7
- **AI**: OpenAI API integration
- **Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Migration**: Flyway
- **Build Tool**: Maven

## 🏗 Architecture

The project follows a modular monolith architecture with a focus on domain-driven design and reliability.

### Multi-tenancy
The system implements multi-tenancy at the data level. Most resources (Users, Workspaces, Agents, Workflows) are scoped to a `Tenant`. Tenant isolation is enforced via `TenantContext` and validated in the service layer.

### Document Knowledge Base & Embeddings
Documents are uploaded to workspaces, partitioned into chunks, and transformed into vector embeddings using OpenAI's models. This enables AI agents to perform RAG (Retrieval-Augmented Generation) for more accurate, context-aware responses.

### Agents & Workflows
- **Agents**: Configurable AI personalities that can interact with the knowledge base.
- **Workflows**: Defined as a series of steps. A `WorkflowRun` tracks the execution of these steps.
- **Approvals**: Certain workflow steps can be marked as requiring human approval, pausing the workflow until a `TENANT_ADMIN` approves or rejects it.

### Action Framework & Outbox Pattern
The Action Framework allows agents and workflows to trigger external systems. To ensure reliability, the **Outbox Pattern** is used: domain events are first persisted to the database in the same transaction as the business logic, and then published to external listeners/systems.

## ⚙️ Setup & Installation

### Prerequisites
- Java 25 JDK
- Docker and Docker Compose
- OpenAI API Key (configured in environment variables)

### Running with Docker Compose
To start the required infrastructure (PostgreSQL and Redis):

```powershell
docker-compose up -d
```

### Running the Application
Set your OpenAI API key and start the Spring Boot application:

```powershell
$env:OPENAI_API_KEY="your-key-here"
./mvnw.cmd spring-boot:run
```

### Running with Demo Seed Data
To run the project with pre-seeded data for demo purposes (Acme Logistics tenant, demo user, workspace, documents, agents, and workflows), use the `demo` profile:

```powershell
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=demo
```

When running with the `demo` profile:
- A tenant `Acme Logistics` (slug: `acme-logistics`) is created.
- A tenant admin user `demo@acme.com` is created.
- A workspace `Customer Operations` is created.
- Sample documents (`Refund Policy`, `Escalation SOP`) are added.
- A `Customer Support Agent` and a `Complaint Review Workflow` are configured.
- The system uses an in-memory H2 database.
- **Note**: OpenAI is NOT called automatically on startup. Embeddings are NOT generated automatically.

The API will be available at `http://localhost:8080`.
Swagger UI: `http://localhost:8080/swagger-ui.html`

### Running the React Frontend
The demo dashboard is available under `frontend/` and expects the backend at `http://localhost:8080`.

```powershell
cd frontend
npm install
npm run dev
```

The Vite app will be available at `http://localhost:5173`. To point it at another backend, update `frontend/.env` and set `VITE_API_BASE_URL`.

## 🔐 Authentication Flow

The application uses JWT-based authentication. In demo mode, you can log in using an existing user's email.

1. **Login**: POST `/api/auth/login` with email.
2. **Token**: Receive a JWT token.
3. **Authorized Requests**: Include the token in the `Authorization: Bearer <token>` header for subsequent requests.

## 📝 Demo API Flow (Manual)

If you are not using the `demo` profile, follow these steps to explore the system's capabilities. Replace placeholders like `<token>`, `<tenantId>`, etc., with actual values from previous responses.

### 1. Create Tenant
Initial setup usually requires a `TENANT_ADMIN` role.

```bash
curl -X POST http://localhost:8080/api/tenants \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Acme Corp",
    "slug": "acme"
  }'
```

### 2. Create User
Add a user to the newly created tenant.

```bash
curl -X POST http://localhost:8080/api/tenants/<tenantId>/users \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@acme.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "MEMBER"
  }'
```

### 3. Login
Get a token for the user.

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@acme.com"
  }'
```

### 4. Create Workspace
Workspaces are logical containers for documents and agents.

```bash
curl -X POST http://localhost:8080/api/tenants/<tenantId>/workspaces \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Engineering Knowledge Base",
    "description": "Internal docs and agents"
  }'
```

### 5. Create Document
Upload or define a document within a workspace.

```bash
curl -X POST http://localhost:8080/api/workspaces/<workspaceId>/documents \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Onboarding Guide",
    "content": "Welcome to Acme Corp! Our mission is to build great things..."
  }'
```

### 6. Create Embeddings
Process the document into vector embeddings.

```bash
curl -X POST http://localhost:8080/api/documents/<documentId>/embeddings \
  -H "Authorization: Bearer <token>"
```

### 7. Create Agent
Create an AI agent that can access the workspace knowledge.

```bash
curl -X POST http://localhost:8080/api/workspaces/<workspaceId>/agents \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Onboarding Buddy",
    "systemPrompt": "You are a helpful assistant for new employees. Use the provided context to answer questions."
  }'
```

### 8. Ask Agent
Interact with the agent.

```bash
curl -X POST http://localhost:8080/api/agents/<agentId>/ask \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is the mission of Acme Corp?"
  }'
```

### 9. Create Workflow
Define a workflow with steps.

```bash
curl -X POST http://localhost:8080/api/workspaces/<workspaceId>/workflows \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Employee Approval",
    "description": "Process for onboarding",
    "steps": [
      {
        "name": "Initial Check",
        "type": "AUTOMATED",
        "order": 1
      },
      {
        "name": "Manager Approval",
        "type": "MANUAL_APPROVAL",
        "order": 2
      }
    ]
  }'
```

### 10. Approve Workflow
Start a run and approve it when it hits a manual step.

```bash
# Start Run
curl -X POST http://localhost:8080/api/workflows/<workflowId>/runs \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{}'

# Approve (replace <runId> with the ID from the start run response)
curl -X POST http://localhost:8080/api/workflow-runs/<runId>/approve \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "note": "Everything looks good."
  }'
```

## 🚀 Demo Profile Quick Start (Curl Examples)

When running with `-Dspring-boot.run.profiles=demo`, use these examples to interact with the pre-seeded data.

### 1. Login as Demo Admin
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "demo@acme.com"
  }'
```
*Take note of the `token` and `tenantId` in the response.*

### 2. List Workspaces
```bash
curl -X GET http://localhost:8080/api/tenants/<tenantId>/workspaces \
  -H "Authorization: Bearer <token>"
```

### 3. Ask Agent
```bash
# Get agentId from GET /api/workspaces/<workspaceId>/agents
curl -X POST http://localhost:8080/api/agents/<agentId>/ask \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What is the refund policy for logistics delays?"
  }'
```

### 4. Start Workflow
```bash
# Get workflowId from GET /api/workspaces/<workspaceId>/workflows
curl -X POST http://localhost:8080/api/workflows/<workflowId>/runs \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "input": "My package is 3 days late and I want a refund."
  }'
```

### 5. Approve Workflow
```bash
# Get runId from the start workflow response
curl -X POST http://localhost:8080/api/workflow-runs/<runId>/approve \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "note": "Valid complaint, proceeding with refund."
  }'
```
