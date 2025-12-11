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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

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
        try {
            // Validaciones básicas
            if (adopcion == null || adopcion.getAnimal() == null || adopcion.getAnimal().getId() == null) {
                model.addAttribute("error", "No se especificó el animal para la adopción. Vuelve a intentarlo.");
                model.addAttribute("adopcion", adopcion);
                return "adopcionCreate";
            }

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
            if (animalOriginal == null) {
                model.addAttribute("error", "El animal seleccionado no existe.");
                model.addAttribute("adopcion", adopcion);
                return "adopcionCreate";
            }

            animalOriginal.setEstado(estadoPendiente);
            animalService.update(animalOriginal.getId(), animalOriginal);

            model.addAttribute("mensaje", "Solicitud de adopción enviada correctamente.");
            return "adopcionSuccess";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al procesar la solicitud: " + e.getMessage());
            model.addAttribute("adopcion", adopcion);
            return "adopcionCreate";
        }
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

    // Vista de detalle de adopción: último solicitante y animal
    @GetMapping("/detalle/{animalId}")
    public String verDetalleAdopcion(@PathVariable Long animalId, Model model) {
        Animal animal = animalService.getById(animalId);
        Adopcion adopcion = adopcionService.getUltimaAdopcionPorAnimal(animalId);
        if (adopcion == null) {
            model.addAttribute("error", "No hay solicitudes de adopción para este animal.");
            model.addAttribute("animal", animal);
            return "adopcionDetalle";
        }
        model.addAttribute("adopcion", adopcion);
        model.addAttribute("adoptante", adopcion);
        model.addAttribute("animal", animal);
        return "adopcionDetalle";
    }

    // Endpoint JSON para usar desde AJAX y mostrar el detalle en la misma página
    @GetMapping("/detalle-json/{animalId}")
    @ResponseBody
    public ResponseEntity<?> detalleJson(@PathVariable Long animalId) {
        Animal animal = animalService.getById(animalId);
        Adopcion adopcion = adopcionService.getUltimaAdopcionPorAnimal(animalId);

        // Construir mapas simples para evitar problemas de serialización de entidades JPA
        Map<String, Object> animalMap = new HashMap<>();
        if (animal != null) {
            animalMap.put("id", animal.getId());
            animalMap.put("nombre", animal.getNombre());
            animalMap.put("tipo_animal", animal.getTipo_animal());
            animalMap.put("raza", animal.getRaza());
            animalMap.put("edad", animal.getEdad());
            animalMap.put("imagen", animal.getImagen());
            if (animal.getEstado() != null) {
                animalMap.put("estado", animal.getEstado().getNombreEstado());
            }
        }

        Map<String, Object> adopcionMap = null;
        if (adopcion != null) {
            adopcionMap = new HashMap<>();
            adopcionMap.put("id", adopcion.getId());
            adopcionMap.put("nombre", adopcion.getNombre());
            adopcionMap.put("apellido", adopcion.getApellido());
            adopcionMap.put("email", adopcion.getEmail());
            adopcionMap.put("telefono", adopcion.getTelefono());
            adopcionMap.put("direccion", adopcion.getDireccion());
            adopcionMap.put("fecha", adopcion.getFecha());
            if (adopcion.getEstado() != null) {
                adopcionMap.put("estado", adopcion.getEstado().getNombreEstado());
            }
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("animal", animalMap);
        resp.put("adopcion", adopcionMap);

        System.out.println("[DEBUG] /adopcion/detalle-json/" + animalId + " -> returns animal=" + animalMap + " adopcion=" + adopcionMap);

        return ResponseEntity.ok(resp);
    }

    // Aprobar una adopción: marcar el animal como ADOPTADO y actualizar el estado de la adopción
    @PostMapping("/aprobar/{adopcionId}")
    @ResponseBody
    public ResponseEntity<?> aprobarAdopcion(@PathVariable Long adopcionId) {
        Adopcion adopcion = adopcionService.getById(adopcionId);
        if (adopcion == null) {
            return ResponseEntity.status(404).body("Adopción no encontrada");
        }
        // Obtener estados necesarios
        Estado estadoAdoptado = estadoRepository.findByNombreEstado("ADOPTADO")
                .orElseThrow(() -> new RuntimeException("Estado ADOPTADO no encontrado"));
        Estado estadoActivo = estadoRepository.findByNombreEstado("ACTIVO")
                .orElseThrow(() -> new RuntimeException("Estado ACTIVO no encontrado"));

        // Actualizar animal a ADOPTADO
        Animal animal = adopcion.getAnimal();
        if (animal != null) {
            animal.setEstado(estadoAdoptado);
            animalService.update(animal.getId(), animal);
        }

        // Actualizar adopcion a ACTIVO (aprobada pero aún en proceso)
        adopcion.setEstado(estadoActivo);
        adopcionService.update(adopcion.getId(), adopcion);

        return ResponseEntity.ok("Adopción aprobada y animal marcado como ADOPTADO");
    }

    // Finalizar una adopción: marcar la adopción como FINALIZADO y animal como ADOPTADO
    @PostMapping("/finalizar/{adopcionId}")
    @ResponseBody
    public ResponseEntity<?> finalizarAdopcion(@PathVariable Long adopcionId) {
        Adopcion adopcion = adopcionService.getById(adopcionId);
        if (adopcion == null) {
            return ResponseEntity.status(404).body("Adopción no encontrada");
        }
        Estado estadoFinalizado = estadoRepository.findByNombreEstado("FINALIZADO")
                .orElseThrow(() -> new RuntimeException("Estado FINALIZADO no encontrado"));
        Estado estadoActivo = estadoRepository.findByNombreEstado("ACTIVO")
                .orElseThrow(() -> new RuntimeException("Estado ACTIVO no encontrado"));

        // Actualizar animal a ACTIVO (volver a estar disponible)
        Animal animal = adopcion.getAnimal();
        if (animal != null) {
            animal.setEstado(estadoActivo);
            animalService.update(animal.getId(), animal);
        }

        // Actualizar adopcion a FINALIZADO
        adopcion.setEstado(estadoFinalizado);
        adopcionService.update(adopcion.getId(), adopcion);

        return ResponseEntity.ok("Adopción finalizada y animal marcado como ACTIVO");
    }

    // Historial de adopciones para un animal (JSON)
    @GetMapping("/historial/{animalId}")
    @ResponseBody
    public ResponseEntity<?> historialPorAnimal(@PathVariable Long animalId) {
        java.util.List<com.app.adoptwithlove.entity.Adopcion> lista = adopcionService.getHistorialPorAnimal(animalId);
        java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
        for (com.app.adoptwithlove.entity.Adopcion a : lista) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", a.getId());
            m.put("nombre", a.getNombre());
            m.put("apellido", a.getApellido());
            m.put("email", a.getEmail());
            m.put("telefono", a.getTelefono());
            m.put("direccion", a.getDireccion());
            m.put("fecha", a.getFecha());
            if (a.getEstado() != null) m.put("estado", a.getEstado().getNombreEstado());
            out.add(m);
        }
        return ResponseEntity.ok(out);
    }
}
