package com.wajahat.aiworkflow.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Enterprise AI Workflow Copilot API",
                version = "0.0.1",
                description = "APIs for tenant-scoped workspaces, documents, agents, and workflow automation.",
                contact = @Contact(name = "Enterprise AI Workflow Copilot")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    @Bean
    OpenAPI enterpriseAiWorkflowOpenApi() {
        return new OpenAPI();
    }

    @Bean
    GroupedOpenApi platformApi() {
        return GroupedOpenApi.builder()
                .group("platform")
                .pathsToMatch("/api/auth/**", "/api/health", "/api/tenants/**", "/api/tenants/*/users/**")
                .build();
    }

    @Bean
    GroupedOpenApi knowledgeApi() {
        return GroupedOpenApi.builder()
                .group("knowledge")
                .pathsToMatch("/api/workspaces/**", "/api/documents/**")
                .build();
    }

    @Bean
    GroupedOpenApi aiApi() {
        return GroupedOpenApi.builder()
                .group("ai")
                .pathsToMatch("/api/agents/**", "/api/workspaces/*/agents/**", "/api/workspaces/*/search")
                .build();
    }

    @Bean
    GroupedOpenApi workflowApi() {
        return GroupedOpenApi.builder()
                .group("workflows")
                .pathsToMatch("/api/workflows/**", "/api/workflow-runs/**", "/api/workspaces/*/workflows/**")
                .build();
    }
}
