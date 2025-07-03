package com.app.adoptwithlove.Dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class personaDTO {
    private  Long id;
    private String nombre;
    private String apellido;
    private String contacto;
    private String fechaNacimiento;
    private String contrasena;
    private String email;
}
