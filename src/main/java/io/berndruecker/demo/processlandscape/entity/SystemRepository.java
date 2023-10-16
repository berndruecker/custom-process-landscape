package io.berndruecker.demo.processlandscape.entity;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SystemRepository extends CrudRepository<SystemDefinition, String> {
}
