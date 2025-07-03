package com.app.adoptwithlove.Controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.ui.Model;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String mostrarFormularioLogin(@RequestParam(value = "error", required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Correo o contraseña incorrectos");
        }
        return "login"; // Vista login.html
    }

    @GetMapping("/postLogin")
    public String redireccionSegunRol() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    String email = auth.getName(); // obtiene el email del usuario autenticado

    // Si el correo es el del admin, redirigir al dashboard general
    if (email.equals("admin@gmail.com")) {
        return "redirect:/dashboard";
    } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_FUNDACION"))) {
        return "redirect:/dashboardFundacion";
    } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TIENDA"))) {
        return "redirect:/dashboardVendedor";
    }

    return "redirect:/";
}

}
