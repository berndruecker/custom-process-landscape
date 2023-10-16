package io.berndruecker.demo.processlandscape.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class TaskDefinition {

    @Id
    private String id;

    private String name;

    private String type;

    public static final String TYPE_SYSTEM = "SYSTEM";
    public static final String TYPE_USER = "USER";
    public static final String TYPE_CALLACTIVITY = "SUBPROCESS";

    @ManyToOne
    private ProcessDefinition process;

    @ManyToOne
    private SystemDefinition system;
    @ManyToOne
    private UserRoleDefinition userRole;
    @ManyToOne
    private FormDefinition form;
    //@ManyToOne
    private String calledProcessId;

    public String getId() {
        return id;
    }

    public TaskDefinition setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public TaskDefinition setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public TaskDefinition setType(String type) {
        this.type = type;
        return this;
    }

    public ProcessDefinition getProcess() {
        return process;
    }

    public TaskDefinition setProcess(ProcessDefinition process) {
        this.process = process;
        return this;
    }

    public SystemDefinition getSystem() {
        return system;
    }

    public TaskDefinition setSystem(SystemDefinition system) {
        this.system = system;
        return this;
    }

    public UserRoleDefinition getUserRole() {
        return userRole;
    }

    public TaskDefinition setUserRole(UserRoleDefinition userRole) {
        this.userRole = userRole;
        return this;
    }

    public FormDefinition getForm() {
        return form;
    }

    public TaskDefinition setForm(FormDefinition form) {
        this.form = form;
        return this;
    }

    public TaskDefinition setCalledProcessId(String calledProcessId) {
        this.calledProcessId = calledProcessId;
        return this;
    }
}
