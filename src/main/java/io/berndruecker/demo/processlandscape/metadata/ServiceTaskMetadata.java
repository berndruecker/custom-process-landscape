package io.berndruecker.demo.processlandscape.metadata;

public record ServiceTaskMetadata (
        String id,
        String name,
        String processId,
        String processName,
        String system) {
}
