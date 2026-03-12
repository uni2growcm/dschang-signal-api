package u2g.codylab.dschang_signal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(name = "location_text")
    private String locationText;

    @Enumerated(EnumType.STRING)
    @Column(name = "moderation_status", nullable = false)
    private ModerationStatus moderationStatus;

    @Column(name = "report_status")
    private String reportStatus;

    @Column(name = "rejection_reason")
    private  String rejectionReason;

    @Column(updatable = false, nullable = false)
    private Timestamp createdAt;

    @Column(name = "reviewed_at")
    private Timestamp reviewedAt;

    @Column(updatable = true, nullable = false)
    private Timestamp updatedAt;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "report")
    private List<Media> media;

    @ManyToMany
    @JoinTable(
            name = "report_category",
            joinColumns = @JoinColumn(name = "report_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();
}
