
package com.shadowbase.employeeproject.entity;

import com.shadowbase.employee.entity.Employee;
import com.shadowbase.project.entity.Project;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "employee_projects")
@Getter
@Setter
public class EmployeeProject {

    @EmbeddedId
    private EmployeeProjectId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("employeeId")
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private OffsetDateTime assignedAt;

    @Column(name = "role_on_project", length = 100)
    private String roleOnProject;

    @PrePersist
    protected void onCreate() {
        assignedAt = OffsetDateTime.now();
    }
}
