package com.app.adoptwithlove.repository;

import com.app.adoptwithlove.entity.Animal;
import com.app.adoptwithlove.entity.Fundacion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnimalesRepository extends JpaRepository<Animal, Long> {
    List<Animal> findByFundacion(Fundacion fundacion);
}
