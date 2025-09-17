package com.app.adoptwithlove.service;

import com.app.adoptwithlove.entity.Reclamos;
import com.app.adoptwithlove.repository.ReclamosRepository;
import com.app.adoptwithlove.service.Dao.Idao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReclamosService implements Idao<Reclamos, Long> {
    @Autowired
    private ReclamosRepository reclamosRepository;


    @Override
    public List<Reclamos> getAll() {
        return reclamosRepository.findAll();
    }

    @Override
    public Reclamos getById(Long id) {
        return reclamosRepository.findById(id).orElse(null);
    }

    @Override
    public Reclamos create(Reclamos entity) {
        return reclamosRepository.save(entity);
    }

    @Override
    public Reclamos update(Long id, Reclamos entity) {
        return reclamosRepository.save(entity);
    }

    @Override
    public void delete(Long id) {
        reclamosRepository.deleteById(id);
    }
}
