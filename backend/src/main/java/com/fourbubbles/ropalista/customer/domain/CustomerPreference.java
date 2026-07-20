package com.fourbubbles.ropalista.customer.domain;

import com.fourbubbles.ropalista.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customer_preference")
public class CustomerPreference extends AuditableEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    private Customer customer;

    @Column(length = 80)
    private String fragrance;

    @Column(name = "fragrance_intensity", length = 30)
    private String fragranceIntensity;

    @Column(name = "soap_type", length = 80)
    private String soapType;

    @Column(name = "softener_allowed", nullable = false)
    private boolean softenerAllowed = true;

    @Column(name = "allergy_notes", columnDefinition = "text")
    private String allergyNotes;

    @Column(name = "baby_clothes", nullable = false)
    private boolean babyClothes;

    @Column(name = "dryer_allowed", nullable = false)
    private boolean dryerAllowed = true;

    @Column(name = "max_temperature_celsius")
    private Integer maxTemperatureCelsius;

    @Column(name = "color_mix_allowed", nullable = false)
    private boolean colorMixAllowed;

    @Column(name = "exclusive_cycle", nullable = false)
    private boolean exclusiveCycle;

    @Column(name = "stain_treatment", nullable = false)
    private boolean stainTreatment;

    @Column(name = "preferred_payment_method", length = 40)
    private String preferredPaymentMethod;
}
