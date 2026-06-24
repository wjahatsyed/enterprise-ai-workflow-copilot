package com.wajahat.aiworkflow.agent;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentRepository extends JpaRepository<Agent, UUID> {
    List<Agent> findByWorkspaceId(UUID workspaceId);
}