package u2g.codylab.dschang_signal.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import u2g.codylab.dschang_signal.entity.ModerationStatus;
import u2g.codylab.dschang_signal.entity.Report;

import java.util.List;


@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByModerationStatus (ModerationStatus moderationStatus, Pageable pageable);

    List<Report> id(Long id);
}