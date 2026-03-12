package u2g.codylab.dschang_signal.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;
import u2g.codylab.dschang_signal.dto.UpdateReportStatusRequestApiDTO;
import u2g.codylab.dschang_signal.entity.ModerationStatus;
import u2g.codylab.dschang_signal.entity.Report;
import u2g.codylab.dschang_signal.mapper.ReportMapper;
import u2g.codylab.dschang_signal.repository.ReportRepository;
import java.time.OffsetDateTime;


@Slf4j
@Transactional
@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;

    public ReportService(ReportRepository reportRepository, ReportMapper reportMapper) {
        this.reportRepository = reportRepository;
        this.reportMapper = reportMapper;
    }

    public ReportApiDTO getReportById(Long id) {
        log.debug("Request to fetch report by id {}", id);
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "The report which " + id + " does not exist."
                ));
        log.debug("Report with id {} found", id);
        return reportMapper.toReportDTO(report);
    }

    public ReportApiDTO updateReportStatus(Long id, UpdateReportStatusRequestApiDTO request) {
        log.debug("Request to update status of report with id {}", id);
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "The report with id " + id + " does not exist."
                ));
        report.setModerationStatus(ModerationStatus.valueOf(request.getStatus().getValue()));
        report.setReviewedAt(OffsetDateTime.now());
        report.setUpdatedAt(OffsetDateTime.now());
        Report updated = reportRepository.save(report);
        log.debug("Report with id {} status updated to {}", id, updated.getModerationStatus());
        return reportMapper.toReportDTO(updated);
    }
}