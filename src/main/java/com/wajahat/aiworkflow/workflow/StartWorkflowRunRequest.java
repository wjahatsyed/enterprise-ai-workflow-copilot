package com.wajahat.aiworkflow.workflow;

import jakarta.validation.constraints.NotBlank;

public record StartWorkflowRunRequest(
        @NotBlank String inputJson
) {}