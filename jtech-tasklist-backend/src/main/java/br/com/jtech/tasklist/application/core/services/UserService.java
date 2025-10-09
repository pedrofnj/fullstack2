package br.com.jtech.tasklist.application.core.services;

import br.com.jtech.tasklist.application.core.domains.User;
import br.com.jtech.tasklist.application.ports.output.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepositoryPort repository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User create(User user) {
        if (user.getEmail() == null || !user.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("Email inválido.");
        }
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Já existe um usuário cadastrado com este email.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
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

    public User login(String email, String password) {
        Optional<User> userOpt = repository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            } else if (user.getPassword().equals(password)) { // Temporário para usuários existentes
                user.setPassword(passwordEncoder.encode(password));
                repository.save(user);
                return user;
            }
        }
        return null;
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
