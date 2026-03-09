package codylab.dschang_signal.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.AnyDiscriminatorImplicitValues;
import org.hibernate.type.descriptor.jdbc.TimestampWithTimeZoneJdbcType;
import java.security.Timestamp;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column( unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String full_name;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private TimestampWithTimeZoneJdbcType created_ad;

    @Column(name = "is_active", nullable = false)
    private Boolean is_active;

}
