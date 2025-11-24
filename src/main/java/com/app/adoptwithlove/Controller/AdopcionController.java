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
import com.app.adoptwithlove.entity.Animal;
import com.app.adoptwithlove.entity.Estado;
import com.app.adoptwithlove.entity.Fundacion;
import com.app.adoptwithlove.repository.EstadoRepository;
import com.app.adoptwithlove.service.AdopcionService;
import com.app.adoptwithlove.service.AnimalService;

@Controller
@RequestMapping("/adopcion")
public class AdopcionController {
    @Autowired
    private AdopcionService adopcionService;

    @Autowired
    private AnimalService animalService;

    @Autowired
    private EstadoRepository estadoRepository;

    // Mostrar formulario de adopción para un animal específico
    @GetMapping("/nuevo/{animalId}")
    public String mostrarFormulario(@PathVariable Long animalId, Model model){
        Adopcion adopcion = new Adopcion();
        Animal animal = animalService.getById(animalId);
        adopcion.setAnimal(animal); // asignamos el animal al formulario
        model.addAttribute("adopcion", adopcion);
        return "adopcionCreate";
    }

    // Crear adopción
    @PostMapping("/crear")
    public String crearAdopcion(@ModelAttribute("adopcion") Adopcion adopcion, Model model){
        // Estado activo para adopción
        Estado estadoActivo = estadoRepository.findByNombreEstado("ACTIVO")
                .orElseThrow(() -> new RuntimeException("Estado ACTIVO no encontrado"));
        adopcion.setEstado(estadoActivo);

        // Guardamos adopción
        adopcionService.create(adopcion);

        // Cambiamos estado del animal a pendiente
Estado estadoPendiente = estadoRepository.findByNombreEstado("PENDIENTE")
        .orElseThrow(() -> new RuntimeException("Estado PENDIENTE no encontrado"));

Animal animalOriginal = animalService.getById(adopcion.getAnimal().getId());
animalOriginal.setEstado(estadoPendiente);
animalService.update(animalOriginal.getId(), animalOriginal);



        model.addAttribute("mensaje", "Solicitud de adopción enviada correctamente.");
        return "adopcionSuccess";
    }

    public String getAll(Model modelo) {
        modelo.addAttribute("adopciones", adopcionService.getAll());
        return "adopcion";
    }

    @GetMapping("adopcion/nuevo")
    public String show(Model modelo) {
        Fundacion fundacion = new Fundacion();
        modelo.addAttribute("adopcion", fundacion);
        return "adopcionCreate";
    }

    @PostMapping("/adopcion")
    public String create(@ModelAttribute("adopcion") Adopcion adopcion, Model model) {
        adopcionService.create(adopcion);
        return "redirect:/adopcion";
    }

    @GetMapping("/adopcion/edit/{id}")
    public String getById(@PathVariable Long id, Model modelo) {
        Adopcion adopcion = adopcionService.getById(id);
        modelo.addAttribute("adopcion", adopcion);
        return "adopcionUpdate";
    }

    @PostMapping("/adopcion/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("adopcion") Adopcion adopcion) {
        Adopcion adopcionExistente = adopcionService.getById(id);
        adopcionExistente.setId(id);
        adopcionExistente.setAnimal(adopcion.getAnimal());
        adopcionExistente.setFecha(adopcion.getFecha());
        adopcionService.update(id, adopcionExistente);
        return "redirect:/adopcion";
    }

    @GetMapping("/adopcion/{id}")
    public String delete(@PathVariable Long id) {
        adopcionService.delete(id);
        return "redirect:/adopcion";
    }
}
