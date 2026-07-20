package ar.com.ropalista.location.domain;

import ar.com.ropalista.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "zones")
public class Zone extends AuditableEntity {
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @Column(nullable = false, length = 120)
    private String name;
    @Column(nullable = false, length = 120)
    private String locality;
    @Column(nullable = false)
    private boolean active = true;
}
