package u2g.codylab.dschang_signal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;
import u2g.codylab.dschang_signal.dto.UpdateReportStatusRequestApiDTO;
import u2g.codylab.dschang_signal.entity.ModerationStatus;
import u2g.codylab.dschang_signal.entity.Report;
import u2g.codylab.dschang_signal.exception.BadRequestException;
import u2g.codylab.dschang_signal.mapper.ReportMapper;
import u2g.codylab.dschang_signal.repository.ReportRepository;

import java.time.OffsetDateTime;

@Slf4j
@Service
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;

    public ReportService(ReportRepository reportRepository, ReportMapper reportMapper) {
        this.reportRepository = reportRepository;
        this.reportMapper = reportMapper;
    }

    @Transactional(readOnly = true)
    public ReportApiDTO getReportById(Long id) {
        log.debug("Request to fetch report by id {}", id);
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "The report with id " + id + " does not exist."
                ));
        log.debug("Report with id {} found", id);
        return reportMapper.toReportDTO(report);
    }

    @Transactional(readOnly = true)
    public Page<ReportApiDTO> getPublicReports(Pageable pageable) {
        log.debug("Request to fetch public (RESOLVED) reports");
        return reportRepository.findByModerationStatus(ModerationStatus.RESOLVED, pageable)
                .map(reportMapper::toReportDTO);
    }

    @Transactional
    public ReportApiDTO updateReportStatus(Long id, UpdateReportStatusRequestApiDTO request) {
        log.debug("Request to update status of report with id {}", id);

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "The report with id " + id + " does not exist."
                ));

        ModerationStatus currentStatus = report.getModerationStatus();
        ModerationStatus newStatus = ModerationStatus.valueOf(request.getStatus().getValue());

        if (currentStatus == ModerationStatus.RESOLVED && newStatus == ModerationStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status transition: cannot move from RESOLVED back to PENDING."
            );
        }
        if (currentStatus == ModerationStatus.REJECTED && newStatus == ModerationStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status transition: cannot move from REJECTED back to PENDING."
            );
        }

        if (currentStatus == ModerationStatus.REJECTED && newStatus == ModerationStatus.RESOLVED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid status transition: cannot move from REJECTED back to RESOLVED."
            );
        }

        if (newStatus == ModerationStatus.REJECTED) {
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A rejection reason is required when rejecting a report."
                );
            }
            report.setRejectionReason(request.getRejectionReason());
        } else {
            report.setRejectionReason(null);
        }

        report.setModerationStatus(newStatus);
        report.setReviewedAt(OffsetDateTime.now());
        report.setUpdatedAt(OffsetDateTime.now());

        Report updated = reportRepository.save(report);
        log.debug("Report with id {} status updated to {}", id, updated.getModerationStatus());
        return reportMapper.toReportDTO(updated);
    }
}