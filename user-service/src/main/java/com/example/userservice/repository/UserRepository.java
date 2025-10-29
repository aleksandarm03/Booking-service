package com.example.userservice.repository;

import com.example.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


//IZ ENTITETA/MODELA SE NE MOZE CITATI IZ BAZE, ZBOG TOGA POSTOJI REPOSITORY
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}

