package com.app.adoptwithlove.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.app.adoptwithlove.entity.Adopcion;
import com.app.adoptwithlove.entity.Fundacion;
import com.app.adoptwithlove.service.AdopcionService;

@Controller
@RequestMapping("/adopcion/registro")
public class AdopcionController {
    @Autowired
    private AdopcionService service;

    @GetMapping("/adopcion")
    public String getAll(Model modelo){
        modelo.addAttribute("adopciones", service.getAll());
        return "adopcion";
    }

    @GetMapping("adopcion/nuevo")
    public String show(Model modelo){
        Fundacion fundacion = new Fundacion();
        modelo.addAttribute("adopcion", fundacion);
        return "adopcionCreate";
    }

    @PostMapping("/adopcion")
    public String create(@ModelAttribute("adopcion") Adopcion adopcion, Model model){
        service.create(adopcion);
        return "redirect:/adopcion";
    }

    @GetMapping("/adopcion/edit/{id}")
    public String getById(@PathVariable Long id, Model modelo){
        Adopcion adopcion = service.getById(id);
        modelo.addAttribute("adopcion", adopcion );
        return "adopcionUpdate";
    }

    @PostMapping("/adopcion/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("adopcion") Adopcion adopcion ){
        Adopcion adopcionExistente = service.getById(id);
        adopcionExistente.setId(id);
        adopcionExistente.setAnimal(adopcion.getAnimal());
        adopcionExistente.setFecha(adopcion.getFecha());
        adopcionExistente.setPersona(adopcion.getPersona());
        service.update(id,adopcionExistente);
        return "redirect:/adopcion";
    }

    @GetMapping("/adopcion/{id}")
    public String delete(@PathVariable Long id){
        service.delete(id);
        return "redirect:/adopcion";
    }
}
