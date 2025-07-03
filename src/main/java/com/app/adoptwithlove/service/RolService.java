package com.app.adoptwithlove.service;

import com.app.adoptwithlove.entity.Rol;
import com.app.adoptwithlove.repository.RolRepository;
import com.app.adoptwithlove.service.Dao.Idao;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolService implements Idao<Rol, Long> {

    @Autowired
    private RolRepository rolRepository;

    @Override
    public List<Rol> getAll() {
        return rolRepository.findAll();
    }

    public Rol findByNombreRol(String nombreRol) {
        return rolRepository.findByNombreRol(nombreRol);
    }

    @Override
    public Rol getById(Long id) {
        return rolRepository.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public Rol create(Rol entity) {
        return rolRepository.save(entity);
    }

    @Transactional
    @Override
    public Rol update(Long id, Rol entity) {
        return rolRepository.save(entity);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        rolRepository.deleteById(id);
    }
}
