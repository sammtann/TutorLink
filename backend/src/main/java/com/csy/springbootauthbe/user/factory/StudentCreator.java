package com.csy.springbootauthbe.user.factory;

import com.csy.springbootauthbe.student.dto.StudentDTO;
import com.csy.springbootauthbe.student.service.StudentService;
import com.csy.springbootauthbe.user.entity.User;
import com.csy.springbootauthbe.user.utils.RegisterRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StudentCreator implements RoleEntityCreator{

    private final StudentService studentService;

    @Override
    public void createEntity(User user, RegisterRequest request) {
        var studentDTO = StudentDTO.builder()
                .userId(user.getId())
                .studentNumber(request.getStudentNumber())
                .gradeLevel(request.getGradeLevel())
                .build();
        studentService.createStudent(studentDTO);

    }
}
