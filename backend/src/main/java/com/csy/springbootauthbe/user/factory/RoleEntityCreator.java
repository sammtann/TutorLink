package com.csy.springbootauthbe.user.factory;

import com.csy.springbootauthbe.user.entity.User;
import com.csy.springbootauthbe.user.utils.RegisterRequest;

public interface RoleEntityCreator {
    void createEntity(User user, RegisterRequest request);
}
