package u2g.codylab.dschang_signal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import u2g.codylab.dschang_signal.entity.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> { }