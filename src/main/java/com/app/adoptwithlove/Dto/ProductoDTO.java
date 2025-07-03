package com.app.adoptwithlove.Dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ProductoDTO {
    private Long id;
    private String nombre;
    private Double precio;     
    private String cantidad;  
    private String tipoProducto;
    private String descripcion;
    private String imagen;
}
