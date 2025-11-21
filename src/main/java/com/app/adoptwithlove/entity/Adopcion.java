package com.app.adoptwithlove.entity;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "adopcion")
@Data

public class Adopcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_adopcion", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_animal")
    private Animal animal;

    @ManyToOne
    @JoinColumn(name = "estado_id", referencedColumnName = "id_estado")
    private Estado estado;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "telefono", nullable = false)
    private String telefono;

    @Column(name = "edad", nullable = false, length = 100)
    private String edad;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "direccion", nullable = false, length = 100)
    private String direccion;

    @Column(name = "nIdentificacion", nullable = false)
    private String nIdentificacion;

    @PrePersist
    public void prePersist() {
        this.fecha = LocalDate.now();
    }
}
