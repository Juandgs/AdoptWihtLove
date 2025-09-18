package com.app.adoptwithlove.repository;

import com.app.adoptwithlove.entity.Reclamos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReclamosRepository extends JpaRepository<Reclamos, Long> {
}
