package com.app.adoptwithlove.repository;

import com.app.adoptwithlove.entity.Fundacion;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FundacionRepository extends JpaRepository<Fundacion, Long> {
    Optional<Fundacion> findByPersona_Id(Long idPersona);
}
