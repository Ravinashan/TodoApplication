package com.Todo.Todo.service;

import com.Todo.Todo.dto.TodoResponse;
import com.Todo.Todo.dto.TodoUpdateRequest;
import com.Todo.Todo.model.Priority;
import com.Todo.Todo.model.Todo;
import com.Todo.Todo.model.User;
import com.Todo.Todo.repository.TodoRepository;
import com.Todo.Todo.repository.UserRepository;
import com.Todo.Todo.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TodoServiceImpl implements TodoService {

    private static final Logger log = LoggerFactory.getLogger(TodoServiceImpl.class);

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public TodoResponse createTodo(Todo todo, String token) {
        log.info("Creating Todo: {}", todo.getId());
        TodoResponse todoResponse = new TodoResponse();

        // Validate the token
        if (!jwtUtil.validateToken(token)) {
            todoResponse.setStatus("Invalid Token");
            todoResponse.setMessage("Invalid JWT token.");
            log.warn(todoResponse.getMessage());
            return todoResponse;
        }

        String email = jwtUtil.extractUserEmail(token);
        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            todoResponse.setStatus("Failure");
            todoResponse.setMessage("User not found.");
            log.warn(todoResponse.getMessage());
            return todoResponse;
        }

        todo.setUser(user);

        if (todoRepository.existsByTitleAndUserId(todo.getTitle(), user.getId())) {
            todoResponse.setStatus("Failure");
            todoResponse.setMessage("A todo with this title already exists. Please choose another title.");
            log.warn(todoResponse.getMessage());
            return todoResponse;
        }

        // Validate due date
        if (todo.getDueDate() != null && todo.getDueDate().isBefore(LocalDateTime.now())) {
            todoResponse.setStatus("Failure");
            todoResponse.setMessage("The due date cannot be in the past.");
            log.warn(todoResponse.getMessage());
            return todoResponse;
        }

        Todo savedTodo = todoRepository.save(todo);
        todoResponse.setId(savedTodo.getId());
        todoResponse.setTitle(savedTodo.getTitle());
        todoResponse.setDescription(savedTodo.getDescription());
        todoResponse.setDueDate(savedTodo.getDueDate());
        todoResponse.setPriority(savedTodo.getPriority());
        todoResponse.setCompleted(savedTodo.getCompleted());
        todoResponse.setStatus("Success");
        todoResponse.setMessage("Todo created successfully.");
        log.info("Todo created successfully for user: {}", email);

        return todoResponse;
    }

    @Override
    public List<TodoResponse> getTodoList(String token) {
        List<TodoResponse> todoResponses = new ArrayList<>();

        log.info("Starting getTodoList with token: {}", token);

        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid token provided.");
            TodoResponse todoResponse = new TodoResponse();
            todoResponse.setStatus("Invalid Token");
            todoResponse.setMessage("Invalid JWT token.");
            todoResponses.add(todoResponse);
            return todoResponses;
        }

        String email = jwtUtil.extractUserEmail(token);
        log.info("Token validated successfully, extracted email: {}", email);

        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            log.warn("No user found with email: {}", email);
            TodoResponse todoResponse = new TodoResponse();
            todoResponse.setStatus("Failure");
            todoResponse.setMessage("User not found.");
            todoResponses.add(todoResponse);
            return todoResponses;
        }
        log.info("User found: {}", user.getEmail());

        List<Todo> todoList = todoRepository.findTodosByUserId(user.getId());
        log.info("Retrieved {} todos for user with ID: {}", todoList.size(), user.getId());

        for (Todo todo : todoList) {
            TodoResponse response = new TodoResponse();
            response.setId(todo.getId());
            response.setTitle(todo.getTitle());
            response.setDescription(todo.getDescription());
            response.setDueDate(todo.getDueDate());
            response.setPriority(todo.getPriority());
            response.setCompleted(todo.getCompleted());
            response.setStatus("Success");
            response.setMessage("Todo retrieved successfully.");
            todoResponses.add(response);

            log.debug("Added todo to response: ID={}, Title={}, Status={}",
                    todo.getId(), todo.getTitle(), response.getStatus());
        }

        log.info("Completed getTodoList for user: {}. Total todos returned: {}", email, todoResponses.size());
        return todoResponses;
    }


    @Override
    public TodoResponse updateTodoByTitle(String title, TodoUpdateRequest updateRequest, String token) {
        TodoResponse todoResponse = new TodoResponse();
        log.info("Starting updateTodoByTitle for title: {} with token: {}", title, token);

        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid token provided.");
            todoResponse.setStatus("Invalid Token");
            todoResponse.setMessage("Invalid JWT token.");
            return todoResponse;
        }

        String email = jwtUtil.extractUserEmail(token);
        log.info("Token validated successfully, extracted email: {}", email);

        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            log.warn("User not found with email: {}", email);
            todoResponse.setStatus("Failure");
            todoResponse.setMessage("User not found.");
            return todoResponse;
        }
        log.info("User found: {}", user.getEmail());

        Todo todo = todoRepository.findTodoByTitleAndUserId(title, user.getId());
        if (todo == null) {
            log.warn("Todo not found with title: {} for user ID: {}", title, user.getId());
            todoResponse.setStatus("Not Found");
            todoResponse.setMessage("Todo not found for the given title.");
            return todoResponse;
        }
        log.info("Todo found with title: {} for user ID: {}", title, user.getId());

        if (updateRequest.getDescription() != null) {
            todo.setDescription(updateRequest.getDescription());
            log.debug("Updated description for Todo with title: {}", title);
        }
        if (updateRequest.getDueDate() != null) {
            todo.setDueDate(updateRequest.getDueDate());
            log.debug("Updated due date for Todo with title: {}", title);
        }
        if (updateRequest.getPriority() != null) {
            todo.setPriority(updateRequest.getPriority());
            log.debug("Updated priority for Todo with title: {}", title);
        }
        if (updateRequest.getCompleted() != null) {
            todo.setCompleted(updateRequest.getCompleted());
            log.debug("Updated completed status for Todo with title: {}", title);
        }

        Todo updatedTodo = todoRepository.save(todo);
        log.info("Todo updated successfully for user: {}", email);

        todoResponse.setId(updatedTodo.getId());
        todoResponse.setTitle(updatedTodo.getTitle());
        todoResponse.setDescription(updatedTodo.getDescription());
        todoResponse.setDueDate(updatedTodo.getDueDate());
        todoResponse.setPriority(updatedTodo.getPriority());
        todoResponse.setCompleted(updatedTodo.getCompleted());
        todoResponse.setStatus("Success");
        todoResponse.setMessage("Todo updated successfully.");

        log.info("Completed updateTodoByTitle for title: {} with status: {}", title, todoResponse.getStatus());
        return todoResponse;
    }


    @Override
    public TodoResponse deleteTodoByTitle(String title, String token) {
        TodoResponse todoResponse = new TodoResponse();
        log.info("Starting deleteTodoByTitle for title: {} with token: {}", title, token);

        if (!jwtUtil.validateToken(token)) {
            log.warn("Invalid token provided.");
            todoResponse.setStatus("Invalid Token");
            todoResponse.setMessage("Invalid JWT token.");
            return todoResponse;
        }

        String email = jwtUtil.extractUserEmail(token);
        log.info("Token validated successfully, extracted email: {}", email);

        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            log.warn("User not found with email: {}", email);
            todoResponse.setStatus("Failure");
            todoResponse.setMessage("User not found.");
            return todoResponse;
        }
        log.info("User found: {}", user.getEmail());

        Todo todo = todoRepository.findTodoByTitleAndUserId(title, user.getId());
        if (todo == null) {
            log.warn("Todo not found with title: {} for user ID: {}", title, user.getId());
            todoResponse.setStatus("Not Found");
            todoResponse.setMessage("Todo not found for the given title.");
            return todoResponse;
        }
        log.info("Todo found with title: {} for user ID: {}", title, user.getId());

        todoRepository.delete(todo);
        log.info("Todo deleted successfully for user: {}", email);

        todoResponse.setStatus("Success");
        todoResponse.setMessage("Todo deleted successfully.");

        log.info("Completed deleteTodoByTitle for title: {} with status: {}", title, todoResponse.getStatus());
        return todoResponse;
    }


    @Override
    public List<TodoResponse> getSortedTodoList(String token, String field, String order) {
        List<TodoResponse> todoResponses = new ArrayList<>();
        log.info("Starting getSortedTodoList with field: {} and order: {}", field, order);

        if (!jwtUtil.validateToken(token)) {
            TodoResponse todoResponse = new TodoResponse();
            todoResponse.setStatus("Failure");
            todoResponse.setMessage("Invalid JWT token.");
            log.warn("Invalid JWT token provided.");
            todoResponses.add(todoResponse);
            return todoResponses;
        }
        log.info("JWT token validated successfully.");

        String email = jwtUtil.extractUserEmail(token);
        log.info("Extracted email from token: {}", email);

        User user = userRepository.findUserByEmail(email);
        if (user == null) {
            TodoResponse todoResponse = new TodoResponse();
            todoResponse.setStatus("Failure");
            todoResponse.setMessage("User not found.");
            log.warn("User not found with email: {}", email);
            todoResponses.add(todoResponse);
            return todoResponses;
        }
        log.info("User found: {}", user.getEmail());

        Sort.Direction sortDirection = order.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        log.info("Sorting direction set to: {}", sortDirection);

        Sort sort = Sort.by(sortDirection, field);
        log.info("Sorting todos by field: {} in {} order", field, sortDirection);

        List<Todo> todoList = todoRepository.findTodosByUserId(user.getId(), sort);
        log.info("Retrieved {} todos for user: {}", todoList.size(), user.getEmail());

        for (Todo todo : todoList) {
            TodoResponse response = new TodoResponse();
            response.setId(todo.getId());
            response.setTitle(todo.getTitle());
            response.setDescription(todo.getDescription());
            response.setDueDate(todo.getDueDate());
            response.setPriority(todo.getPriority());
            response.setCompleted(todo.getCompleted());
            response.setStatus("Success");
            response.setMessage("Todo retrieved successfully.");

            todoResponses.add(response);
        }

        log.info("Completed getSortedTodoList with {} todos sorted by {} in {} order", todoResponses.size(), field, order);
        return todoResponses;
    }



    @Override
    public List<TodoResponse> searchTodos(String token, String keyword, Priority priority, LocalDateTime dueDate, Boolean completed) {
        List<TodoResponse> todoResponses = new ArrayList<>();
        log.info("Starting searchTodos with keyword: {}, priority: {}, dueDate: {}, completed: {}", keyword, priority, dueDate, completed);

        if (!jwtUtil.validateToken(token)) {
            TodoResponse todoResponse = new TodoResponse();
            todoResponse.setStatus("Failure");
            todoResponse.setMessage("Invalid JWT token.");
            log.warn("Invalid JWT token provided.");
            todoResponses.add(todoResponse);
            return todoResponses;
        }
        log.info("JWT token validated successfully.");

        String email = jwtUtil.extractUserEmail(token);
        log.info("Extracted email from token: {}", email);
        User user = userRepository.findUserByEmail(email);

        if (user == null) {
            TodoResponse todoResponse = new TodoResponse();
            todoResponse.setStatus("Failure");
            todoResponse.setMessage("User not found.");
            log.warn("User not found with email: {}", email);
            todoResponses.add(todoResponse);
            return todoResponses;
        }
        log.info("User found: {}", user.getEmail());

        log.info("Searching todos for user: {} with criteria - keyword: {}, priority: {}, dueDate: {}, completed: {}",
                user.getEmail(), keyword, priority, dueDate, completed);
        List<Todo> todos = todoRepository.findTodosByCriteria(user.getId(), keyword, priority, dueDate, completed);

        log.info("Found {} todos matching the criteria for user: {}", todos.size(), user.getEmail());

        for (Todo todo : todos) {
            TodoResponse response = new TodoResponse();
            response.setId(todo.getId());
            response.setTitle(todo.getTitle());
            response.setDescription(todo.getDescription());
            response.setDueDate(todo.getDueDate());
            response.setPriority(todo.getPriority());
            response.setCompleted(todo.getCompleted());
            response.setStatus("Success");
            response.setMessage("Todo retrieved successfully.");
            todoResponses.add(response);
        }

        log.info("Completed searchTodos with {} todos found matching criteria.", todoResponses.size());
        return todoResponses;
    }

}
