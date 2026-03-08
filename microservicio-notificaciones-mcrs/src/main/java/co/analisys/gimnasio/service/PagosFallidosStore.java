package co.analisys.gimnasio.service;

import co.analisys.gimnasio.event.PagoEvent;
import co.analisys.gimnasio.model.PagoFallidoRegistro;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Guarda en memoria los pagos que llegaron a la DLQ para poder consultarlos (GET /pagos-fallidos).
 */
@Service
public class PagosFallidosStore {

    private final List<PagoFallidoRegistro> registros = new CopyOnWriteArrayList<>();

    public void agregar(PagoEvent event) {
        registros.add(new PagoFallidoRegistro(
                event.getMiembroId(),
                event.getConcepto(),
                event.getMonto(),
                event.getTimestamp() != null ? event.getTimestamp() : Instant.now(),
                Instant.now()
        ));
    }

    public List<PagoFallidoRegistro> listar() {
        return Collections.unmodifiableList(new ArrayList<>(registros));
    }
}
