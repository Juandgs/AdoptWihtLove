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
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/productos")

@CrossOrigin(origins = "*")

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

    public ResponseEntity<String> uploadCSV(@RequestParam("file") MultipartFile file,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío");
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            List<Productos> nuevosProductos = new ArrayList<>();

            Persona persona = personaRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] data = line.split(",");
                if (data.length < 5) {
                    return ResponseEntity.badRequest().body("El archivo no tiene todas las columnas requeridas (mínimo 5)");
                }

                String nombre = data[0].trim();
                double precio = Double.parseDouble(data[1].trim());
                String cantidad = data[2].trim();
                String tipoProducto = data[3].trim();
                String descripcion = data[4].trim();
                String imagen = data.length > 5 && !data[5].trim().isEmpty() ? data[5].trim() : null;

                Optional<Productos> productoExistente = productoRepository.findByPersona(persona).stream()
                    .filter(p -> p.getNombre().equalsIgnoreCase(nombre)
                            && p.getPrecio() == precio
                            && p.getTipoProducto().equalsIgnoreCase(tipoProducto)
                            && p.getDescripcion().equalsIgnoreCase(descripcion)
                            && p.getPersona().getId().equals(persona.getId()))
                    .findFirst();


                if (productoExistente.isPresent()) {
                    Productos existente = productoExistente.get();
                    int cantidadActual = Integer.parseInt(existente.getCantidad());
                    int cantidadNueva = Integer.parseInt(cantidad);
                    existente.setCantidad(String.valueOf(cantidadActual + cantidadNueva));
                    productoRepository.save(existente);
                    System.out.println("Cantidad actualizada para producto existente: " + nombre);
                } else {
                    Productos producto = new Productos();
                    producto.setNombre(nombre);
                    producto.setPrecio(precio);
                    producto.setCantidad(cantidad);
                    producto.setTipoProducto(tipoProducto);
                    producto.setDescripcion(descripcion);
                    producto.setImagen(imagen);
                    producto.setPersona(persona);
                    nuevosProductos.add(producto);
                }
            }
            if (!nuevosProductos.isEmpty()) {
                productoRepository.saveAll(nuevosProductos);
            }

            return ResponseEntity.ok("Se procesaron correctamente los productos del archivo");

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
            e.printStackTrace();
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
