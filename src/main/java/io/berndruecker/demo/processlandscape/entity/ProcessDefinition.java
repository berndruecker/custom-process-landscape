package io.berndruecker.demo.processlandscape.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ProcessDefinition {

    @Id
    private String id;

    private String name;
    private String variant;
    private String type;
    private boolean isValueChain;

    @ManyToOne
    private ValueChain valueChain;

    public String getId() {
        return id;
    }

    public ProcessDefinition setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public ProcessDefinition setName(String name) {
        this.name = name;
        return this;
    }

    public String getVariant() {
        return variant;
    }

    public ProcessDefinition setVariant(String variant) {
        this.variant = variant;
        return this;
    }

    public String getType() {
        return type;
    }

    public ProcessDefinition setType(String type) {
        this.type = type;
        return this;
    }

    public ValueChain getValueChain() {
        return valueChain;
    }

    public ProcessDefinition setValueChain(ValueChain valueChain) {
        this.valueChain = valueChain;
        return this;
    }

    public boolean isValueChain() {
        return isValueChain;
    }

    public ProcessDefinition setValueChain(boolean valueChain) {
        isValueChain = valueChain;
        return this;
    }
}
