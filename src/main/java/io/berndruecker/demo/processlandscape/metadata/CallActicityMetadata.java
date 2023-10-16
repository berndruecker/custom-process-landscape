package io.berndruecker.demo.processlandscape.metadata;

import org.aspectj.lang.annotation.DeclarePrecedence;

import java.util.List;

@Deprecated
public record CallActicityMetadata(
        String id,
        String name,
        String calledElementExpression,
        List<CalledProcessMetadata> calledProcesses) {
}
