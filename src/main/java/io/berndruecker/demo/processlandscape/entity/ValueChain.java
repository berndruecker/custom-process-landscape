package io.berndruecker.demo.processlandscape.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class ValueChain {

    @Id
    private String id;

    private String name;

    public String getId() {
        return id;
    }

    public ValueChain setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ValueChain setName(String name) {
        this.name = name;
        return this;
    }
}
