package com.fourbubbles.ropalista.pricing.domain;

import com.fourbubbles.ropalista.common.application.BusinessRuleException;

import java.time.LocalDate;

public class PromotionPolicy {
    public void validate(Promotion promotion, LocalDate date, long previousCustomerOrders,
                         boolean usedAtAddress, long totalUses, long dailyUses, long monthlyUses) {
        if (!promotion.isValidOn(date)) {
            throw new BusinessRuleException("PROMOTION_NOT_VALID", "La promoción no está vigente");
        }
        if (promotion.isNewCustomersOnly() && previousCustomerOrders > 0) {
            throw new BusinessRuleException("FIRST_PURCHASE_REQUIRED",
                    "La promoción requiere que el cliente no tenga compras previas");
        }
        if (promotion.isOnePerAddress() && usedAtAddress) {
            throw new BusinessRuleException("PROMOTION_ALREADY_USED_AT_ADDRESS",
                    "La promoción ya fue utilizada en este domicilio");
        }
        if (promotion.getTotalQuota() != null && totalUses >= promotion.getTotalQuota()) {
            throw new BusinessRuleException("PROMOTION_TOTAL_QUOTA_EXHAUSTED", "La promoción agotó su cupo total");
        }
        if (promotion.getDailyQuota() != null && dailyUses >= promotion.getDailyQuota()) {
            throw new BusinessRuleException("PROMOTION_DAILY_QUOTA_EXHAUSTED", "La promoción agotó su cupo diario");
        }
        if (promotion.getMonthlyQuota() != null && monthlyUses >= promotion.getMonthlyQuota()) {
            throw new BusinessRuleException("PROMOTION_MONTHLY_QUOTA_EXHAUSTED", "La promoción agotó su cupo mensual");
        }
    }
}
