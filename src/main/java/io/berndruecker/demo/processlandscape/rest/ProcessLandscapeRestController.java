package io.berndruecker.demo.processlandscape.rest;

import io.berndruecker.demo.processlandscape.metadata.ProcessMetadata;
import io.berndruecker.demo.processlandscape.metadata.ServiceTaskMetadata;
import io.berndruecker.demo.processlandscape.metadata.UserTaskMetadata;
import io.berndruecker.demo.processlandscape.repo.ModelInformationRepository;
import io.berndruecker.demo.processlandscape.repo.WebModelerLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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

  @GetMapping("/users/{id}/usage")
  public ResponseEntity<List<UserTaskMetadata>> getAssignment(@PathVariable("id") String assignment) {
    return ResponseEntity.ok(repository.getUserUsage(assignment));
  }

  @Autowired
  private WebModelerLoader loader;

  @GetMapping("/reload")
  public ResponseEntity<String> reload() throws Exception {
    long millis = System.currentTimeMillis();
    loader.loadModels();
    return ResponseEntity.ok("Reloaded in " + (System.currentTimeMillis() - millis) + " ms");
  }
}
