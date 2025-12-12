package com.app.adoptwithlove.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.app.adoptwithlove.entity.Persona;
import com.app.adoptwithlove.repository.PersonaRepository;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Configuration
@EnableWebSecurity
public class securityConfig {

    @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/", "/login", "/registro",
                "/fundaciones", "/fundaciones/**", "/fundacion/**",
                "/tiendas/**", "/productos/**", "/api/fundaciones/**",
                "/css/**", "/js/**", "/img/**", "/root/**",
                "/animales/**", "/postLogin",
                "/adopcion/**",
                // Rutas para API pública y recursos subidos en runtime
                "/animal/**", "/animal/api/**", "/uploads/**"
            ).permitAll()

            // ✅ Permitir libre acceso a estas rutas sin rol
            .requestMatchers("/habilitar/**", "/bloquear/**", "/reclamos/**", "/vendedores/bloqueados").permitAll()

            .anyRequest().authenticated()
        )
        // ✅ CSRF activo pero ignorado solo en rutas específicas
        .csrf(csrf -> csrf
            .ignoringRequestMatchers(
                "/habilitar/**",
                "/bloquear/**",
                "/adopcion/**",
                "/reclamo/**",
                "/reclamos/**",
                "/animal/**",
                "/api/**",
                "/productos/**",
                "/productos/upload-csv"
            )
        )
        .formLogin(form -> form
            .loginPage("/login")
            .failureUrl("/login?error=true")
            .defaultSuccessUrl("/postLogin", true)
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/")
            .permitAll()
        );

    return http.build();
}


    @Bean
    public UserDetailsService userDetailsService(PersonaRepository personaRepository) {
        return email -> {
            if (email.equalsIgnoreCase("admin@gmail.com")) {
                return new User(
                    "admin@gmail.com",
                    passwordEncoder().encode("123456"),
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                );
            }

            Persona persona = personaRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            String rolNombre = persona.getRol().getNombreRol();
            if (rolNombre == null || rolNombre.isBlank()) {
                throw new UsernameNotFoundException("Rol no asignado");
            }

            return new User(
                persona.getEmail(),
                persona.getContrasena(),
                List.of(new SimpleGrantedAuthority("ROLE_" + rolNombre.toUpperCase()))
            );
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
