package ar.com.ropalista.pricing.application;

import ar.com.ropalista.catalog.domain.ServiceOffering;
import ar.com.ropalista.common.api.BusinessException;
import ar.com.ropalista.customer.domain.Address;
import ar.com.ropalista.customer.domain.Client;
import ar.com.ropalista.pricing.domain.PriceDefinition;
import ar.com.ropalista.pricing.domain.Promotion;
import ar.com.ropalista.pricing.persistence.PriceDefinitionRepository;
import ar.com.ropalista.pricing.persistence.PromotionRepository;
import ar.com.ropalista.pricing.persistence.PromotionUsageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
public class PricingService {
    private final PriceDefinitionRepository prices;
    private final PromotionRepository promotions;
    private final PromotionUsageRepository usages;

    public PricingService(PriceDefinitionRepository prices, PromotionRepository promotions, PromotionUsageRepository usages) {
        this.prices = prices;
        this.promotions = promotions;
        this.usages = usages;
    }

    public PriceQuote quote(ServiceOffering service, Client client, Address address, String promotionCode, boolean firstOrder, OffsetDateTime at) {
        PriceDefinition price = prices.findApplicable(service.getId(), address.getZone().getId(), at).stream().findFirst()
                .orElseThrow(() -> new BusinessException("PRICE_NOT_FOUND", "No existe un precio vigente para el servicio y la zona", HttpStatus.UNPROCESSABLE_ENTITY));
        List<BreakdownLine> breakdown = new ArrayList<>();
        BigDecimal base = money(price.getAmount());
        breakdown.add(new BreakdownLine("BASE_PRICE", service.getName(), base));

        Promotion promotion = null;
        BigDecimal discount = BigDecimal.ZERO.setScale(2);
        BigDecimal total = base;
        if (promotionCode != null && !promotionCode.isBlank()) {
            promotion = validatePromotion(promotionCode, service, address, firstOrder, at);
            if (promotion.getFixedPrice() != null) {
                total = money(promotion.getFixedPrice());
                discount = base.subtract(total).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
                breakdown.add(new BreakdownLine("PROMOTION_FIXED_PRICE", promotion.getName(), total.subtract(base)));
            } else if (promotion.getPercentageDiscount() != null) {
                discount = money(base.multiply(promotion.getPercentageDiscount()));
                total = base.subtract(discount);
                breakdown.add(new BreakdownLine("PROMOTION_PERCENTAGE", promotion.getName(), discount.negate()));
            }
        }
        return new PriceQuote(price, promotion, base, discount, money(total.max(BigDecimal.ZERO)), "ARS", List.copyOf(breakdown));
    }

    private Promotion validatePromotion(String code, ServiceOffering service, Address address, boolean firstOrder, OffsetDateTime at) {
        Promotion promotion = promotions.findActive(code, at)
                .orElseThrow(() -> new BusinessException("PROMOTION_NOT_FOUND", "La promoción no existe o está inactiva", HttpStatus.UNPROCESSABLE_ENTITY));
        if (!promotion.isAutomaticApplicable()) {
            throw new BusinessException("PROMOTION_REQUIRES_MANUAL_VALIDATION",
                    "La promoción requiere validación manual de sus condiciones", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (promotion.getApplicableServiceCode() != null
                && !promotion.getApplicableServiceCode().equalsIgnoreCase(service.getCode())) {
            throw new BusinessException("PROMOTION_SERVICE_MISMATCH",
                    "La promoción no aplica al servicio seleccionado", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (promotion.getValidFrom().isAfter(at) || (promotion.getValidTo() != null && promotion.getValidTo().isBefore(at))) {
            throw new BusinessException("PROMOTION_OUT_OF_DATE", "La promoción no está vigente", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (promotion.isOnePerAddress() && usages.existsByPromotionIdAndAddressId(promotion.getId(), address.getId())) {
            throw new BusinessException("PROMOTION_ALREADY_USED_AT_ADDRESS", "La promoción ya fue utilizada en este domicilio", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (promotion.isNewCustomersOnly() && !firstOrder) {
            throw new BusinessException("PROMOTION_NEW_CUSTOMERS_ONLY", "La promoción es exclusiva para primera compra", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (promotion.getTotalQuota() != null && usages.countByPromotionId(promotion.getId()) >= promotion.getTotalQuota()) {
            throw new BusinessException("PROMOTION_QUOTA_EXHAUSTED", "La promoción agotó su cupo total", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        LocalDate date = at.toLocalDate();
        if (promotion.getDailyQuota() != null) {
            OffsetDateTime from = date.atStartOfDay().atOffset(at.getOffset());
            if (usages.countInPeriod(promotion.getId(), from, from.plusDays(1)) >= promotion.getDailyQuota()) {
                throw new BusinessException("PROMOTION_DAILY_QUOTA_EXHAUSTED", "La promoción agotó su cupo diario", HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }
        if (promotion.getMonthlyQuota() != null) {
            LocalDate first = date.with(TemporalAdjusters.firstDayOfMonth());
            OffsetDateTime from = first.atStartOfDay().atOffset(at.getOffset());
            if (usages.countInPeriod(promotion.getId(), from, from.plusMonths(1)) >= promotion.getMonthlyQuota()) {
                throw new BusinessException("PROMOTION_MONTHLY_QUOTA_EXHAUSTED", "La promoción agotó su cupo mensual", HttpStatus.UNPROCESSABLE_ENTITY);
            }
        }
        return promotion;
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public record BreakdownLine(String code, String description, BigDecimal amount) {}
    public record PriceQuote(PriceDefinition priceDefinition, Promotion promotion, BigDecimal basePrice,
                             BigDecimal discount, BigDecimal total, String currency, List<BreakdownLine> breakdown) {}
}
