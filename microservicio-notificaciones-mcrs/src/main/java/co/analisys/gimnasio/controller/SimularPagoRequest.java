package co.analisys.gimnasio.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimularPagoRequest {

    private String miembroId = "1";
    private String concepto = "membresía";
    private BigDecimal monto = new BigDecimal("50.00");
}
