package com.wajahat.aiworkflow.agent;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Optional<Conversation> findByIdAndAgentWorkspaceTenantId(UUID id, UUID tenantId);
}
