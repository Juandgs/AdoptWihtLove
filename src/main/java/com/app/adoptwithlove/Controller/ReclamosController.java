package com.app.adoptwithlove.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
