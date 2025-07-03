package com.app.adoptwithlove.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name ="productos")
@Data
public class Productos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto", nullable = false)
    private  Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name="precio", nullable = false)
    private Double  precio;

    @Column(name="cantidad", nullable = false, length = 100)
    private String cantidad;

    @Column(name="tipoProducto", nullable = false)
    private String tipoProducto;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "imagen", columnDefinition = "TEXT")
    private String imagen;

    @ManyToOne
    @JoinColumn(name = "personaId")
    @JsonIgnoreProperties({"productos", "fundaciones", "adopciones", "rol"}) // evita recursión
    private Persona persona;

}
