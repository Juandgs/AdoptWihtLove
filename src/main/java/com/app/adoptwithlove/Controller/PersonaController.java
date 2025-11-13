package com.app.adoptwithlove.Controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Optional;
import com.app.adoptwithlove.Dto.VendedorBloqueadoDTO;
import com.app.adoptwithlove.entity.Estado;
import com.app.adoptwithlove.entity.Persona;
import com.app.adoptwithlove.service.PersonaService;
import com.app.adoptwithlove.repository.EstadoRepository;
import com.app.adoptwithlove.repository.PersonaRepository;
import com.app.adoptwithlove.repository.ProductosRepository;
import com.app.adoptwithlove.entity.Productos;
import com.app.adoptwithlove.entity.Reclamos;
import com.app.adoptwithlove.repository.ReclamosRepository;

@Controller
public class PersonaController {
    @Autowired
    private PersonaRepository personaRepository;
    @Autowired
    private ProductosRepository productoRepository;

    @Autowired
    private ReclamosRepository reclamoRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    @Autowired
    private PersonaService service;

    @GetMapping("/vendedores/bloqueados")
    @ResponseBody
    public List<VendedorBloqueadoDTO> listarVendedoresBloqueados() {
        List<Persona> vendedoresBloqueados = personaRepository.findByRol_NombreRolAndEstado_NombreEstado("TIENDA", "BLOQUEADO");

        List<VendedorBloqueadoDTO> respuesta = new ArrayList<>();

        for (Persona vendedor : vendedoresBloqueados) {
            List<Productos> productos = productoRepository.findByPersona_Id(vendedor.getId());

            List<String> reclamos = new ArrayList<>();
            for (Productos producto : productos) {
                List<Reclamos> reclamosProducto = reclamoRepository.findByProducto_Id(producto.getId());
                for (Reclamos r : reclamosProducto) {
                    reclamos.add("Producto: " + producto.getNombre() + " → " + r.getDescripcion());
                }
            }

            respuesta.add(
                    new VendedorBloqueadoDTO(vendedor.getId(), vendedor.getNombre(), vendedor.getEmail(), reclamos));
        }

        return respuesta;
    }

    @PutMapping("/bloquear/{id}")
    public ResponseEntity<?> bloquearPersona(@PathVariable Long id) {
        Optional<Persona> personaOpt = personaRepository.findById(id);

        if (personaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Persona no encontrada");
        }

        Persona persona = personaOpt.get();
        Estado estadoBloqueado = estadoRepository.findByNombreEstado("BLOQUEADO")
                .orElseThrow(() -> new RuntimeException("El estado 'BLOQUEADO' no existe. Ejecuta el seeder primero."));
        persona.setEstado(estadoBloqueado);
        personaRepository.save(persona);

        return ResponseEntity.ok("Persona bloqueada correctamente");
    }

    @PutMapping("/habilitar/{id}")
    public ResponseEntity<?> habilitarPersona(@PathVariable Long id) {
        Optional<Persona> personaOpt = personaRepository.findById(id);

        if (personaOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Persona no encontrada");
        }

        Persona persona = personaOpt.get();
        Estado estadoActivo = estadoRepository.findByNombreEstado("ACTIVO")
                .orElseThrow(() -> new RuntimeException("El estado 'ACTIVO' no existe. Ejecuta el seeder primero."));
        persona.setEstado(estadoActivo);
        personaRepository.save(persona);

        return ResponseEntity.ok("Persona activada correctamente");
    }

    @GetMapping("/persona")
    public String getAll(Model modelo) {
        modelo.addAttribute("personas", service.getAll());
        return "persona";
    }

    @GetMapping("persona/nuevo")
    public String show(Model modelo) {
        Persona persona = new Persona();
        modelo.addAttribute("persona", persona);
        return "personaCreate";
    }

    @PostMapping("/persona")
    public String create(@ModelAttribute("persona") Persona persona, Model model) {
        service.create(persona);
        return "redirect:/persona";
    }

    @GetMapping("/persona/edit/{id}")
    public String getById(@PathVariable Long id, Model modelo) {
        Persona persona = service.getById(id);
        modelo.addAttribute("persona", persona);
        return "personaUpdate";
    }

    @PostMapping("/persona/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("persona") Persona persona) {
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
        service.update(id, personaExistente);
        return "redirect:/persona";
    }

    @GetMapping("/persona/{id}")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/persona";
    }

}