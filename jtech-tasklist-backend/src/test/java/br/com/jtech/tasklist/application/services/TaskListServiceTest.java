package br.com.jtech.tasklist.application.services;

import br.com.jtech.tasklist.application.core.domains.TaskList;
import br.com.jtech.tasklist.application.core.services.TaskListService;
import br.com.jtech.tasklist.application.ports.output.TaskListRepositoryPort;
import br.com.jtech.tasklist.application.ports.output.TaskRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskListServiceTest {

    @Mock
    private TaskListRepositoryPort repository;
    @Mock
    private TaskRepositoryPort taskRepository;

    @InjectMocks
    private TaskListService service;

    private TaskList list;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
        list = TaskList.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .name("Lista")
                .build();
    }

    @Test
    @DisplayName("Deve criar lista")
    void create() {
        when(repository.save(any())).thenReturn(list);
        TaskList created = service.create(list);
        assertThat(created.getName()).isEqualTo("Lista");
    }

    @Nested
    class GetByIdSecurity {
        @Test
        @DisplayName("Deve retornar lista quando usuário é dono")
        void getByIdOk() {
            when(repository.findById(list.getId())).thenReturn(Optional.of(list));
            assertThat(service.getById(list.getId(), userId)).isPresent();
        }

        @Test
        @DisplayName("Deve lançar exceção quando usuário não é dono")
        void getByIdUnauthorized() {
            when(repository.findById(list.getId())).thenReturn(Optional.of(list));
            assertThrows(IllegalArgumentException.class, () -> service.getById(list.getId(), UUID.randomUUID().toString()));
        }
    }

    @Nested
    class DeleteTests {
        @Test
        @DisplayName("Deve deletar lista quando sem tarefas e autorizado")
        void deleteSuccess() {
            when(repository.findById(list.getId())).thenReturn(Optional.of(list));
            when(taskRepository.existsByListId(list.getId())).thenReturn(false);
            service.delete(list.getId(), userId);
            verify(repository).deleteById(list.getId());
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar lista inexistente")
        void deleteNotFound() {
            when(repository.findById(list.getId())).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> service.delete(list.getId(), userId));
            verify(repository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar lista de outro usuário")
        void deleteUnauthorized() {
            when(repository.findById(list.getId())).thenReturn(Optional.of(list));
            assertThrows(IllegalArgumentException.class, () -> service.delete(list.getId(), UUID.randomUUID().toString()));
            verify(repository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar lista com tarefas")
        void deleteWithTasks() {
            when(repository.findById(list.getId())).thenReturn(Optional.of(list));
            when(taskRepository.existsByListId(list.getId())).thenReturn(true);
            assertThrows(IllegalArgumentException.class, () -> service.delete(list.getId(), userId));
            verify(repository, never()).deleteById(any());
        }
    }

    @Nested
    class UpdateTests {
        @Test
        @DisplayName("Deve atualizar lista quando autorizado")
        void updateSuccess() {
            when(repository.findById(list.getId())).thenReturn(Optional.of(list));
            when(repository.update(any())).thenReturn(list);
            TaskList updated = service.update(list, userId);
            assertThat(updated.getId()).isEqualTo(list.getId());
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar lista inexistente")
        void updateNotFound() {
            when(repository.findById(list.getId())).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> service.update(list, userId));
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar lista de outro usuário")
        void updateUnauthorized() {
            when(repository.findById(list.getId())).thenReturn(Optional.of(list));
            assertThrows(IllegalArgumentException.class, () -> service.update(list, UUID.randomUUID().toString()));
        }
    }

    @Test
    @DisplayName("Deve retornar listas por usuário")
    void getByUser() {
        when(repository.findAllByUserId(userId)).thenReturn(List.of(list));
        assertThat(service.getByUser(userId)).hasSize(1);
    }

    @Test
    @DisplayName("Deve retornar todas listas")
    void getAll() {
        when(repository.findAll()).thenReturn(List.of(list));
        assertThat(service.getAll()).hasSize(1);
    }
}

