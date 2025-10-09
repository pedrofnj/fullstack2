package br.com.jtech.tasklist.adapters.input.controllers;

import br.com.jtech.tasklist.application.core.domains.Task;
import br.com.jtech.tasklist.application.core.services.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService service;

    @PostMapping
    public ResponseEntity<Task> create(@RequestBody Task task, @RequestHeader("X-User-Id") String userId) {
        if (!task.getUserId().equals(userId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }
        return ResponseEntity.ok(service.create(task));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getById(@PathVariable String id, @RequestHeader("X-User-Id") String userId) {
        return service.getById(id, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Task>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(service.getByUser(userId));
    }

    @GetMapping("/list/{listId}")
    public ResponseEntity<List<Task>> getByList(@PathVariable String listId, @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(service.getByList(listId, userId));
    }

    @GetMapping
    public ResponseEntity<List<Task>> getAll(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(service.getByUser(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> update(@PathVariable String id, @RequestBody Task task, @RequestHeader("X-User-Id") String userId) {
        task.setId(id);
        return ResponseEntity.ok(service.update(task, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, @RequestHeader("X-User-Id") String userId) {
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Task> toggleCompleted(@PathVariable String id, @RequestBody ToggleRequest req, @RequestHeader("X-User-Id") String userId) {
        Task updated = service.toggleCompleted(id, req.completed, userId);
        return ResponseEntity.ok(updated);
    }

    public static class ToggleRequest {
        public boolean completed;
    }
}
