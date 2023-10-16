package io.berndruecker.demo.processlandscape.entity;

import org.springframework.data.repository.CrudRepository;

public interface TaskRepository extends CrudRepository<TaskDefinition, String> {
}
