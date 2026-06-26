package com.wajahat.aiworkflow.agent;

import com.wajahat.aiworkflow.ai.OpenAiChatClient;
import com.wajahat.aiworkflow.document.SearchRequest;
import com.wajahat.aiworkflow.document.SearchResultResponse;
import com.wajahat.aiworkflow.document.SemanticSearchService;
import com.wajahat.aiworkflow.workspace.Workspace;
import com.wajahat.aiworkflow.workspace.WorkspaceRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;
    private final WorkspaceRepository workspaceRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final SemanticSearchService semanticSearchService;
    private final OpenAiChatClient openAiChatClient;
    private final MeterRegistry meterRegistry;

    @Value("${openai.chat-model}")
    private String defaultChatModel;

    @Transactional
    public AgentResponse create(UUID workspaceId, CreateAgentRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new IllegalArgumentException("Workspace not found"));

        Agent agent = new Agent();
        agent.setWorkspace(workspace);
        agent.setName(request.name());
        agent.setDescription(request.description());
        agent.setSystemPrompt(request.systemPrompt());
        agent.setModel(request.model() == null || request.model().isBlank()
                ? defaultChatModel
                : request.model());
        agent.setStatus(AgentStatus.ACTIVE);

        return toResponse(agentRepository.save(agent));
    }

    public List<AgentResponse> findByWorkspace(UUID workspaceId) {
        return agentRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AgentResponse findById(UUID agentId) {
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found"));

        return toResponse(agent);
    }

    @Transactional
    public AskAgentResponse ask(UUID agentId, AskAgentRequest request) {
        Timer.Sample sample = Timer.start(meterRegistry);
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found"));

        Conversation conversation = resolveConversation(agent, request);

        messageRepository.save(newMessage(conversation, MessageRole.USER, request.question()));

        List<SearchResultResponse> results = semanticSearchService.search(
                agent.getWorkspace().getId(),
                new SearchRequest(request.question(), 5)
        );

        String context = buildContext(results);

        String answer = openAiChatClient.chat(
                agent.getModel(),
                agent.getSystemPrompt(),
                context,
                request.question()
        );

        messageRepository.save(newMessage(conversation, MessageRole.ASSISTANT, answer));

        meterRegistry.counter("ai.agent.calls", "agentId", agentId.toString()).increment();
        sample.stop(meterRegistry.timer("ai.agent.call.duration", "agentId", agentId.toString()));

        return new AskAgentResponse(conversation.getId(), answer);
    }

    private Conversation resolveConversation(Agent agent, AskAgentRequest request) {
        if (request.conversationId() != null) {
            return conversationRepository.findById(request.conversationId())
                    .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));
        }

        Conversation conversation = new Conversation();
        conversation.setAgent(agent);
        conversation.setTitle("Conversation with " + agent.getName());
        return conversationRepository.save(conversation);
    }

    private Message newMessage(Conversation conversation, MessageRole role, String content) {
        Message message = new Message();
        message.setConversation(conversation);
        message.setRole(role);
        message.setContent(content);
        return message;
    }

    @Transactional
    public AskAgentResponse askFromWorkflow(UUID agentId, String question) {
        return ask(agentId, new AskAgentRequest(null, question));
    }

    private String buildContext(List<SearchResultResponse> results) {
        StringBuilder builder = new StringBuilder();

        for (SearchResultResponse result : results) {
            builder.append("Document: ")
                    .append(result.documentTitle())
                    .append("\nChunk ")
                    .append(result.chunkIndex())
                    .append(":\n")
                    .append(result.content())
                    .append("\n\n");
        }

        return builder.toString();
    }

    private AgentResponse toResponse(Agent agent) {
        return new AgentResponse(
                agent.getId(),
                agent.getWorkspace().getId(),
                agent.getName(),
                agent.getDescription(),
                agent.getModel(),
                agent.getStatus()
        );
    }
}