package co.analisys.gimnasio.controller;

import co.analisys.gimnasio.model.Entrenador;
import co.analisys.gimnasio.model.EntrenadorId;
import co.analisys.gimnasio.model.Especialidad;
import co.analisys.gimnasio.service.EntrenadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/entrenadores")
public class EntrenadorController {

    @Autowired
    private EntrenadorService entrenadorService;

    @GetMapping
    public List<Entrenador> obtenerTodosEntrenadores() {
        return entrenadorService.obtenerTodosEntrenadores();
    }

    @GetMapping("/{id}")
    public Entrenador obtenerEntrenador(@PathVariable String id) {
        return entrenadorService.obtenerEntrenador(new EntrenadorId(id));
    }

    @GetMapping("/{id}/existe")
    public boolean existeEntrenador(@PathVariable String id) {
        return entrenadorService.existeEntrenador(new EntrenadorId(id));
    }

    @PostMapping
    public Entrenador agregarEntrenador(@RequestBody Entrenador entrenador) {
        return entrenadorService.agregarEntrenador(entrenador);
    }

    @PutMapping("/{id}/especialidad")
    public void actualizarEspecialidad(@PathVariable String id, @RequestBody String nuevaEspecialidad) {
        entrenadorService.actualizarEspecialidad(new EntrenadorId(id), new Especialidad(nuevaEspecialidad));
    }
}
