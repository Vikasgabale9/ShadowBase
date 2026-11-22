
package com.shadowbase.department.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
public class DepartmentResponseDto {

    private Long id;

    private String departmentName;

    private String location;

    private OffsetDateTime createdAt;
}

