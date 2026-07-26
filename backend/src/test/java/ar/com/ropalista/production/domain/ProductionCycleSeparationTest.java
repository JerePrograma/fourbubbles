package ar.com.ropalista.production.domain;

import ar.com.ropalista.order.domain.LaundryOrder;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class ProductionCycleSeparationTest {
    @Test
    void cycleCannotStartUntilEveryRequiredSeparationIsConfirmed() {
        ProductionCycle cycle = cycle();
        ProductionCycleOrder assignment = new ProductionCycleOrder(mock(LaundryOrder.class), 1, 1500, true);
        cycle.addOrder(assignment);

        assertThatThrownBy(() -> cycle.start(OffsetDateTime.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("separación física");

        assignment.confirmSeparation("BAG-001", "operator", OffsetDateTime.now());
        cycle.start(OffsetDateTime.now());

        assertThat(cycle.getStatus()).isEqualTo(ProductionCycleStatus.RUNNING);
        assertThat(assignment.isSeparationConfirmed()).isTrue();
    }

    @Test
    void cycleWithoutRequiredSeparationStartsNormally() {
        ProductionCycle cycle = cycle();
        cycle.addOrder(new ProductionCycleOrder(mock(LaundryOrder.class), 1, 1500, false));

        cycle.start(OffsetDateTime.now());

        assertThat(cycle.getStatus()).isEqualTo(ProductionCycleStatus.RUNNING);
    }

    private ProductionCycle cycle() {
        return new ProductionCycle("PC-TEST", "production-test-key",
                mock(ProductionMachine.class), mock(ProductionProgram.class), 1500, null);
    }
}
