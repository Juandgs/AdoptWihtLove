package com.app.adoptwithlove.entity;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "animal")
@Data
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false) // antes estaba mal como id_persona
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "edad", nullable = false)
    private Integer edad;

    @Column(name = "raza", nullable = false)
    private String raza;

    @Column(name = "tipo_animal", nullable = false)
    private String tipo_animal;

    @Column(name = "imagen", columnDefinition = "TEXT") // base64 puede ser largo
    private String imagen;

    @ManyToOne
    @JoinColumn(name = "fundacion_id_fundacion")
    @JsonIgnoreProperties("animales")
    private Fundacion fundacion;

    @OneToMany(mappedBy = "animal")
    private List<Adopcion> adopciones;
}
