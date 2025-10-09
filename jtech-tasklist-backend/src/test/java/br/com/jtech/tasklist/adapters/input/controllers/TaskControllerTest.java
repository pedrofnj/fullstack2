package br.com.jtech.tasklist.adapters.input.controllers;

import br.com.jtech.tasklist.application.core.domains.Task;
import br.com.jtech.tasklist.application.core.domains.TaskList;
import br.com.jtech.tasklist.application.core.domains.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TaskControllerTest {

    @LocalServerPort
    int port;
    @Autowired
    TestRestTemplate restTemplate;

    private String usersUrl() { return "http://localhost:" + port + "/users"; }
    private String listsUrl() { return "http://localhost:" + port + "/tasklists"; }
    private String baseUrl() { return "http://localhost:" + port + "/tasks"; }

    private String userId;
    private String otherUserId;
    private String listId;

    @BeforeEach
    void init() {
        userId = createUser("userA");
        otherUserId = createUser("userB");
        listId = createList(userId);
    }

    private String createUser(String name) {
        User u = User.builder().name(name+UUID.randomUUID()).email(name+UUID.randomUUID()+"@ex.com").password("123").build();
        return restTemplate.postForEntity(usersUrl(), u, User.class).getBody().getId();
    }

    private String createList(String user) {
        TaskList tl = TaskList.builder().name("Lista"+UUID.randomUUID()).userId(user).build();
        return restTemplate.postForEntity(listsUrl(), tl, TaskList.class).getBody().getId();
    }

    private ResponseEntity<Task> createTask(String user, String list) {
        Task t = Task.builder().title("Task"+UUID.randomUUID()).description("Desc").userId(user).listId(list).dueDate(LocalDate.now().plusDays(3)).completed(false).build();
        return restTemplate.postForEntity(baseUrl(), t, Task.class);
    }

    @Test
    @DisplayName("[IT] Criar e buscar tarefa autorizada")
    void createAndGet() {
        ResponseEntity<Task> created = createTask(userId, listId);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", userId);
        ResponseEntity<Task> got = restTemplate.exchange(baseUrl()+"/"+created.getBody().getId(), HttpMethod.GET, new HttpEntity<>(headers), Task.class);
        assertThat(got.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(got.getBody().getId()).isEqualTo(created.getBody().getId());
    }

    @Test
    @DisplayName("[IT] Acesso não autorizado à tarefa")
    void getUnauthorized() {
        ResponseEntity<Task> created = createTask(userId, listId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", otherUserId);
        ResponseEntity<String> resp = restTemplate.exchange(baseUrl()+"/"+created.getBody().getId(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(resp.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    @DisplayName("[IT] Atualizar tarefa")
    void updateTask() {
        ResponseEntity<Task> created = createTask(userId, listId);
        Task toUpdate = created.getBody();
        toUpdate.setTitle("Novo"+UUID.randomUUID());
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", userId);
        ResponseEntity<Task> resp = restTemplate.exchange(baseUrl()+"/"+toUpdate.getId(), HttpMethod.PUT, new HttpEntity<>(toUpdate, headers), Task.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getTitle()).startsWith("Novo");
    }

    @Test
    @DisplayName("[IT] Deletar tarefa autorizado e tentar com outro usuário")
    void deleteTask() {
        ResponseEntity<Task> created = createTask(userId, listId);
        HttpHeaders headersOther = new HttpHeaders();
        headersOther.add("X-User-Id", otherUserId);
        ResponseEntity<String> unauthorized = restTemplate.exchange(baseUrl()+"/"+created.getBody().getId(), HttpMethod.DELETE, new HttpEntity<>(headersOther), String.class);
        assertThat(unauthorized.getStatusCode().is5xxServerError()).isTrue();

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", userId);
        ResponseEntity<Void> del = restTemplate.exchange(baseUrl()+"/"+created.getBody().getId(), HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
