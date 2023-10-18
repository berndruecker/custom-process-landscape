package io.berndruecker.demo.processlandscape.entity;

import org.springframework.data.repository.CrudRepository;

public interface FlowNodeRepository extends CrudRepository<FlowNodeDefinition, String> {
}
