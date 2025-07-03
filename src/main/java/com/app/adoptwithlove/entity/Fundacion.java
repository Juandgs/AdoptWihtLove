package com.app.adoptwithlove.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name ="fundacion")
@Data
public class Fundacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fundacion", nullable = false)
    private Long id;

    @Column(name = "nombre_fundacion", nullable = false)
    private String nombre_fundacion;

    @Column(name = "direccion", nullable = false, length = 100)
    private String direccion;

    @Column(name = "correo", nullable = false)
    private String correo;

    @Column(name = "telefono", nullable = false, length = 100)
    private String telefono;

    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "persona_id_persona")
    private Persona persona;

    @OneToMany(mappedBy = "fundacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("fundacion")
    private List<Animal> animales;

}
