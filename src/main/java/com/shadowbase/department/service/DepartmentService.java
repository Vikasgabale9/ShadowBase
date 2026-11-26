
package com.shadowbase.department.service;

import com.shadowbase.department.dto.DepartmentRequestDto;
import com.shadowbase.department.dto.DepartmentResponseDto;

import java.util.List;

public interface DepartmentService {

    DepartmentResponseDto createDepartment(DepartmentRequestDto requestDto);

    DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto requestDto);

    DepartmentResponseDto getDepartmentById(Long id);

    List<DepartmentResponseDto> getAllDepartments();

    void deleteDepartment(Long id);
}

