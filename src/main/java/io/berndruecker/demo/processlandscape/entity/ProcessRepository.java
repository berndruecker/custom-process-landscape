package io.berndruecker.demo.processlandscape.entity;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ProcessRepository extends CrudRepository<ProcessDefinition, String> {
    List<ProcessDefinition> findAll();
    Optional<ProcessDefinition> findByBaseIdWithoutVariant(String baseIdWithoutVariant);
}
