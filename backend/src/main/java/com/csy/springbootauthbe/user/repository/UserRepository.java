package com.csy.springbootauthbe.user.repository;

import com.csy.springbootauthbe.user.entity.AccountStatus;
import com.csy.springbootauthbe.user.entity.Role;
import com.csy.springbootauthbe.user.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Desc:
 * This interface provides access to user data stored in MongoDB.
 * It extends MongoRepository interface, which provides basic CRUD operations.
 */
public interface UserRepository extends MongoRepository<User, String> {

//    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndStatusNot(String email, AccountStatus status);

//    boolean existsByEmail(String email);

    boolean existsByEmailAndStatusNot(String email, AccountStatus status);

    List<User> findAllByRole(Role role);

}
