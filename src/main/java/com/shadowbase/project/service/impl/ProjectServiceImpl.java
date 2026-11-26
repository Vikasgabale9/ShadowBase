package com.shadowbase.project.service.impl;

import com.shadowbase.project.dto.ProjectRequestDto;
import com.shadowbase.project.dto.ProjectResponseDto;
import com.shadowbase.project.entity.Project;
import com.shadowbase.project.repository.ProjectRepository;
import com.shadowbase.project.service.ProjectService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public ProjectResponseDto createProject(ProjectRequestDto requestDto) {

        validateDates(requestDto);

        Project project = new Project();

        project.setProjectName(requestDto.getProjectName());
        project.setDescription(requestDto.getDescription());

        String status = requestDto.getStatus();

        if (status == null || status.isBlank()) {
            status = "PLANNED";
        } else {
            validateStatus(status);
        }

        project.setStatus(status);
        project.setStartDate(requestDto.getStartDate());
        project.setEndDate(requestDto.getEndDate());

        Project savedProject = projectRepository.save(project);

        return mapToResponseDto(savedProject);
    }
    @Override
    public ProjectResponseDto updateProject(Long id, ProjectRequestDto requestDto) {

        Project project = projectRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Project not found with id: " + id
            ));

        validateDates(requestDto);

        project.setProjectName(requestDto.getProjectName());
        project.setDescription(requestDto.getDescription());

        if (requestDto.getStatus() != null && !requestDto.getStatus().isBlank()) {
            validateStatus(requestDto.getStatus());
            project.setStatus(requestDto.getStatus());
        }

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
    private static final Set<String> ALLOWED_STATUSES = Set.of(
    	    "PLANNED",
    	    "IN_PROGRESS",
    	    "COMPLETED",
    	    "ON_HOLD",
    	    "CANCELLED"
    	);

    	private void validateStatus(String status) {
    	    if (!ALLOWED_STATUSES.contains(status)) {
    	        throw new ResponseStatusException(
    	            HttpStatus.BAD_REQUEST,
    	            "Invalid project status. Allowed values: " + ALLOWED_STATUSES
    	        );
    	    }
    	}

    	private void validateDates(ProjectRequestDto requestDto) {
    	    if (requestDto.getStartDate() != null
    	            && requestDto.getEndDate() != null
    	            && requestDto.getEndDate().isBefore(requestDto.getStartDate())) {

    	        throw new ResponseStatusException(
    	            HttpStatus.BAD_REQUEST,
    	            "Project end date cannot be before start date"
    	        );
    	    }
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