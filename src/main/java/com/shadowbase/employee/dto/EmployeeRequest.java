
package com.shadowbase.employee.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class EmployeeRequest {

    @NotBlank(message = "Employee name is required")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String phone;

    @NotNull(message = "Salary is required")
    @DecimalMin(value = "0.00", message = "Salary cannot be negative")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal salary;

    private LocalDate hireDate;

    @NotBlank(message = "Employee status is required")
    @Pattern(
            regexp = "ACTIVE|INACTIVE|ON_LEAVE",
            message = "Status must be ACTIVE, INACTIVE, or ON_LEAVE"
    )
    private String status = "ACTIVE";

    @NotNull(message = "Department ID is required")
    @Positive(message = "Department ID must be positive")
    private Long departmentId;
}