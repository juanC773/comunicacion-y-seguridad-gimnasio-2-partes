package co.analisys.gimnasio.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entrenador {

    @EmbeddedId
    private EntrenadorId id;

    @Column(name = "nombre")
    private String nombre;

    @Embedded
    private Especialidad especialidad;

    public void actualizarEspecialidad(Especialidad nuevaEspecialidad) {
        this.especialidad = nuevaEspecialidad;
    }
}
