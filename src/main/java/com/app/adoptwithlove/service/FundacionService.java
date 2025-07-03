package com.app.adoptwithlove.service;
import com.app.adoptwithlove.entity.Fundacion;
import com.app.adoptwithlove.repository.FundacionRepository;
import com.app.adoptwithlove.service.Dao.Idao;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FundacionService implements Idao<Fundacion, Long>{
    @Autowired
    private FundacionRepository Fundacion;

    @Override
    public List<com.app.adoptwithlove.entity.Fundacion> getAll() {
        return Fundacion.findAll();
    }


    @Override
    public Fundacion getById(Long id) {
        return Fundacion.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public Fundacion create(Fundacion entity){
        return Fundacion.save(entity);
    }


    @Transactional
    @Override
    public Fundacion update(Long id, Fundacion entity){
        return Fundacion.save(entity);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Fundacion.deleteById(id);
    }
}
