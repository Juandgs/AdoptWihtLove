package com.app.adoptwithlove.Controller;

    import java.util.List;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.ModelAttribute;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.ResponseBody;

    import com.app.adoptwithlove.Dto.fundacionDTO;
    import com.app.adoptwithlove.entity.Animal;
    import com.app.adoptwithlove.entity.Fundacion;
    import com.app.adoptwithlove.repository.AnimalesRepository;
    import com.app.adoptwithlove.repository.FundacionRepository;
    import com.app.adoptwithlove.service.FundacionService;

    @Controller
    public class FundacionController {
        @Autowired
        private FundacionService service;

        @Autowired
        private FundacionRepository fundacionRepository;

        @Autowired
        private AnimalesRepository animalesRepository;

        // ✅ Listar fundaciones en formato DTO para el frontend
        @GetMapping("/api/fundaciones")
        @ResponseBody
        public List<fundacionDTO> listarFundaciones() {
            return service.getAll().stream()
                    .map(f -> new fundacionDTO(
                            f.getId(),
                            f.getNombre_fundacion(),
                            f.getDireccion(),
                            f.getCorreo(),
                            f.getTelefono()
                    ))
                    .toList();
        }

        @GetMapping("/api/fundaciones/{id}")
        @ResponseBody
        public fundacionDTO obtenerFundacionPorId(@PathVariable Long id) {
            Fundacion fundacion = fundacionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Fundación no encontrada"));
            return new fundacionDTO(
                    fundacion.getId(),
                    fundacion.getNombre_fundacion(),
                    fundacion.getDireccion(),
                    fundacion.getCorreo(),
                    fundacion.getTelefono()
            );
        }

        @GetMapping("/api/fundaciones/{id}/animales")
        @ResponseBody
        public List<Animal> obtenerAnimalesPorFundacion(@PathVariable Long id) {
            Fundacion fundacion = fundacionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Fundación no encontrada"));
            return animalesRepository.findByFundacionAndEstadoNombreEstado(fundacion, "ACTIVO");

        }

        @GetMapping("/fundacion/{id}/animales")
        public String verAnimalesPorFundacion(@PathVariable Long id, Model model) {
            model.addAttribute("fundacionId", id);
            return "animalesFundacion"; // sin .html
        }

        @GetMapping("/fundaciones")
        public String mostrarFundaciones() {
            return "fundaciones"; // sin .html 
        }

        @GetMapping("/dashboardFundacion")
        public String mostrarDashboard() {
            return "dashboardFundacion"; // sin .html
        }

        @GetMapping("/fundacion")
        public String getAll(Model modelo) {
            modelo.addAttribute("fundaciones", service.getAll());
            return "fundacion";
        }

        @GetMapping("fundacion/nuevo")
        public String show(Model modelo) {
            Fundacion fundacion = new Fundacion();
            modelo.addAttribute("fundacion", fundacion);
            return "fundacionCreate";
        }

        @PostMapping("/fundacion")
        public String create(@ModelAttribute("fundacion") Fundacion fundacion, Model model) {
            service.create(fundacion);
            return "redirect:/fundacion";
        }

        @GetMapping("/fundacion/edit/{id}")
        public String getById(@PathVariable Long id, Model modelo) {
            Fundacion fundacion = service.getById(id);
            modelo.addAttribute("fundacion", fundacion);
            return "fundacionUpdate";
        }

        @PostMapping("/fundacion/{id}")
        public String update(@PathVariable Long id, @ModelAttribute("fundacion") Fundacion fundacion) {
            Fundacion fundacionExistente = service.getById(id);
            fundacionExistente.setId(id);
            fundacionExistente.setNombre_fundacion(fundacion.getNombre_fundacion());
            fundacionExistente.setDireccion(fundacion.getDireccion());
            fundacionExistente.setCorreo(fundacion.getCorreo());
            fundacionExistente.setTelefono(fundacion.getTelefono());
            fundacionExistente.setAnimales(fundacion.getAnimales());
            service.update(id, fundacionExistente);
            return "redirect:/fundacion";
        }

        @GetMapping("/fundacion/{id}")
        public String delete(@PathVariable Long id) {
            service.delete(id);
            return "redirect:/fundacion";
        }

    }
