package com.Todo.Todo.repository;

import com.Todo.Todo.model.Priority;
import com.Todo.Todo.model.Todo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findTodosByUserId(Long userId);

    List<Todo> findTodosByUserId(Long userId, Sort sort);

    Todo findTodoByTitleAndUserId(String title, Long userId);

    boolean existsByTitleAndUserId(String title, Long userId);

    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId " +
            "AND (:keyword IS NULL OR t.title LIKE %:keyword% OR t.description LIKE %:keyword%) " +
            "AND (:priority IS NULL OR t.priority = :priority) " + // Priority type used here
            "AND (:dueDate IS NULL OR t.dueDate = :dueDate) " +
            "AND (:completed IS NULL OR t.completed = :completed)")
    List<Todo> findTodosByCriteria(Long userId, String keyword, Priority priority, LocalDateTime dueDate, Boolean completed);





}



