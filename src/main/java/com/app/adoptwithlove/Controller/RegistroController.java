package com.app.adoptwithlove.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.app.adoptwithlove.Dto.RegistroDTO;
import com.app.adoptwithlove.entity.Fundacion;
import com.app.adoptwithlove.entity.Persona;
import com.app.adoptwithlove.entity.Rol;
import com.app.adoptwithlove.service.FundacionService;
import com.app.adoptwithlove.service.PersonaService;
import com.app.adoptwithlove.service.RolService;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/registro")
public class RegistroController {

    @Autowired
    private PersonaService personaService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private FundacionService fundacionService;                                                                                                                  

    @Autowired
    private RolService rolService;

    @GetMapping
    public String mostrarFormulario(Model model, HttpServletRequest request) {
    CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
    model.addAttribute("_csrf", token);
    return "registro";
}
    @PostMapping
    public String registrar(@ModelAttribute RegistroDTO dto) {
        Persona persona = new Persona();
        persona.setNombre(dto.getNombre());
        persona.setApellido(dto.getApellido());
        persona.setContacto(dto.getContacto());
        persona.setFechaNacimiento(dto.getFechaNacimiento());
        String contrasenaCifrada = passwordEncoder.encode(dto.getContrasena());
        persona.setContrasena(contrasenaCifrada);
        persona.setEmail(dto.getEmail());
        Rol rol = rolService.findByNombreRol(dto.getNombreRol());
        System.out.println("Registrando usuario: " + persona.getEmail());
        if (rol == null) {
            throw new RuntimeException("Rol no encontrado: " + dto.getNombreRol());
        }
        persona.setRol(rol);

        personaService.create(persona);
try {
    List<SimpleGrantedAuthority> authorities = List.of(
        new SimpleGrantedAuthority("ROLE_" + dto.getNombreRol().toUpperCase())
    );

    User userDetails = new User(persona.getEmail(), persona.getContrasena(), authorities);
    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

    SecurityContextHolder.getContext().setAuthentication(authToken);
} catch (Exception e) {
    System.out.println("Error autenticando manualmente: " + e.getMessage());
}


        if ("fundacion".equals(dto.getNombreRol())) {
            Fundacion fundacion = new Fundacion();
            fundacion.setNombre_fundacion(dto.getNombreFundacion());
            fundacion.setDireccion(dto.getDireccion());
            fundacion.setCorreo(dto.getCorreoFundacion());
            fundacion.setTelefono(dto.getTelefonoFundacion());
            fundacion.setPersona(persona);
            fundacionService.create(fundacion);
            return "redirect:/dashboardFundacion";
        }else{
            return "redirect:/dashboardVendedor";
        }

        // Autenticación manual para iniciar sesión automáticamente



    }
}
