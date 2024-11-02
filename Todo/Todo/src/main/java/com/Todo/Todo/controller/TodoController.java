package com.Todo.Todo.controller;

import com.Todo.Todo.dto.TodoRequest;
import com.Todo.Todo.dto.TodoResponse;
import com.Todo.Todo.dto.TodoUpdateRequest;
import com.Todo.Todo.model.Priority;
import com.Todo.Todo.model.Todo;
import com.Todo.Todo.service.TodoService;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("Todo/todos/")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @PostMapping(value = "createtodo")
    public ResponseEntity<TodoResponse> createTodo(
            @RequestBody TodoRequest todoRequest,
            @RequestHeader(value = "Authorization") String token) {

        Todo todo = new Todo();
        BeanUtils.copyProperties(todoRequest, todo);

        TodoResponse todoResponse = todoService.createTodo(todo, token);

        switch (todoResponse.getStatus()) {
            case "Success":
                return new ResponseEntity<>(todoResponse, HttpStatus.CREATED);
            case "Failure":
                return new ResponseEntity<>(todoResponse, HttpStatus.CONFLICT);
            case "Invalid Token":
                return new ResponseEntity<>(todoResponse, HttpStatus.UNAUTHORIZED);
            default:
                return new ResponseEntity<>(todoResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "gettodo")
    public ResponseEntity<List<TodoResponse>> getTodo(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        List<TodoResponse> todoResponses = todoService.getTodoList(token);

        if (todoResponses.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if ("Failure".equals(todoResponses.get(0).getStatus())) {
            return new ResponseEntity<>(todoResponses, HttpStatus.UNAUTHORIZED);
        }

        int start = Math.min(page * size, todoResponses.size());
        int end = Math.min((page + 1) * size, todoResponses.size());
        List<TodoResponse> paginatedTodos = todoResponses.subList(start, end);

        return new ResponseEntity<>(paginatedTodos, HttpStatus.OK);
    }


    @PutMapping(value = "updatetodo/{title}")
    public ResponseEntity<TodoResponse> updateTodo(
            @PathVariable String title,
            @RequestBody TodoUpdateRequest updateRequest,
            @RequestHeader(value = "Authorization") String token) {

        TodoResponse todoResponse = todoService.updateTodoByTitle(title, updateRequest, token);

        switch (todoResponse.getStatus()) {
            case "Success":
                return new ResponseEntity<>(todoResponse, HttpStatus.OK);
            case "Not Found":
                return new ResponseEntity<>(todoResponse, HttpStatus.NOT_FOUND);
            case "Invalid Token":
                return new ResponseEntity<>(todoResponse, HttpStatus.UNAUTHORIZED);
            default:
                return new ResponseEntity<>(todoResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(value = "deletetodo/{title}")
    public ResponseEntity<TodoResponse> deleteTodo(
            @PathVariable String title,
            @RequestHeader(value = "Authorization") String token) {

        TodoResponse todoResponse = todoService.deleteTodoByTitle(title, token);

        switch (todoResponse.getStatus()) {
            case "Success":
                return new ResponseEntity<>(todoResponse, HttpStatus.OK);
            case "Not Found":
                return new ResponseEntity<>(todoResponse, HttpStatus.NOT_FOUND);
            case "Invalid Token":
                return new ResponseEntity<>(todoResponse, HttpStatus.UNAUTHORIZED);
            default:
                return new ResponseEntity<>(todoResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(value = "sort/todo")
    public ResponseEntity<List<TodoResponse>> sortTodos(
            @RequestParam String field,
            @RequestParam(defaultValue = "asc") String order,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "Authorization") String token) {

        List<TodoResponse> todoResponses = todoService.getSortedTodoList(token, field, order);

        if (todoResponses.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if ("Failure".equals(todoResponses.get(0).getStatus())) {
            return new ResponseEntity<>(todoResponses, HttpStatus.UNAUTHORIZED);
        }

        int start = Math.min(page * size, todoResponses.size());
        int end = Math.min(start + size, todoResponses.size());

        List<TodoResponse> paginatedResponse = todoResponses.subList(start, end);

        return new ResponseEntity<>(paginatedResponse, HttpStatus.OK);
    }



    @GetMapping(value = "search")
    public ResponseEntity<List<TodoResponse>> searchTodos(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) LocalDateTime dueDate,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "Authorization") String token) {

        List<TodoResponse> todoResponses = todoService.searchTodos(token, keyword, priority, dueDate, completed);

        if (!todoResponses.isEmpty() && "Failure".equals(todoResponses.get(0).getStatus())) {
            return new ResponseEntity<>(todoResponses, HttpStatus.UNAUTHORIZED);
        }

        int start = Math.min(page * size, todoResponses.size());
        int end = Math.min(start + size, todoResponses.size());

        List<TodoResponse> paginatedResponses = todoResponses.subList(start, end);

        return paginatedResponses.isEmpty() ?
                new ResponseEntity<>(HttpStatus.NOT_FOUND) :
                new ResponseEntity<>(paginatedResponses, HttpStatus.OK);
    }








}
