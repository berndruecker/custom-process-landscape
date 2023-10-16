package io.berndruecker.demo.processlandscape.entity;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProcessRepository extends CrudRepository<ProcessDefinition, String> {
    List<ProcessDefinition> findAll();
}
