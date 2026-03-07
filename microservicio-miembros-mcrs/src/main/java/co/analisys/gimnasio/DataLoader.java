package co.analisys.gimnasio;

import co.analisys.gimnasio.model.Email;
import co.analisys.gimnasio.model.Miembro;
import co.analisys.gimnasio.model.MiembroId;
import co.analisys.gimnasio.repository.MiembroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private MiembroRepository miembroRepository;

    @Override
    public void run(String... args) throws Exception {
        Miembro miembro1 = new Miembro(
                new MiembroId("1"),
                "Juan Pérez",
                new Email("juan@email.com"),
                LocalDate.now(),
                true
        );
        miembroRepository.save(miembro1);

        Miembro miembro2 = new Miembro(
                new MiembroId("2"),
                "María López",
                new Email("maria@email.com"),
                LocalDate.now().minusDays(30),
                true
        );
        miembroRepository.save(miembro2);

        Miembro miembro3 = new Miembro(
                new MiembroId("3"),
                "Pedro Inactivo",
                new Email("pedro@email.com"),
                LocalDate.now().minusDays(60),
                false
        );
        miembroRepository.save(miembro3);

        System.out.println("Datos de miembros cargados exitosamente.");
    }
}
