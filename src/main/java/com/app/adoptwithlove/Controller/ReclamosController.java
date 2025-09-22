package com.app.adoptwithlove.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.app.adoptwithlove.entity.Persona;
import com.app.adoptwithlove.entity.Productos;
import com.app.adoptwithlove.entity.Reclamos;
import com.app.adoptwithlove.repository.ProductosRepository;
import com.app.adoptwithlove.service.IEmailService;
import com.app.adoptwithlove.service.ReclamosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

import com.app.adoptwithlove.repository.PersonaRepository;
import com.app.adoptwithlove.repository.ProductosRepository;
import com.app.adoptwithlove.repository.ReclamosRepository;
import com.app.adoptwithlove.Dto.ReclamosDTO;
import com.app.adoptwithlove.entity.Reclamos;

@RestController
@RequestMapping("/reclamos")
public class ReclamosController {
    @Autowired
    private ReclamosRepository reclamoRepository;

    @Autowired
    private ProductosRepository productoRepository;

    @Autowired
    private PersonaRepository personaRepository;
  
    @Autowired
    private ReclamosService reclamosService;

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private IEmailService emailService; // servicio de correo

    // 🔍 Obtener reclamos por producto
   @GetMapping("/producto/{id}")
public ResponseEntity<List<ReclamosDTO>> obtenerReclamosPorProducto(@PathVariable Long id) {
    List<Reclamos> reclamos = reclamoRepository.findByProducto_Id(id);

    List<ReclamosDTO> respuesta = reclamos.stream()
        .map(ReclamosDTO::new) 
        .toList();

    return ResponseEntity.ok(respuesta);
}


    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarReclamo(@PathVariable Long id) {
        if (!reclamoRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reclamo no encontrado");
        }

        reclamoRepository.deleteById(id);
        return ResponseEntity.ok("Reclamo eliminado");
    }

    

    // Mostrar formulario de reclamo
    @GetMapping("/reclamo/{productoId}")
    public String form(@PathVariable Long productoId, Model model) {
        Productos producto = productosRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        model.addAttribute("producto", producto);
        model.addAttribute("correoVendedor", producto.getPersona().getEmail()); // correo del dueño del producto
        model.addAttribute("reclamos", new Reclamos()); // para el form

        return "reclamo"; // Renderiza reclamo.html
    }

    // Guardar reclamo y enviar correo
    @PostMapping("/reclamoSend")
    public String create(@ModelAttribute Reclamos reclamos, RedirectAttributes redirectAttrs) {
        // 1) obtener id del producto enviado por binding
        if (reclamos.getProducto() == null || reclamos.getProducto().getId() == null) {
            redirectAttrs.addFlashAttribute("error", "No se especificó el producto para el reclamo.");
            return "redirect:/tiendas";
        }

        Long productoId = reclamos.getProducto().getId();

        // 2) cargar el producto completo desde BD (incluye persona si existe)
        Productos producto = productosRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // 3) setear el producto real en el reclamo antes de guardar
        reclamos.setProducto(producto);
        reclamosService.create(reclamos);

        // 4) intentar notificar al vendedor (si tiene persona y email)
        Persona vendedor = producto.getPersona();
        if (vendedor != null && vendedor.getEmail() != null && !vendedor.getEmail().isBlank()) {
            String subject = "Ha recibido una PQR sobre uno de sus productos";
            String message = "Estimado vendedor,\n\n" +
                    "Ha recibido un nuevo reclamo (PQR) relacionado con uno de sus productos publicados en la plataforma.\n\n" +
                    "Detalles del reclamo:\n" +
                    "- Correo del usuario: " + reclamos.getCorreo() + "\n" +
                    "- Descripción del reclamo: " + reclamos.getDescripcion() + "\n\n" +
                    "Le recomendamos revisar este caso y tomar las acciones correspondientes a la mayor brevedad posible.\n\n" +
                    "Atentamente,\n" +
                    "Equipo Adopt with Love";

            emailService.sendEmail(new String[]{vendedor.getEmail()}, subject, message);
            redirectAttrs.addFlashAttribute("success", "Reclamo guardado y vendedor notificado.");
        } else {
            redirectAttrs.addFlashAttribute("warning", "Reclamo guardado pero el producto no tiene un vendedor con correo.");
        }

        return "redirect:/tiendas";
    }

}
