package co.analisys.gimnasio.controller;

import co.analisys.gimnasio.model.Clase;
import co.analisys.gimnasio.model.ClaseId;
import co.analisys.gimnasio.model.MiembroId;
import co.analisys.gimnasio.service.ClaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clases")
public class ClaseController {

    @Autowired
    private ClaseService claseService;

    @GetMapping
    public List<Clase> obtenerTodasClases() {
        return claseService.obtenerTodasClases();
    }

    @GetMapping("/{id}")
    public Clase obtenerClase(@PathVariable String id) {
        return claseService.obtenerClase(new ClaseId(id));
    }

    @PostMapping
    public Clase programarClase(@RequestBody Clase clase) {
        return claseService.programarClase(clase);
    }

    @PostMapping("/{claseId}/miembros")
    public Clase inscribirMiembro(@PathVariable String claseId, @RequestBody MiembroId miembroId) {
        return claseService.inscribirMiembro(new ClaseId(claseId), miembroId);
    }
}
