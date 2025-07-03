package com.app.adoptwithlove.Controller;

import java.util.List;

import com.app.adoptwithlove.Dto.ProductoDTO;
import com.app.adoptwithlove.entity.Persona;
import com.app.adoptwithlove.entity.Productos;
import com.app.adoptwithlove.repository.PersonaRepository;
import com.app.adoptwithlove.repository.ProductosRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController  // << CAMBIO: usar RestController evita usar @ResponseBody cada vez
@RequestMapping("/productos")
@CrossOrigin(origins = "*") // Opcional: para permitir peticiones desde el frontend
public class ProductosController {

    @Autowired
    private ProductosRepository productoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @GetMapping
    public List<Productos> getAllProductos() {
        return productoRepository.findAll();
    }

    @GetMapping("/mis-productos")
    public List<Productos> getProductosDelVendedorAutenticado(@AuthenticationPrincipal UserDetails userDetails) {
        Persona vendedor = personaRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));
        return productoRepository.findByPersona(vendedor);
    }

    @PostMapping("/crear")
    public ResponseEntity<String> createProducto(@RequestBody ProductoDTO dto, 
                                                @AuthenticationPrincipal UserDetails userDetails) {
        try {
            System.out.println("DTO recibido: " + dto);
            System.out.println("Usuario autenticado: " + (userDetails != null ? userDetails.getUsername() : "NO AUTENTICADO"));

            Persona vendedor = personaRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

            Productos producto = new Productos();
            producto.setNombre(dto.getNombre());
            producto.setPrecio(dto.getPrecio());
            producto.setCantidad(dto.getCantidad());
            producto.setTipoProducto(dto.getTipoProducto());
            producto.setDescripcion(dto.getDescripcion());
            producto.setImagen(dto.getImagen());
            producto.setPersona(vendedor);

            productoRepository.save(producto);

            return ResponseEntity.ok("Producto guardado correctamente");
        } catch (Exception e) {
            e.printStackTrace(); // muestra el error real en la consola
            return ResponseEntity.status(500).body("Error al guardar el producto: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Productos> getProducto(@PathVariable Long id) {
        return productoRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/editar/{id}")
    public ResponseEntity<String> updateProducto(@PathVariable Long id, 
                                                @RequestBody ProductoDTO dto,
                                                @AuthenticationPrincipal UserDetails userDetails) {
        return productoRepository.findById(id).map(producto -> {
            producto.setNombre(dto.getNombre());
            producto.setPrecio(dto.getPrecio());
            producto.setCantidad(dto.getCantidad());
            producto.setTipoProducto(dto.getTipoProducto());
            producto.setDescripcion(dto.getDescripcion());
            producto.setImagen(dto.getImagen());
            productoRepository.save(producto);
            return ResponseEntity.ok("Producto actualizado");
        }).orElse(ResponseEntity.status(404).body("Producto no encontrado"));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> deleteProducto(@PathVariable Long id) {
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
            return ResponseEntity.ok("Producto eliminado");
        } else {
            return ResponseEntity.status(404).body("Producto no encontrado");
        }
    }

@GetMapping("/debug")
public ResponseEntity<?> debugProductos() {
    List<Productos> productos = productoRepository.findAll();
    productos.forEach(p -> {
        System.out.println("Producto: " + p.getNombre() + " | Dueño: " + p.getPersona().getNombre());
    });
    return ResponseEntity.ok(productos);
}


}
