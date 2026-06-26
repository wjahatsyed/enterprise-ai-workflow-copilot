## Summary

- Add comprehensive README.md with project overview, features, and architecture documentation.
- Document multi-tenancy implementation with tenant isolation at the data level enforced via `TenantContext`.
- Explain document knowledge base and semantic search capabilities using OpenAI embeddings.
- Describe AI agent configuration and workflow orchestration with human-in-the-loop approvals.
- Detail Action Framework and Outbox Pattern for reliable domain event publishing.
- Provide setup instructions for Docker Compose (PostgreSQL and Redis) and application startup.
- Document JWT-based authentication flow with demo mode using email-based login.
- Include complete demo API flow with 10 practical examples covering:
  - Tenant and user creation
  - Login and authentication
  - Workspace and document management
  - Embedding generation for semantic search
  - AI agent creation and interaction
  - Workflow definition and execution
  - Human approval workflows
- Add Technology Stack section (Java 25, Spring Boot 4.1.0, PostgreSQL 17, Redis 7, OpenAI API).
- Include curl examples for easy API testing and exploration.
- Provide Swagger UI documentation link.

## Validation

- `./mvnw.cmd clean test` passed.
- Result: 21 tests, 0 failures, 0 errors.

## Notes

This PR provides comprehensive documentation and demo workflow examples to help new developers and users quickly understand and explore the Enterprise AI Workflow Copilot platform's capabilities.
