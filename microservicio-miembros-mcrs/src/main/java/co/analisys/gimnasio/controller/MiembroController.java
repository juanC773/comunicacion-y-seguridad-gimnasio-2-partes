package co.analisys.gimnasio.controller;

import co.analisys.gimnasio.dto.PuedeAsistirClaseResponse;
import co.analisys.gimnasio.model.Email;
import co.analisys.gimnasio.model.Miembro;
import co.analisys.gimnasio.model.MiembroId;
import co.analisys.gimnasio.service.MiembroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/miembros")
public class MiembroController {

    @Autowired
    private MiembroService miembroService;

    @GetMapping("/public/status")
    public String getPublicStatus() {
        return "El servicio de miembros está funcionando correctamente";
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
    public List<Miembro> obtenerTodosMiembros() {
        return miembroService.obtenerTodosMiembros();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER', 'ROLE_MEMBER')")
    public Miembro obtenerMiembro(@PathVariable String id) {
        return miembroService.obtenerMiembro(new MiembroId(id));
    }

    @GetMapping("/{id}/puede-asistir-clase")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER', 'ROLE_MEMBER')")
    public PuedeAsistirClaseResponse puedeAsistirAClase(@PathVariable String id) {
        return miembroService.puedeAsistirAClase(new MiembroId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER')")
    public Miembro registrarMiembro(@RequestBody Miembro miembro) {
        return miembroService.registrarMiembro(miembro);
    }

    @PutMapping("/{id}/email")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_TRAINER', 'ROLE_MEMBER')")
    public void actualizarEmail(@PathVariable String id, @RequestBody String nuevoEmail) {
        miembroService.actualizarEmail(new MiembroId(id), new Email(nuevoEmail));
    }
}
