package com.app.adoptwithlove.repository;

import com.app.adoptwithlove.entity.Persona;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Long> {
    Optional<Persona> findByEmail(String email);
    List<Persona> findByRol_NombreRolAndEstado(String rol, String estado);
    Optional<Persona> findById(Long id);
}
