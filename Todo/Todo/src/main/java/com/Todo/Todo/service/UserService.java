package com.Todo.Todo.service;

import com.Todo.Todo.dto.AuthRequest;
import com.Todo.Todo.dto.AuthResponse;
import com.Todo.Todo.model.User;

import java.util.List;

public interface UserService {

     User register(User user);

     List<User> findUsersByEmail(String email);

     User findUserByEmail(String email);

     AuthResponse login(User user);



}
