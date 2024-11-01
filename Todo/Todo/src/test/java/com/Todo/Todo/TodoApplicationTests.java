package com.Todo.Todo;

import com.Todo.Todo.dto.TodoResponse;
import com.Todo.Todo.dto.TodoUpdateRequest;
import com.Todo.Todo.model.Todo;
import com.Todo.Todo.model.User;
import com.Todo.Todo.repository.TodoRepository;
import com.Todo.Todo.repository.UserRepository;
import com.Todo.Todo.service.TodoServiceImpl;
import com.Todo.Todo.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class TodoApplicationTests {

	@InjectMocks
	private TodoServiceImpl todoService;

	@Mock
	private TodoRepository todoRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private JwtUtil jwtUtil;


	private User user;
	private Todo todo;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		user = new User();
		user.setId(1L);
		user.setEmail("test@example.com");
		user.setPassword("plainPassword");

		todo = new Todo();
		todo.setId(1L);
		todo.setTitle("Test Todo");
		todo.setDescription("Test Description");
		todo.setUser(user);
	}


	@Test
	void createTodo_success() {
		when(jwtUtil.validateToken(anyString())).thenReturn(true);
		when(jwtUtil.extractUserEmail(anyString())).thenReturn("test@example.com");
		when(userRepository.findUserByEmail("test@example.com")).thenReturn(user);
		when(todoRepository.save(any(Todo.class))).thenReturn(todo);

		TodoResponse response = todoService.createTodo(todo, "test-token");

		assertEquals("Success", response.getStatus());
		assertEquals("Todo created successfully.", response.getMessage());
		assertNotNull(response.getId());
	}

	@Test
	void createTodo_invalidToken() {
		when(jwtUtil.validateToken(anyString())).thenReturn(false);

		TodoResponse response = todoService.createTodo(todo, "invalid-token");

		assertEquals("Invalid Token", response.getStatus());
		assertEquals("Invalid JWT token.", response.getMessage());
	}

	@Test
	void getTodoList_success() {
		when(jwtUtil.validateToken(anyString())).thenReturn(true);
		when(jwtUtil.extractUserEmail(anyString())).thenReturn("test@example.com");
		when(userRepository.findUserByEmail("test@example.com")).thenReturn(user);
		when(todoRepository.findTodosByUserId(1L)).thenReturn(List.of(todo));

		List<TodoResponse> responseList = todoService.getTodoList("test-token");

		assertFalse(responseList.isEmpty());
		assertEquals("Success", responseList.get(0).getStatus());
		assertEquals("Todo retrieved successfully.", responseList.get(0).getMessage());
	}

	@Test
	void updateTodoByTitle_success() {
		TodoUpdateRequest updateRequest = new TodoUpdateRequest();
		updateRequest.setDescription("Updated Description");

		when(jwtUtil.validateToken(anyString())).thenReturn(true);
		when(jwtUtil.extractUserEmail(anyString())).thenReturn("test@example.com");
		when(userRepository.findUserByEmail("test@example.com")).thenReturn(user);
		when(todoRepository.findTodoByTitleAndUserId("Test Todo", 1L)).thenReturn(todo);
		when(todoRepository.save(any(Todo.class))).thenReturn(todo);

		TodoResponse response = todoService.updateTodoByTitle("Test Todo", updateRequest, "test-token");

		assertEquals("Success", response.getStatus());
		assertEquals("Todo updated successfully.", response.getMessage());
	}

	@Test
	void deleteTodoByTitle_success() {
		when(jwtUtil.validateToken(anyString())).thenReturn(true);
		when(jwtUtil.extractUserEmail(anyString())).thenReturn("test@example.com");
		when(userRepository.findUserByEmail("test@example.com")).thenReturn(user);
		when(todoRepository.findTodoByTitleAndUserId("Test Todo", 1L)).thenReturn(todo);

		TodoResponse response = todoService.deleteTodoByTitle("Test Todo", "test-token");

		assertEquals("Success", response.getStatus());
		assertEquals("Todo deleted successfully.", response.getMessage());
		verify(todoRepository, times(1)).delete(todo);
	}
}
