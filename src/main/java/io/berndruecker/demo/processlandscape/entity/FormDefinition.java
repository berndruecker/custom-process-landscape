package io.berndruecker.demo.processlandscape.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class FormDefinition {

    @Id
    private String id;

    private String name;

    public String getId() {
        return id;
    }

    public FormDefinition setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public FormDefinition setName(String name) {
        this.name = name;
        return this;
    }
}
