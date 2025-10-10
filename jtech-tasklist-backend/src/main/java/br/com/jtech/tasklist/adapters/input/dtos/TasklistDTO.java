package br.com.jtech.tasklist.adapters.input.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TasklistDTO {
    private String id;

    private String name;

    private String userId;
}
