package com.app.adoptwithlove.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rol")
@Getter
@Setter
@ToString(exclude = "personas")
@EqualsAndHashCode(exclude = "personas")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol", nullable = false)
    private Long id;

    @Column(name = "nombreRol", nullable = false)
    private String nombreRol;

    @OneToMany(mappedBy = "rol")
    @JsonIgnore // evita bucle al serializar
    private List<Persona> personas;
}
