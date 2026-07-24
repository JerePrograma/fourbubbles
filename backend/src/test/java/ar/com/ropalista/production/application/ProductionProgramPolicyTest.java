package ar.com.ropalista.production.application;

import ar.com.ropalista.compatibility.domain.ColorGroup;
import ar.com.ropalista.compatibility.domain.FragrancePolicy;
import ar.com.ropalista.compatibility.domain.MaterialGroup;
import ar.com.ropalista.compatibility.domain.OrderTreatmentProfile;
import ar.com.ropalista.production.domain.MachineType;
import ar.com.ropalista.production.domain.ProductionProgram;
import ar.com.ropalista.production.domain.ProductionStage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductionProgramPolicyTest {
    private final ProductionProgramPolicy policy = new ProductionProgramPolicy();

    @Test
    void compatibleWashProgramIsAllowed() {
        ProductionProgram program = washProgram(30, true, false, FragrancePolicy.NONE);
        OrderTreatmentProfile profile = profile(MaterialGroup.DELICATE, 30,
                false, false, FragrancePolicy.NONE, false);

        var result = policy.evaluate(program, profile);

        assertThat(result.allowed()).isTrue();
        assertThat(result.reasons()).isEmpty();
    }

    @Test
    void washProgramCannotRelaxTemperatureProductsOrMaterialCare() {
        ProductionProgram program = washProgram(40, false, true, FragrancePolicy.STANDARD);
        OrderTreatmentProfile profile = profile(MaterialGroup.WOOL, 30,
                false, false, FragrancePolicy.NONE, false);

        var result = policy.evaluate(program, profile);

        assertThat(result.allowed()).isFalse();
        assertThat(result.reasons()).hasSize(4);
    }

    @Test
    void dryerRequiresPermissionAndGentleProgramForSensitiveMaterial() {
        ProductionProgram program = dryProgram(false);
        OrderTreatmentProfile profile = profile(MaterialGroup.DELICATE, 30,
                false, false, FragrancePolicy.NONE, false);

        var result = policy.evaluate(program, profile);

        assertThat(result.allowed()).isFalse();
        assertThat(result.reasons()).containsExactly(
                "El pedido no permite secadora",
                "El material exige secado delicado");
    }

    private ProductionProgram washProgram(int temperature, boolean gentle,
                                            boolean softener, FragrancePolicy fragrance) {
        return new ProductionProgram("TEST_WASH", "Test", ProductionStage.WASH,
                MachineType.WASHER, 30, temperature, gentle, softener, fragrance, null);
    }

    private ProductionProgram dryProgram(boolean gentle) {
        return new ProductionProgram("TEST_DRY", "Test", ProductionStage.DRY,
                MachineType.DRYER, 30, null, gentle, false, null, null);
    }

    private OrderTreatmentProfile profile(MaterialGroup material, int temperature,
                                             boolean dryerAllowed, boolean softenerAllowed,
                                             FragrancePolicy fragrance, boolean exclusive) {
        OrderTreatmentProfile profile = mock(OrderTreatmentProfile.class);
        when(profile.getColorGroup()).thenReturn(ColorGroup.LIGHT);
        when(profile.getMaterialGroup()).thenReturn(material);
        when(profile.getMaxTemperatureC()).thenReturn(temperature);
        when(profile.isDryerAllowed()).thenReturn(dryerAllowed);
        when(profile.isSoftenerAllowed()).thenReturn(softenerAllowed);
        when(profile.getFragrancePolicy()).thenReturn(fragrance);
        when(profile.isExclusiveCycle()).thenReturn(exclusive);
        return profile;
    }
}
