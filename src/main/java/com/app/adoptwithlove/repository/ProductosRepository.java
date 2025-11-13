package com.app.adoptwithlove.repository;

import com.app.adoptwithlove.entity.Persona;
import com.app.adoptwithlove.entity.Productos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductosRepository extends JpaRepository<Productos, Long> {
    Productos findByNombreAndTipoProductoAndPersona(String nombre, String tipoProducto, Persona persona);
    List<Productos> findByPersona(Persona persona);
    List<Productos> findByPersonaEmail(String email);
    List<Productos> findByPersona_Id(Long personaId);
    // Filtrar por estado usando el nombre del estado
    List<Productos> findByEstado_NombreEstadoIn(List<String> nombresEstado);

    // Filtrar por vendedor y estado
    List<Productos> findByPersona_IdAndEstado_NombreEstadoIn(Long personaId, List<String> nombresEstado);
}
