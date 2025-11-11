package com.app.adoptwithlove.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name ="estado")
public class Estado {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_estado", nullable = false, length = 50)
    private String nombreEstado;

    @OneToMany(mappedBy = "estado")
    private List<Persona> personas;

    @OneToMany(mappedBy = "estado")
    private List<Productos> productos;

    @OneToMany(mappedBy = "estado")
    private List<Fundacion> fundaciones;

    @OneToMany(mappedBy = "estado")
    private List<Animal> animales;

    @OneToMany(mappedBy = "estado")
    private List<Adopcion> adopciones;
}
