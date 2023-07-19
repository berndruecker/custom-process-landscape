package io.berndruecker.demo.processlandscape.metadata;

public record UserTaskMetadata(
        String id,
        String name,
        String processId,
        String processName,
        String assignment) {
}
