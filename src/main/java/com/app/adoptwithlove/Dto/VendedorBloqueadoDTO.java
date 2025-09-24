package com.app.adoptwithlove.Dto;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VendedorBloqueadoDTO {
    private Long id;
    private String nombre;
    private String correo;
    private List<String> reclamos;
}
