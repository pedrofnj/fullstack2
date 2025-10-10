package br.com.jtech.tasklist.application.services;

import br.com.jtech.tasklist.application.core.domains.Task;
import br.com.jtech.tasklist.application.core.services.TaskService;
import br.com.jtech.tasklist.application.ports.output.TaskRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepositoryPort repository;

    @InjectMocks
    private TaskService service;

    private Task task;

    @BeforeEach
    void setUp() {
        task = Task.builder()
                .id("task-123")
                .title("Test Task")
                .description("Description")
                .userId("user-123")
                .listId("list-123")
                .dueDate(LocalDate.now().plusDays(1))
                .completed(false)
                .build();
    }

    @Nested
    class CreateTests {
        @Test
        @DisplayName("Deve criar tarefa quando título não existe na lista")
        void createSuccess() {
            when(repository.existsByTitleAndListId(task.getTitle(), task.getListId())).thenReturn(false);
            when(repository.save(task)).thenReturn(task);

            Task created = service.create(task);

            assertThat(created).isNotNull();
            assertThat(created.getTitle()).isEqualTo(task.getTitle());
            verify(repository).save(task);
        }

        @Test
        @DisplayName("Deve lançar exceção ao criar tarefa com título duplicado na lista")
        void createDuplicateTitle() {
            when(repository.existsByTitleAndListId(task.getTitle(), task.getListId())).thenReturn(true);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.create(task));
            assertThat(ex.getMessage()).contains("tarefa com esse título");
            verify(repository, never()).save(any());
        }
    }

    @Nested
    class GetByIdTests {
        @Test
        @DisplayName("Deve retornar tarefa quando pertence ao usuário")
        void getByIdSuccess() {
            when(repository.findById(task.getId())).thenReturn(Optional.of(task));

            Optional<Task> result = service.getById(task.getId(), task.getUserId());

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(task.getId());
        }

        @Test
        @DisplayName("Deve lançar exceção quando tarefa não pertence ao usuário")
        void getByIdUnauthorized() {
            when(repository.findById(task.getId())).thenReturn(Optional.of(task));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.getById(task.getId(), "other-user"));
            assertThat(ex.getMessage()).contains("permissão");
        }

        @Test
        @DisplayName("Deve retornar vazio quando tarefa não existe")
        void getByIdNotFound() {
            when(repository.findById(task.getId())).thenReturn(Optional.empty());

            Optional<Task> result = service.getById(task.getId(), task.getUserId());

            assertThat(result).isEmpty();
        }
    }

    @Test
    @DisplayName("Deve retornar todas as tarefas do usuário")
    void getByUser() {
        when(repository.findAllByUserId(task.getUserId())).thenReturn(List.of(task));

        List<Task> result = service.getByUser(task.getUserId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(task.getUserId());
    }

    @Test
    @DisplayName("Deve retornar tarefas da lista filtradas por usuário")
    void getByList() {
        Task otherTask = Task.builder().id("task-456").title("Other").userId("other-user").listId(task.getListId()).build();
        when(repository.findAllByListId(task.getListId())).thenReturn(List.of(task, otherTask));

        List<Task> result = service.getByList(task.getListId(), task.getUserId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(task.getId());
    }

    @Nested
    class UpdateTests {
        @Test
        @DisplayName("Deve atualizar tarefa quando pertence ao usuário")
        void updateSuccess() {
            when(repository.findById(task.getId())).thenReturn(Optional.of(task));
            when(repository.update(task)).thenReturn(task);

            Task updated = service.update(task, task.getUserId());

            assertThat(updated.getId()).isEqualTo(task.getId());
            verify(repository).update(task);
        }

        @Test
        @DisplayName("Deve lançar exceção quando tarefa não pertence ao usuário")
        void updateUnauthorized() {
            when(repository.findById(task.getId())).thenReturn(Optional.of(task));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.update(task, "other-user"));
            assertThat(ex.getMessage()).contains("permissão");
            verify(repository, never()).update(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando tarefa não existe")
        void updateNotFound() {
            when(repository.findById(task.getId())).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.update(task, task.getUserId()));
            assertThat(ex.getMessage()).contains("não encontrada");
            verify(repository, never()).update(any());
        }

        @Test
        @DisplayName("Deve lançar exceção ao alterar título para um já existente na lista")
        void updateDuplicateTitle() {
            Task updatedTask = Task.builder().id(task.getId()).title("New Title").listId(task.getListId()).userId(task.getUserId()).build();
            when(repository.findById(task.getId())).thenReturn(Optional.of(task));
            when(repository.existsByTitleAndListId("New Title", task.getListId())).thenReturn(true);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.update(updatedTask, task.getUserId()));
            assertThat(ex.getMessage()).contains("tarefa com esse título");
            verify(repository, never()).update(any());
        }
    }

    @Nested
    class DeleteTests {
        @Test
        @DisplayName("Deve deletar tarefa quando pertence ao usuário")
        void deleteSuccess() {
            when(repository.findById(task.getId())).thenReturn(Optional.of(task));

            service.delete(task.getId(), task.getUserId());

            verify(repository).deleteById(task.getId());
        }

        @Test
        @DisplayName("Deve lançar exceção quando tarefa não pertence ao usuário")
        void deleteUnauthorized() {
            when(repository.findById(task.getId())).thenReturn(Optional.of(task));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.delete(task.getId(), "other-user"));
            assertThat(ex.getMessage()).contains("permissão");
            verify(repository, never()).deleteById(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando tarefa não existe")
        void deleteNotFound() {
            when(repository.findById(task.getId())).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.delete(task.getId(), task.getUserId()));
            assertThat(ex.getMessage()).contains("não encontrada");
            verify(repository, never()).deleteById(any());
        }
    }

    @Nested
    class ToggleCompletedTests {
        @Test
        @DisplayName("Deve alternar status quando tarefa pertence ao usuário")
        void toggleSuccess() {
            when(repository.findById(task.getId())).thenReturn(Optional.of(task));
            when(repository.update(any())).thenReturn(task);

            Task result = service.toggleCompleted(task.getId(), true, task.getUserId());

            assertThat(result).isNotNull();
            verify(repository).update(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando tarefa não pertence ao usuário")
        void toggleUnauthorized() {
            when(repository.findById(task.getId())).thenReturn(Optional.of(task));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.toggleCompleted(task.getId(), true, "other-user"));
            assertThat(ex.getMessage()).contains("permissão");
            verify(repository, never()).update(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando tarefa não existe")
        void toggleNotFound() {
            when(repository.findById(task.getId())).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> service.toggleCompleted(task.getId(), true, task.getUserId()));
            assertThat(ex.getMessage()).contains("não encontrada");
            verify(repository, never()).update(any());
        }
    }

    @Test
    @DisplayName("Deve retornar todas as tarefas")
    void getAll() {
        when(repository.findAll()).thenReturn(List.of(task));

        List<Task> result = service.getAll();

        assertThat(result).hasSize(1);
    }
}
