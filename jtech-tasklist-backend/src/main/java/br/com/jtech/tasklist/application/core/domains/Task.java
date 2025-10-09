package br.com.jtech.tasklist.application.core.domains;

import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private String id;
    private String userId;
    private String listId;
    private String title;
    private String description;
    private boolean completed;
    private LocalDate dueDate;
}
