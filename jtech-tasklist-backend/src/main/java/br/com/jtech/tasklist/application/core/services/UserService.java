package br.com.jtech.tasklist.application.core.services;

import br.com.jtech.tasklist.application.core.domains.User;
import br.com.jtech.tasklist.application.ports.output.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepositoryPort repository;

    public User create(User user) {
        if (user.getEmail() == null || !user.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("Email inválido.");
        }
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este email.");
        }
        return repository.save(user);
    }

    public Optional<User> getById(String id) {
        return repository.findById(id);
    }

    public List<User> getAll() {
        return repository.findAll();
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    // Login mockado (sem JWT ainda)
    public User login(String email, String password) {
        return repository.findByEmail(email)
                .filter(u -> u.getPassword().equals(password)) // ainda sem hash
                .orElse(null);
    }

    public User update(User user) {
        Optional<User> existing = repository.findById(user.getId());
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        if (!existing.get().getEmail().equals(user.getEmail())) {
            if (repository.findByEmail(user.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Já existe um usuário cadastrado com este email.");
            }
        }
        return repository.update(user);
    }
}
