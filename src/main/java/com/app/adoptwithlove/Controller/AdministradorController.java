package com.app.adoptwithlove.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdministradorController {

    @GetMapping("/")
    public String mostrarInicio() {
    return "index"; 
    }
    
    @GetMapping("/dashboard")
    public String mostrarDashboard() {
    return "dashboard"; 
    }
}
