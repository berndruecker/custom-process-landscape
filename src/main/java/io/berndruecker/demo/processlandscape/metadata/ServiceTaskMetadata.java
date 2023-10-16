package io.berndruecker.demo.processlandscape.metadata;

@Deprecated
public record ServiceTaskMetadata (
        String id,
        String name,
        String processId,
        String processName,
        String system) {
}
