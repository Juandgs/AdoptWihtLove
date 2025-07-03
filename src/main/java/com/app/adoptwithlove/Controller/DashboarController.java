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

    @GetMapping("/productos/admin")
@ResponseBody
public List<Productos> obtenerTodosLosProductos() {
    return productoRepository.findAll();
}

@GetMapping("/animales/admin")
@ResponseBody
public List<Animal> obtenerTodosLosAnimales() {
    return animalesRepository.findAll();
}

    
}
