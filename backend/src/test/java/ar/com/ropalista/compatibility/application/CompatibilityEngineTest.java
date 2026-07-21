package ar.com.ropalista.compatibility.application;

import ar.com.ropalista.compatibility.domain.ColorGroup;
import ar.com.ropalista.compatibility.domain.FragrancePolicy;
import ar.com.ropalista.compatibility.domain.MaterialGroup;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompatibilityEngineTest {
    private final CompatibilityEngine engine = new CompatibilityEngine();

    @Test
    void compatibleProfilesProduceSharedRecommendationAndWarningsOnly() {
        var a = profile(ColorGroup.LIGHT, MaterialGroup.COTTON, 40, true, true, false, false, false, false, false);
        var b = profile(ColorGroup.LIGHT, MaterialGroup.SYNTHETIC, 30, false, false, false, false, false, false, false);

        var result = engine.evaluate(a, b);

        assertThat(result.compatible()).isTrue();
        assertThat(result.reasons()).allMatch(reason -> reason.severity() == CompatibilityEngine.Severity.WARNING);
        assertThat(result.recommendation().maxTemperatureC()).isEqualTo(30);
        assertThat(result.recommendation().dryerAllowed()).isFalse();
        assertThat(result.recommendation().softenerAllowed()).isFalse();
        assertThat(result.recommendation().cycleMode()).isEqualTo("SHARED");
    }

    @Test
    void colorUnknownExclusiveAndFragranceMismatchAreExplainedAsHardBlocks() {
        var a = profile(ColorGroup.UNKNOWN, MaterialGroup.COTTON, 40, true, true, false, false, false, false, true);
        var b = new CompatibilityEngine.ProfileData(ColorGroup.DARK, MaterialGroup.COTTON, 40,
                true, FragrancePolicy.NONE, true, false, false, false, false, false);

        var result = engine.evaluate(a, b);

        assertThat(result.compatible()).isFalse();
        assertThat(result.reasons()).extracting(CompatibilityEngine.Reason::code)
                .contains("UNKNOWN_COLOR", "COLOR_GROUP_MISMATCH", "EXCLUSIVE_CYCLE_REQUIRED",
                        "FRAGRANCE_POLICY_MISMATCH");
        assertThat(result.recommendation().cycleMode()).isEqualTo("BLOCKED");
    }

    @Test
    void sensitiveLoadsAreIsolatedFromPetAndHeavySoil() {
        var babyHypo = profile(ColorGroup.WHITES, MaterialGroup.DELICATE, 30,
                false, false, true, true, false, false, false);
        var dirtyPet = profile(ColorGroup.WHITES, MaterialGroup.DELICATE, 30,
                false, false, false, false, true, true, false);

        var result = engine.evaluate(babyHypo, dirtyPet);

        assertThat(result.compatible()).isFalse();
        assertThat(result.reasons()).extracting(CompatibilityEngine.Reason::code)
                .contains("HYPOALLERGENIC_ISOLATION", "BABY_PET_CROSS_CONTAMINATION",
                        "HEAVY_SOIL_SENSITIVE_LOAD");
    }

    @Test
    void delicateAndStandardMaterialsDoNotShareAProgram() {
        var delicate = profile(ColorGroup.LIGHT, MaterialGroup.DELICATE, 30,
                false, false, false, false, false, false, false);
        var cotton = profile(ColorGroup.LIGHT, MaterialGroup.COTTON, 30,
                false, false, false, false, false, false, false);

        assertThat(engine.evaluate(delicate, cotton).reasons())
                .extracting(CompatibilityEngine.Reason::code)
                .contains("MATERIAL_GROUP_MISMATCH");
    }

    private CompatibilityEngine.ProfileData profile(ColorGroup color, MaterialGroup material,
                                                      int temperature, boolean dryer, boolean softener,
                                                      boolean hypoallergenic, boolean baby, boolean pet,
                                                      boolean heavySoil, boolean exclusive) {
        return new CompatibilityEngine.ProfileData(color, material, temperature, dryer,
                FragrancePolicy.STANDARD, softener, hypoallergenic, baby, pet, heavySoil, exclusive);
    }
}
