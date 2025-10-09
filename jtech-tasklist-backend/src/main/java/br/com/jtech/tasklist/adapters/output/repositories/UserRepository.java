package br.com.jtech.tasklist.adapters.output.repositories;

import br.com.jtech.tasklist.adapters.output.repositories.entities.UserEntity;
import br.com.jtech.tasklist.application.core.domains.User;
import br.com.jtech.tasklist.application.ports.output.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserRepository implements UserRepositoryPort {

    private final SpringDataUserRepository repository;

    @Override
    public User save(User user) {
        UserEntity entity = UserEntity.builder()
                .id(user.getId() != null ? java.util.UUID.fromString(user.getId()) : null)
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .build();

        UserEntity saved = repository.save(entity);
        return User.builder()
                .id(saved.getId().toString())
                .name(saved.getName())
                .email(saved.getEmail())
                .password(saved.getPassword())
                .build();
    }

    @Override
    public User update(User user) {
        UserEntity entity = UserEntity.builder()
                .id(java.util.UUID.fromString(user.getId()))
                .name(user.getName())
                .email(user.getEmail())
                .password(user.getPassword())
                .build();

        UserEntity saved = repository.save(entity);
        return User.builder()
                .id(saved.getId().toString())
                .name(saved.getName())
                .email(saved.getEmail())
                .password(saved.getPassword())
                .build();
    }

    @Override
    public Optional<User> findById(String id) {
        return repository.findById(java.util.UUID.fromString(id))
                .map(e -> User.builder()
                        .id(e.getId().toString())
                        .name(e.getName())
                        .email(e.getEmail())
                        .password(e.getPassword())
                        .build());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email)
                .map(e -> User.builder()
                        .id(e.getId().toString())
                        .name(e.getName())
                        .email(e.getEmail())
                        .password(e.getPassword())
                        .build());
    }

    @Override
    public List<User> findAll() {
        return repository.findAll().stream()
                .map(e -> User.builder()
                        .id(e.getId().toString())
                        .name(e.getName())
                        .email(e.getEmail())
                        .password(e.getPassword())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(java.util.UUID.fromString(id));
    }
}
