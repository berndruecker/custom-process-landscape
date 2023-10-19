package io.berndruecker.demo.processlandscape.repo;

import io.berndruecker.demo.processlandscape.entity.FormDefinition;
import io.berndruecker.demo.processlandscape.entity.FormRepository;
import io.berndruecker.demo.processlandscape.entity.ProcessDefinition;
import io.berndruecker.demo.processlandscape.entity.ProcessRepository;
import io.berndruecker.demo.processlandscape.entity.SystemDefinition;
import io.berndruecker.demo.processlandscape.entity.SystemRepository;
import io.berndruecker.demo.processlandscape.entity.FlowNodeDefinition;
import io.berndruecker.demo.processlandscape.entity.FlowNodeRepository;
import io.berndruecker.demo.processlandscape.entity.UserRoleDefinition;
import io.berndruecker.demo.processlandscape.entity.UserRoleRepository;
import io.berndruecker.demo.processlandscape.entity.ValueChain;
import io.berndruecker.demo.processlandscape.entity.ValueChainRepository;
import io.berndruecker.demo.processlandscape.metadata.CallActicityMetadata;
import io.berndruecker.demo.processlandscape.metadata.CalledProcessMetadata;
import io.berndruecker.demo.processlandscape.metadata.ProcessMetadata;
import io.berndruecker.demo.processlandscape.metadata.ServiceTaskMetadata;
import io.berndruecker.demo.processlandscape.metadata.UserTaskMetadata;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import io.camunda.zeebe.model.bpmn.instance.BaseElement;
import io.camunda.zeebe.model.bpmn.instance.CallActivity;
import io.camunda.zeebe.model.bpmn.instance.FlowNode;
import io.camunda.zeebe.model.bpmn.instance.Lane;
import io.camunda.zeebe.model.bpmn.instance.Process;
import io.camunda.zeebe.model.bpmn.instance.ServiceTask;
import io.camunda.zeebe.model.bpmn.instance.UserTask;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeAssignmentDefinition;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeCalledElement;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeProperties;
import io.camunda.zeebe.model.bpmn.instance.zeebe.ZeebeProperty;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
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

    @Autowired
    private ProcessRepository processRepository;
    @Autowired
    private FlowNodeRepository taskRepository;
    @Autowired
    private SystemRepository systemRepository;
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private FormRepository formRepository;
    @Autowired
    private ValueChainRepository valueChainRepository;

    private List<Map.Entry<String, String>> calledProcesses = new ArrayList<>();

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
        // Add parent processes
        for (Map.Entry<String, String> processCall : calledProcesses) {
            processRepository.findById(processCall.getValue()).ifPresent(process -> {
                process.setParentProcessDefinitionId(processCall.getKey());
                processRepository.save(process);
            });
            processRepository.findByBaseIdWithoutVariant(processCall.getValue()).ifPresent(process -> {
                process.setParentProcessDefinitionId(processCall.getKey());
                processRepository.save(process);
            });
        }


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

        ProcessDefinition processDefinition = new ProcessDefinition()
                .setId(bpmn.getId())
                .setName(bpmn.getName())
                .setVariant(findProcessPropertyValue(bpmn, PROPERTY_NAME_VARIANT))
                .setValueChain(Boolean.valueOf(findProcessPropertyValue(bpmn, PROPERTY_NAME_TOP_LEVEL)));
        processRepository.save(processDefinition);

        if (processDefinition.isValueChain()) {
            valueChainRepository.save(
                    new ValueChain()
                        .setId(bpmn.getId())
                        .setName(bpmn.getName()));
        }
        retrieveFlowNodes(bpmnModel, processDefinition);

        ProcessMetadata metadata = new ProcessMetadata(
                bpmn.getId(),
                bpmn.getName(),
                findProcessPropertyValue(bpmn, PROPERTY_NAME_VARIANT),
                Boolean.valueOf(findProcessPropertyValue(bpmn, PROPERTY_NAME_TOP_LEVEL))
        );
        retrieveServiceTasks(bpmnModel, metadata);
        retrieveCallActivities(bpmnModel, metadata);
        retrieveUserTasks(bpmnModel, metadata);
        //TODO: retrieveForms();

        addProcess(metadata, processDefinition);

        System.out.println(metadata);
        return metadata;
    }

    protected void addProcess(ProcessMetadata process, ProcessDefinition processDefinition) {
        processRepository.save(processDefinition);

        processById.put(process.id(), process);

        if (!processIdByBaseIdAndVariant.containsKey(process.baseIdWithoutVariant())) {
            processIdByBaseIdAndVariant.put(process.baseIdWithoutVariant(), new HashMap<>());
        }
        processIdByBaseIdAndVariant.get(process.baseIdWithoutVariant()).put(process.variant(), process.id());
    }

    private void retrieveFlowNodes(BpmnModelInstance bpmnModel, ProcessDefinition processDefinition) {
        bpmnModel.getModelElementsByType(FlowNode.class).forEach(flowNode -> {

            FlowNodeDefinition taskDefinition = new FlowNodeDefinition()
                    .setId(flowNode.getId())
                    .setName(flowNode.getName())
                    .setType(flowNode.getElementType().getTypeName())
                    .setProcess(processDefinition);

            Lane lane = bpmnModel.getModelElementsByType(Lane.class).stream().filter(l -> l.getFlowNodeRefs().contains(flowNode)).findFirst().orElse(null);
            if (lane!=null) {
                taskDefinition.setLane(lane.getName());
            }

            if (ServiceTask.class == flowNode.getElementType().getInstanceType()) {
                String systemId = findProcessPropertyValue(flowNode, PROPERTY_NAME_SYSTEM);
                if (systemId!=null) {
                    taskDefinition.setSystem(getSystem(systemId));
                }
            }
            if (CallActivity.class == flowNode.getElementType().getInstanceType()) {
                String calledProcessId = flowNode.getSingleExtensionElement(ZeebeCalledElement.class).getProcessId();
                String calledProcessBaseId = getProcessBaseId(calledProcessId);
                taskDefinition
                    .setCalledProcessExpression(calledProcessId)
                    .setCalledProcessId(calledProcessBaseId);
                calledProcesses.add(new AbstractMap.SimpleEntry<String, String>(processDefinition.getId(), calledProcessBaseId));
            }
            if (UserTask.class == flowNode.getElementType().getInstanceType()) {
                // TODO: Shall we introduce extension properties?
                String assignment = null;
                ZeebeAssignmentDefinition assignmentDef = flowNode.getSingleExtensionElement(ZeebeAssignmentDefinition.class);
                if (assignmentDef!=null) {
                    System.out.println("Assignment by definition: " + assignmentDef.getAssignee());
                    assignment = assignmentDef.getAssignee();
                }

                if (taskDefinition.getLane()!=null) {
                    System.out.println("Assignment by lane: " + lane.getName());
                    assignment = taskDefinition.getLane();
                }

                taskDefinition.setUserRole(getUserRoleDefinition(assignment));
            }
            taskRepository.save(taskDefinition);
        });
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
            String calledProcessId = task.getSingleExtensionElement(ZeebeCalledElement.class).getProcessId();
            String calledProcessBaseId = getProcessBaseId(calledProcessId);

            List<CalledProcessMetadata> calledProcesses = new ArrayList<>();
            CallActicityMetadata metadata = new CallActicityMetadata(
                    task.getId(),
                    task.getName(),
                    calledProcessId,
                    calledProcesses);
            processMetadata.callActivities().add(metadata);
        });
    }

    private String getProcessBaseId(String calledProcessId) {
        if (calledProcessId != null && calledProcessId.indexOf("\"")>0 && calledProcessId.indexOf("\"", calledProcessId.indexOf("\"")+1) > 0) {
            // called element using an expression like
            // = "process_application_" + variantCreator
            String processBaseId = calledProcessId.substring(
                    calledProcessId.indexOf("\"") + 1,
                    calledProcessId.indexOf("\"", calledProcessId.indexOf("\"") + 1));
            if (processBaseId.endsWith("_")) {
                processBaseId = processBaseId.substring(0, processBaseId.length() - 1);
            }
            return processBaseId;
        }
        return calledProcessId;
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
        taskRepository.deleteAll();
        processRepository.deleteAll();
        valueChainRepository.deleteAll();
        systemRepository.deleteAll();
        userRoleRepository.deleteAll();
        formRepository.deleteAll();

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

    public SystemDefinition getSystem(String id) {
        if (systemRepository.existsById(id)) {
            return systemRepository.findById(id).get();
        } else {
            SystemDefinition systemDefinition = new SystemDefinition().setId(id);
            systemRepository.save(systemDefinition);
            return systemDefinition;
        }
    }
    public UserRoleDefinition getUserRoleDefinition(String id) {
        if (userRoleRepository.existsById(id)) {
            return userRoleRepository.findById(id).get();
        } else {
            UserRoleDefinition userRoleDefinition = new UserRoleDefinition().setId(id);
            userRoleRepository.save(userRoleDefinition);
            return userRoleDefinition;
        }
    }
    public FormDefinition getForm(String id) {
        if (formRepository.existsById(id)) {
            return formRepository.findById(id).get();
        } else {
            FormDefinition systemDefinition = new FormDefinition().setId(id);
            formRepository.save(systemDefinition);
            return systemDefinition;
        }
    }
}
