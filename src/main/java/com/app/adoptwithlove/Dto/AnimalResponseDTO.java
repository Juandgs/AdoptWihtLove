package com.app.adoptwithlove.Dto;

import com.app.adoptwithlove.entity.Animal;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnimalResponseDTO {
    private Long id;
    private String nombre;
    private int edad;
    private String raza;
    private String tipo_animal;
    private String imagen;
    private String estadoNombre;

    public AnimalResponseDTO(Animal a) {
        this.id = a.getId();
        this.nombre = a.getNombre();
        this.edad = a.getEdad();
        this.raza = a.getRaza();
        this.tipo_animal = a.getTipo_animal();
        this.imagen = a.getImagen();
        this.estadoNombre = a.getEstado() != null ? a.getEstado().getNombreEstado() : null;
    }
}
