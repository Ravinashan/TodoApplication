package com.Todo.Todo;

import com.Todo.Todo.dto.AuthResponse;
import com.Todo.Todo.model.User;
import com.Todo.Todo.repository.UserRepository;
import com.Todo.Todo.service.UserServiceImpl;
import com.Todo.Todo.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
    }

    @Test
    void testRegister_Success() {
        // Set up mocks
        when(userRepository.findUsersByEmail(testUser.getEmail())).thenReturn(List.of());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Execute the method
        User registeredUser = userService.register(testUser);

        // Verify outcomes
        assertNotNull(registeredUser);
        assertEquals("encodedPassword", registeredUser.getPassword());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        // Set up mocks
        when(userRepository.findUsersByEmail(testUser.getEmail())).thenReturn(List.of(testUser));

        // Execute and verify exception
        Exception exception = assertThrows(RuntimeException.class, () -> userService.register(testUser));
        assertEquals("Email is already registered. Use another email!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

//    @Test
//    void testLogin_Success() {
//        // Set up mocks
//        when(userRepository.findUserByEmail(testUser.getEmail())).thenReturn(testUser);
//        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
//        when(jwtUtil.generateToken(testUser.getEmail())).thenReturn("mocked-token");
//
//        // Execute the method
//        AuthResponse authResponse = userService.login(testUser);
//
//        // Verify outcomes
//        assertEquals("Success", authResponse.getStatus());
//        assertEquals("User logged in successfully", authResponse.getMessage());
//        assertEquals("mocked-token", authResponse.getJwtToken());
//    }

    @Test
    void testLogin_UserNotFound() {
        // Set up mocks
        when(userRepository.findUserByEmail(testUser.getEmail())).thenReturn(null);

        // Execute the method
        AuthResponse authResponse = userService.login(testUser);

        // Verify outcomes
        assertEquals("Failure", authResponse.getStatus());
        assertEquals("User not found", authResponse.getMessage());
        assertNull(authResponse.getJwtToken());
    }

    @Test
    void testLogin_IncorrectPassword() {
        // Set up mocks
        when(userRepository.findUserByEmail(testUser.getEmail())).thenReturn(testUser);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Execute the method
        AuthResponse authResponse = userService.login(testUser);

        // Verify outcomes
        assertEquals("Failure", authResponse.getStatus());
        assertEquals("Incorrect email or password", authResponse.getMessage());
        assertNull(authResponse.getJwtToken());
    }

    @Test
    void testFindUsersByEmail() {
        // Set up mocks
        List<User> users = List.of(testUser);
        when(userRepository.findUsersByEmail(anyString())).thenReturn(users);

        // Execute the method
        List<User> result = userService.findUsersByEmail(testUser.getEmail());

        // Verify outcomes
        assertEquals(1, result.size());
        assertEquals(testUser.getEmail(), result.get(0).getEmail());
    }

    @Test
    void testFindUserByEmail() {
        // Set up mocks
        when(userRepository.findUserByEmail(anyString())).thenReturn(testUser);

        // Execute the method
        User result = userService.findUserByEmail(testUser.getEmail());

        // Verify outcomes
        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
    }
}
