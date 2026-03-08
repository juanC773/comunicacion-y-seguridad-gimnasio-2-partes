package co.analisys.gimnasio.controller;

import co.analisys.gimnasio.model.Equipo;
import co.analisys.gimnasio.model.EquipoId;
import co.analisys.gimnasio.service.EquipoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipos")
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    @GetMapping("/public/status")
    public String getPublicStatus() {
        return "El servicio de equipos está funcionando correctamente";
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER', 'ROLE_MEMBER')")
    public List<Equipo> obtenerTodosEquipos() {
        return equipoService.obtenerTodosEquipos();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER', 'ROLE_MEMBER')")
    public Equipo obtenerEquipo(@PathVariable String id) {
        return equipoService.obtenerEquipo(new EquipoId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
    public Equipo agregarEquipo(@RequestBody Equipo equipo) {
        return equipoService.agregarEquipo(equipo);
    }

    @PutMapping("/{id}/cantidad")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
    public void actualizarCantidad(@PathVariable String id, @RequestBody int nuevaCantidad) {
        equipoService.actualizarCantidad(new EquipoId(id), nuevaCantidad);
    }
}
