package codylab.dschang_signal.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Timestamp;

@Data
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

    @Column(nullable = false)
    private String category;

    @Column(name = "location_text")
    private String location_text;

    @Column(name = "photo_url")
    private String photo_url;

    @Column(name = "moderation_status", nullable = false)
    private String moderation_status;

    @Column(name = "report_statuts")
    private String report_statuts;

    @Column(name = "rejection_reason")
    private  String rejection_reason;

    @Column(updatable = false, nullable = false)
    private Timestamp created_at;

    @Column(name = "reviewed_at")
    private Timestamp reviewed_at;

    @Column(updatable = true, nullable = false)
    private Timestamp updated_at;
}

