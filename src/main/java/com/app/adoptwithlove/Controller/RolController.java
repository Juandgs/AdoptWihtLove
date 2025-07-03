package com.app.adoptwithlove.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.adoptwithlove.entity.Rol;
import com.app.adoptwithlove.service.RolService;

@Controller
@RequestMapping("/api/productos")
public class RolController {
    @Autowired
    private RolService service;

    @GetMapping("/rol")
    public String getAll(Model modelo){
        modelo.addAttribute("roles", service.getAll());
        return "rol";
    }

    @GetMapping("rol/nuevo")
    public String show(Model modelo){
        Rol rol = new Rol();
        modelo.addAttribute("rol", rol);
        return "rolCreate";
    }

    @PostMapping("/rol")
    public String create(@ModelAttribute("rol") Rol rol, Model model){
        service.create(rol);
        return "redirect:/rol";
    }

    @GetMapping("/rol/edit/{id}")
    public String getById(@PathVariable Long id, Model modelo){
        Rol rol = service.getById(id);
        modelo.addAttribute("rol", rol );
        return "rolUpdate";
    }

    @PostMapping("/rol/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("rol") Rol rol ){
        Rol rolExistente = service.getById(id);
        rolExistente.setId(id);
        rolExistente.setNombreRol(rol.getNombreRol());
        rolExistente.setPersonas(rol.getPersonas());
        service.update(id,rolExistente);
        return "redirect:/rol";
    }

    @GetMapping("/rol/{id}")
    public String delete(@PathVariable Long id){
        service.delete(id);
        return "redirect:/rol";
    }

}
