package com.app.adoptwithlove.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "estado")
public class Estado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado", nullable = false)
    private Long id;

    @Column(name = "nombre_estado", nullable = false, length = 50)
    private String nombreEstado;

    @OneToMany(mappedBy = "estado")
    @JsonManagedReference // personas que apuntan a este estado
    private List<Persona> personas;

    @OneToMany(mappedBy = "estado")
    @JsonManagedReference // productos que apuntan a este estado
    private List<Productos> productos;

    // Lo mismo si quieres para fundaciones, animales, adopciones
    @OneToMany(mappedBy = "estado")
    @JsonManagedReference
    private List<Fundacion> fundaciones;

    @OneToMany(mappedBy = "estado")
    @JsonManagedReference
    private List<Animal> animales;

    @OneToMany(mappedBy = "estado")
    @JsonManagedReference
    private List<Adopcion> adopciones;
}
