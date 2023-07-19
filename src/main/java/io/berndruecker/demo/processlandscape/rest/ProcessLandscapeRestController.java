package io.berndruecker.demo.processlandscape.rest;

import io.berndruecker.demo.processlandscape.metadata.ProcessMetadata;
import io.berndruecker.demo.processlandscape.metadata.ServiceTaskMetadata;
import io.berndruecker.demo.processlandscape.repo.ModelInformationRepository;
import org.camunda.community.webmodeler.client.springboot.CamundaWebModelerApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class ProcessLandscapeRestController {

  @Autowired
  private ModelInformationRepository repository;

  @GetMapping("/processes")
  public ResponseEntity<List<ProcessMetadata>> getTopLevelProcesses() {
    return ResponseEntity.ok(repository.getTopLevelProcesses());
  }

  @GetMapping("/processes/{id}")
  public ResponseEntity<ProcessMetadata> getProcess(@PathVariable("id") String id) {
    return ResponseEntity.ok(repository.getProcess(id));
  }

  @GetMapping("/processes/{id}/xml")
  public ResponseEntity<String> getProcessXml(@PathVariable("id") String id) {
    return ResponseEntity.ok(repository.getProcessXml(id));
  }

  @GetMapping("/systems/{id}/usage")
  public ResponseEntity<List<ServiceTaskMetadata>> getSystem(@PathVariable("id") String systemId) {
    return ResponseEntity.ok(repository.getSystemUsage(systemId));
  }
}
