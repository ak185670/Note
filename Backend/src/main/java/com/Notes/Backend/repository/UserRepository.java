package com.Notes.Backend.repository;

import com.Notes.Backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User,String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    Optional<List<User>> findByUsernameContainingIgnoreCase(String username);
}
