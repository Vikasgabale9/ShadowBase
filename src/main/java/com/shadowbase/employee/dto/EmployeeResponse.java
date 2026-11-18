
package com.shadowbase.employee.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
public class EmployeeResponse {

    private Long id;

    private String fullName;

    private String email;

    private String phone;

    private BigDecimal salary;

    private LocalDate hireDate;

    private String status;

    private Long departmentId;

    private String departmentName;

    private OffsetDateTime createdAt;

    private OffsetDateTime updatedAt;
}
