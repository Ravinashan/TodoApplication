package com.Todo.Todo.repository;

import com.Todo.Todo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findUserByEmail(String email);

    List<User> findUsersByEmail(String email);




}
