// Archivo: VendedorController.java
package com.app.adoptwithlove.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class VendedorController {

    @Autowired


    @GetMapping("/tiendas")
    public String mostrarFundaciones() {
        return "tiendas"; // sin .html
    }

   


    
}
