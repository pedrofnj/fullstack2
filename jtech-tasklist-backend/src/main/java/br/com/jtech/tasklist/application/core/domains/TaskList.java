package br.com.jtech.tasklist.application.core.domains;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskList {
    private String id;
    private String userId;
    private String name;
}