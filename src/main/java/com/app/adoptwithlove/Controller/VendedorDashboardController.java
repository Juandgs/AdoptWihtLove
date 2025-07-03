// Archivo: VendedorDashboardController.java
package com.app.adoptwithlove.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.adoptwithlove.entity.Persona;
import com.app.adoptwithlove.entity.Productos;
import com.app.adoptwithlove.repository.PersonaRepository;
import com.app.adoptwithlove.repository.ProductosRepository;

@Controller
@RequestMapping("/dashboardVendedor")
public class VendedorDashboardController {

    @Autowired
    private ProductosRepository productoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @GetMapping
    public String mostrarDashboard(Model model) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        List<Productos> productos = productoRepository.findByPersonaEmail(email);
        model.addAttribute("productos", productos);

        Persona vendedor = personaRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));
        model.addAttribute("vendedor", vendedor);

        return "dashboardVendedor";
    }

    
}
