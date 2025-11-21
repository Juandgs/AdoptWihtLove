// Archivo: VendedorController.java
package com.app.adoptwithlove.Controller;


import com.app.adoptwithlove.repository.ProductosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class VendedorController {

    @Autowired
    private ProductosRepository productosRepository;

    @GetMapping("/tiendas")
    public String mostrarTiendas(Model model) {
        model.addAttribute("productos", productosRepository.findAll());
        return "tiendas"; // Renderiza tiendas.html
    }
    
}
