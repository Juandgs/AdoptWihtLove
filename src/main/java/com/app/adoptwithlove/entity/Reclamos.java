package com.app.adoptwithlove.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "reclamos")
@Data
public class Reclamos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reclamo", nullable = false)
    private Long id;

    @Column(name = "correo", nullable = false)
    private String correo;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    @JsonIgnoreProperties("reclamos") // evita recursión infinita
    private Productos producto;

}
