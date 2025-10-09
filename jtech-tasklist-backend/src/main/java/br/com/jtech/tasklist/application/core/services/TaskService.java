package br.com.jtech.tasklist.application.core.services;

import br.com.jtech.tasklist.application.core.domains.Task;
import br.com.jtech.tasklist.application.ports.output.TaskRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepositoryPort repository;

    public Task create(Task task) {
        if (repository.existsByTitleAndListId(task.getTitle(), task.getListId())) {
            throw new IllegalArgumentException("Já existe uma tarefa com esse título nesta lista.");
        }
        return repository.save(task);
    }

    public Optional<Task> getById(String id, String userId) {
        Optional<Task> task = repository.findById(id);
        if (task.isPresent() && !task.get().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Você não tem permissão para acessar esta tarefa.");
        }
        return task;
    }

    public List<Task> getByUser(String userId) {
        return repository.findAllByUserId(userId);
    }

    public List<Task> getByList(String listId) {
        return repository.findAllByListId(listId);
    }

    public void delete(String id, String userId) {
        Optional<Task> task = repository.findById(id);
        if (task.isEmpty()) {
            throw new IllegalArgumentException("Tarefa não encontrada.");
        }
        if (!task.get().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Você não tem permissão para excluir esta tarefa.");
        }
        repository.deleteById(id);
    }

    public List<Task> getAll() {
        return repository.findAll();
    }

    public Task update(Task task, String userId) {
        Optional<Task> existing = repository.findById(task.getId());
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Tarefa não encontrada.");
        }
        if (!existing.get().getUserId().equals(userId)) {
            throw new IllegalArgumentException("Você não tem permissão para atualizar esta tarefa.");
        }
        if (!existing.get().getTitle().equals(task.getTitle()) &&
            repository.existsByTitleAndListId(task.getTitle(), task.getListId())) {
            throw new IllegalArgumentException("Já existe uma tarefa com esse título nesta lista.");
        }
        return repository.update(task);
    }
}
