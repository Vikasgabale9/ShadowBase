package com.shadowbase.employeeproject.repository;

import com.shadowbase.employeeproject.entity.EmployeeProject;
import com.shadowbase.employeeproject.entity.EmployeeProjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeProjectRepository extends JpaRepository<EmployeeProject, EmployeeProjectId> {
}
