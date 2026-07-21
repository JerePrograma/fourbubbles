package ar.com.ropalista.production.application;

import ar.com.ropalista.audit.application.AuditService;
import ar.com.ropalista.common.api.BusinessException;
import ar.com.ropalista.compatibility.application.CompatibilityEngine;
import ar.com.ropalista.compatibility.domain.CompatibilityEvaluation;
import ar.com.ropalista.compatibility.domain.FragrancePolicy;
import ar.com.ropalista.compatibility.domain.OrderTreatmentProfile;
import ar.com.ropalista.compatibility.persistence.CompatibilityEvaluationRepository;
import ar.com.ropalista.compatibility.persistence.OrderTreatmentProfileRepository;
import ar.com.ropalista.order.application.OrderTransitionPolicy;
import ar.com.ropalista.order.domain.LaundryOrder;
import ar.com.ropalista.order.domain.OrderStateHistory;
import ar.com.ropalista.order.domain.OrderStatus;
import ar.com.ropalista.order.persistence.LaundryOrderRepository;
import ar.com.ropalista.order.persistence.OrderStateHistoryRepository;
import ar.com.ropalista.production.api.ProductionDtos;
import ar.com.ropalista.production.domain.MachineStatus;
import ar.com.ropalista.production.domain.MachineType;
import ar.com.ropalista.production.domain.ProductionCycle;
import ar.com.ropalista.production.domain.ProductionCycleHistory;
import ar.com.ropalista.production.domain.ProductionCycleOrder;
import ar.com.ropalista.production.domain.ProductionCycleStatus;
import ar.com.ropalista.production.domain.ProductionMachine;
import ar.com.ropalista.production.domain.ProductionProgram;
import ar.com.ropalista.production.domain.ProductionStage;
import ar.com.ropalista.production.domain.QualityDecision;
import ar.com.ropalista.production.persistence.ProductionCycleHistoryRepository;
import ar.com.ropalista.production.persistence.ProductionCycleOrderRepository;
import ar.com.ropalista.production.persistence.ProductionCycleRepository;
import ar.com.ropalista.production.persistence.ProductionMachineRepository;
import ar.com.ropalista.production.persistence.ProductionProgramRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ProductionService {
    private static final Set<ProductionCycleStatus> ACTIVE_CYCLE_STATUSES =
            Set.of(ProductionCycleStatus.PLANNED, ProductionCycleStatus.RUNNING);

    private final ProductionMachineRepository machines;
    private final ProductionProgramRepository programs;
    private final ProductionCycleRepository cycles;
    private final ProductionCycleOrderRepository assignments;
    private final ProductionCycleHistoryRepository cycleHistories;
    private final LaundryOrderRepository orders;
    private final OrderStateHistoryRepository orderHistories;
    private final OrderTreatmentProfileRepository profiles;
    private final CompatibilityEvaluationRepository evaluations;
    private final OrderTransitionPolicy transitionPolicy;
    private final ProductionProgramPolicy programPolicy;
    private final AuditService audit;
    private final JdbcTemplate jdbcTemplate;

    public ProductionService(ProductionMachineRepository machines,
                             ProductionProgramRepository programs,
                             ProductionCycleRepository cycles,
                             ProductionCycleOrderRepository assignments,
                             ProductionCycleHistoryRepository cycleHistories,
                             LaundryOrderRepository orders,
                             OrderStateHistoryRepository orderHistories,
                             OrderTreatmentProfileRepository profiles,
                             CompatibilityEvaluationRepository evaluations,
                             OrderTransitionPolicy transitionPolicy,
                             ProductionProgramPolicy programPolicy,
                             AuditService audit,
                             JdbcTemplate jdbcTemplate) {
        this.machines = machines;
        this.programs = programs;
        this.cycles = cycles;
        this.assignments = assignments;
        this.cycleHistories = cycleHistories;
        this.orders = orders;
        this.orderHistories = orderHistories;
        this.profiles = profiles;
        this.evaluations = evaluations;
        this.transitionPolicy = transitionPolicy;
        this.programPolicy = programPolicy;
        this.audit = audit;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public ProductionDtos.MachineResponse createMachine(ProductionDtos.MachineRequest request) {
        String code = normalizeCode(request.code());
        if (machines.findByCodeIgnoreCase(code).isPresent()) {
            throw conflict("MACHINE_CODE_EXISTS", "Ya existe una máquina con ese código");
        }
        ProductionMachine machine = new ProductionMachine(code, request.name().trim(),
                request.machineType(), request.capacityGrams(), trim(request.notes()));
        machine.update(request.name().trim(), request.capacityGrams(), request.status(),
                request.active(), trim(request.notes()));
        ProductionMachine saved = machines.saveAndFlush(machine);
        audit.record("PRODUCTION_MACHINE", saved.getId(), "CREATE", null,
                machineSummary(saved), "Alta de máquina");
        return toMachineResponse(saved);
    }

    @Transactional
    public ProductionDtos.MachineResponse updateMachine(UUID id, ProductionDtos.MachineRequest request) {
        ProductionMachine machine = machines.findByIdForUpdate(id)
                .orElseThrow(() -> notFound("PRODUCTION_MACHINE_NOT_FOUND", "Máquina inexistente"));
        if (!machine.getCode().equalsIgnoreCase(request.code())
                || machine.getMachineType() != request.machineType()) {
            throw unprocessable("PRODUCTION_MACHINE_IDENTITY_IMMUTABLE",
                    "El código y tipo de máquina no pueden modificarse");
        }
        if (cycles.existsByMachineIdAndStatusIn(id, ACTIVE_CYCLE_STATUSES)) {
            throw conflict("PRODUCTION_MACHINE_IN_USE",
                    "No puede modificarse una máquina con un ciclo activo");
        }
        Map<String, Object> before = machineSummary(machine);
        machine.update(request.name().trim(), request.capacityGrams(), request.status(),
                request.active(), trim(request.notes()));
        audit.record("PRODUCTION_MACHINE", machine.getId(), "UPDATE", before,
                machineSummary(machine), "Actualización de máquina");
        return toMachineResponse(machine);
    }

    @Transactional(readOnly = true)
    public List<ProductionDtos.MachineResponse> listMachines() {
        return machines.findAllByOrderByNameAsc().stream().map(this::toMachineResponse).toList();
    }

    @Transactional
    public ProductionDtos.ProgramResponse createProgram(ProductionDtos.ProgramRequest request) {
        String code = normalizeCode(request.code());
        if (programs.findByCodeIgnoreCase(code).isPresent()) {
            throw conflict("PROGRAM_CODE_EXISTS", "Ya existe un programa con ese código");
        }
        validateProgramRequest(request);
        MachineType machineType = request.stage() == ProductionStage.WASH
                ? MachineType.WASHER : MachineType.DRYER;
        ProductionProgram program = new ProductionProgram(code, request.name().trim(),
                request.stage(), machineType, request.durationMinutes(), request.maxTemperatureC(),
                request.gentle(), request.usesSoftener(), request.fragrancePolicy(), trim(request.notes()));
        program.update(request.name().trim(), request.durationMinutes(), request.maxTemperatureC(),
                request.gentle(), request.usesSoftener(), request.fragrancePolicy(),
                request.active(), trim(request.notes()));
        ProductionProgram saved = programs.saveAndFlush(program);
        audit.record("PRODUCTION_PROGRAM", saved.getId(), "CREATE", null,
                programSummary(saved), "Alta de programa");
        return toProgramResponse(saved);
    }

    @Transactional
    public ProductionDtos.ProgramResponse updateProgram(UUID id, ProductionDtos.ProgramRequest request) {
        ProductionProgram program = programs.findByIdForUpdate(id)
                .orElseThrow(() -> notFound("PRODUCTION_PROGRAM_NOT_FOUND", "Programa inexistente"));
        if (!program.getCode().equalsIgnoreCase(request.code()) || program.getStage() != request.stage()) {
            throw unprocessable("PRODUCTION_PROGRAM_IDENTITY_IMMUTABLE",
                    "El código y etapa del programa no pueden modificarse");
        }
        if (cycles.existsByProgramIdAndStatusIn(id, ACTIVE_CYCLE_STATUSES)) {
            throw conflict("PRODUCTION_PROGRAM_IN_USE",
                    "No puede modificarse un programa con un ciclo activo");
        }
        validateProgramRequest(request);
        Map<String, Object> before = programSummary(program);
        program.update(request.name().trim(), request.durationMinutes(), request.maxTemperatureC(),
                request.gentle(), request.usesSoftener(), request.fragrancePolicy(),
                request.active(), trim(request.notes()));
        audit.record("PRODUCTION_PROGRAM", program.getId(), "UPDATE", before,
                programSummary(program), "Actualización de programa");
        return toProgramResponse(program);
    }

    @Transactional(readOnly = true)
    public List<ProductionDtos.ProgramResponse> listPrograms(ProductionStage stage) {
        List<ProductionProgram> values = stage == null
                ? programs.findAllByOrderByStageAscNameAsc()
                : programs.findAllByStageAndActiveTrueOrderByNameAsc(stage);
        return values.stream().map(this::toProgramResponse).toList();
    }

    @Transactional
    public ProductionDtos.CycleResponse createCycle(String idempotencyKey,
                                                     ProductionDtos.CreateCycleRequest request) {
        String key = normalizeIdempotencyKey(idempotencyKey);
        acquireIdempotencyLock(key);
        var existing = cycles.findByIdempotencyKey(key);
        if (existing.isPresent()) {
            validateIdempotentReplay(existing.get(), request);
            return toCycleResponse(existing.get());
        }

        List<UUID> orderIds = canonicalUniqueOrderIds(request.orderIds());
        ProductionMachine machine = machines.findByIdForUpdate(request.machineId())
                .orElseThrow(() -> notFound("PRODUCTION_MACHINE_NOT_FOUND", "Máquina inexistente"));
        ProductionProgram program = programs.findByIdForUpdate(request.programId())
                .orElseThrow(() -> notFound("PRODUCTION_PROGRAM_NOT_FOUND", "Programa inexistente"));
        validateMachineAndProgram(machine, program);
        if (cycles.existsByMachineIdAndStatusIn(machine.getId(), ACTIVE_CYCLE_STATUSES)) {
            throw conflict("PRODUCTION_MACHINE_BUSY", "La máquina ya posee un ciclo activo");
        }

        List<LaundryOrder> lockedOrders = new ArrayList<>();
        List<OrderTreatmentProfile> lockedProfiles = new ArrayList<>();
        int plannedWeight = 0;
        for (UUID orderId : orderIds) {
            LaundryOrder order = orders.findByIdForUpdate(orderId)
                    .orElseThrow(() -> notFound("ORDER_NOT_FOUND", "Pedido inexistente: " + orderId));
            validateOrderStage(order, program.getStage());
            if (assignments.existsActiveAssignment(orderId, program.getStage(), ACTIVE_CYCLE_STATUSES)) {
                throw conflict("ORDER_ALREADY_ASSIGNED_TO_ACTIVE_CYCLE",
                        "El pedido ya está asignado a un ciclo activo de la misma etapa");
            }
            OrderTreatmentProfile profile = profiles.findByOrderId(orderId)
                    .orElseThrow(() -> unprocessable("TREATMENT_PROFILE_NOT_FOUND",
                            "Falta el perfil de tratamiento del pedido " + orderId));
            var policyResult = programPolicy.evaluate(program, profile);
            if (!policyResult.allowed()) {
                throw unprocessable("PROGRAM_NOT_ALLOWED_FOR_ORDER",
                        String.join("; ", policyResult.reasons()));
            }
            Integer actualWeight = order.getActualWeightGrams();
            if (actualWeight == null || actualWeight <= 0) {
                throw unprocessable("ORDER_ACTUAL_WEIGHT_REQUIRED",
                        "El pedido debe poseer peso real de recepción");
            }
            plannedWeight = Math.addExact(plannedWeight, actualWeight);
            lockedOrders.add(order);
            lockedProfiles.add(profile);
        }
        if (plannedWeight > machine.getCapacityGrams()) {
            throw unprocessable("PRODUCTION_MACHINE_CAPACITY_EXCEEDED",
                    "El peso planificado supera la capacidad de la máquina");
        }

        boolean separationRequired = false;
        if (lockedOrders.size() == 2) {
            if (lockedProfiles.stream().anyMatch(OrderTreatmentProfile::isExclusiveCycle)) {
                throw unprocessable("EXCLUSIVE_ORDER_CANNOT_SHARE_CYCLE",
                        "Un pedido exige ciclo exclusivo");
            }
            CompatibilityEvaluation evaluation = currentEvaluation(lockedOrders, lockedProfiles);
            if (!evaluation.isEffectivelyCompatible()) {
                throw unprocessable("ORDERS_NOT_EFFECTIVELY_COMPATIBLE",
                        "Los pedidos no son compatibles para compartir ciclo");
            }
            separationRequired = !evaluation.isCompatible();
        }

        for (LaundryOrder order : lockedOrders) {
            prepareOrderForPlanning(order, program.getStage());
        }

        ProductionCycle cycle = new ProductionCycle(nextCycleNumber(), key, machine, program,
                plannedWeight, trim(request.notes()));
        for (int index = 0; index < lockedOrders.size(); index++) {
            LaundryOrder order = lockedOrders.get(index);
            cycle.addOrder(new ProductionCycleOrder(order, index + 1,
                    order.getActualWeightGrams(), separationRequired));
        }
        ProductionCycle saved = cycles.saveAndFlush(cycle);
        cycleHistories.save(new ProductionCycleHistory(saved, null,
                ProductionCycleStatus.PLANNED, "Ciclo planificado"));
        audit.record("PRODUCTION_CYCLE", saved.getId(), "CREATE", null,
                cycleSummary(saved), "Planificación de ciclo");
        return toCycleResponse(saved);
    }

    @Transactional(readOnly = true)
    public ProductionDtos.CycleResponse getCycle(UUID id) {
        return toCycleResponse(cycles.findDetailedById(id)
                .orElseThrow(() -> notFound("PRODUCTION_CYCLE_NOT_FOUND", "Ciclo inexistente")));
    }

    @Transactional(readOnly = true)
    public Page<ProductionDtos.CycleResponse> searchCycles(ProductionCycleStatus status,
                                                            ProductionStage stage,
                                                            int page, int size) {
        Specification<ProductionCycle> specification = Specification.where(null);
        if (status != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("status"), status));
        }
        if (stage != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("program").get("stage"), stage));
        }
        var pageable = PageRequest.of(Math.max(0, page), Math.max(1, Math.min(100, size)),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return cycles.findAll(specification, pageable).map(this::toCycleResponse);
    }

    @Transactional
    public ProductionDtos.CycleResponse startCycle(UUID id, ProductionDtos.CycleActionRequest request) {
        ProductionCycle cycle = lockCycle(id);
        ProductionMachine machine = machines.findByIdForUpdate(cycle.getMachine().getId())
                .orElseThrow(() -> notFound("PRODUCTION_MACHINE_NOT_FOUND", "Máquina inexistente"));
        if (!machine.isAvailable()) {
            throw unprocessable("PRODUCTION_MACHINE_UNAVAILABLE", "La máquina no está disponible");
        }
        ProductionCycleStatus previous = cycle.getStatus();
        try {
            cycle.start(OffsetDateTime.now());
        } catch (IllegalStateException ex) {
            throw conflict("PRODUCTION_CYCLE_NOT_STARTABLE", ex.getMessage());
        }
        for (ProductionCycleOrder assignment : cycle.getOrders()) {
            LaundryOrder order = orders.findByIdForUpdate(assignment.getOrder().getId())
                    .orElseThrow(() -> notFound("ORDER_NOT_FOUND", "Pedido inexistente"));
            OrderStatus expected = cycle.getProgram().getStage() == ProductionStage.WASH
                    ? OrderStatus.WAITING_WASH : OrderStatus.WAITING_DRY;
            if (order.getStatus() != expected) {
                throw conflict("ORDER_NOT_READY_FOR_CYCLE_START",
                        "El pedido no está listo para iniciar esta etapa");
            }
            changeOrderStatus(order, cycle.getProgram().getStage() == ProductionStage.WASH
                    ? OrderStatus.WASHING : OrderStatus.DRYING,
                    "Inicio del ciclo " + cycle.getCycleNumber());
        }
        recordCycleStatus(cycle, previous, cycle.getStatus(), trim(request.observation()));
        audit.record("PRODUCTION_CYCLE", cycle.getId(), "START",
                Map.of("status", previous), Map.of("status", cycle.getStatus()),
                trim(request.observation()));
        return toCycleResponse(cycle);
    }

    @Transactional
    public ProductionDtos.CycleResponse completeCycle(UUID id,
                                                       ProductionDtos.CompleteCycleRequest request) {
        ProductionCycle cycle = lockCycle(id);
        ProductionMachine machine = machines.findByIdForUpdate(cycle.getMachine().getId())
                .orElseThrow(() -> notFound("PRODUCTION_MACHINE_NOT_FOUND", "Máquina inexistente"));
        if (request.actualWeightGrams() > machine.getCapacityGrams()) {
            throw unprocessable("PRODUCTION_MACHINE_CAPACITY_EXCEEDED",
                    "El peso real supera la capacidad de la máquina");
        }
        ProductionCycleStatus previous = cycle.getStatus();
        try {
            cycle.complete(request.actualWeightGrams(), OffsetDateTime.now());
        } catch (IllegalStateException ex) {
            throw conflict("PRODUCTION_CYCLE_NOT_COMPLETABLE", ex.getMessage());
        }
        for (ProductionCycleOrder assignment : cycle.getOrders()) {
            LaundryOrder order = orders.findByIdForUpdate(assignment.getOrder().getId())
                    .orElseThrow(() -> notFound("ORDER_NOT_FOUND", "Pedido inexistente"));
            if (cycle.getProgram().getStage() == ProductionStage.WASH) {
                OrderTreatmentProfile profile = profiles.findByOrderId(order.getId())
                        .orElseThrow(() -> unprocessable("TREATMENT_PROFILE_NOT_FOUND",
                                "Falta el perfil de tratamiento del pedido"));
                changeOrderStatus(order, profile.isDryerAllowed()
                                ? OrderStatus.WAITING_DRY : OrderStatus.QUALITY_CONTROL,
                        "Lavado completado en " + cycle.getCycleNumber());
            } else {
                changeOrderStatus(order, OrderStatus.QUALITY_CONTROL,
                        "Secado completado en " + cycle.getCycleNumber());
            }
        }
        recordCycleStatus(cycle, previous, cycle.getStatus(), trim(request.observation()));
        audit.record("PRODUCTION_CYCLE", cycle.getId(), "COMPLETE",
                Map.of("status", previous), cycleSummary(cycle), trim(request.observation()));
        return toCycleResponse(cycle);
    }

    @Transactional
    public ProductionDtos.CycleResponse cancelCycle(UUID id, ProductionDtos.CycleActionRequest request) {
        ProductionCycle cycle = lockCycle(id);
        ProductionCycleStatus previous = cycle.getStatus();
        try {
            cycle.cancel(OffsetDateTime.now());
        } catch (IllegalStateException ex) {
            throw conflict("PRODUCTION_CYCLE_NOT_CANCELLABLE", ex.getMessage());
        }
        recordCycleStatus(cycle, previous, cycle.getStatus(), trim(request.observation()));
        audit.record("PRODUCTION_CYCLE", cycle.getId(), "CANCEL",
                Map.of("status", previous), Map.of("status", cycle.getStatus()),
                trim(request.observation()));
        return toCycleResponse(cycle);
    }

    @Transactional
    public ProductionDtos.CycleOrderResponse qualityControl(UUID orderId,
                                                             ProductionDtos.QualityControlRequest request) {
        LaundryOrder order = orders.findByIdForUpdate(orderId)
                .orElseThrow(() -> notFound("ORDER_NOT_FOUND", "Pedido inexistente"));
        if (order.getStatus() != OrderStatus.QUALITY_CONTROL) {
            throw unprocessable("ORDER_NOT_IN_QUALITY_CONTROL",
                    "El pedido no está en control de calidad");
        }
        OrderStatus target = request.decision() == QualityDecision.PASS
                ? OrderStatus.FOLDING : OrderStatus.REWASH_REQUIRED;
        changeOrderStatus(order, target, request.observation());
        audit.record("ORDER", order.getId(), "QUALITY_CONTROL",
                Map.of("status", OrderStatus.QUALITY_CONTROL),
                Map.of("status", target, "decision", request.decision()), request.observation());
        return new ProductionDtos.CycleOrderResponse(order.getId(), order.getOrderNumber(),
                0, order.getActualWeightGrams(), false, order.getStatus().name());
    }

    private void validateMachineAndProgram(ProductionMachine machine, ProductionProgram program) {
        if (!machine.isAvailable()) {
            throw unprocessable("PRODUCTION_MACHINE_UNAVAILABLE", "La máquina no está disponible");
        }
        if (!program.isActive()) {
            throw unprocessable("PRODUCTION_PROGRAM_INACTIVE", "El programa no está activo");
        }
        if (machine.getMachineType() != program.getRequiredMachineType()) {
            throw unprocessable("PRODUCTION_MACHINE_PROGRAM_MISMATCH",
                    "El programa no corresponde al tipo de máquina");
        }
    }

    private void validateOrderStage(LaundryOrder order, ProductionStage stage) {
        Set<OrderStatus> allowed = stage == ProductionStage.WASH
                ? Set.of(OrderStatus.CLASSIFIED, OrderStatus.WAITING_WASH, OrderStatus.REWASH_REQUIRED)
                : Set.of(OrderStatus.WAITING_DRY);
        if (!allowed.contains(order.getStatus())) {
            throw unprocessable("ORDER_NOT_READY_FOR_PRODUCTION_STAGE",
                    "El pedido no está listo para la etapa " + stage);
        }
    }

    private void prepareOrderForPlanning(LaundryOrder order, ProductionStage stage) {
        if (stage == ProductionStage.WASH
                && (order.getStatus() == OrderStatus.CLASSIFIED
                || order.getStatus() == OrderStatus.REWASH_REQUIRED)) {
            changeOrderStatus(order, OrderStatus.WAITING_WASH, "Asignación a ciclo de lavado");
        }
    }

    private CompatibilityEvaluation currentEvaluation(List<LaundryOrder> lockedOrders,
                                                        List<OrderTreatmentProfile> lockedProfiles) {
        LaundryOrder firstOrder = lockedOrders.get(0);
        LaundryOrder secondOrder = lockedOrders.get(1);
        OrderTreatmentProfile firstProfile = lockedProfiles.get(0);
        OrderTreatmentProfile secondProfile = lockedProfiles.get(1);
        if (canonical(firstOrder.getId()).compareTo(canonical(secondOrder.getId())) > 0) {
            LaundryOrder orderSwap = firstOrder;
            firstOrder = secondOrder;
            secondOrder = orderSwap;
            OrderTreatmentProfile profileSwap = firstProfile;
            firstProfile = secondProfile;
            secondProfile = profileSwap;
        }
        UUID firstId = firstOrder.getId();
        UUID secondId = secondOrder.getId();
        long firstVersion = firstProfile.getVersion();
        long secondVersion = secondProfile.getVersion();
        Specification<CompatibilityEvaluation> specification = (root, query, builder) -> builder.and(
                builder.equal(root.get("orderA").get("id"), firstId),
                builder.equal(root.get("orderB").get("id"), secondId),
                builder.equal(root.get("profileAVersion"), firstVersion),
                builder.equal(root.get("profileBVersion"), secondVersion),
                builder.equal(root.get("ruleVersion"), CompatibilityEngine.RULE_VERSION));
        CompatibilityEvaluation found = evaluations.findOne(specification)
                .orElseThrow(() -> unprocessable("CURRENT_COMPATIBILITY_EVALUATION_REQUIRED",
                        "Debe evaluar la compatibilidad con los perfiles vigentes"));
        return evaluations.findByIdForUpdate(found.getId())
                .orElseThrow(() -> notFound("COMPATIBILITY_EVALUATION_NOT_FOUND",
                        "Evaluación de compatibilidad inexistente"));
    }

    private void validateIdempotentReplay(ProductionCycle existing,
                                          ProductionDtos.CreateCycleRequest request) {
        Set<UUID> existingOrders = existing.getOrders().stream()
                .map(value -> value.getOrder().getId()).collect(java.util.stream.Collectors.toSet());
        Set<UUID> requestedOrders = new LinkedHashSet<>(request.orderIds());
        if (!existing.getMachine().getId().equals(request.machineId())
                || !existing.getProgram().getId().equals(request.programId())
                || !existingOrders.equals(requestedOrders)) {
            throw conflict("IDEMPOTENCY_KEY_REUSED_WITH_DIFFERENT_PAYLOAD",
                    "La clave idempotente ya fue usada con otra planificación");
        }
    }

    private List<UUID> canonicalUniqueOrderIds(List<UUID> input) {
        LinkedHashSet<UUID> unique = new LinkedHashSet<>(input);
        if (unique.size() != input.size()) {
            throw new BusinessException("DUPLICATE_ORDER_IN_CYCLE",
                    "No puede repetirse un pedido en el ciclo", HttpStatus.BAD_REQUEST);
        }
        return unique.stream().sorted(Comparator.comparing(this::canonical)).toList();
    }

    private ProductionCycle lockCycle(UUID id) {
        return cycles.findByIdForUpdate(id)
                .orElseThrow(() -> notFound("PRODUCTION_CYCLE_NOT_FOUND", "Ciclo inexistente"));
    }

    private void changeOrderStatus(LaundryOrder order, OrderStatus target, String observation) {
        OrderStatus previous = order.getStatus();
        if (!transitionPolicy.canTransition(previous, target)) {
            throw unprocessable("INVALID_STATUS_TRANSITION",
                    "No se permite pasar de " + previous + " a " + target);
        }
        order.changeStatus(target);
        orderHistories.save(new OrderStateHistory(order, previous, target, observation, null, null));
        audit.record("ORDER", order.getId(), "STATUS_CHANGE",
                Map.of("status", previous), Map.of("status", target), observation);
    }

    private void recordCycleStatus(ProductionCycle cycle, ProductionCycleStatus previous,
                                   ProductionCycleStatus target, String observation) {
        cycleHistories.save(new ProductionCycleHistory(cycle, previous, target, observation));
    }

    private void validateProgramRequest(ProductionDtos.ProgramRequest request) {
        if (request.stage() == ProductionStage.WASH) {
            if (request.maxTemperatureC() == null || request.fragrancePolicy() == null) {
                throw new BusinessException("INVALID_WASH_PROGRAM",
                        "Un programa de lavado requiere temperatura y política de fragancia",
                        HttpStatus.BAD_REQUEST);
            }
        } else if (request.maxTemperatureC() != null || request.fragrancePolicy() != null
                || request.usesSoftener()) {
            throw new BusinessException("INVALID_DRY_PROGRAM",
                    "Un programa de secado no admite temperatura de lavado, fragancia ni suavizante",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private void acquireIdempotencyLock(String key) {
        jdbcTemplate.execute((ConnectionCallback<Void>) connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "select pg_advisory_xact_lock(hashtext(?))")) {
                statement.setString(1, key);
                statement.execute();
                return null;
            }
        });
    }

    private String nextCycleNumber() {
        Long value = jdbcTemplate.queryForObject(
                "select nextval('production_cycle_number_seq')", Long.class);
        return "PC-%06d".formatted(value);
    }

    private ProductionDtos.CycleResponse toCycleResponse(ProductionCycle cycle) {
        List<ProductionDtos.CycleOrderResponse> orderResponses = cycle.getOrders().stream()
                .sorted(Comparator.comparingInt(ProductionCycleOrder::getAssignmentOrder))
                .map(value -> new ProductionDtos.CycleOrderResponse(
                        value.getOrder().getId(), value.getOrder().getOrderNumber(),
                        value.getAssignmentOrder(), value.getAssignedWeightGrams(),
                        value.isSeparationRequired(), value.getOrder().getStatus().name()))
                .toList();
        List<ProductionDtos.CycleHistoryResponse> history = cycle.getId() == null ? List.of()
                : cycleHistories.findAllByCycleIdOrderByCreatedAtAsc(cycle.getId()).stream()
                .map(value -> new ProductionDtos.CycleHistoryResponse(
                        value.getPreviousStatus() == null ? null : value.getPreviousStatus().name(),
                        value.getNewStatus().name(), value.getObservation(),
                        value.getCreatedAt(), value.getCreatedBy()))
                .toList();
        return new ProductionDtos.CycleResponse(cycle.getId(), cycle.getCycleNumber(), cycle.getStatus(),
                cycle.getMachine().getId(), cycle.getMachine().getCode(), cycle.getMachine().getMachineType(),
                cycle.getProgram().getId(), cycle.getProgram().getCode(), cycle.getProgram().getStage(),
                cycle.getPlannedWeightGrams(), cycle.getActualWeightGrams(), cycle.getNotes(),
                cycle.getStartedAt(), cycle.getCompletedAt(), cycle.getCancelledAt(), cycle.getCreatedAt(),
                orderResponses, history);
    }

    private ProductionDtos.MachineResponse toMachineResponse(ProductionMachine machine) {
        return new ProductionDtos.MachineResponse(machine.getId(), machine.getCode(), machine.getName(),
                machine.getMachineType(), machine.getCapacityGrams(), machine.getStatus(),
                machine.isActive(), machine.getNotes(), machine.getVersion());
    }

    private ProductionDtos.ProgramResponse toProgramResponse(ProductionProgram program) {
        return new ProductionDtos.ProgramResponse(program.getId(), program.getCode(), program.getName(),
                program.getStage(), program.getRequiredMachineType(), program.getDurationMinutes(),
                program.getMaxTemperatureC(), program.isGentle(), program.isUsesSoftener(),
                program.getFragrancePolicy(), program.isActive(), program.getNotes(), program.getVersion());
    }

    private Map<String, Object> machineSummary(ProductionMachine machine) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("code", machine.getCode());
        value.put("type", machine.getMachineType());
        value.put("capacityGrams", machine.getCapacityGrams());
        value.put("status", machine.getStatus());
        value.put("active", machine.isActive());
        return value;
    }

    private Map<String, Object> programSummary(ProductionProgram program) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("code", program.getCode());
        value.put("stage", program.getStage());
        value.put("durationMinutes", program.getDurationMinutes());
        value.put("temperature", program.getMaxTemperatureC());
        value.put("gentle", program.isGentle());
        value.put("softener", program.isUsesSoftener());
        value.put("fragrance", program.getFragrancePolicy());
        value.put("active", program.isActive());
        return value;
    }

    private Map<String, Object> cycleSummary(ProductionCycle cycle) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("cycleNumber", cycle.getCycleNumber());
        value.put("status", cycle.getStatus());
        value.put("machineCode", cycle.getMachine().getCode());
        value.put("programCode", cycle.getProgram().getCode());
        value.put("plannedWeightGrams", cycle.getPlannedWeightGrams());
        value.put("actualWeightGrams", cycle.getActualWeightGrams());
        value.put("orders", cycle.getOrders().stream()
                .map(assignment -> assignment.getOrder().getId()).toList());
        return value;
    }

    private String normalizeCode(String value) {
        return value.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_]+", "_");
    }

    private String normalizeIdempotencyKey(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("IDEMPOTENCY_KEY_REQUIRED",
                    "Idempotency-Key es obligatorio", HttpStatus.BAD_REQUEST);
        }
        String normalized = value.trim();
        if (normalized.length() < 8 || normalized.length() > 120) {
            throw new BusinessException("INVALID_IDEMPOTENCY_KEY",
                    "Idempotency-Key debe contener entre 8 y 120 caracteres", HttpStatus.BAD_REQUEST);
        }
        return normalized;
    }

    private String canonical(UUID value) {
        return value.toString();
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private BusinessException notFound(String code, String message) {
        return new BusinessException(code, message, HttpStatus.NOT_FOUND);
    }

    private BusinessException conflict(String code, String message) {
        return new BusinessException(code, message, HttpStatus.CONFLICT);
    }

    private BusinessException unprocessable(String code, String message) {
        return new BusinessException(code, message, HttpStatus.UNPROCESSABLE_ENTITY);
    }
}
