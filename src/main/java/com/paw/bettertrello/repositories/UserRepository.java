package com.paw.bettertrello.repositories;

import com.paw.bettertrello.models.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);

    @Query(value = "{'username' : ?0 }", fields = "{ 'password' : 0, 'id' : 0, 'isEnabled' : 0}")
    Optional<User> findByUsernameExcludingSensitiveData(String username);
}
