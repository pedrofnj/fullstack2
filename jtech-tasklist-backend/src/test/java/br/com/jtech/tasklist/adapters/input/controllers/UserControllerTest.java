package br.com.jtech.tasklist.adapters.input.controllers;

import br.com.jtech.tasklist.application.core.domains.User;
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
class UserControllerTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    private String baseUrl() { return "http://localhost:" + port + "/users"; }

    @Test
    @DisplayName("[IT] Deve criar usuário e retornar 200")
    void createUser() {
        User u = User.builder().name("Pedro"+UUID.randomUUID()).email("pedro"+UUID.randomUUID()+"@ex.com").password("123").build();
        ResponseEntity<User> resp = restTemplate.postForEntity(baseUrl(), u, User.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getId()).isNotBlank();
    }

    @Test
    @DisplayName("[IT] Deve retornar 500 ao criar usuário com email duplicado")
    void createDuplicateEmail() {
        String email = "dup"+UUID.randomUUID()+"@ex.com";
        User u1 = User.builder().name("A").email(email).password("1").build();
        User u2 = User.builder().name("B").email(email).password("1").build();
        restTemplate.postForEntity(baseUrl(), u1, User.class);
        ResponseEntity<String> resp = restTemplate.postForEntity(baseUrl(), u2, String.class);
        assertThat(resp.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    @DisplayName("[IT] Deve retornar 404 ao buscar usuário inexistente")
    void getNotFound() {
        ResponseEntity<User> resp = restTemplate.getForEntity(baseUrl()+"/"+UUID.randomUUID(), User.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("[IT] Login sucesso e falha")
    void loginFlow() {
        String email = "login"+UUID.randomUUID()+"@ex.com";
        User u = User.builder().name("Login").email(email).password("123").build();
        restTemplate.postForEntity(baseUrl(), u, User.class);

        ResponseEntity<Map> ok = restTemplate.postForEntity(baseUrl()+"/login", Map.of("email", email, "password", "123"), Map.class);
        assertThat(ok.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ok.getBody()).containsKey("token");

        ResponseEntity<Map> fail = restTemplate.postForEntity(baseUrl()+"/login", Map.of("email", email, "password", "x"), Map.class);
        assertThat(fail.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
