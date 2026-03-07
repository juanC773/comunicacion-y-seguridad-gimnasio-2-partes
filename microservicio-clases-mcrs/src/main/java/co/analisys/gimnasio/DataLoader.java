package co.analisys.gimnasio;

import co.analisys.gimnasio.model.Clase;
import co.analisys.gimnasio.model.ClaseId;
import co.analisys.gimnasio.model.EntrenadorId;
import co.analisys.gimnasio.repository.ClaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private ClaseRepository claseRepository;

    @Override
    public void run(String... args) throws Exception {
        // Los IDs de entrenadores coinciden con los cargados en microservicio-entrenadores-mcrs
        Clase clase1 = new Clase(
                new ClaseId("1"),
                "Yoga Matutino",
                LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0),
                20,
                new EntrenadorId("1"),
                new HashSet<>()
        );
        claseRepository.save(clase1);

        Clase clase2 = new Clase(
                new ClaseId("2"),
                "Spinning Vespertino",
                LocalDateTime.now().plusDays(1).withHour(18).withMinute(0).withSecond(0).withNano(0),
                15,
                new EntrenadorId("2"),
                new HashSet<>()
        );
        claseRepository.save(clase2);

        System.out.println("Datos de clases cargados exitosamente.");
    }
}
