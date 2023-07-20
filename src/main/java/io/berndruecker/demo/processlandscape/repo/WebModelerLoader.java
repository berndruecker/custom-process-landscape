package io.berndruecker.demo.processlandscape.repo;

import io.berndruecker.demo.processlandscape.metadata.ProcessMetadata;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import jakarta.annotation.PostConstruct;
import org.camunda.community.webmodeler.client.dto.FileDto;
import org.camunda.community.webmodeler.client.dto.ProjectDto;
import org.camunda.community.webmodeler.client.invoker.ApiException;
import org.camunda.community.webmodeler.client.springboot.CamundaWebModelerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "landscape.repository", havingValue = "webmodeler")
public class WebModelerLoader {

    @Autowired
    private CamundaWebModelerApi camundaWebModelerApi;

    @Value("${landscape.webmodeler.projectId}")
    private String projectId;

    @Autowired
    private ModelInformationRepository repository;

    @PostConstruct
    public void loadModels() throws Exception {
        repository.reset();
        try {
            ProjectDto project = camundaWebModelerApi.projectsApi().getProject(UUID.fromString(projectId));
            project.getContent().getFiles().forEach(file -> {
                try {
                    FileDto fileData = camundaWebModelerApi.filesApi().getFile(UUID.fromString(file.getId()));
                    String fileContentAdString = fileData.getContent();
                    String fileType = fileData.getMetadata().getType();
                    //System.out.println("File: " + file.getName() + " |  " + fileType);

                    if ("BPMN".equalsIgnoreCase(fileType)) {
                        BpmnModelInstance bpmnModel = Bpmn.readModelFromStream(new ByteArrayInputStream(fileContentAdString.getBytes()));
                        ProcessMetadata processMetadata = repository.processBpmnModel(bpmnModel);
                        repository.addProcessXml(processMetadata.id(), fileContentAdString);
                    }

                } catch (ApiException e) {
                    throw new RuntimeException("Could not load file: " + file.getName(), e);
                }
            });

        } catch (ApiException e) {
            System.out.println("Project not found: " + projectId + "(" + e.getMessage() + ")");
            System.out.println("Select from existing projects:\n" +
                    camundaWebModelerApi.projectsApi().listProjects());
        }
        repository.postProcessRelationships();
    }
}
