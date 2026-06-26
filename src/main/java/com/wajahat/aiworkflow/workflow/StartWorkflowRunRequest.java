package com.wajahat.aiworkflow.workflow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record StartWorkflowRunRequest(
        @Schema(example = "{\"ticketId\":\"SUP-1001\",\"priority\":\"HIGH\"}")
        @NotBlank String inputJson
) {}
