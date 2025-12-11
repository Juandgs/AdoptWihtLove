package com.app.adoptwithlove.service;

import com.app.adoptwithlove.entity.Adopcion;
import com.app.adoptwithlove.entity.Estado;
import com.app.adoptwithlove.repository.AdopcionRepository;
import com.app.adoptwithlove.repository.EstadoRepository;
import com.app.adoptwithlove.service.Dao.Idao;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdopcionService implements Idao<Adopcion, Long> {

    @Autowired
    private AdopcionRepository Adopcion;

        public Adopcion getUltimaAdopcionPorAnimal(Long animalId) {
            return Adopcion.findFirstByAnimalIdOrderByFechaDesc(animalId);
        }
    @Autowired
    private EstadoRepository estadoRepository;

    @Override
    public List<Adopcion> getAll() {
        return Adopcion.findAll();
    }

    @Override
    public Adopcion getById(Long id) {
        return Adopcion.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public Adopcion create(Adopcion entity) {
        // Buscar estado ACTIVO
        Estado activo = estadoRepository.findByNombreEstado("ACTIVO")
                .orElseThrow(() -> new RuntimeException("Estado ACTIVO no encontrado"));

        entity.setEstado(activo);
        return Adopcion.save(entity);
    }

    @Transactional
    @Override
    public Adopcion update(Long id, Adopcion entity) {
        return Adopcion.save(entity);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Adopcion adopcion = Adopcion.findById(id)
                .orElseThrow(() -> new RuntimeException("Adopcion no encontrada con ID: " + id));

        // Buscar estado BLOQUEADO
        Estado bloqueado = estadoRepository.findByNombreEstado("BLOQUEADO")
                .orElseThrow(() -> new RuntimeException("Estado BLOQUEADO no encontrado"));

        adopcion.setEstado(bloqueado);
        Adopcion.save(adopcion);
    }
    
    // Obtener historial de adopciones por animal (más reciente primero)
    public java.util.List<Adopcion> getHistorialPorAnimal(Long animalId) {
        return Adopcion.findByAnimalIdOrderByFechaDesc(animalId);
    }
    
}
