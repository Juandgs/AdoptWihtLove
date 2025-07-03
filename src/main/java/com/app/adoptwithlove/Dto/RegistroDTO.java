package com.app.adoptwithlove.Dto;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RegistroDTO {
    private String nombreRol;

    // Campos comunes (Persona)
    private String nombre;
    private String apellido;
    private String contacto;
    private String fechaNacimiento;
    private String contrasena;
    private String email;

    // Campos solo para Fundación
    private String nit;
    private String nombreFundacion;
    private String direccion;
    private String correoFundacion;
    private String telefonoFundacion;

    // Getters y setters..

}
