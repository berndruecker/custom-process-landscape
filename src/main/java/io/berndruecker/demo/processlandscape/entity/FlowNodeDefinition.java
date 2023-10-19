package io.berndruecker.demo.processlandscape.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class FlowNodeDefinition {

    @Id
    private String id;

    private String name;

    private String type;

    public static final String TYPE_SYSTEM = "SYSTEM";
    public static final String TYPE_USER = "USER";
    public static final String TYPE_CALLACTIVITY = "SUBPROCESS";

    private String lane;

    @ManyToOne
    private ProcessDefinition process;

    @ManyToOne
    private SystemDefinition system;
    @ManyToOne
    private UserRoleDefinition userRole;
    @ManyToOne
    private FormDefinition form;

    // Base id of called process (without variant). If no variantes are used - this is the process id
    private String calledProcessId;
    private String calledProcessExpression;

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public String getId() {
        return id;
    }

    public FlowNodeDefinition setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public FlowNodeDefinition setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public FlowNodeDefinition setType(String type) {
        this.type = type;
        return this;
    }

    public ProcessDefinition getProcess() {
        return process;
    }

    public FlowNodeDefinition setProcess(ProcessDefinition process) {
        this.process = process;
        return this;
    }

    public SystemDefinition getSystem() {
        return system;
    }

    public FlowNodeDefinition setSystem(SystemDefinition system) {
        this.system = system;
        return this;
    }

    public UserRoleDefinition getUserRole() {
        return userRole;
    }

    public FlowNodeDefinition setUserRole(UserRoleDefinition userRole) {
        this.userRole = userRole;
        return this;
    }

    public FormDefinition getForm() {
        return form;
    }

    public FlowNodeDefinition setForm(FormDefinition form) {
        this.form = form;
        return this;
    }

    public FlowNodeDefinition setCalledProcessId(String calledProcessId) {
        this.calledProcessId = calledProcessId;
        return this;
    }

    public String getCalledProcessId() {
        return calledProcessId;
    }

    public String getCalledProcessExpression() {
        return calledProcessExpression;
    }

    public FlowNodeDefinition setCalledProcessExpression(String calledProcessExpression) {
        this.calledProcessExpression = calledProcessExpression;
        return this;
    }
}
