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
    Productos producto = productoRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    // Cambiar estado a INACTIVO
    Estado estadoInactivo = estadoRepository.findByNombreEstado("INACTIVO")
        .orElseThrow(() -> new RuntimeException("Estado INACTIVO no existe"));
    producto.setEstado(estadoInactivo);
    productoRepository.save(producto);
}

    public List<Productos> filtrarPorEstado(List<String> estados) {
        return productoRepository.findByEstado_NombreEstadoIn(estados);
    }

    public List<Productos> filtrarPorVendedorYEstado(Long personaId, List<String> estados) {
        return productoRepository.findByPersona_IdAndEstado_NombreEstadoIn(personaId, estados);
    }

}
