package co.analisys.gimnasio.service;

import co.analisys.gimnasio.model.Equipo;
import co.analisys.gimnasio.model.EquipoId;
import co.analisys.gimnasio.repository.EquipoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;

    public Equipo obtenerEquipo(EquipoId id) {
        return equipoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
    }

    public Equipo agregarEquipo(Equipo equipo) {
        return equipoRepository.save(equipo);
    }

    public List<Equipo> obtenerTodosEquipos() {
        return equipoRepository.findAll();
    }

    public void actualizarCantidad(EquipoId id, int nuevaCantidad) {
        Equipo equipo = obtenerEquipo(id);
        equipo.actualizarCantidad(nuevaCantidad);
        equipoRepository.save(equipo);
    }
}
