package com.app.adoptwithlove.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.app.adoptwithlove.entity.Adopcion;

@Repository
public interface AdopcionRepository extends JpaRepository<Adopcion, Long> {


	Adopcion findFirstByAnimalIdOrderByFechaDesc(Long animalId);

	// Devuelve todas las adopciones de un animal ordenadas de la más reciente a la más antigua
	java.util.List<Adopcion> findByAnimalIdOrderByFechaDesc(Long animalId);
}
