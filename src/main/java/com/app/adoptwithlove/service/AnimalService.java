package com.app.adoptwithlove.service;

import com.app.adoptwithlove.entity.Animal;
import com.app.adoptwithlove.entity.Estado;
import com.app.adoptwithlove.entity.Fundacion;
import com.app.adoptwithlove.repository.AnimalesRepository;
import com.app.adoptwithlove.repository.EstadoRepository;
import com.app.adoptwithlove.service.Dao.Idao;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnimalService implements Idao<Animal, Long> {

    @Autowired
    private AnimalesRepository animalesRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    @Override
    public List<Animal> getAll() {
        return animalesRepository.findAll();
    }

    public List<Animal> getByFundacion(Long fundacionId) {
        Fundacion fundacion = new Fundacion();
        fundacion.setId(fundacionId);
        return animalesRepository.findByFundacion(fundacion);
    }

    @Override
    public Animal getById(Long id) {
        return animalesRepository.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public Animal create(Animal entity) {
        // Asignar estado ACTIVO automáticamente
       Estado activo = estadoRepository.findByNombreEstado("ACTIVO")
        .orElseThrow(() -> new RuntimeException("El estado 'ACTIVO' no existe. Ejecuta el seeder primero."));
        entity.setEstado(activo);
        return animalesRepository.save(entity);
    }
 
    @Transactional
    @Override
    public Animal update(Long id, Animal entity) {
        return animalesRepository.save(entity);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        // Eliminación lógica: cambiar a INACTIVO
        Animal animal = animalesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Animal no encontrado"));

        Estado inactivo = estadoRepository.findByNombreEstado("INACTIVO")
        .orElseThrow(() -> new RuntimeException("El estado 'INACTIVO' no existe. Ejecuta el seeder primero."));

        animal.setEstado(inactivo);
        animalesRepository.save(animal);
    }
}
