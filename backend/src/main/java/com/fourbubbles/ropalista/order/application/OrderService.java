package com.fourbubbles.ropalista.order.application;

import com.fourbubbles.ropalista.audit.application.AuditService;
import com.fourbubbles.ropalista.common.application.BusinessRuleException;
import com.fourbubbles.ropalista.common.application.ResourceNotFoundException;
import com.fourbubbles.ropalista.customer.domain.Address;
import com.fourbubbles.ropalista.customer.domain.Customer;
import com.fourbubbles.ropalista.customer.infrastructure.AddressRepository;
import com.fourbubbles.ropalista.customer.infrastructure.CustomerRepository;
import com.fourbubbles.ropalista.garment.domain.EquivalenceResult;
import com.fourbubbles.ropalista.garment.domain.GarmentEquivalence;
import com.fourbubbles.ropalista.garment.domain.GarmentEquivalenceCalculator;
import com.fourbubbles.ropalista.garment.infrastructure.GarmentEquivalenceRepository;
import com.fourbubbles.ropalista.order.api.OrderDtos;
import com.fourbubbles.ropalista.order.domain.LaundryOrder;
import com.fourbubbles.ropalista.order.domain.OrderItem;
import com.fourbubbles.ropalista.order.domain.OrderStatus;
import com.fourbubbles.ropalista.order.domain.OrderStatusHistory;
import com.fourbubbles.ropalista.order.infrastructure.LaundryOrderRepository;
import com.fourbubbles.ropalista.order.infrastructure.OrderItemRepository;
import com.fourbubbles.ropalista.order.infrastructure.OrderStatusHistoryRepository;
import com.fourbubbles.ropalista.pricing.domain.OrderLimitPolicy;
import com.fourbubbles.ropalista.pricing.domain.PriceVersion;
import com.fourbubbles.ropalista.pricing.domain.Promotion;
import com.fourbubbles.ropalista.pricing.domain.PromotionPolicy;
import com.fourbubbles.ropalista.pricing.domain.PromotionUsage;
import com.fourbubbles.ropalista.pricing.domain.ServicePlan;
import com.fourbubbles.ropalista.pricing.infrastructure.PriceVersionRepository;
import com.fourbubbles.ropalista.pricing.infrastructure.PromotionRepository;
import com.fourbubbles.ropalista.pricing.infrastructure.PromotionUsageRepository;
import com.fourbubbles.ropalista.pricing.infrastructure.ServicePlanRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    private final LaundryOrderRepository orders;
    private final OrderItemRepository items;
    private final OrderStatusHistoryRepository histories;
    private final CustomerRepository customers;
    private final AddressRepository addresses;
    private final ServicePlanRepository plans;
    private final GarmentEquivalenceRepository equivalences;
    private final PriceVersionRepository prices;
    private final PromotionRepository promotions;
    private final PromotionUsageRepository promotionUsages;
    private final GarmentEquivalenceCalculator calculator;
    private final OrderStateMachine stateMachine;
    private final AuditService audit;
    private final JdbcTemplate jdbc;

    public OrderService(LaundryOrderRepository orders, OrderItemRepository items,
                        OrderStatusHistoryRepository histories, CustomerRepository customers,
                        AddressRepository addresses, ServicePlanRepository plans,
                        GarmentEquivalenceRepository equivalences, PriceVersionRepository prices,
                        PromotionRepository promotions, PromotionUsageRepository promotionUsages,
                        GarmentEquivalenceCalculator calculator, OrderStateMachine stateMachine,
                        AuditService audit, JdbcTemplate jdbc) {
        this.orders = orders;
        this.items = items;
        this.histories = histories;
        this.customers = customers;
        this.addresses = addresses;
        this.plans = plans;
        this.equivalences = equivalences;
        this.prices = prices;
        this.promotions = promotions;
        this.promotionUsages = promotionUsages;
        this.calculator = calculator;
        this.stateMachine = stateMachine;
        this.audit = audit;
        this.jdbc = jdbc;
    }

    @Transactional
    public OrderDtos.OrderResponse create(OrderDtos.CreateOrderRequest request) {
        Customer customer = customers.findById(request.customerId()).filter(value -> !value.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.customerId()));
        Address address = addresses.findByIdAndCustomerIdAndDeletedAtIsNull(request.addressId(), customer.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Domicilio del cliente", request.addressId()));
        ServicePlan plan = plans.findByIdAndActiveTrueAndDeletedAtIsNull(request.servicePlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Servicio activo", request.servicePlanId()));

        LaundryOrder order = new LaundryOrder();
        order.setOrderNumber(nextOrderNumber());
        order.setCustomer(customer);
        order.setAddress(address);
        order.setServicePlan(plan);
        order.setStatus(OrderStatus.INQUIRY);
        order.setModality(request.modality().trim().toUpperCase());
        order.setExclusiveCycle(request.exclusiveCycle());
        order.setDeclaredWeightGrams(request.declaredWeightGrams());
        order.setPickupScheduledAt(request.pickupScheduledAt());
        order.setPromisedAt(request.promisedAt());
        order.setNotes(request.notes());
        orders.save(order);
        recordHistory(order, null, OrderStatus.INQUIRY, "Pedido creado", null, null);
        audit.record("LaundryOrder", order.getId(), "CREATE", null, order.getOrderNumber(), "Alta de pedido");
        return response(order);
    }

    @Transactional
    public OrderDtos.OrderResponse receive(UUID orderId, OrderDtos.ReceiveOrderRequest request) {
        LaundryOrder order = order(orderId);
        if (order.getStatus() != OrderStatus.PICKED_UP && order.getStatus() != OrderStatus.RECEIVED
                && order.getStatus() != OrderStatus.PENDING_INSPECTION) {
            throw new BusinessRuleException("ORDER_NOT_READY_FOR_RECEPTION",
                    "El pedido debe estar retirado para registrar la recepción");
        }

        items.deleteByOrderId(orderId);
        int physicalTotal = 0;
        BigDecimal equivalentTotal = BigDecimal.ZERO;
        boolean exclusiveRequired = order.isExclusiveCycle();

        for (OrderDtos.ItemRequest input : request.items()) {
            GarmentEquivalence rule = equivalences.findByIdAndActiveTrueAndDeletedAtIsNull(input.garmentEquivalenceId())
                    .filter(value -> value.isValidOn(LocalDate.now(BUSINESS_ZONE)))
                    .orElseThrow(() -> new ResourceNotFoundException("Equivalencia vigente",
                            input.garmentEquivalenceId()));
            EquivalenceResult result = calculator.calculate(input.physicalPieceCount(),
                    rule.getPhysicalUnitsPerGroup(), rule.getEquivalentUnits());

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setGarmentEquivalence(rule);
            item.setRuleNameSnapshot(rule.getName());
            item.setPhysicalUnitsPerGroupSnapshot(rule.getPhysicalUnitsPerGroup());
            item.setEquivalentUnitsPerGroupSnapshot(rule.getEquivalentUnits());
            item.setPhysicalPieceCount(result.physicalPieces());
            item.setGroupCount(result.groups());
            item.setEquivalentUnits(result.equivalentUnits());
            item.setObservations(input.observations());
            items.save(item);

            physicalTotal += result.physicalPieces();
            equivalentTotal = equivalentTotal.add(result.equivalentUnits());
            exclusiveRequired = exclusiveRequired || rule.isExclusiveCycleRequired();
        }

        ServicePlan plan = order.getServicePlan();
        if (plan.getMaxEquivalentUnits() != null && plan.getMaxWeightGrams() != null) {
            OrderLimitPolicy.LimitResult limit = new OrderLimitPolicy().evaluate(equivalentTotal,
                    request.actualWeightGrams(), plan.getMaxEquivalentUnits(), plan.getMaxWeightGrams());
            if (limit.exceeded()) {
                throw new BusinessRuleException("SERVICE_LIMIT_EXCEEDED",
                        "El pedido supera el plan por " + limit.firstLimit()
                                + "; debe dividirse, cambiar de servicio o presupuestarse");
            }
        }

        order.setPhysicalPieceCount(physicalTotal);
        order.setEquivalentUnits(equivalentTotal);
        order.setActualWeightGrams(request.actualWeightGrams());
        order.setExclusiveCycle(exclusiveRequired);
        transitionInternal(order, OrderStatus.RECEIVED, request.observation(), null, null, false);
        return response(order);
    }

    @Transactional
    public OrderDtos.PriceQuoteResponse quote(UUID orderId, OrderDtos.QuoteRequest request) {
        LaundryOrder order = order(orderId);
        LocalDate today = LocalDate.now(BUSINESS_ZONE);
        PriceVersion price = prices.findApplicable(order.getServicePlan().getId(),
                        order.getAddress().getZone().getId(), today)
                .stream().findFirst()
                .orElseThrow(() -> new BusinessRuleException("PRICE_NOT_FOUND",
                        "No existe un precio vigente para el servicio y la zona"));

        BigDecimal base = price.getAmount();
        BigDecimal discount = BigDecimal.ZERO;
        BigDecimal credit = BigDecimal.ZERO;
        Promotion promotion = null;
        String explanation = "Precio base vigente: " + base + " " + price.getCurrency();

        if (request.promotionCode() != null && !request.promotionCode().isBlank()) {
            promotion = promotions.findByCodeIgnoreCaseAndActiveTrueAndDeletedAtIsNull(request.promotionCode().trim())
                    .orElseThrow(() -> new ResourceNotFoundException("Promoción", request.promotionCode()));
            validatePromotion(order, promotion, today);

            if (promotion.getFixedPrice() != null) {
                discount = base.subtract(promotion.getFixedPrice()).max(BigDecimal.ZERO);
                explanation += "; promoción " + promotion.getCode() + " fija el precio en "
                        + promotion.getFixedPrice();
            } else if (promotion.getPercentageDiscount() != null) {
                discount = base.multiply(promotion.getPercentageDiscount())
                        .setScale(2, RoundingMode.HALF_UP);
                explanation += "; descuento " + promotion.getPercentageDiscount().movePointRight(2) + "%";
            }
            if (promotion.getCreditAmount() != null) {
                credit = promotion.getCreditAmount();
                explanation += "; genera crédito futuro de " + credit;
            }
        }

        BigDecimal total = base.subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        order.setPriceVersion(price);
        order.setPromotion(promotion);
        order.setQuotedBaseAmount(base);
        order.setDiscountAmount(discount);
        order.setCreditAmount(credit);
        order.setQuotedAmount(total);
        order.setCurrency(price.getCurrency());
        order.setPricingExplanation(explanation);
        if (order.getStatus() == OrderStatus.INQUIRY) {
            transitionInternal(order, OrderStatus.QUOTED, "Cotización calculada", null, null, true);
        }
        audit.record("LaundryOrder", order.getId(), "QUOTE", null, total.toPlainString(), explanation);
        return new OrderDtos.PriceQuoteResponse(base, discount, credit, total, price.getCurrency(),
                promotion == null ? null : promotion.getCode(), explanation);
    }

    @Transactional
    public OrderDtos.OrderResponse confirmPrice(UUID orderId) {
        LaundryOrder order = order(orderId);
        if (order.getQuotedAmount() == null || order.getPriceVersion() == null) {
            throw new BusinessRuleException("ORDER_NOT_QUOTED", "El pedido debe cotizarse antes de confirmar");
        }
        if (order.getConfirmedAmount() != null) {
            return response(order);
        }
        order.setConfirmedAmount(order.getQuotedAmount());
        if (order.getPromotion() != null) {
            validatePromotion(order, order.getPromotion(), LocalDate.now(BUSINESS_ZONE));
            PromotionUsage usage = new PromotionUsage();
            usage.setPromotion(order.getPromotion());
            usage.setCustomer(order.getCustomer());
            usage.setAddress(order.getAddress());
            promotionUsages.save(usage);
        }
        audit.record("LaundryOrder", order.getId(), "CONFIRM_PRICE", null,
                order.getConfirmedAmount().toPlainString(), order.getPricingExplanation());
        return response(order);
    }

    @Transactional
    public OrderDtos.OrderResponse transition(UUID orderId, OrderDtos.TransitionRequest request) {
        LaundryOrder order = order(orderId);
        OrderStatus target;
        try {
            target = OrderStatus.valueOf(request.newStatus().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessRuleException("INVALID_ORDER_STATUS", "El estado solicitado no existe");
        }
        transitionInternal(order, target, request.observation(), request.location(),
                request.notificationReference(), true);
        return response(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDtos.OrderResponse> list() {
        return orders.findByDeletedAtIsNullOrderByCreatedAtDesc().stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public OrderDtos.OrderResponse get(UUID id) {
        return response(order(id));
    }

    private void validatePromotion(LaundryOrder order, Promotion promotion, LocalDate today) {
        Instant dayStart = today.atStartOfDay(BUSINESS_ZONE).toInstant();
        Instant dayEnd = today.plusDays(1).atStartOfDay(BUSINESS_ZONE).toInstant();
        LocalDate monthStartDate = today.with(TemporalAdjusters.firstDayOfMonth());
        Instant monthStart = monthStartDate.atStartOfDay(BUSINESS_ZONE).toInstant();
        Instant monthEnd = monthStartDate.plusMonths(1).atStartOfDay(BUSINESS_ZONE).toInstant();
        long ordersForCustomer = Math.max(0, orders.countByCustomerIdAndDeletedAtIsNull(order.getCustomer().getId()) - 1);
        new PromotionPolicy().validate(promotion, today, ordersForCustomer,
                promotionUsages.existsByPromotionIdAndAddressIdAndDeletedAtIsNull(
                        promotion.getId(), order.getAddress().getId()),
                promotionUsages.countByPromotionIdAndDeletedAtIsNull(promotion.getId()),
                promotionUsages.countByPromotionIdAndCreatedAtBetweenAndDeletedAtIsNull(
                        promotion.getId(), dayStart, dayEnd),
                promotionUsages.countByPromotionIdAndCreatedAtBetweenAndDeletedAtIsNull(
                        promotion.getId(), monthStart, monthEnd));
    }

    private void transitionInternal(LaundryOrder order, OrderStatus target, String observation,
                                    String location, String notificationReference, boolean validate) {
        OrderStatus previous = order.getStatus();
        if (previous == target) {
            return;
        }
        if (validate) {
            stateMachine.validate(previous, target);
        }
        order.setStatus(target);
        if (target == OrderStatus.DELIVERED) {
            order.setDeliveredAt(Instant.now());
        }
        recordHistory(order, previous, target, observation, location, notificationReference);
        audit.record("LaundryOrder", order.getId(), "STATUS_CHANGE",
                previous == null ? null : previous.name(), target.name(), observation);
    }

    private void recordHistory(LaundryOrder order, OrderStatus previous, OrderStatus target,
                               String observation, String location, String notificationReference) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setPreviousStatus(previous);
        history.setNewStatus(target);
        history.setObservation(observation);
        history.setLocation(location);
        history.setNotificationReference(notificationReference);
        histories.save(history);
    }

    private LaundryOrder order(UUID id) {
        return orders.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", id));
    }

    private String nextOrderNumber() {
        Long value = jdbc.queryForObject("select nextval('order_number_seq')", Long.class);
        return "RL-%06d".formatted(value);
    }

    private OrderDtos.OrderResponse response(LaundryOrder order) {
        List<OrderDtos.ItemResponse> itemResponses =
                items.findByOrderIdAndDeletedAtIsNullOrderByCreatedAt(order.getId()).stream()
                        .map(item -> new OrderDtos.ItemResponse(item.getId(),
                                item.getGarmentEquivalence().getId(), item.getRuleNameSnapshot(),
                                item.getPhysicalPieceCount(), item.getGroupCount(),
                                item.getEquivalentUnits(), item.getObservations()))
                        .toList();
        return new OrderDtos.OrderResponse(order.getId(), order.getOrderNumber(), order.getCustomer().getId(),
                order.getCustomer().getFirstName() + " " + order.getCustomer().getLastName(),
                order.getAddress().getId(), order.getServicePlan().getId(), order.getServicePlan().getName(),
                order.getStatus().name(), order.getModality(), order.isExclusiveCycle(),
                order.getPhysicalPieceCount(), order.getEquivalentUnits(), order.getDeclaredWeightGrams(),
                order.getActualWeightGrams(), order.getQuotedAmount(), order.getConfirmedAmount(),
                order.getCurrency(), order.getPricingExplanation(), order.getPickupScheduledAt(),
                order.getPromisedAt(), order.getDeliveredAt(), itemResponses);
    }
}
