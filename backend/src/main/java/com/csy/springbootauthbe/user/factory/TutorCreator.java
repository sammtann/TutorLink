package com.csy.springbootauthbe.user.factory;

import com.csy.springbootauthbe.tutor.dto.TutorDTO;
import com.csy.springbootauthbe.tutor.service.TutorService;
import com.csy.springbootauthbe.user.entity.User;
import com.csy.springbootauthbe.user.utils.RegisterRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TutorCreator  implements RoleEntityCreator{

    private final TutorService tutorService;

    @Override
    public void createEntity(User user, RegisterRequest request) {
        TutorDTO tutorDTO = TutorDTO.builder().userId(user.getId()).subject(request.getSubject()).build();
        tutorService.createTutor(tutorDTO);
    }
}
