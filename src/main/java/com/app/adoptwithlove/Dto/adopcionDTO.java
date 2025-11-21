package com.app.adoptwithlove.Dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class adopcionDTO {
    private  Long id;
    private String fecha;
    private String nombre;
    private String apellido;
    private String telefono;
    private String edad;
    private String email;    
    private String direccion;
    private String nIdentificacion;
}
