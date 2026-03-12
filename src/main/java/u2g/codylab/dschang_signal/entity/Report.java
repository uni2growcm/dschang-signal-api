package u2g.codylab.dschang_signal.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "reports")
public class Report {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(name = "location_text")
    private String locationText;

    @Column(name = "photo_url")
    private String photoUrl;

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
}
