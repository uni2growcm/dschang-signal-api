package codylab.dschang_signal.entity;
import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;

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

    @Column(nullable = false,updatable = false)
    private Timestamp created_at;

    @Column(nullable = false,updatable = true)
    private Timestamp updatable_at;

    @Column(name = "is_active", nullable = false)
    private Boolean is_active;

}
