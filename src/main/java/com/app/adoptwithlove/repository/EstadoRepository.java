package com.app.adoptwithlove.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.adoptwithlove.entity.Estado;

public interface EstadoRepository extends JpaRepository<Estado, Long>  {
    Optional<Estado> findById(Long id);
    
}
