package com.csy.springbootauthbe.user.factory;

import com.csy.springbootauthbe.admin.dto.AdminDTO;
import com.csy.springbootauthbe.admin.service.AdminService;
import com.csy.springbootauthbe.user.entity.User;
import com.csy.springbootauthbe.user.utils.RegisterRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AdminCreator implements RoleEntityCreator{

    private final AdminService adminService;

    @Override
    public void createEntity(User user, RegisterRequest request) {
        AdminDTO adminDTO = AdminDTO.builder()
                    .userId(user.getId())
                    .permissions(request.getPermissions())
                    .build();
            adminService.createAdmin(adminDTO);
    }
}
