package com.Todo.Todo.service;

import com.Todo.Todo.dto.AuthResponse;
import com.Todo.Todo.model.User;
import com.Todo.Todo.repository.UserRepository;
import com.Todo.Todo.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private JwtUtil jwtUtil;

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(User user) {
        log.info("Attempting to register user with email: {}", user.getEmail());

        if (!findUsersByEmail(user.getEmail()).isEmpty()) {
            log.warn("Registration failed - email already in use: {}", user.getEmail());
            throw new RuntimeException("Email is already registered. Use another email!");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        log.info("User registered successfully with email: {}", user.getEmail());
        return user;
    }

    @Override
    public AuthResponse login(User user) {
        log.info("Login attempt for user with email: {}", user.getEmail());
        AuthResponse authResponse = new AuthResponse();

        User userFromDb = userRepository.findUserByEmail(user.getEmail());

        if (userFromDb == null) {
            log.warn("Login failed - user not found for email: {}", user.getEmail());
            authResponse.setJwtToken(null);
            authResponse.setStatus("Failure");
            authResponse.setMessage("User not found");
            return authResponse;
        }

        if (passwordEncoder.matches(user.getPassword(), userFromDb.getPassword())) {
            String token = jwtUtil.generateToken(user.getEmail());
            authResponse.setJwtToken(token);
            authResponse.setStatus("Success");
            authResponse.setMessage("User logged in successfully");
            log.info("User logged in successfully for email: {}", user.getEmail());
        } else {
            log.warn("Login failed - incorrect password for email: {}", user.getEmail());
            authResponse.setJwtToken(null);
            authResponse.setStatus("Failure");
            authResponse.setMessage("Incorrect email or password");
        }

        return authResponse;
    }

    public List<User> findUsersByEmail(String email){
        log.debug("Starting findUsersByEmail for email: {}", email);
        List<User> users = userRepository.findUsersByEmail(email);
        log.debug("Completed findUsersByEmail - {} users found for email: {}", users.size(), email);
        return users;
    }

    public User findUserByEmail(String email){
        log.debug("Starting findUserByEmail for email: {}", email);
        User user = userRepository.findUserByEmail(email);
        if (user != null) {
            log.debug("User found with email: {}", email);
        } else {
            log.warn("No user found for email: {}", email);
        }
        return user;
    }
}
