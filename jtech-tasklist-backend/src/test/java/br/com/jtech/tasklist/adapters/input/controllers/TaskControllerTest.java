package br.com.jtech.tasklist.adapters.input.controllers;

import br.com.jtech.tasklist.adapters.input.dtos.TaskDTO;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TaskControllerTest {

    @LocalServerPort
    int port;
    @Autowired
    TestRestTemplate restTemplate;

    private String usersUrl() { return "http://localhost:" + port + "/auth"; }
    private String listsUrl() { return "http://localhost:" + port + "/tasklists"; }
    private String baseUrl() { return "http://localhost:" + port + "/tasks"; }

    private String userId;
    private String otherUserId;
    private String listId;
    private String userEmailA, userEmailB;
    private String tokenUserA, tokenUserB;

    @BeforeEach
    void init() {
        userEmailA = "userA@test.com";
        userId = createUser("userA", userEmailA);
        userEmailB = "userB@test.com";
        otherUserId = createUser("userB", userEmailB);
        tokenUserA = getToken(userEmailA, "123");
        tokenUserB = getToken(userEmailB, "123");
        listId = createList(userId);
    }

    private String createUser(String name, String email) {
        User u = User.builder().name(name).email(email).password("123").build();
        ResponseEntity<User> resp = restTemplate.postForEntity(usersUrl() + "/register", u, User.class);
        return resp.getBody().getId();
    }

    private String createList(String user) {
        TaskList tl = TaskList.builder().name("Lista"+UUID.randomUUID()).userId(user).build();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + tokenUserA);
        HttpEntity<TaskList> entity = new HttpEntity<>(tl, headers);
        return restTemplate.postForEntity(listsUrl(), entity, TaskList.class).getBody().getId();
    }

    private ResponseEntity<TaskDTO> createTask(String user, String list) {
        Task t = Task.builder().title("Task"+UUID.randomUUID()).description("Desc").userId(user).listId(list).dueDate(LocalDate.now().plusDays(3)).completed(false).build();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + (user.equals(userId) ? tokenUserA : tokenUserB));
        HttpEntity<Task> entity = new HttpEntity<>(t, headers);
        return restTemplate.postForEntity(baseUrl(), entity, TaskDTO.class);
    }

    private String getToken(String email, String password) {
        Map<String, String> login = Map.of("email", email, "password", password);
        ResponseEntity<Map> resp = restTemplate.postForEntity("http://localhost:" + port + "/auth/login", login, Map.class);
        return (String) resp.getBody().get("token");
    }

    @Test
    @DisplayName("[IT] Criar e buscar tarefa autorizada")
    void createAndGet() {
        ResponseEntity<TaskDTO> created = createTask(userId, listId);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + tokenUserA);
        headers.add("X-User-Id", userId);
        ResponseEntity<TaskDTO> got = restTemplate.exchange(baseUrl()+"/"+created.getBody().getId(), HttpMethod.GET, new HttpEntity<>(headers), TaskDTO.class);
        assertThat(got.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(got.getBody().getId()).isEqualTo(created.getBody().getId());
    }

    @Test
    @DisplayName("[IT] Acesso não autorizado à tarefa")
    void getUnauthorized() {
        ResponseEntity<TaskDTO> created = createTask(userId, listId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + tokenUserB);
        headers.add("X-User-Id", otherUserId);
        ResponseEntity<String> resp = restTemplate.exchange(baseUrl()+"/"+created.getBody().getId(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("[IT] Atualizar tarefa")
    void updateTask() {
        ResponseEntity<TaskDTO> created = createTask(userId, listId);
        Task toUpdate = Task.builder()
                .id(created.getBody().getId())
                .title("Novo"+UUID.randomUUID())
                .description(created.getBody().getDescription())
                .userId(created.getBody().getUserId())
                .listId(created.getBody().getListId())
                .dueDate(created.getBody().getDueDate() != null ? LocalDate.parse(created.getBody().getDueDate()) : null)
                .completed(created.getBody().getCompleted())
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + tokenUserA);
        headers.add("X-User-Id", userId);
        ResponseEntity<TaskDTO> resp = restTemplate.exchange(baseUrl()+"/"+toUpdate.getId(), HttpMethod.PUT, new HttpEntity<>(toUpdate, headers), TaskDTO.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getTitle()).startsWith("Novo");
    }

    @Test
    @DisplayName("[IT] Deletar tarefa autorizado e tentar com outro usuário")
    void deleteTask() {
        ResponseEntity<TaskDTO> created = createTask(userId, listId);
        HttpHeaders headersOther = new HttpHeaders();
        headersOther.add("Authorization", "Bearer " + tokenUserB);
        headersOther.add("X-User-Id", otherUserId);
        ResponseEntity<String> unauthorized = restTemplate.exchange(baseUrl()+"/"+created.getBody().getId(), HttpMethod.DELETE, new HttpEntity<>(headersOther), String.class);
        assertThat(unauthorized.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + tokenUserA);
        headers.add("X-User-Id", userId);
        ResponseEntity<Void> del = restTemplate.exchange(baseUrl()+"/"+created.getBody().getId(), HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
