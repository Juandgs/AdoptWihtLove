package com.app.adoptwithlove.Controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.app.adoptwithlove.Dto.AnimalDTO;
import com.app.adoptwithlove.Dto.AnimalResponseDTO;
import com.app.adoptwithlove.entity.Animal;
import com.app.adoptwithlove.entity.Estado;
import com.app.adoptwithlove.entity.Fundacion;
import com.app.adoptwithlove.entity.Persona;
import com.app.adoptwithlove.repository.AnimalesRepository;
import com.app.adoptwithlove.repository.EstadoRepository;
import com.app.adoptwithlove.repository.FundacionRepository;
import com.app.adoptwithlove.repository.PersonaRepository;
import com.app.adoptwithlove.service.AnimalService;

@Controller
@RequestMapping("/animal")
public class AnimalController {

    @Autowired
    private AnimalService service;

    @Autowired
    private FundacionRepository fundacionRepository;

    @Autowired
    private PersonaRepository personaRepository;

    @Autowired
    private AnimalesRepository animalesRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    // 🔍 Listar todos los animales (API pública)
    @GetMapping("/api/animales")
    @ResponseBody
    public List<Animal> listarAnimales() {
        return service.getAll();
    }

    // 📥 Carga por CSV
    @PostMapping("/upload-csv")
    public ResponseEntity<String> uploadCSV(@RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío");
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            List<Animal> nuevosAnimales = new ArrayList<>();
            int animalesIgnorados = 0;
            int animalesSubidos = 0;

            Persona persona = personaRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));
            Fundacion fundacion = fundacionRepository.findByPersona_Id(persona.getId())
                    .orElseThrow(() -> new RuntimeException("Fundación autenticada no encontrada"));

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] data = line.split(",");
                if (data.length < 4) {
                    return ResponseEntity.badRequest()
                            .body("El archivo debe tener al menos 4 columnas: Nombre, Edad, Raza, Tipo");
                }

                String nombre = data[0].trim();
                int edad = Integer.parseInt(data[1].trim());
                String raza = data[2].trim();
                String tipo = data[3].trim();
                String imagen = data.length > 4 && !data[4].trim().isEmpty() ? data[4].trim() : null;

                // Buscar solo animales ACTIVOS de la fundación
                Optional<Animal> animalExistente = animalesRepository.findByFundacion(fundacion).stream()
                        .filter(a -> a.getNombre().equalsIgnoreCase(nombre)
                                && a.getEdad() == edad
                                && a.getRaza().equalsIgnoreCase(raza)
                                && a.getTipo_animal().equalsIgnoreCase(tipo)
                                && a.getEstado() != null
                                && "ACTIVO".equalsIgnoreCase(a.getEstado().getNombreEstado()))
                        .findFirst();

                if (animalExistente.isPresent()) {
                    // Animal existe y está ACTIVO, ignorar
                    animalesIgnorados++;
                    System.out.println("Animal ignorado (ya existe con estado ACTIVO): " + nombre);
                } else {
                    // Animal no existe o está INACTIVO, crear nuevo
                    Animal nuevo = new Animal();
                    nuevo.setNombre(nombre);
                    nuevo.setEdad(edad);
                    nuevo.setRaza(raza);
                    nuevo.setTipo_animal(tipo);
                    nuevo.setImagen(imagen);
                    nuevo.setFundacion(fundacion);
                    nuevosAnimales.add(nuevo);
                }
            }

            if (!nuevosAnimales.isEmpty()) {
                // Usar el servicio para asignar estado ACTIVO automáticamente
                for (Animal animal : nuevosAnimales) {
                    service.create(animal);
                    animalesSubidos++;
                }
            }

            // Construir mensaje de respuesta
            String mensaje = String.format("%d animal(es) subidos correctamente, %d animal(es) ignorados",
                    animalesSubidos, animalesIgnorados);
            return ResponseEntity.ok(mensaje);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error al procesar el archivo: " + e.getMessage());
        }
    }

    // 🐾 Animales de la fundación autenticada
    @GetMapping("/mis-animales")
    @ResponseBody
    public ResponseEntity<List<AnimalResponseDTO>> getAnimalesFundacionAutenticada() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Persona persona = personaRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
        Fundacion fundacion = fundacionRepository.findByPersona_Id(persona.getId())
                .orElseThrow(() -> new RuntimeException("Fundación no encontrada"));

        List<Animal> animales = animalesRepository.findByFundacion(fundacion);

        List<AnimalResponseDTO> respuesta = animales.stream()
                .map(AnimalResponseDTO::new)
                .toList();

        return ResponseEntity.ok(respuesta);
    }

    @GetMapping("/mis-animales-estado")
    @ResponseBody
    public ResponseEntity<List<AnimalResponseDTO>> getAnimalesFundacionPorEstado(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) List<String> estados) {

        String email = userDetails.getUsername();
        Persona persona = personaRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Fundacion fundacion = fundacionRepository.findByPersona_Id(persona.getId())
                .orElseThrow(() -> new RuntimeException("Fundación no encontrada"));

        // Por defecto, si no envías estados, se toman todos: ACTIVO, INACTIVO,
        // BLOQUEADO
        if (estados == null || estados.isEmpty()) {
            estados = List.of("ACTIVO", "INACTIVO", "BLOQUEADO");
        }

        List<Animal> animales = service.getByFundacionYEstados(fundacion.getId(), estados);

        List<AnimalResponseDTO> respuesta = animales.stream()
                .map(AnimalResponseDTO::new)
                .toList();

        return ResponseEntity.ok(respuesta);
    }

    // 🔍 Obtener animal por ID
    @GetMapping("/editar/{id}")
    @ResponseBody
    public ResponseEntity<AnimalResponseDTO> obtenerAnimalPorId(@PathVariable Long id) {
        Animal animal = animalesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Animal no encontrado con ID: " + id));

        AnimalResponseDTO dto = new AnimalResponseDTO(animal);
        return ResponseEntity.ok(dto);
    }

    // ✏️ Editar animal
    @PutMapping("/editar/{id}")
    @ResponseBody
    public ResponseEntity<?> editarAnimal(@PathVariable Long id, @RequestBody AnimalDTO dto) {
        Animal existente = animalesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Animal no encontrado con ID: " + id));

        existente.setNombre(dto.getNombre());
        existente.setEdad(dto.getEdad());
        existente.setRaza(dto.getRaza());
        existente.setTipo_animal(dto.getTipo_animal());
        existente.setImagen(dto.getImagen());
        
        // Actualizar estado si se proporciona
        if (dto.getEstadoNombre() != null && !dto.getEstadoNombre().isEmpty()) {
            Estado nuevoEstado = estadoRepository.findByNombreEstado(dto.getEstadoNombre())
                    .orElseThrow(() -> new RuntimeException("Estado no encontrado: " + dto.getEstadoNombre()));
            existente.setEstado(nuevoEstado);
        }

        animalesRepository.save(existente);
        return ResponseEntity.ok("Animal actualizado correctamente");
    }

    // 🧾 Vista Thymeleaf (si usas plantillas)
    @GetMapping("/animal")
    public String getAll(Model modelo) {
        modelo.addAttribute("animales", service.getAll());
        return "animal";
    }

    @GetMapping("/animal/nuevo")
    public String show(Model modelo) {
        Animal animal = new Animal();
        modelo.addAttribute("animal", animal);
        return "animalCreate";
    }

    @PostMapping("/animal")
    public String create(@ModelAttribute("animal") Animal animal, Model model) {
        service.create(animal);
        return "redirect:/animal";
    }

    @PostMapping("/animal/{id}")
    public String update(@PathVariable Long id, @ModelAttribute("animal") Animal animal) {
        Animal animalExistente = service.getById(id);
        animalExistente.setId(id);
        animalExistente.setTipo_animal(animal.getTipo_animal());
        animalExistente.setEdad(animal.getEdad());
        animalExistente.setNombre(animal.getNombre());
        animalExistente.setRaza(animal.getRaza());
        animalExistente.setAdopciones(animal.getAdopciones());
        animalExistente.setFundacion(animal.getFundacion());
        service.update(id, animalExistente);
        return "redirect:/animal";
    }

    // 🗑️ Eliminar animal
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminarAnimal(@PathVariable Long id) {
        try {
            // ✅ Usar servicio que cambia a INACTIVO
            service.delete(id);
            return ResponseEntity.ok("Animal marcado como INACTIVO");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar animal: " + e.getMessage());
        }
    }

    // 📦 Catálogo de animales por fundación
    @GetMapping("/catalogo")
    public List<AnimalDTO> obtenerCatalogoFundacion(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();

        Persona fundacion = personaRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Fundación no encontrada con el email: " + email));

        List<Animal> animales = service.getByFundacion(fundacion.getId());

        return animales.stream()
                .map(a -> new AnimalDTO(
                        a.getId(),
                        a.getNombre(),
                        a.getEdad(),
                        a.getRaza(),
                        a.getTipo_animal(),
                        a.getImagen()))
                .collect(Collectors.toList());
    }

    @PostMapping("/crear")
    @ResponseBody
    public ResponseEntity<?> crearAnimal(@RequestParam("nombre") String nombre,
            @RequestParam("edad") int edad,
            @RequestParam("raza") String raza,
            @RequestParam("tipo_animal") String tipoAnimal,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            String email = userDetails.getUsername();
            Persona persona = personaRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
            Fundacion fundacion = fundacionRepository.findByPersona_Id(persona.getId())
                    .orElseThrow(() -> new RuntimeException("Fundación no encontrada"));

            if (imagen == null || imagen.isEmpty()) {
                return ResponseEntity.badRequest().body("La imagen es obligatoria para crear un animal");
            }

            String original = Paths.get(imagen.getOriginalFilename()).getFileName().toString();
            String nombreArchivo = System.currentTimeMillis() + "_" + (original == null ? "imagen" : original);
            Path uploadsDir = Paths.get("uploads", "img");
            Files.createDirectories(uploadsDir);
            Path rutaImagen = uploadsDir.resolve(nombreArchivo);
            Files.copy(imagen.getInputStream(), rutaImagen, StandardCopyOption.REPLACE_EXISTING);
            String rutaWeb = "/uploads/img/" + nombreArchivo;

            Animal animal = new Animal();
            animal.setNombre(nombre);
            animal.setEdad(edad);
            animal.setRaza(raza);
            animal.setTipo_animal(tipoAnimal);
            animal.setImagen(rutaWeb);
            animal.setFundacion(fundacion);

            // ✅ Usar el servicio para asignar automáticamente ACTIVO
            service.create(animal);

            return ResponseEntity.ok("Animal registrado correctamente");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar el animal: " + e.getMessage());
        }
    }
}