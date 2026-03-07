package co.analisys.gimnasio;

import co.analisys.gimnasio.model.Equipo;
import co.analisys.gimnasio.model.EquipoId;
import co.analisys.gimnasio.repository.EquipoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private EquipoRepository equipoRepository;

    @Override
    public void run(String... args) throws Exception {
        Equipo equipo1 = new Equipo(
                new EquipoId("1"),
                "Mancuernas",
                "Set de mancuernas de 5kg",
                20
        );
        equipoRepository.save(equipo1);

        Equipo equipo2 = new Equipo(
                new EquipoId("2"),
                "Bicicleta estática",
                "Bicicleta para spinning",
                15
        );
        equipoRepository.save(equipo2);

        System.out.println("Datos de equipos cargados exitosamente.");
    }
}
