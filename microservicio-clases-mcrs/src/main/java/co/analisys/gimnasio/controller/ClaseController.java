package co.analisys.gimnasio.controller;

import co.analisys.gimnasio.dto.ActualizarHorarioRequest;
import co.analisys.gimnasio.model.Clase;
import co.analisys.gimnasio.model.ClaseId;
import co.analisys.gimnasio.model.MiembroId;
import co.analisys.gimnasio.service.ClaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clases")
public class ClaseController {

    @Autowired
    private ClaseService claseService;

    @GetMapping("/public/status")
    public String getPublicStatus() {
        return "El servicio de clases está funcionando correctamente";
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER', 'ROLE_MEMBER')")
    public List<Clase> obtenerTodasClases() {
        return claseService.obtenerTodasClases();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER', 'ROLE_MEMBER')")
    public Clase obtenerClase(@PathVariable String id) {
        return claseService.obtenerClase(new ClaseId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
    public Clase programarClase(@RequestBody Clase clase) {
        return claseService.programarClase(clase);
    }

    @PostMapping("/{claseId}/miembros")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER', 'ROLE_MEMBER')")
    public Clase inscribirMiembro(@PathVariable String claseId, @RequestBody MiembroId miembroId) {
        return claseService.inscribirMiembro(new ClaseId(claseId), miembroId);
    }

    @PutMapping("/{id}/horario")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
    public Clase actualizarHorario(@PathVariable String id, @RequestBody ActualizarHorarioRequest request) {
        return claseService.actualizarHorario(new ClaseId(id), request.getHorario());
    }
}
