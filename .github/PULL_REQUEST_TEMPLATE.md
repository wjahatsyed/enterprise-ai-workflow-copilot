## Summary

- Add API Key authentication as alternative to JWT for programmatic access.
- Introduce `ApiKey` entity, `ApiKeyRepository`, and `ApiKeyService` with SHA-256 hashing for secure key storage.
- Add `ApiKeyAuthenticationFilter` to validate and authenticate requests using `X-API-Key` header.
- Implement outbox pattern for reliable domain event publishing with `OutboxEvent`, `OutboxEventService`, and `OutboxEventProcessor`.
- Add `TenantContext` and `TenantFilter` for tenant isolation across authenticated requests.
- Update security configuration to support stateless authentication with JWT, API Key, and tenant context filters.
- Add `@PreAuthorize` annotations across all endpoints (`TenantController`, `AppUserController`, `WorkspaceController`, `WorkspaceMemberController`, `DocumentController`, `SearchController`, `AgentController`, `WorkflowController`, `EmbeddingController`) for role-based access control with `TENANT_ADMIN` and `MEMBER` roles.
- Enable method security and update repositories to support tenant-scoped queries across workflows, runs, and workspaces.
- Update tests with security configurations and add comprehensive test coverage for API key authentication, JWT token generation/validation, and role-based access control.

## Validation

- `./mvnw.cmd clean test` passed.
- Result: 21 tests, 0 failures, 0 errors.

## Notes

This PR implements API key authentication, tenant isolation, and event-driven architecture patterns to enhance security, multi-tenancy, and system reliability.
