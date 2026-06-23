package com.wajahat.aiworkflow.document;

import jakarta.validation.constraints.NotBlank;

public record SearchRequest(
        @NotBlank String query,
        Integer topK
) {}