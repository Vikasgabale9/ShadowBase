package com.shadowbase.project.service;

import com.shadowbase.project.dto.ProjectRequestDto;
import com.shadowbase.project.dto.ProjectResponseDto;

import java.util.List;

public interface ProjectService {

    ProjectResponseDto createProject(ProjectRequestDto requestDto);

    ProjectResponseDto updateProject(Long id, ProjectRequestDto requestDto);

    ProjectResponseDto getProjectById(Long id);

    List<ProjectResponseDto> getAllProjects();

    void deleteProject(Long id);
}
