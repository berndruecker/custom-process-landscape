package io.berndruecker.demo.processlandscape.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class ProcessDefinition {

    @Id
    private String id;
    
    private String baseIdWithoutVariant;

    private String name;
    private String variant;
    private String type;
    private boolean isValueChain;

    @ManyToOne
    private ValueChain valueChain;

    public void deriveBaseIdWithoutVariant() {
        if (id!=null && variant != null && id.indexOf(variant)>0) {
            baseIdWithoutVariant = id.substring(0, id.lastIndexOf(variant));
            if (baseIdWithoutVariant.endsWith("_")) {
                baseIdWithoutVariant = baseIdWithoutVariant.substring(0, baseIdWithoutVariant.length() - 1);
            }
        }
    }

    public String getBaseIdWithoutVariant() {
        return baseIdWithoutVariant;
    }

    public String getId() {
        return id;
    }

    public ProcessDefinition setId(String id) {
        this.id = id;
        deriveBaseIdWithoutVariant();
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
        deriveBaseIdWithoutVariant();
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
