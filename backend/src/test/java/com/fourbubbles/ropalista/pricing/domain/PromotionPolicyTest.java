package com.fourbubbles.ropalista.pricing.domain;

import com.fourbubbles.ropalista.common.application.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PromotionPolicyTest {
    private final PromotionPolicy policy = new PromotionPolicy();
    private final LocalDate today = LocalDate.of(2026, 7, 20);

    @Test
    void firstPurchasePromotionRejectsReturningCustomer() {
        Promotion promotion = promotion();
        promotion.setNewCustomersOnly(true);

        assertThatThrownBy(() -> policy.validate(promotion, today, 1, false, 0, 0, 0))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("compras previas");
    }

    @Test
    void onePerAddressCannotBeReused() {
        Promotion promotion = promotion();
        promotion.setOnePerAddress(true);

        assertThatThrownBy(() -> policy.validate(promotion, today, 0, true, 0, 0, 0))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("domicilio");
    }

    @Test
    void totalQuotaIsEnforced() {
        Promotion promotion = promotion();
        promotion.setTotalQuota(10);

        assertThatThrownBy(() -> policy.validate(promotion, today, 0, false, 10, 0, 0))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cupo total");
    }

    @Test
    void dailyQuotaIsEnforced() {
        Promotion promotion = promotion();
        promotion.setDailyQuota(2);

        assertThatThrownBy(() -> policy.validate(promotion, today, 0, false, 1, 2, 1))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("cupo diario");
    }

    private Promotion promotion() {
        Promotion promotion = new Promotion();
        promotion.setActive(true);
        promotion.setStartsOn(today.minusDays(1));
        promotion.setEndsOn(today.plusDays(1));
        return promotion;
    }
}
