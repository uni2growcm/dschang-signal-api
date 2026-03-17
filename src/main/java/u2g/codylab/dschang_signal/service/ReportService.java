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
import u2g.codylab.dschang_signal.dto.UpdateReportProgressRequestApiDTO;
import u2g.codylab.dschang_signal.entity.ModerationStatus;
import u2g.codylab.dschang_signal.entity.Report;
import u2g.codylab.dschang_signal.entity.ReportStatus;
import u2g.codylab.dschang_signal.entity.User;
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
                        HttpStatus.NOT_FOUND, "Report with id " + id + " not found!"
                ));
        log.debug("Report with id {} found", id);
        return reportMapper.toReportDTO(report);
    }

    @Transactional(readOnly = true)
    public Page<ReportApiDTO> getPublicReports(Pageable pageable) {
        log.debug("Request to fetch all reports by page {}", pageable);
        try {
            Page<ReportApiDTO> dtos = reportRepository.findByModerationStatus(ModerationStatus.ACCEPTED, pageable)
                    .map(reportMapper::toReportDTO);
            log.debug("Found {} reports by page {}", dtos.getTotalElements(), pageable);
            return dtos;
        } catch (Exception e) {
            throw new BadRequestException("Invalid pagination parameters");
        }
    }

    @Transactional(readOnly = true)
    public Page<ReportApiDTO> getAllReports(Pageable pageable) {
        log.debug("Request to get all reports");
        try {
            Page<Report> reports = reportRepository.findAll(pageable);
            Page<ReportApiDTO> reportsDTO = reports
                    .map(reportMapper::toReportDTO);
            log.debug("Reports found: {}", reportsDTO);
            return reportsDTO;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BadRequestException("Error occurred while fetching reports");
        }
    }

    @Transactional
    public ReportApiDTO updateReportStatus(Long id, UpdateReportStatusRequestApiDTO request) {
        log.debug("Request to update moderation status of report with id {}", id);

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "The report with id " + id + " does not exist."
                ));

        ModerationStatus currentModStatus = report.getModerationStatus();
        ModerationStatus newModStatus = ModerationStatus.valueOf(request.getStatus().getValue());

        if (currentModStatus != ModerationStatus.PENDING_REVIEW) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot update moderation status when current status is " + currentModStatus
            );
        }

        if (newModStatus == ModerationStatus.ACCEPTED) {
            // Rien à faire de plus, on va juste mettre à jour le statut
        } else if (newModStatus == ModerationStatus.REJECTED) {
            if (request.getRejectionReason() == null || request.getRejectionReason().isBlank()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "A rejection reason is required when rejecting a report."
                );
            }
            report.setRejectionReason(request.getRejectionReason());
            report.setReportStatus(ReportStatus.REJECTED);
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid moderation status transition from " + currentModStatus + " to " + newModStatus
            );
        }

        report.setModerationStatus(newModStatus);
        report.setUpdatedAt(OffsetDateTime.now());
        if (newModStatus == ModerationStatus.ACCEPTED || newModStatus == ModerationStatus.REJECTED) {
            report.setReviewedAt(OffsetDateTime.now());
        }

        Report updated = reportRepository.save(report);
        log.debug("Report with id {} moderation status updated to {}", id, updated.getModerationStatus());
        return reportMapper.toReportDTO(updated);
    }

    @Transactional
    public ReportApiDTO updateReportProgress(Long id, ReportStatus newStatus) {
        log.debug("Request to update progress status of report with id {} to {}", id, newStatus);

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "The report with id " + id + " does not exist."
                ));

        ModerationStatus currentModStatus = report.getModerationStatus();
        ReportStatus currentRepStatus = report.getReportStatus();

        if (currentModStatus != ModerationStatus.ACCEPTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot update progress status when moderation status is " + currentModStatus + ". Report must be ACCEPTED first."
            );
        }

        if (currentRepStatus == ReportStatus.REJECTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot update progress status of a rejected report"
            );
        }

        if (newStatus == ReportStatus.IN_PROGRESS) {
            if (currentRepStatus != ReportStatus.PENDING) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cannot move to IN_PROGRESS from " + currentRepStatus
                );
            }
        } else if (newStatus == ReportStatus.RESOLVED) {
            if (currentRepStatus != ReportStatus.IN_PROGRESS) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cannot move to RESOLVED from " + currentRepStatus
                );
            }
        } else if (newStatus == ReportStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot revert to PENDING from " + currentRepStatus
            );
        } else if (newStatus == ReportStatus.REJECTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Use the moderation status endpoint to reject a report"
            );
        }

        report.setReportStatus(newStatus);
        report.setUpdatedAt(OffsetDateTime.now());

        Report updated = reportRepository.save(report);
        log.debug("Report with id {} progress status updated to {}", id, updated.getReportStatus());

        return reportMapper.toReportDTO(updated);
    }
}