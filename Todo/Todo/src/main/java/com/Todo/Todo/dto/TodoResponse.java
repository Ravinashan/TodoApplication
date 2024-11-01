package com.Todo.Todo.dto;

import com.Todo.Todo.model.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TodoResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Priority priority;
    private Boolean completed;

    private String status;
    private String message;


}
