package u2g.codylab.dschang_signal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import u2g.codylab.dschang_signal.entity.ModerationStatus;
import u2g.codylab.dschang_signal.entity.Report;
import u2g.codylab.dschang_signal.entity.User;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByModerationStatus (ModerationStatus moderationStatus, Pageable pageable);
    boolean existsByTitleAndLocationText(String title, String locationText);

    @EntityGraph(attributePaths = {"createdBy"})
    Page<Report> findByCreatedBy(User createdBy, Pageable pageable);
}