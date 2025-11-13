package com.app.adoptwithlove.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.app.adoptwithlove.entity.Animal;
import com.app.adoptwithlove.entity.Productos;
import com.app.adoptwithlove.repository.AnimalesRepository;
import com.app.adoptwithlove.repository.ProductosRepository;

@RestController
public class DashboarController {

    @Autowired
    private ProductosRepository productoRepository;

    @Autowired
    private AnimalesRepository animalesRepository;

    // ✅ Solo productos de vendedores con estado ACTIVO
    @GetMapping("/productos/admin")
@ResponseBody
public List<Productos> obtenerProductosDeVendedoresActivos() {
    List<Productos> productos = productoRepository.findAll();

    return productos.stream()
        .filter(p -> {
            if (p.getPersona() == null) return false;
            if (p.getPersona().getEstado() == null) return false;

            String estadoNombre = p.getPersona().getEstado().getNombreEstado();
            if (estadoNombre == null) return false;

            estadoNombre = estadoNombre.trim().toUpperCase();
            return estadoNombre.equals("ACTIVO");
        })
        .toList();
}


    @GetMapping("/animales/admin")
    @ResponseBody
    public List<Animal> obtenerTodosLosAnimales() {
        return animalesRepository.findAll();
    }
}
