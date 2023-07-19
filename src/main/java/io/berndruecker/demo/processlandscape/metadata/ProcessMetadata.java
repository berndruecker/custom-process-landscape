package io.berndruecker.demo.processlandscape.metadata;

import java.util.ArrayList;
import java.util.List;

public record ProcessMetadata(
        String id,
        String name,
        String variant,
        boolean topLevel,
        String parentProcessId,
        String parentProcessName,
        List<ServiceTaskMetadata> serviceTasks,
        List<CallActicityMetadata> callActivities) {

    public ProcessMetadata(String id,
                           String name,
                           String variant,
                           boolean topLevel) {
        this(id, name, variant, topLevel, null, null, new ArrayList<>(), new ArrayList<>());
    }

    public ProcessMetadata withParent(String parentProcessId, String parentProcessName) {
        return new ProcessMetadata(id, name, variant, topLevel, parentProcessId, parentProcessName, serviceTasks, callActivities);
    }

    public String baseIdWithoutVariant() {
        return id.substring(0, id.lastIndexOf("_"));
    }

}
