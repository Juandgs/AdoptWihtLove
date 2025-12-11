package com.app.adoptwithlove.Dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnimalDTO {
    private Long id;
    private String nombre;
    private int edad;
    private String raza;
    private String tipo_animal;
    private String imagen;
    private String estadoNombre;
    
    // Constructor existente
    public AnimalDTO(Long id, String nombre, int edad, String raza, String tipo_animal, String imagen) {
        this.id = id;
        this.nombre = nombre;
        this.edad = edad;
        this.raza = raza;
        this.tipo_animal = tipo_animal;
        this.imagen = imagen;
    }
}
