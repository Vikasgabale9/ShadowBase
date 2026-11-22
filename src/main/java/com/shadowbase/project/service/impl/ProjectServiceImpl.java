package com.shadowbase.project.service.impl;

import com.shadowbase.project.dto.ProjectRequestDto;
import com.shadowbase.project.dto.ProjectResponseDto;
import com.shadowbase.project.entity.Project;
import com.shadowbase.project.repository.ProjectRepository;
import com.shadowbase.project.service.ProjectService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

//import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public ProjectResponseDto createProject(ProjectRequestDto requestDto) {

        Project project = new Project();

        project.setProjectName(requestDto.getProjectName());
        project.setDescription(requestDto.getDescription());

        project.setStatus(
            requestDto.getStatus() == null || requestDto.getStatus().isBlank()
                ? "PLANNED"
                : requestDto.getStatus()
        );

        project.setStartDate(requestDto.getStartDate());
        project.setEndDate(requestDto.getEndDate());
//        project.setCreatedAt(OffsetDateTime.now());

        Project savedProject = projectRepository.save(project);

        return mapToResponseDto(savedProject);
    }

    @Override
    public ProjectResponseDto updateProject(Long id, ProjectRequestDto requestDto) {

        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Project not found with id: " + id
            ));

        project.setProjectName(requestDto.getProjectName());
        project.setDescription(requestDto.getDescription());
        project.setStatus(requestDto.getStatus());
        project.setStartDate(requestDto.getStartDate());
        project.setEndDate(requestDto.getEndDate());

        Project updatedProject = projectRepository.save(project);

        return mapToResponseDto(updatedProject);
    }

    @Override
    public ProjectResponseDto getProjectById(Long id) {

        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Project not found with id: " + id
            ));

        return mapToResponseDto(project);
    }

    @Override
    public List<ProjectResponseDto> getAllProjects() {

        return projectRepository.findAll()
            .stream()
            .map(this::mapToResponseDto)
            .toList();
    }

    @Override
    public void deleteProject(Long id) {

        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Project not found with id: " + id
            ));

        projectRepository.delete(project);
    }

    private ProjectResponseDto mapToResponseDto(Project project) {

        ProjectResponseDto responseDto = new ProjectResponseDto();

        responseDto.setId(project.getId());
        responseDto.setProjectName(project.getProjectName());
        responseDto.setDescription(project.getDescription());
        responseDto.setStatus(project.getStatus());
        responseDto.setStartDate(project.getStartDate());
        responseDto.setEndDate(project.getEndDate());
        responseDto.setCreatedAt(project.getCreatedAt());

        return responseDto;
    }
}