package com.app.adoptwithlove.Controller;

import java.util.List;

import com.app.adoptwithlove.Dto.ProductoDTO;
import com.app.adoptwithlove.entity.Persona;
import com.app.adoptwithlove.entity.Productos;
import com.app.adoptwithlove.repository.PersonaRepository;
import com.app.adoptwithlove.repository.ProductosRepository;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/productos")
public class ProductosController {

    @Autowired
    private ProductosRepository productoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @GetMapping
    public List<Productos> getAllProductos() {
        return productoRepository.findAll();
    }

    @PostMapping("/upload-csv")
public ResponseEntity<String> uploadCSV(@RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
        return ResponseEntity.badRequest().body("El archivo está vacío");
    }

    try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        boolean firstLine = true;
        int nuevos = 0;
        int actualizados = 0;

        while ((line = br.readLine()) != null) {
            if (firstLine) { // ignorar encabezado
                firstLine = false;
                continue;
            }
            String[] data = line.split(",");

            String nombre = data[0].trim();
            String tipoProducto = data[1].trim();
            double precio = Double.parseDouble(data[2].trim());
            String cantidad = data[3].trim();
            Long vendedorId = Long.parseLong(data[4].trim());

            Persona vendedor = personaRepository.findById(vendedorId)
                    .orElseThrow(() -> new RuntimeException("Vendedor con ID " + vendedorId + " no encontrado"));

            // Buscar si ya existe un producto igual para este vendedor
            Productos existente = productoRepository.findByNombreAndTipoProductoAndPersona(nombre, tipoProducto, vendedor);

            if (existente != null) {
                // Actualizar cantidad (sumar)
                existente.setCantidad(existente.getCantidad() + cantidad);
                // Opcional: también actualizar precio si cambió
                existente.setPrecio(precio);
                productoRepository.save(existente);
                actualizados++;
            } else {
                // Crear nuevo producto
                Productos nuevo = new Productos();
                nuevo.setNombre(nombre);
                nuevo.setTipoProducto(tipoProducto);
                nuevo.setPrecio(precio);
                nuevo.setCantidad(cantidad);
                nuevo.setPersona(vendedor);
                productoRepository.save(nuevo);
                nuevos++;
            }
        }

        return ResponseEntity.ok("Se guardaron " + nuevos + " productos nuevos y se actualizaron " + actualizados + " existentes");

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body("Error al procesar el archivo: " + e.getMessage());
    }
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
