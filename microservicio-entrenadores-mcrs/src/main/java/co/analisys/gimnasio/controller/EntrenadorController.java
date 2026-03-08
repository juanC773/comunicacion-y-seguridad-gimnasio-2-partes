package co.analisys.gimnasio.controller;

import co.analisys.gimnasio.model.Entrenador;
import co.analisys.gimnasio.model.EntrenadorId;
import co.analisys.gimnasio.model.Especialidad;
import co.analisys.gimnasio.service.EntrenadorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/entrenadores")
public class EntrenadorController {

    @Autowired
    private EntrenadorService entrenadorService;

    @GetMapping("/public/status")
    public String getPublicStatus() {
        return "El servicio de entrenadores está funcionando correctamente";
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER', 'ROLE_MEMBER')")
    public List<Entrenador> obtenerTodosEntrenadores() {
        return entrenadorService.obtenerTodosEntrenadores();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER', 'ROLE_MEMBER')")
    public Entrenador obtenerEntrenador(@PathVariable String id) {
        return entrenadorService.obtenerEntrenador(new EntrenadorId(id));
    }

    @GetMapping("/{id}/existe")
    public boolean existeEntrenador(@PathVariable String id) {
        return entrenadorService.existeEntrenador(new EntrenadorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
    public Entrenador agregarEntrenador(@RequestBody Entrenador entrenador) {
        return entrenadorService.agregarEntrenador(entrenador);
    }

    @PutMapping("/{id}/especialidad")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
    public void actualizarEspecialidad(@PathVariable String id, @RequestBody String nuevaEspecialidad) {
        entrenadorService.actualizarEspecialidad(new EntrenadorId(id), new Especialidad(nuevaEspecialidad));
    }
}
