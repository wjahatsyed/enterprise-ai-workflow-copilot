package com.wajahat.aiworkflow.document;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateDocumentRequest(
        @Schema(example = "Support escalation policy")
        @NotBlank String title,
        @Schema(example = "Escalate enterprise support requests when priority is high or SLA risk is detected.")
        @NotBlank String content
) {}
