package com.app.adoptwithlove.service;
import com.app.adoptwithlove.entity.Adopcion;
import com.app.adoptwithlove.repository.AdopcionRepository;
import com.app.adoptwithlove.service.Dao.Idao;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdopcionService implements Idao<Adopcion, Long>{
    @Autowired
    private AdopcionRepository Adopcion;
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
    public Adopcion create(Adopcion entity){
        return Adopcion.save(entity);
    }

    @Transactional
    @Override
    public Adopcion update(Long id, Adopcion entity){
        return Adopcion.save(entity);
    }

    @Transactional
    @Override
    public void delete(Long id){
        Adopcion.deleteById(id);
    }
}
