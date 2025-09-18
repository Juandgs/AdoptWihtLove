package com.app.adoptwithlove.Dto;

import com.app.adoptwithlove.entity.Productos;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ReclamosDTO {
    private Long id;
    private String correo;
    private String descripcion;
    private Productos productos;
}
