package com.app.adoptwithlove.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name ="adopcion")
@Data

public class Adopcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_adopcion", nullable = false)
    private  Long id;

    @Column(name = "fecha", nullable = false)
    private String fecha;

    @ManyToOne
    @JoinColumn(name = "id_animal")
    private Animal animal;

    @ManyToOne
    @JoinColumn(name = "persona_id_persona")
    private Persona persona;

    @ManyToOne
    @JoinColumn(name = "estado_id", referencedColumnName = "id_estado")
    private Estado estado;
    
}
