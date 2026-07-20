package com.fourbubbles.ropalista.pricing.domain;

import com.fourbubbles.ropalista.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "service_plan")
public class ServicePlan extends AuditableEntity {
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "max_equivalent_units", precision = 10, scale = 2)
    private BigDecimal maxEquivalentUnits;

    @Column(name = "max_weight_grams")
    private Integer maxWeightGrams;

    @Column(nullable = false)
    private boolean active;
}
