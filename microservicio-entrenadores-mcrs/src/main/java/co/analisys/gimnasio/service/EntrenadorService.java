package co.analisys.gimnasio.service;

import co.analisys.gimnasio.model.Entrenador;
import co.analisys.gimnasio.model.EntrenadorId;
import co.analisys.gimnasio.model.Especialidad;
import co.analisys.gimnasio.repository.EntrenadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntrenadorService {

    @Autowired
    private EntrenadorRepository entrenadorRepository;

    public Entrenador obtenerEntrenador(EntrenadorId id) {
        return entrenadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Entrenador no encontrado"));
    }

    public boolean existeEntrenador(EntrenadorId id) {
        return entrenadorRepository.existsById(id);
    }

    public Entrenador agregarEntrenador(Entrenador entrenador) {
        return entrenadorRepository.save(entrenador);
    }

    public List<Entrenador> obtenerTodosEntrenadores() {
        return entrenadorRepository.findAll();
    }

    public void actualizarEspecialidad(EntrenadorId id, Especialidad nuevaEspecialidad) {
        Entrenador entrenador = obtenerEntrenador(id);
        entrenador.actualizarEspecialidad(nuevaEspecialidad);
        entrenadorRepository.save(entrenador);
    }
}
