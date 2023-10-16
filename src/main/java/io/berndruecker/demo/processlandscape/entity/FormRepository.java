package io.berndruecker.demo.processlandscape.entity;

import org.springframework.data.repository.CrudRepository;

public interface FormRepository extends CrudRepository<FormDefinition, String> {
}
