package ar.com.ropalista.compatibility.domain;

import ar.com.ropalista.common.domain.AuditableEntity;
import ar.com.ropalista.order.domain.LaundryOrder;
import ar.com.ropalista.reception.domain.OrderReception;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "order_treatment_profiles")
public class OrderTreatmentProfile extends AuditableEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private LaundryOrder order;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reception_id", nullable = false, unique = true)
    private OrderReception reception;

    @Enumerated(EnumType.STRING)
    @Column(name = "color_group", nullable = false, length = 30)
    private ColorGroup colorGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_group", nullable = false, length = 30)
    private MaterialGroup materialGroup;

    @Column(name = "max_temperature_c", nullable = false)
    private int maxTemperatureC;

    @Column(name = "dryer_allowed", nullable = false)
    private boolean dryerAllowed;

    @Enumerated(EnumType.STRING)
    @Column(name = "fragrance_policy", nullable = false, length = 30)
    private FragrancePolicy fragrancePolicy;

    @Column(name = "softener_allowed", nullable = false)
    private boolean softenerAllowed;

    @Column(nullable = false)
    private boolean hypoallergenic;

    @Column(name = "baby_clothes", nullable = false)
    private boolean babyClothes;

    @Column(name = "pet_contact", nullable = false)
    private boolean petContact;

    @Column(name = "heavy_soil", nullable = false)
    private boolean heavySoil;

    @Column(name = "exclusive_cycle", nullable = false)
    private boolean exclusiveCycle;

    @Column(length = 1000)
    private String notes;

    public OrderTreatmentProfile(LaundryOrder order, OrderReception reception, ColorGroup colorGroup,
                                 MaterialGroup materialGroup, int maxTemperatureC, boolean dryerAllowed,
                                 FragrancePolicy fragrancePolicy, boolean softenerAllowed,
                                 boolean hypoallergenic, boolean babyClothes, boolean petContact,
                                 boolean heavySoil, boolean exclusiveCycle, String notes) {
        this.order = order;
        this.reception = reception;
        update(colorGroup, materialGroup, maxTemperatureC, dryerAllowed, fragrancePolicy,
                softenerAllowed, hypoallergenic, babyClothes, petContact, heavySoil,
                exclusiveCycle, notes);
    }

    public void update(ColorGroup colorGroup, MaterialGroup materialGroup, int maxTemperatureC,
                       boolean dryerAllowed, FragrancePolicy fragrancePolicy,
                       boolean softenerAllowed, boolean hypoallergenic, boolean babyClothes,
                       boolean petContact, boolean heavySoil, boolean exclusiveCycle, String notes) {
        this.colorGroup = colorGroup;
        this.materialGroup = materialGroup;
        this.maxTemperatureC = maxTemperatureC;
        this.dryerAllowed = dryerAllowed;
        this.fragrancePolicy = fragrancePolicy;
        this.softenerAllowed = softenerAllowed;
        this.hypoallergenic = hypoallergenic;
        this.babyClothes = babyClothes;
        this.petContact = petContact;
        this.heavySoil = heavySoil;
        this.exclusiveCycle = exclusiveCycle;
        this.notes = notes;
    }
}
