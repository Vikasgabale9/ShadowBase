package com.shadowbase.project.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectResponseDto {

    private Long id;

    private String projectName;

    private String description;

    private String status;

    private LocalDate startDate;

    private LocalDate endDate;

    private OffsetDateTime createdAt;
}
