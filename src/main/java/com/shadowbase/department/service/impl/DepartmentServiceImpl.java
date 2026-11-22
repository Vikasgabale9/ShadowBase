
package com.shadowbase.department.service.impl;

import com.shadowbase.department.dto.DepartmentRequestDto;
import com.shadowbase.department.dto.DepartmentResponseDto;
import com.shadowbase.department.entity.Department;
import com.shadowbase.department.repository.DepartmentRepository;
import com.shadowbase.department.service.DepartmentService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentServiceImpl(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Override
    public DepartmentResponseDto createDepartment(DepartmentRequestDto requestDto) {

        if (departmentRepository.existsByDepartmentName(requestDto.getDepartmentName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Department name already exists"
            );
        }

        Department department = new Department();

        department.setDepartmentName(requestDto.getDepartmentName());
        department.setLocation(requestDto.getLocation());
        department.setCreatedAt(OffsetDateTime.now());

        Department savedDepartment = departmentRepository.save(department);

        return mapToResponseDto(savedDepartment);
    }

    @Override
    public DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto requestDto) {

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Department not found with id: " + id
                ));

        if (departmentRepository.existsByDepartmentNameAndIdNot(
                requestDto.getDepartmentName(), id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Department name already exists"
            );
        }

        department.setDepartmentName(requestDto.getDepartmentName());
        department.setLocation(requestDto.getLocation());

        Department updatedDepartment = departmentRepository.save(department);

        return mapToResponseDto(updatedDepartment);
    }

    @Override
    public DepartmentResponseDto getDepartmentById(Long id) {

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Department not found with id: " + id
                ));

        return mapToResponseDto(department);
    }

    @Override
    public List<DepartmentResponseDto> getAllDepartments() {

        return departmentRepository.findAll()
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    public void deleteDepartment(Long id) {

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Department not found with id: " + id
                ));

        departmentRepository.delete(department);
    }

    private DepartmentResponseDto mapToResponseDto(Department department) {

        DepartmentResponseDto responseDto = new DepartmentResponseDto();

        responseDto.setId(department.getId());
        responseDto.setDepartmentName(department.getDepartmentName());
        responseDto.setLocation(department.getLocation());
        responseDto.setCreatedAt(department.getCreatedAt());

        return responseDto;
    }
}

