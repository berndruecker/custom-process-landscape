package io.berndruecker.demo.processlandscape.entity;

import org.springframework.data.repository.CrudRepository;

public interface UserRoleRepository extends CrudRepository<UserRoleDefinition, String> {
}
