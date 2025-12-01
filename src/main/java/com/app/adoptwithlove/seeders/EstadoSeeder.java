package com.app.adoptwithlove.seeders;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.app.adoptwithlove.entity.Estado;
import com.app.adoptwithlove.repository.EstadoRepository;

@Component
public class EstadoSeeder implements CommandLineRunner {

    private final EstadoRepository estadoRepository;

    public EstadoSeeder(EstadoRepository estadoRepository) {
        this.estadoRepository = estadoRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Verifica si ya hay registros
        if (estadoRepository.count() == 0) {
            Estado activo = new Estado();
            activo.setNombreEstado("ACTIVO");

            Estado bloqueado = new Estado();
            bloqueado.setNombreEstado("BLOQUEADO");

            Estado inactivo = new Estado();
            inactivo.setNombreEstado("INACTIVO");

            Estado pendiente = new Estado();
            pendiente.setNombreEstado("PENDIENTE");

            Estado adoptado = new Estado();
            adoptado.setNombreEstado("ADOPTADO");

            Estado noDisponible = new Estado();
            noDisponible.setNombreEstado("NO DISPONIBLE");

            estadoRepository.save(activo);
            estadoRepository.save(bloqueado);
            estadoRepository.save(inactivo);
            estadoRepository.save(pendiente);
            estadoRepository.save(adoptado);
            estadoRepository.save(noDisponible);

            System.out.println("✅ Seeder: Estados creados correctamente.");
        } else {
            System.out.println("ℹ️ Seeder: Los estados ya existen, no se insertaron nuevos.");
        }
    }
}
