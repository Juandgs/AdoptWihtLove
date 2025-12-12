package com.app.adoptwithlove.Controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import com.app.adoptwithlove.entity.Persona;
import com.app.adoptwithlove.entity.Productos;
import com.app.adoptwithlove.repository.PersonaRepository;
import com.app.adoptwithlove.repository.ProductosRepository;
import com.app.adoptwithlove.service.ProductoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/productos")
@CrossOrigin(origins = "*")
public class ProductosController {

    @Autowired
    private ProductosRepository productoRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private ProductoService productoService;

    @GetMapping
public List<Productos> getAllProductosActivos() {
    return productoService.filtrarPorEstado(List.of("ACTIVO"));
}


    @PostMapping("/crear")
    public ResponseEntity<String> createProducto(@RequestParam("nombre") String nombre,
                                                 @RequestParam("precio") Double precio,
                                                 @RequestParam("cantidad") String cantidad,
                                                 @RequestParam("tipoProducto") String tipoProducto,
                                                 @RequestParam("descripcion") String descripcion,
                                                 @RequestParam(value = "imagen", required = false) MultipartFile imagen,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Persona vendedor = personaRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

            String rutaWeb = null;
            if (imagen != null && !imagen.isEmpty()) {
                String original = Paths.get(imagen.getOriginalFilename()).getFileName().toString();
                String nombreArchivo = System.currentTimeMillis() + "_" + (original == null ? "imagen" : original);
                Path uploadsDir = Paths.get("uploads", "img");
                Files.createDirectories(uploadsDir);
                Path rutaImagen = uploadsDir.resolve(nombreArchivo);
                Files.copy(imagen.getInputStream(), rutaImagen, StandardCopyOption.REPLACE_EXISTING);
                rutaWeb = "/uploads/img/" + nombreArchivo;
            }

            Productos producto = new Productos();
            producto.setNombre(nombre);
            producto.setPrecio(precio);
            producto.setCantidad(cantidad);
            producto.setTipoProducto(tipoProducto);
            producto.setDescripcion(descripcion);
            if (rutaWeb != null) producto.setImagen(rutaWeb);
            producto.setPersona(vendedor);

            // ✅ Usamos el servicio para que asigne el estado ACTIVO automáticamente
            productoService.create(producto);

            return ResponseEntity.ok("Producto guardado correctamente con estado ACTIVO");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error al guardar la imagen: " + e.getMessage());
        }
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
            int productosIgnorados = 0;
            int productosSubidos = 0;

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

                // Buscar solo productos ACTIVOS del vendedor
                Optional<Productos> productoExistente = productoRepository.findByPersona(persona).stream()
                    .filter(p -> p.getNombre().equalsIgnoreCase(nombre)
                            && p.getPrecio() == precio
                            && p.getTipoProducto().equalsIgnoreCase(tipoProducto)
                            && p.getDescripcion().equalsIgnoreCase(descripcion)
                            && p.getPersona().getId().equals(persona.getId())
                            && p.getEstado() != null 
                            && "ACTIVO".equalsIgnoreCase(p.getEstado().getNombreEstado()))
                    .findFirst();

                if (productoExistente.isPresent()) {
                    // Producto existe y está ACTIVO, actualizar cantidad
                    Productos existente = productoExistente.get();
                    int cantidadActual = Integer.parseInt(existente.getCantidad());
                    int cantidadNueva = Integer.parseInt(cantidad);
                    existente.setCantidad(String.valueOf(cantidadActual + cantidadNueva));
                    productoRepository.save(existente);
                    productosIgnorados++;
                    System.out.println("Cantidad actualizada para producto existente ACTIVO: " + nombre);
                } else {
                    // Producto no existe o está INACTIVO, crear nuevo
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
                // ✅ Usamos el servicio para que asigne el estado ACTIVO automáticamente
                for (Productos producto : nuevosProductos) {
                    productoService.create(producto);
                    productosSubidos++;
                }
            }

            // Construir mensaje de respuesta
            String mensaje = String.format("%d producto(s) subidos correctamente, %d producto(s) ignorados", 
                                          productosSubidos, productosIgnorados);
            return ResponseEntity.ok(mensaje);

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

    @GetMapping("/filtrar-estado")
public List<Productos> filtrarProductosPorEstado(@RequestParam List<String> estados) {
    // Si no se envían estados, filtramos por ACTIVO, INACTIVO y BLOQUEADO por defecto
    if (estados == null || estados.isEmpty()) {
        estados = List.of("ACTIVO", "INACTIVO", "BLOQUEADO");
    }
    return productoService.filtrarPorEstado(estados);
}

    @GetMapping("/filtrados")
public List<Productos> misProductosFiltrados(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam(required = false) String[] estados) {

    Persona vendedor = personaRepository.findByEmail(userDetails.getUsername())
            .orElseThrow(() -> new RuntimeException("Vendedor no encontrado"));

    // Si no vienen estados → por defecto ACTIVO
    List<String> listaEstados = (estados == null || estados.length == 0)
            ? List.of("ACTIVO")
            : Arrays.asList(estados);

    return productoService.filtrarPorVendedorYEstado(vendedor.getId(), listaEstados);
}

    @GetMapping("/{id}")
    public ResponseEntity<Productos> getProducto(@PathVariable Long id) {
        return productoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/editar/{id}")
public ResponseEntity<String> updateProducto(
        @PathVariable Long id,
        @RequestParam("nombre") String nombre,
        @RequestParam("precio") Double precio,
        @RequestParam("cantidad") String cantidad,
        @RequestParam("tipoProducto") String tipoProducto,
        @RequestParam("descripcion") String descripcion,
        @RequestParam(value = "imagen", required = false) MultipartFile imagen) {

    return productoRepository.findById(id).map(producto -> {

        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setCantidad(cantidad);
        producto.setTipoProducto(tipoProducto);
        producto.setDescripcion(descripcion);

        if (imagen != null && !imagen.isEmpty()) {
            try {
                String nombreArchivo = imagen.getOriginalFilename();
                Path rutaImagen = Paths.get("src/main/resources/static/img", nombreArchivo);
                Files.copy(imagen.getInputStream(), rutaImagen, StandardCopyOption.REPLACE_EXISTING);
                producto.setImagen("/img/" + nombreArchivo);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error al guardar la imagen: " + e.getMessage());
            }
        }

        productoRepository.save(producto);
        return ResponseEntity.ok("Producto actualizado correctamente");

    }).orElse(ResponseEntity.status(404).body("Producto no encontrado"));
}


    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> deleteProducto(@PathVariable Long id) {
        try {
            // ✅ Usamos el servicio, que cambiará el estado a INACTIVO
            productoService.delete(id);
            return ResponseEntity.ok("Producto marcado como INACTIVO");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al eliminar: " + e.getMessage());
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