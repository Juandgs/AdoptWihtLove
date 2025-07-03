package com.app.adoptwithlove.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


import com.app.adoptwithlove.entity.Persona;
import com.app.adoptwithlove.service.PersonaService;

@Controller
public class PersonaController {
     @Autowired
    private PersonaService service;

    

    @GetMapping("/persona")
    public String getAll(Model modelo){
        modelo.addAttribute("personas", service.getAll());
        return "persona";
    }

    @GetMapping("persona/nuevo")
    public String show(Model modelo){
        Persona persona = new Persona();
        modelo.addAttribute("persona", persona);
        return "personaCreate";
    }

    @PostMapping("/persona")
    public String create(@ModelAttribute("persona") Persona persona, Model model){
        service.create(persona);
        return "redirect:/persona";
    }

    @GetMapping("/persona/edit/{id}")
    public String getById(@PathVariable Long id, Model modelo){
        Persona persona = service.getById(id);
        modelo.addAttribute("persona", persona );
        return "personaUpdate";
    }

    @PostMapping("/persona/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("persona") Persona persona ){
        Persona personaExistente = service.getById(id);
        personaExistente.setId(id);
        personaExistente.setNombre(persona.getNombre());
        personaExistente.setApellido(persona.getApellido());
        personaExistente.setContacto(persona.getContacto());
        personaExistente.setContrasena(persona.getContrasena());
        personaExistente.setRol(persona.getRol());
        personaExistente.setFechaNacimiento(persona.getFechaNacimiento());
        personaExistente.setFundaciones(persona.getFundaciones());
        personaExistente.setProductos(persona.getProductos());
        personaExistente.setAdopciones(persona.getAdopciones());
        service.update(id,personaExistente);
        return "redirect:/persona";
    }

    @GetMapping("/persona/{id}")
    public String delete(@PathVariable Long id){
        service.delete(id);
        return "redirect:/persona";
    }
}
