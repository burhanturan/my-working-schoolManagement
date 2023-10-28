package com.project.schoolmanagment.entity.enums;

public enum RoleType {

    ADMIN("Admin"),
    TEACHER("Teacher"),
    STUDENT("Student"),
    MANAGER("Dean"),
    ASSISTANT_MANAGER("VideDean");
    public final String name;

    RoleType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
