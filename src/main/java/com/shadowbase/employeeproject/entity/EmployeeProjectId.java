
package com.shadowbase.employeeproject.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
public class EmployeeProjectId implements Serializable {

    @Column(name = "employee_id")
    private Long employeeId;

    @Column(name = "project_id")
    private Long projectId;

    public EmployeeProjectId() {
    }

    public EmployeeProjectId(Long employeeId, Long projectId) {
        this.employeeId = employeeId;
        this.projectId = projectId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof EmployeeProjectId that)) return false;

        return Objects.equals(employeeId, that.employeeId)
                && Objects.equals(projectId, that.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, projectId);
    }
}
