package com.app.adoptwithlove.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.app.adoptwithlove.service.RolService;
import com.app.adoptwithlove.entity.Rol;

@Component
public class StartupInit {

    @Autowired
    private RolService rolService;

    @PostConstruct
    public void init() {
        if (rolService.findByNombreRol("fundacion") == null) {
            Rol fundacion = new Rol();
            fundacion.setNombreRol("fundacion");
            rolService.create(fundacion);
        }

        if (rolService.findByNombreRol("tienda") == null) {
            Rol tienda = new Rol();
            tienda.setNombreRol("tienda");
            rolService.create(tienda);
        }
    }
}
