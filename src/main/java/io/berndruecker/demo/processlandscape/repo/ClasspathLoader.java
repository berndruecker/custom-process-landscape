package io.berndruecker.demo.processlandscape.repo;

import io.berndruecker.demo.processlandscape.metadata.ProcessMetadata;
import io.camunda.zeebe.model.bpmn.Bpmn;
import io.camunda.zeebe.model.bpmn.BpmnModelInstance;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@ConditionalOnProperty(name = "landscape.repository", havingValue = "classpath")
public class ClasspathLoader {

    @Autowired
    private ModelInformationRepository repository;

    @Value("classpath:**/*.bpmn")
    private Resource[] processModels;

    @PostConstruct
    public void loadModels() throws Exception {
        repository.reset();
        for (Resource processModelResource : processModels) {
            BpmnModelInstance bpmnModel = Bpmn.readModelFromStream(processModelResource.getInputStream());
            ProcessMetadata processMetadata = repository.processBpmnModel(bpmnModel);
            repository.addProcessXml(processMetadata.id(), processModelResource.getContentAsString(StandardCharsets.UTF_8));
        }
        repository.postProcessRelationships();
    }
}
