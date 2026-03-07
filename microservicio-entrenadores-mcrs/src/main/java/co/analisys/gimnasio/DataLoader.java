package co.analisys.gimnasio;

import co.analisys.gimnasio.model.Entrenador;
import co.analisys.gimnasio.model.EntrenadorId;
import co.analisys.gimnasio.model.Especialidad;
import co.analisys.gimnasio.repository.EntrenadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private EntrenadorRepository entrenadorRepository;

    @Override
    public void run(String... args) throws Exception {
        Entrenador entrenador1 = new Entrenador(
                new EntrenadorId("1"),
                "Carlos Rodríguez",
                new Especialidad("Yoga")
        );
        entrenadorRepository.save(entrenador1);

        Entrenador entrenador2 = new Entrenador(
                new EntrenadorId("2"),
                "Ana Martínez",
                new Especialidad("Spinning")
        );
        entrenadorRepository.save(entrenador2);

        System.out.println("Datos de entrenadores cargados exitosamente.");
    }
}
