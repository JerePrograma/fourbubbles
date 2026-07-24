package ar.com.ropalista.production.application;

import ar.com.ropalista.compatibility.domain.MaterialGroup;
import ar.com.ropalista.compatibility.domain.OrderTreatmentProfile;
import ar.com.ropalista.production.domain.ProductionProgram;
import ar.com.ropalista.production.domain.ProductionStage;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductionProgramPolicy {
    public Result evaluate(ProductionProgram program, OrderTreatmentProfile profile) {
        List<String> reasons = new ArrayList<>();
        boolean sensitiveMaterial = profile.getMaterialGroup() == MaterialGroup.DELICATE
                || profile.getMaterialGroup() == MaterialGroup.WOOL;

        if (program.getStage() == ProductionStage.WASH) {
            if (program.getMaxTemperatureC() == null
                    || program.getMaxTemperatureC() > profile.getMaxTemperatureC()) {
                reasons.add("La temperatura del programa supera el máximo del pedido");
            }
            if (program.isUsesSoftener() && !profile.isSoftenerAllowed()) {
                reasons.add("El programa usa suavizante y el pedido no lo permite");
            }
            if (program.getFragrancePolicy() != profile.getFragrancePolicy()) {
                reasons.add("La política de fragancia del programa no coincide con el pedido");
            }
            if (sensitiveMaterial && !program.isGentle()) {
                reasons.add("El material exige un programa delicado");
            }
        } else {
            if (!profile.isDryerAllowed()) {
                reasons.add("El pedido no permite secadora");
            }
            if (sensitiveMaterial && !program.isGentle()) {
                reasons.add("El material exige secado delicado");
            }
        }
        return new Result(reasons.isEmpty(), List.copyOf(reasons));
    }

    public record Result(boolean allowed, List<String> reasons) {}
}
