package com.app.adoptwithlove.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.app.adoptwithlove.entity.Rol;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
        Rol findByNombreRol(String nombreRol);
}
