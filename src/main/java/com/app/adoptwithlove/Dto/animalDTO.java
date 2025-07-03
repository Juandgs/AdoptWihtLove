package com.app.adoptwithlove.Dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnimalDTO {
    private Long id;
    private String nombre;
    private Integer edad;
    private String raza;
    private String tipo_animal;
    private String imagen;        // Imagen en base64
}
