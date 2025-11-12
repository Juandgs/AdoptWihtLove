package com.app.adoptwithlove.service;

import com.app.adoptwithlove.entity.Estado;
import com.app.adoptwithlove.entity.Fundacion;
import com.app.adoptwithlove.repository.EstadoRepository;
import com.app.adoptwithlove.repository.FundacionRepository;
import com.app.adoptwithlove.service.Dao.Idao;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FundacionService implements Idao<Fundacion, Long> {
    @Autowired
    private FundacionRepository Fundacion;

    @Autowired
    private EstadoRepository estadoRepository;

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
    public Fundacion create(Fundacion entity) {
        Estado activo = estadoRepository.findByNombreEstado("ACTIVO")
                .orElseThrow(() -> new RuntimeException("Estado ACTIVO no encontrado"));
        entity.setEstado(activo);
        return Fundacion.save(entity);
    }

    @Transactional
    @Override
    public Fundacion update(Long id, Fundacion entity) {
        return Fundacion.save(entity);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Fundacion fundacion = Fundacion.findById(id)
                .orElseThrow(() -> new RuntimeException("Fundación no encontrada con ID: " + id));

        Estado bloqueado = estadoRepository.findByNombreEstado("BLOQUEADO")
                .orElseThrow(() -> new RuntimeException("Estado BLOQUEADO no encontrado"));

        fundacion.setEstado(bloqueado);
        Fundacion.save(fundacion);
    }
}
