package br.com.jtech.tasklist.application.services;

import br.com.jtech.tasklist.application.core.domains.User;
import br.com.jtech.tasklist.application.core.services.UserService;
import br.com.jtech.tasklist.application.ports.output.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepositoryPort repository;

    @InjectMocks
    private UserService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("11111111-1111-1111-1111-111111111111")
                .name("Pedro")
                .email("pedro@example.com")
                .password("123456")
                .build();
    }

    @Nested
    class CreateTests {
        @Test
        @DisplayName("Deve criar usuário quando email não existe")
        void createSuccess() {
            when(repository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
            when(repository.save(any())).thenReturn(user);

            User created = service.create(user);

            assertThat(created).isNotNull();
            assertThat(created.getEmail()).isEqualTo(user.getEmail());
            verify(repository).save(any());
        }

        @Test
        @DisplayName("Deve lançar exceção ao criar usuário com email duplicado")
        void createDuplicateEmail() {
            when(repository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.create(user));
            assertThat(ex.getMessage()).contains("email");
            verify(repository, never()).save(any());
        }
    }

    @Nested
    class UpdateTests {
        @Test
        @DisplayName("Deve atualizar usuário mantendo o mesmo email")
        void updateSameEmail() {
            when(repository.findById(user.getId())).thenReturn(Optional.of(user));
            when(repository.update(any())).thenReturn(user);

            User updated = service.update(user);
            assertThat(updated.getName()).isEqualTo("Pedro");
            verify(repository, never()).findByEmail(user.getEmail()); // não precisa consultar novamente pois email igual
            verify(repository).update(any());
        }

        @Test
        @DisplayName("Deve atualizar usuário alterando email para um email livre")
        void updateChangeEmailSuccess() {
            User existing = User.builder().id(user.getId()).name("Pedro").email("old@example.com").password("123").build();
            User toUpdate = User.builder().id(user.getId()).name("Pedro").email("new@example.com").password("123").build();

            when(repository.findById(user.getId())).thenReturn(Optional.of(existing));
            when(repository.findByEmail("new@example.com"))
                    .thenReturn(Optional.empty());
            when(repository.update(any())).thenReturn(toUpdate);

            User updated = service.update(toUpdate);
            assertThat(updated.getEmail()).isEqualTo("new@example.com");
            verify(repository).findByEmail("new@example.com");
            verify(repository).update(any());
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar atualizar usuário inexistente")
        void updateNotFound() {
            when(repository.findById(user.getId())).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.update(user));
            assertThat(ex.getMessage()).contains("Usuário");
            verify(repository, never()).update(any());
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar alterar email para um já usado")
        void updateChangeEmailDuplicate() {
            User existing = User.builder().id(user.getId()).name("Pedro").email("old@example.com").password("123").build();
            User toUpdate = User.builder().id(user.getId()).name("Pedro").email("duplicado@example.com").password("123").build();

            when(repository.findById(user.getId())).thenReturn(Optional.of(existing));
            when(repository.findByEmail("duplicado@example.com"))
                    .thenReturn(Optional.of(User.builder().id("22222222-2222-2222-2222-222222222222").email("duplicado@example.com").name("X").password("x").build()));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> service.update(toUpdate));
            assertThat(ex.getMessage()).contains("email");
            verify(repository, never()).update(any());
        }
    }

    @Test
    @DisplayName("Deve retornar usuário por id")
    void getById() {
        when(repository.findById(user.getId())).thenReturn(Optional.of(user));
        Optional<User> opt = service.getById(user.getId());
        assertThat(opt).isPresent();
    }

    @Test
    @DisplayName("Deve retornar todos usuários")
    void getAll() {
        when(repository.findAll()).thenReturn(List.of(user));
        List<User> all = service.getAll();
        assertThat(all).hasSize(1);
    }

    @Test
    @DisplayName("Deve deletar usuário por id")
    void delete() {
        service.delete(user.getId());
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(repository).deleteById(captor.capture());
        assertThat(captor.getValue()).isEqualTo(user.getId());
    }

    @Nested
    class LoginTests {
        @Test
        @DisplayName("Login sucesso")
        void loginOk() {
            when(repository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            User result = service.login(user.getEmail(), user.getPassword());
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Login falha senha incorreta")
        void loginWrongPassword() {
            when(repository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            User result = service.login(user.getEmail(), "x");
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Login falha email inexistente")
        void loginEmailNotFound() {
            when(repository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
            User result = service.login(user.getEmail(), user.getPassword());
            assertThat(result).isNull();
        }
    }
}
