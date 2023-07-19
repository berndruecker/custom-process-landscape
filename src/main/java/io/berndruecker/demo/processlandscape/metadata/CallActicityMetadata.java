package io.berndruecker.demo.processlandscape.metadata;

import java.util.List;

public record CallActicityMetadata(
        String id,
        String name,
        String calledElementExpression,
        List<CalledProcessMetadata> calledProcesses) {
}
