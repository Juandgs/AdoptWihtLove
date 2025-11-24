package com.app.adoptwithlove.Controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities().isEmpty()) {
            SecurityContextHolder.clearContext();
            return "redirect:/logout";
        }

        String email = auth.getName();

        if (email.equals("admin@gmail.com")) {
            return "redirect:/dashboard?loginExitoso=true";
        } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_FUNDACION"))) {
            return "redirect:/dashboardFundacion?loginExitoso=true";
        } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TIENDA"))) {
            return "redirect:/dashboardVendedor?loginExitoso=true";
        }

        SecurityContextHolder.clearContext();
        return "redirect:/logout"; // rompe el ciclo si el rol no es válido
    }
}
