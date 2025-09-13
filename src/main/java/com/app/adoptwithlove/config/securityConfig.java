package com.app.adoptwithlove.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


import java.util.List;


import com.app.adoptwithlove.entity.Persona;
import com.app.adoptwithlove.repository.PersonaRepository;

@Configuration
@EnableWebSecurity
public class securityConfig {

    @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/tiendas", "/fundaciones", "/", "/login", "/registro",
                "/css/**", "/js/**", "/img/**",
                "/dashboardVendedor", "/dashboardFundacion",
                "/productos/admin", "/animales/admin",
                "/animal/editar/**", "/animal/crear",
                "/productos/editar/**", "/productos/crear"
            ).permitAll()
            .anyRequest().authenticated()
        )
        .csrf(csrf -> csrf
            .ignoringRequestMatchers(
            "/productos/**",  
            "/animal/**",
            "/registro",
            "/tienda",
            "/fundaciones",
            "/animales"
        )
        )
        .formLogin(form -> form
            .loginPage("/login")
            .failureUrl("/login?error=true")
            .successHandler((request, response, authentication) -> {
                response.sendRedirect("/postLogin");
            })
            .permitAll()
        )
        .logout(logout -> logout.permitAll());

    return http.build();
}



    @Bean
    public UserDetailsService userDetailsService(PersonaRepository personaRepository) {
        return email -> {
            // Verificación especial para el administrador
            if (email.equalsIgnoreCase("admin@gmail.com")) {
                return new User(
                    "admin@gmail.com",
                    passwordEncoder().encode("123456"), // contraseña encriptada
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
            }

            Persona persona = personaRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            String rolNombre = "ROLE_" + persona.getRol().getNombreRol().toUpperCase();

            return new User(
                persona.getEmail(),
                persona.getContrasena(),
                List.of(new SimpleGrantedAuthority(rolNombre))
            );
        };
    }



    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
