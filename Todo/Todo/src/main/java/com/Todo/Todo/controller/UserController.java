package com.Todo.Todo.controller;

import com.Todo.Todo.dto.AuthRequest;
import com.Todo.Todo.dto.AuthResponse;
import com.Todo.Todo.model.User;
import com.Todo.Todo.service.UserService;
import com.Todo.Todo.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "Todo/User/")
@CrossOrigin
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping(value = "register")
    public AuthResponse register(@RequestBody AuthRequest authRequest) {
        AuthResponse authResponse = new AuthResponse();
        try {
            User user = new User();
            BeanUtils.copyProperties(authRequest, user);
            User registeredUser = userService.register(user);

            // Generate JWT token
            String token = jwtUtil.generateToken(registeredUser.getEmail());
            authResponse.setJwtToken(token);
            authResponse.setStatus("Success");
            authResponse.setMessage("User registered successfully");

        } catch (RuntimeException e) {
            log.error("Error during registration: ", e);
            authResponse.setStatus("Error");
            authResponse.setMessage(e.getMessage());
        }
        return authResponse;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        User user = new User();
        BeanUtils.copyProperties(authRequest, user);
        AuthResponse authResponse = userService.login(user);

        if ("Success".equals(authResponse.getStatus())) {
            // Create the response entity with the token in the header
            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + authResponse.getJwtToken())
                    .body(authResponse);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(authResponse);
        }
    }





}

