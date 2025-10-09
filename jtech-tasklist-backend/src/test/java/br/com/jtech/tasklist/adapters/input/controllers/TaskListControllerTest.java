package br.com.jtech.tasklist.adapters.input.controllers;

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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TaskListControllerTest {

    @LocalServerPort
    int port;
    @Autowired
    TestRestTemplate restTemplate;

    private String usersUrl() { return "http://localhost:" + port + "/users"; }
    private String baseUrl() { return "http://localhost:" + port + "/tasklists"; }

    private String userId;
    private String otherUserId;

    @BeforeEach
    void init() {
        userId = createUser("userA");
        otherUserId = createUser("userB");
    }

    private String createUser(String name) {
        User u = User.builder().name(name+UUID.randomUUID()).email(name+UUID.randomUUID()+"@ex.com").password("123").build();
        ResponseEntity<User> resp = restTemplate.postForEntity(usersUrl(), u, User.class);
        return resp.getBody().getId();
    }

    private ResponseEntity<TaskList> createList(String user) {
        TaskList tl = TaskList.builder().name("Lista"+UUID.randomUUID()).userId(user).build();
        return restTemplate.postForEntity(baseUrl(), tl, TaskList.class);
    }

    @Test
    @DisplayName("[IT] Criar e buscar lista por id autorizada")
    void createAndGet() {
        ResponseEntity<TaskList> created = createList(userId);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", userId);
        ResponseEntity<TaskList> got = restTemplate.exchange(baseUrl()+"/"+created.getBody().getId(), HttpMethod.GET, new HttpEntity<>(headers), TaskList.class);
        assertThat(got.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(got.getBody().getId()).isEqualTo(created.getBody().getId());
    }

    @Test
    @DisplayName("[IT] Acesso não autorizado à lista")
    void getUnauthorized() {
        ResponseEntity<TaskList> created = createList(userId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", otherUserId);
        ResponseEntity<String> resp = restTemplate.exchange(baseUrl()+"/"+created.getBody().getId(), HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(resp.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    @DisplayName("[IT] Atualizar lista")
    void updateList() {
        ResponseEntity<TaskList> created = createList(userId);
        TaskList toUpdate = created.getBody();
        toUpdate.setName("Nova"+UUID.randomUUID());
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", userId);
        ResponseEntity<TaskList> resp = restTemplate.exchange(baseUrl()+"/"+toUpdate.getId(), HttpMethod.PUT, new HttpEntity<>(toUpdate, headers), TaskList.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getName()).startsWith("Nova");
    }

    @Test
    @DisplayName("[IT] Deletar lista")
    void deleteList() {
        ResponseEntity<TaskList> created = createList(userId);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-User-Id", userId);
        ResponseEntity<Void> del = restTemplate.exchange(baseUrl()+"/"+created.getBody().getId(), HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}
