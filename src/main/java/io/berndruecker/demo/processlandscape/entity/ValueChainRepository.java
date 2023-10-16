package io.berndruecker.demo.processlandscape.entity;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ValueChainRepository extends CrudRepository<ValueChain, String> {
    List<ValueChain> findAll();
}
