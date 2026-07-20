package ar.com.ropalista.customer.domain;

import ar.com.ropalista.common.domain.AuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "clients")
public class Client extends AuditableEntity {
    @Column(nullable = false, length = 100)
    private String firstName;
    @Column(nullable = false, length = 100)
    private String lastName;
    @Column(nullable = false, length = 30)
    private String phone;
    @Column(nullable = false, length = 30)
    private String whatsapp;
    @Column(length = 160)
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClientStatus status = ClientStatus.ACTIVE;
    @Column(name = "acquisition_source", length = 100)
    private String acquisitionSource;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "preferences_json", columnDefinition = "jsonb")
    private String preferencesJson = "{}";
    @Column(length = 2000)
    private String notes;
    @Column(name = "deleted_at")
    private java.time.OffsetDateTime deletedAt;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    public Client(String firstName, String lastName, String phone, String whatsapp, String email,
                  String acquisitionSource, String preferencesJson, String notes) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.whatsapp = whatsapp;
        this.email = email;
        this.acquisitionSource = acquisitionSource;
        this.preferencesJson = preferencesJson == null || preferencesJson.isBlank() ? "{}" : preferencesJson;
        this.notes = notes;
    }

    public void addAddress(Address address) {
        address.attachTo(this);
        addresses.add(address);
    }
}
