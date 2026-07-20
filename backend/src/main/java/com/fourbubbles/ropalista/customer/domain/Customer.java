package com.fourbubbles.ropalista.customer.domain;

import com.fourbubbles.ropalista.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "customer")
public class Customer extends AuditableEntity {
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 40)
    private String phone;

    @Column(length = 40)
    private String whatsapp;

    @Column(length = 180)
    private String email;

    @Column(length = 30)
    private String document;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @Column(name = "acquisition_source", length = 100)
    private String acquisitionSource;

    @Column(name = "referred_by_customer_id")
    private UUID referredByCustomerId;

    @Column(columnDefinition = "text")
    private String notes;
}
