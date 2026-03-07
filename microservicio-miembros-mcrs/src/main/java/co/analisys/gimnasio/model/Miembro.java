package co.analisys.gimnasio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Miembro {

    @EmbeddedId
    private MiembroId id;

    @Column(name = "nombre")
    private String nombre;

    @Embedded
    private Email email;

    @Column(name = "fecha_inscripcion")
    private LocalDate fechaInscripcion;

    @Column(name = "membresia_activa", nullable = false)
    private boolean membresiaActiva = true;

    public void actualizarEmail(Email nuevoEmail) {
        this.email = nuevoEmail;
    }
}
