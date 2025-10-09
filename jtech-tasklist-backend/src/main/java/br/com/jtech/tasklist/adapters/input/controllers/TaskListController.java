package br.com.jtech.tasklist.adapters.input.controllers;

import br.com.jtech.tasklist.application.core.domains.TaskList;
import br.com.jtech.tasklist.application.core.services.TaskListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasklists")
@RequiredArgsConstructor
public class TaskListController {

    private final TaskListService service;

    @PostMapping
    public ResponseEntity<TaskList> create(@RequestBody TaskList taskList) {
        return ResponseEntity.ok(service.create(taskList));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskList> getById(@PathVariable String id, @RequestHeader("X-User-Id") String userId) {
        return service.getById(id, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskList>> getByUser(@PathVariable String userId) {
        return ResponseEntity.ok(service.getByUser(userId));
    }

    @GetMapping
    public ResponseEntity<List<TaskList>> getAll(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(service.getByUser(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskList> update(@PathVariable String id, @RequestBody TaskList taskList, @RequestHeader("X-User-Id") String userId) {
        taskList.setId(id);
        return ResponseEntity.ok(service.update(taskList, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id, @RequestHeader("X-User-Id") String userId) {
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}
