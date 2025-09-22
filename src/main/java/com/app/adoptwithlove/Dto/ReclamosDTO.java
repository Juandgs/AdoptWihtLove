package com.app.adoptwithlove.Dto;

import com.app.adoptwithlove.entity.Reclamos;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReclamosDTO {
    private Long id;
    private String correo;
    private String descripcion;
    private String nombreProducto;
    private String imagenProducto;
    private Long idVendedor;

    // Constructor personalizado desde entidad Reclamos
    public ReclamosDTO(Reclamos reclamo) {
        this.id = reclamo.getId();
        this.correo = reclamo.getCorreo(); // quien hizo el reclamo
        this.descripcion = reclamo.getDescripcion();
        this.nombreProducto = reclamo.getProducto().getNombre();
        this.imagenProducto = reclamo.getProducto().getImagen();
        this.idVendedor = reclamo.getProducto().getPersona().getId(); // vendedor del producto
    }
}
