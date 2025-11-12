package com.app.adoptwithlove.service;

import com.app.adoptwithlove.entity.Estado;
import com.app.adoptwithlove.entity.Productos;
import com.app.adoptwithlove.repository.EstadoRepository;
import com.app.adoptwithlove.repository.ProductosRepository;
import com.app.adoptwithlove.service.Dao.Idao;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService implements Idao<Productos, Long> {

    @Autowired
    private ProductosRepository productoRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    @Override
    public List<Productos> getAll() {
        return productoRepository.findAll();
    }

    @Override
    public Productos getById(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public Productos create(Productos entity) {
        // ✅ Asignar automáticamente el estado ACTIVO al crear
        Estado activo = estadoRepository.findByNombreEstado("ACTIVO")
        .orElseThrow(() -> new RuntimeException("El estado 'ACTIVO' no existe. Ejecuta el seeder primero."));
        entity.setEstado(activo);
        return productoRepository.save(entity);
    }

    @Transactional
    @Override
    public Productos update(Long id, Productos entity) {
        return productoRepository.save(entity);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        // 🚫 En vez de borrar, marcamos el estado como INACTIVO
        Productos producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        Estado inactivo = estadoRepository.findByNombreEstado("INACTIVO")
        .orElseThrow(() -> new RuntimeException("El estado 'INACTIVO' no existe. Ejecuta el seeder primero."));

        producto.setEstado(inactivo);
        productoRepository.save(producto);
    }
}
