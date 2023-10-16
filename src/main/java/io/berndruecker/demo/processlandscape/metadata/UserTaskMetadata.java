package io.berndruecker.demo.processlandscape.metadata;

@Deprecated
public record UserTaskMetadata(
        String id,
        String name,
        String processId,
        String processName,
        String assignment) {
}
