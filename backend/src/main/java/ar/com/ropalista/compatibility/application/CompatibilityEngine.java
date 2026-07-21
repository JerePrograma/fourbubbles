package ar.com.ropalista.compatibility.application;

import ar.com.ropalista.compatibility.domain.ColorGroup;
import ar.com.ropalista.compatibility.domain.FragrancePolicy;
import ar.com.ropalista.compatibility.domain.MaterialGroup;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CompatibilityEngine {
    public static final String RULE_VERSION = "COMPAT-1";

    public Result evaluate(ProfileData a, ProfileData b) {
        List<Reason> reasons = new ArrayList<>();

        hard(a.exclusiveCycle() || b.exclusiveCycle(), "EXCLUSIVE_CYCLE_REQUIRED",
                "Al menos un pedido exige ciclo exclusivo", reasons);
        hard(a.colorGroup() == ColorGroup.UNKNOWN || b.colorGroup() == ColorGroup.UNKNOWN,
                "UNKNOWN_COLOR", "No se permite compartir ciclo con color desconocido", reasons);
        hard(a.colorGroup() != b.colorGroup(), "COLOR_GROUP_MISMATCH",
                "Los grupos de color no coinciden", reasons);
        hard(!materialsCompatible(a.materialGroup(), b.materialGroup()), "MATERIAL_GROUP_MISMATCH",
                "Los materiales requieren programas incompatibles", reasons);
        hard(a.hypoallergenic() != b.hypoallergenic(), "HYPOALLERGENIC_ISOLATION",
                "Un tratamiento hipoalergénico no puede mezclarse con uno estándar", reasons);
        hard((a.babyClothes() && b.petContact()) || (b.babyClothes() && a.petContact()),
                "BABY_PET_CROSS_CONTAMINATION",
                "Ropa de bebé y ropa con contacto de mascotas deben separarse", reasons);
        hard(heavySoilAgainstSensitive(a, b) || heavySoilAgainstSensitive(b, a),
                "HEAVY_SOIL_SENSITIVE_LOAD",
                "La suciedad pesada no puede mezclarse con una carga sensible", reasons);
        hard(a.fragrancePolicy() != b.fragrancePolicy(), "FRAGRANCE_POLICY_MISMATCH",
                "Las políticas de fragancia no coinciden", reasons);

        warning(a.maxTemperatureC() != b.maxTemperatureC(), "TEMPERATURE_REDUCED",
                "Se recomienda usar la menor temperatura máxima admitida", reasons);
        warning(a.dryerAllowed() != b.dryerAllowed(), "DRYER_DISABLED",
                "El secado compartido debe deshabilitarse", reasons);
        warning(a.softenerAllowed() != b.softenerAllowed(), "SOFTENER_DISABLED",
                "El suavizante debe deshabilitarse", reasons);

        boolean compatible = reasons.stream().noneMatch(reason -> reason.severity() == Severity.HARD);
        Recommendation recommendation = new Recommendation(
                Math.min(a.maxTemperatureC(), b.maxTemperatureC()),
                a.dryerAllowed() && b.dryerAllowed(),
                a.softenerAllowed() && b.softenerAllowed(),
                a.fragrancePolicy() == b.fragrancePolicy() ? a.fragrancePolicy() : FragrancePolicy.NONE,
                sensitive(a.materialGroup()) || sensitive(b.materialGroup()) ? "GENTLE" : "NORMAL",
                compatible ? "SHARED" : "BLOCKED");
        return new Result(compatible, List.copyOf(reasons), recommendation);
    }

    private boolean materialsCompatible(MaterialGroup a, MaterialGroup b) {
        if (a == b) return true;
        if ((a == MaterialGroup.COTTON && b == MaterialGroup.SYNTHETIC)
                || (a == MaterialGroup.SYNTHETIC && b == MaterialGroup.COTTON)) return true;
        if (a == MaterialGroup.MIXED) return b == MaterialGroup.COTTON
                || b == MaterialGroup.SYNTHETIC || b == MaterialGroup.MIXED;
        if (b == MaterialGroup.MIXED) return a == MaterialGroup.COTTON
                || a == MaterialGroup.SYNTHETIC || a == MaterialGroup.MIXED;
        return false;
    }

    private boolean heavySoilAgainstSensitive(ProfileData dirty, ProfileData other) {
        return dirty.heavySoil() && (other.hypoallergenic() || other.babyClothes()
                || sensitive(other.materialGroup()));
    }

    private boolean sensitive(MaterialGroup material) {
        return material == MaterialGroup.DELICATE || material == MaterialGroup.WOOL;
    }

    private void hard(boolean condition, String code, String message, List<Reason> reasons) {
        if (condition) reasons.add(new Reason(code, Severity.HARD, message));
    }

    private void warning(boolean condition, String code, String message, List<Reason> reasons) {
        if (condition) reasons.add(new Reason(code, Severity.WARNING, message));
    }

    public enum Severity { HARD, WARNING }

    public record ProfileData(ColorGroup colorGroup, MaterialGroup materialGroup, int maxTemperatureC,
                              boolean dryerAllowed, FragrancePolicy fragrancePolicy,
                              boolean softenerAllowed, boolean hypoallergenic, boolean babyClothes,
                              boolean petContact, boolean heavySoil, boolean exclusiveCycle) {}

    public record Reason(String code, Severity severity, String message) {}

    public record Recommendation(int maxTemperatureC, boolean dryerAllowed,
                                 boolean softenerAllowed, FragrancePolicy fragrancePolicy,
                                 String programMode, String cycleMode) {}

    public record Result(boolean compatible, List<Reason> reasons, Recommendation recommendation) {}
}
