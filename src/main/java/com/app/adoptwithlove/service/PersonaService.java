package com.app.adoptwithlove.service;

import com.app.adoptwithlove.entity.Persona;
import com.app.adoptwithlove.repository.PersonaRepository;
import com.app.adoptwithlove.service.Dao.Idao;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PersonaService implements Idao<Persona, Long> {

    @Autowired
    private PersonaRepository persona;

    @Override
    public List<Persona> getAll() {
        return persona.findAll();
    }

    public Optional<Persona> findByEmail(String email) {
        return persona.findByEmail(email);
    }

    @Override
    public Persona getById(Long id) {
        return persona.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public Persona create(Persona entity) {
            System.out.println(">>> Persona guardada: " + entity.getEmail());
        return persona.save(entity);
    }

    @Transactional
    @Override
    public Persona update(Long id, Persona entity) {
        return persona.save(entity);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        persona.deleteById(id);
    }
}
