package com.Todo.Todo.service;

import com.Todo.Todo.dto.TodoRequest;
import com.Todo.Todo.dto.TodoResponse;
import com.Todo.Todo.dto.TodoUpdateRequest;
import com.Todo.Todo.model.Priority;
import com.Todo.Todo.model.Todo;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoService {

    TodoResponse createTodo(Todo todo, String token);

    List<TodoResponse> getTodoList(String token);

    TodoResponse updateTodoByTitle(String title, TodoUpdateRequest updateRequest, String token);

    TodoResponse deleteTodoByTitle(String title, String token);

    List<TodoResponse> getSortedTodoList(String token, String field, String order);

    List<TodoResponse> searchTodos(String token, String keyword, Priority priority, LocalDateTime dueDate, Boolean completed);
}
