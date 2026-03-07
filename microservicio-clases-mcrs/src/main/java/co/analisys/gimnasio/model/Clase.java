package co.analisys.gimnasio.model;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Clase {

    @EmbeddedId
    private ClaseId id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "horario")
    private LocalDateTime horario;

    @Column(name = "capacidad_maxima")
    private int capacidadMaxima;

    @Embedded
    @AttributeOverride(name = "entrenadorid_value", column = @Column(name = "entrenador_id"))
    private EntrenadorId entrenadorId;

    @ElementCollection
    @CollectionTable(name = "clase_miembros_inscritos", joinColumns = @JoinColumn(name = "clase_id", referencedColumnName = "claseid_value"))
    @AttributeOverrides({ @AttributeOverride(name = "miembroid_value", column = @Column(name = "miembro_id")) })
    private Set<MiembroId> miembrosInscritos = new HashSet<>();

    public void actualizarHorario(LocalDateTime nuevoHorario) {
        this.horario = nuevoHorario;
    }

    public void actualizarCapacidad(int nuevaCapacidad) {
        this.capacidadMaxima = nuevaCapacidad;
    }

    public void inscribirMiembro(MiembroId miembroId) {
        this.miembrosInscritos.add(miembroId);
    }
}
