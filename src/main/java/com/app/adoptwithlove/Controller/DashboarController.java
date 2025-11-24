package com.app.adoptwithlove.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.app.adoptwithlove.entity.Animal;
import com.app.adoptwithlove.entity.Productos;
import com.app.adoptwithlove.entity.Estado;
import com.app.adoptwithlove.repository.AnimalesRepository;
import com.app.adoptwithlove.repository.ProductosRepository;
import com.app.adoptwithlove.Dto.ProductoDashboardDTO;
import com.app.adoptwithlove.Dto.AnimalDashboardDTO;

@RestController
public class DashboarController {

    @Autowired
    private ProductosRepository productoRepository;

    @Autowired
    private AnimalesRepository animalesRepository;

    // ✅ Solo productos de vendedores con estado ACTIVO

        @GetMapping("/productos/admin")
        @ResponseBody
        public List<ProductoDashboardDTO> obtenerProductosDeVendedoresActivos() {
            List<Productos> productos = productoRepository.findAll();
            return productos.stream()
                .filter(p -> {
                    if (p.getPersona() == null) return false;
                    if (p.getPersona().getEstado() == null) return false;
                    String estadoNombre = p.getPersona().getEstado().getNombreEstado();
                    if (estadoNombre == null) return false;
                    estadoNombre = estadoNombre.trim().toUpperCase();
                    return estadoNombre.equals("ACTIVO");
                })
                .map(p -> {
                    ProductoDashboardDTO dto = new ProductoDashboardDTO();
                    dto.setId(p.getId());
                    dto.setNombre(p.getNombre());
                    dto.setPrecio(p.getPrecio());
                    dto.setCantidad(p.getCantidad());
                    dto.setTipoProducto(p.getTipoProducto());
                    dto.setDescripcion(p.getDescripcion());
                    dto.setImagen(p.getImagen());
                    dto.setPersonaId(p.getPersona() != null ? p.getPersona().getId() : null);
                    dto.setEstadoId(p.getEstado() != null ? p.getEstado().getId() : null);
                    dto.setNombreEstado(p.getEstado() != null ? p.getEstado().getNombreEstado() : null);
                    dto.setCantidadReclamos(p.getReclamos() != null ? p.getReclamos().size() : 0);
                    return dto;
                })
                .collect(Collectors.toList());
        }


    @GetMapping("/animales/admin")
    @ResponseBody
    public List<AnimalDashboardDTO> obtenerTodosLosAnimales() {
        List<Animal> animales = animalesRepository.findAll();
        return animales.stream()
            .map(a -> {
                AnimalDashboardDTO dto = new AnimalDashboardDTO();
                dto.setId(a.getId());
                dto.setNombre(a.getNombre());
                dto.setEdad(a.getEdad());
                dto.setRaza(a.getRaza());
                dto.setTipoAnimal(a.getTipo_animal());
                dto.setImagen(a.getImagen());
                dto.setEstadoId(a.getEstado() != null ? a.getEstado().getId() : null);
                dto.setNombreEstado(a.getEstado() != null ? a.getEstado().getNombreEstado() : null);
                dto.setFundacionId(a.getFundacion() != null ? a.getFundacion().getId() : null);
                return dto;
            })
            .collect(Collectors.toList());
    }
}
