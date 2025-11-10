package com.csy.springbootauthbe.common.wrapper;

import com.csy.springbootauthbe.user.entity.AccountStatus;
import com.csy.springbootauthbe.user.entity.User;
import com.csy.springbootauthbe.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceWrapper implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetailsWrapper loadUserByUsername(String email) {
        User user = userRepository.findByEmailAndStatusNot(email, AccountStatus.DELETED)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return new UserDetailsWrapper(user);
    }
}
