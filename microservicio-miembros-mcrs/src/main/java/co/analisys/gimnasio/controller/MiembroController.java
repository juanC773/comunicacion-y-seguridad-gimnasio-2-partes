package co.analisys.gimnasio.controller;

import co.analisys.gimnasio.dto.PuedeAsistirClaseResponse;
import co.analisys.gimnasio.model.Email;
import co.analisys.gimnasio.model.Miembro;
import co.analisys.gimnasio.model.MiembroId;
import co.analisys.gimnasio.service.MiembroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/miembros")
public class MiembroController {

    @Autowired
    private MiembroService miembroService;

    @GetMapping
    public List<Miembro> obtenerTodosMiembros() {
        return miembroService.obtenerTodosMiembros();
    }

    @GetMapping("/{id}")
    public Miembro obtenerMiembro(@PathVariable String id) {
        return miembroService.obtenerMiembro(new MiembroId(id));
    }

    @GetMapping("/{id}/puede-asistir-clase")
    public PuedeAsistirClaseResponse puedeAsistirAClase(@PathVariable String id) {
        return miembroService.puedeAsistirAClase(new MiembroId(id));
    }

    @PostMapping
    public Miembro registrarMiembro(@RequestBody Miembro miembro) {
        return miembroService.registrarMiembro(miembro);
    }

    @PutMapping("/{id}/email")
    public void actualizarEmail(@PathVariable String id, @RequestBody String nuevoEmail) {
        miembroService.actualizarEmail(new MiembroId(id), new Email(nuevoEmail));
    }
}
