package com.app.adoptwithlove.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "persona")
@Getter
@Setter
@ToString(exclude = { "rol", "fundaciones", "productos", "adopciones" })
@EqualsAndHashCode(exclude = { "rol", "fundaciones", "productos", "adopciones" })
public class Persona {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona", nullable = false)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;

    @Column(name = "contacto", nullable = false)
    private String contacto;

    @Column(name = "fechaNacimiento", nullable = false, length = 100)
    private String fechaNacimiento;

    @Column(name = "contrasena", nullable = false, length = 100)
    private String contrasena;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @ManyToOne
    @JoinColumn(name = "estado_id", referencedColumnName = "id_estado")
    @JsonBackReference // rompe ciclo Estado -> Persona
    private Estado estado;

    @ManyToOne
    @JoinColumn(name = "rol_id_rol")
    private Rol rol;

    @OneToMany(mappedBy = "persona")
    @JsonManagedReference // fundaciones que pertenecen a esta persona
    private List<Fundacion> fundaciones;

    @OneToMany(mappedBy = "persona")
    @JsonManagedReference // productos que pertenecen a esta persona
    private List<Productos> productos;

    @OneToMany(mappedBy = "persona")
    private List<Adopcion> adopciones;
}
