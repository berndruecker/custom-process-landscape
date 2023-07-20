package io.berndruecker.demo.processlandscape.repo;

import io.berndruecker.demo.processlandscape.metadata.CallActicityMetadata;
import io.berndruecker.demo.processlandscape.metadata.CalledProcessMetadata;
import io.berndruecker.demo.processlandscape.metadata.ProcessMetadata;
import io.berndruecker.demo.processlandscape.metadata.ServiceTaskMetadata;
import io.berndruecker.demo.processlandscape.metadata.UserTaskMetadata;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.instance.BaseElement;
import io.camunda.zeebe.model.bpmn.instance.CallActivity;
import io.camunda.zeebe.model.bpmn.instance.Lane;
import io.camunda.zeebe.model.bpmn.instance.Process;
import io.camunda.zeebe.model.bpmn.instance.ServiceTask;
import io.camunda.zeebe.model.bpmn.instance.UserTask;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeAssignmentDefinition;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeCalledElement;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeProperties;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeProperty;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ModelInformationRepository {

    private Map<String, ProcessMetadata> processById= new HashMap<>();
    private Map<String, String> processXmlById = new HashMap<>();
    private Map<String, Map<String, String>> processIdByBaseIdAndVariant = new HashMap<>();
    private Map<String, List<ServiceTaskMetadata>> serviceTasksBySystemId = new HashMap<>();
    private Map<String, List<UserTaskMetadata>> userTasksByAssignment = new HashMap<>();

    public static String PROPERTY_NAME_TOP_LEVEL = "topLevel";
    public static String PROPERTY_NAME_VARIANT = "variant";
    public static String PROPERTY_NAME_SYSTEM = "system";
    public static final String VARIANT_DEFAULT = "default";

    public List<ProcessMetadata> getTopLevelProcesses() {
        return processById.values().stream().filter(ProcessMetadata::topLevel).toList();
    }

    public List<String> getVariants() {
        return processById.values().stream().map(ProcessMetadata::variant).distinct().toList();
    }

    public ProcessMetadata getProcess(String id) {
        return processById.get(id);
    }

    public String getProcessXml(String id) {
        return processXmlById.get(id);
    }

    /**
     * Go over all models to discover relationships of call activities with called processes using
     * expressions and variants
     */
    public void postProcessRelationships() {
        // Find called processes
        for (ProcessMetadata process : processById.values()) {
            for (CallActicityMetadata callActivity : process.callActivities()) {
                String calledElementExpression = callActivity.calledElementExpression();
                if (calledElementExpression != null) {

                    if (calledElementExpression.indexOf("\"")>0 && calledElementExpression.indexOf("\"", calledElementExpression.indexOf("\"")+1) > 0) {
                        // called element using an expression like
                        // = "process_application_" + variantCreator
                        String processBaseId = calledElementExpression.substring(calledElementExpression.indexOf("\"")
                                + 1, calledElementExpression.indexOf("\"", calledElementExpression.indexOf("\"") + 1));
                        if (processBaseId.endsWith("_")) {
                            processBaseId = processBaseId.substring(0, processBaseId.length() - 1);
                        }
                        Map<String, String> processIdByVariant = processIdByBaseIdAndVariant.get(processBaseId);
                        if (processIdByVariant!=null) {
                            for (Map.Entry<String, String> entry : processIdByVariant.entrySet()) {
                                addCalledProcess(process, callActivity, entry.getKey(), entry.getValue());
                            }
                        }
                    } else {
                        // no expression, just use the process linked as default variant
                        addCalledProcess(process, callActivity, VARIANT_DEFAULT, calledElementExpression);
                    }
                }
            }
        }
    }

    protected void addCalledProcess(ProcessMetadata process, CallActicityMetadata callActivity, String variant, String processId) {
        String processName = processId;
        if (processById.containsKey(processId)) {
            processName = processById.get(processId).name();
        }
        callActivity.calledProcesses().add(
                new CalledProcessMetadata(variant, processId, processName)
        );

        // also add the link to the parent, therefore overwrite the existing metadata
        if (processById.containsKey(processId)) {
            ProcessMetadata processWithParent = processById.get(processId).withParent(process.id(), process.name());
            processById.put(processId, processWithParent);
        } // TODO: else log warning
    }

    public ProcessMetadata processBpmnModel(BpmnModelInstance bpmnModel) {
        Process bpmn = getBpmnProcess(bpmnModel);
        ProcessMetadata metadata = new ProcessMetadata(
                bpmn.getId(),
                bpmn.getName(),
                findProcessPropertyValue(bpmn, PROPERTY_NAME_VARIANT),
                Boolean.valueOf(findProcessPropertyValue(bpmn, PROPERTY_NAME_TOP_LEVEL))
        );
        retrieveServiceTasks(bpmnModel, metadata);
        retrieveCallActivities(bpmnModel, metadata);
        retrieveUserTasks(bpmnModel, metadata);

        addProcess(metadata);

        System.out.println(metadata);
        return metadata;
    }

    protected void addProcess(ProcessMetadata process) {
        processById.put(process.id(), process);

        if (!processIdByBaseIdAndVariant.containsKey(process.baseIdWithoutVariant())) {
            processIdByBaseIdAndVariant.put(process.baseIdWithoutVariant(), new HashMap<>());
        }
        processIdByBaseIdAndVariant.get(process.baseIdWithoutVariant()).put(process.variant(), process.id());
    }

    protected void retrieveServiceTasks(BpmnModelInstance bpmnModel, ProcessMetadata processMetadata) {
        bpmnModel.getModelElementsByType(ServiceTask.class).forEach(serviceTask -> {
            ServiceTaskMetadata taskMetadata = new ServiceTaskMetadata(
                    serviceTask.getId(),
                    serviceTask.getName(),
                    processMetadata.id(),
                    processMetadata.name(),
                    findProcessPropertyValue(serviceTask, PROPERTY_NAME_SYSTEM));
            processMetadata.serviceTasks().add(taskMetadata);
            if (!serviceTasksBySystemId.containsKey(taskMetadata.system())) {
                serviceTasksBySystemId.put(taskMetadata.system(), new ArrayList<>());
            }
            serviceTasksBySystemId.get(taskMetadata.system()).add(taskMetadata);
        });
    }

    protected void retrieveCallActivities(BpmnModelInstance bpmnModel, ProcessMetadata processMetadata) {
        bpmnModel.getModelElementsByType(CallActivity.class).forEach(task -> {
            List<CalledProcessMetadata> calledProcesses = new ArrayList<>();
            CallActicityMetadata metadata = new CallActicityMetadata(
                    task.getId(),
                    task.getName(),
                    task.getSingleExtensionElement(ZeebeCalledElement.class).getProcessId(),
                    calledProcesses);
            processMetadata.callActivities().add(metadata);
        });
    }

    protected void retrieveUserTasks(BpmnModelInstance bpmnModel, ProcessMetadata process) {
        bpmnModel.getModelElementsByType(UserTask.class).forEach(task -> {

            // TODO: Shall we introduce extension properties?
            String assignment = null;
            ZeebeAssignmentDefinition assignmentDef = task.getSingleExtensionElement(ZeebeAssignmentDefinition.class);
            if (assignmentDef!=null) {
                System.out.println("Assignment by definition: " + assignmentDef.getAssignee());
                assignment = assignmentDef.getAssignee();
            }

            Lane lane = bpmnModel.getModelElementsByType(Lane.class).stream().filter(l -> l.getFlowNodeRefs().contains(task)).findFirst().orElse(null);
            if (lane!=null) {
                System.out.println("Assignment by lane: " + lane.getName());
                assignment = lane.getName();
            }

            UserTaskMetadata metadata = new UserTaskMetadata(
                    task.getId(),
                    task.getName(),
                    process.id(),
                    process.name(),
                    assignment);
            process.userTasks().add(metadata);
            if (!userTasksByAssignment.containsKey(metadata.assignment())) {
                userTasksByAssignment.put(metadata.assignment(), new ArrayList<>());
            }
            userTasksByAssignment.get(metadata.assignment()).add(metadata);
        });
    }

    @NotNull
    protected Process getBpmnProcess(BpmnModelInstance bpmnModel) {
        return bpmnModel.getModelElementsByType(Process.class).stream().findFirst().get();
    }

    private String findProcessPropertyValue(BaseElement bpmn, String propertyName) {
        ZeebeProperties zeebeProperties = bpmn.getSingleExtensionElement(ZeebeProperties.class);
        if (zeebeProperties!=null && !zeebeProperties.getProperties().isEmpty()) {
            Optional<ZeebeProperty> zeebeProperty = zeebeProperties.getProperties().stream().filter(p -> propertyName.equals(p.getName())).findFirst();
            if (zeebeProperty.isPresent()) {
                return zeebeProperty.get().getValue();
            }
        }
        return null;
    }

    public void addProcessXml(String id, String xmlString) {
        processXmlById.put(id, xmlString);
    }

    public void reset() {
        processById = new HashMap<>();
        processXmlById = new HashMap<>();
        processIdByBaseIdAndVariant = new HashMap<>();
        serviceTasksBySystemId = new HashMap<>();
        userTasksByAssignment = new HashMap<>();
    }

    public List<ServiceTaskMetadata> getSystemUsage(String systemId) {
        return serviceTasksBySystemId.get(systemId);
    }

    public List<UserTaskMetadata> getUserUsage(String assignment) {
        return userTasksByAssignment.get(assignment);
    }
}
