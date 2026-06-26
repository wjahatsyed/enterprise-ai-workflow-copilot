package com.wajahat.aiworkflow.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health", description = "Application health endpoints")
public class HealthController {

    @GetMapping("/api/health")
    @Operation(summary = "Check application health", description = "Returns a simple public liveness message.")
    public String health() {
        return "Enterprise AI Workflow Copilot is running";
    }
}
