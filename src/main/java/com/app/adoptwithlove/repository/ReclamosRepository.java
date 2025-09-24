package com.app.adoptwithlove.repository;

import com.app.adoptwithlove.entity.Reclamos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReclamosRepository extends JpaRepository<Reclamos, Long> {
    List<Reclamos> findByProducto_Id(Long productoId);
}
