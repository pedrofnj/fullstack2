package br.com.jtech.tasklist.adapters.input.controllers;

import br.com.jtech.tasklist.adapters.input.dtos.TasklistDTO;
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

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TaskListControllerTest {

    @LocalServerPort
    int port;
    @Autowired
    TestRestTemplate restTemplate;

    private String usersUrl() { return "http://localhost:" + port + "/auth"; }
    private String baseUrl() { return "http://localhost:" + port + "/tasklists"; }

    private String userId;
    private String otherUserId;
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
    }

    private String createUser(String name, String email) {
        User u = User.builder().name(name).email(email).password("123").build();
        ResponseEntity<User> resp = restTemplate.postForEntity(usersUrl() + "/register", u, User.class);
        return resp.getBody().getId();
    }

    private ResponseEntity<TasklistDTO> createList(String user) {
        TaskList tl = TaskList.builder().name("Lista"+UUID.randomUUID()).userId(user).build();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + tokenUserA);
        headers.add("X-User-Id", user);
        HttpEntity<TaskList> entity = new HttpEntity<>(tl, headers);
        return restTemplate.postForEntity(baseUrl(), entity, TasklistDTO.class);
    }

    private String getToken(String email, String password) {
        Map<String, String> login = Map.of("email", email, "password", password);
        ResponseEntity<Map> resp = restTemplate.postForEntity("http://localhost:" + port + "/auth/login", login, Map.class);
        return (String) resp.getBody().get("token");
    }

    @Test
    @DisplayName("[IT] Criar e buscar lista por id autorizada")
    void createAndGet() {
        ResponseEntity<TasklistDTO> created = createList(userId);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + tokenUserA);
        headers.add("X-User-Id", userId);
        ResponseEntity<TasklistDTO> got = restTemplate.exchange(baseUrl()+"/"+created.getBody().getId(), HttpMethod.GET, new HttpEntity<>(headers), TasklistDTO.class);
        assertThat(got.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(got.getBody().getId()).isEqualTo(created.getBody().getId());
    }

    @Test
    @DisplayName("[IT] Acesso não autorizado à lista")
    void getUnauthorized() {
        ResponseEntity<TasklistDTO> created = createList(userId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + tokenUserB);
        headers.add("X-User-Id", otherUserId);
        ResponseEntity<String> resp = restTemplate.exchange(baseUrl()+"/"+created.getBody().getId(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("[IT] Atualizar lista")
    void updateList() {
        ResponseEntity<TasklistDTO> created = createList(userId);
        TaskList toUpdate = TaskList.builder()
                .id(created.getBody().getId())
                .name("Nova"+UUID.randomUUID())
                .userId(created.getBody().getUserId())
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + tokenUserA);
        headers.add("X-User-Id", userId);
        ResponseEntity<TasklistDTO> resp = restTemplate.exchange(baseUrl()+"/"+toUpdate.getId(), HttpMethod.PUT, new HttpEntity<>(toUpdate, headers), TasklistDTO.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getName()).startsWith("Nova");
    }

    @Test
    @DisplayName("[IT] Deletar lista")
    void deleteList() {
        ResponseEntity<TasklistDTO> created = createList(userId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + tokenUserA);
        headers.add("X-User-Id", userId);
        ResponseEntity<Void> del = restTemplate.exchange(baseUrl()+"/"+created.getBody().getId(), HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
