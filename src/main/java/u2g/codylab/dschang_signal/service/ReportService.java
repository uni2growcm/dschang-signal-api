package u2g.codylab.dschang_signal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;
import u2g.codylab.dschang_signal.dto.UpdateModerationStatusRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UpdateReportStatusRequestApiDTO;
import u2g.codylab.dschang_signal.entity.ModerationStatus;
import u2g.codylab.dschang_signal.entity.Report;
import u2g.codylab.dschang_signal.entity.ReportStatus;
import u2g.codylab.dschang_signal.entity.User;
import u2g.codylab.dschang_signal.exception.BadRequestException;
import u2g.codylab.dschang_signal.exception.NotFoundException;
import u2g.codylab.dschang_signal.mapper.ReportMapper;
import u2g.codylab.dschang_signal.repository.ReportRepository;

import java.sql.Timestamp;

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
    public ReportApiDTO updateModerationStatus(Long id, UpdateModerationStatusRequestApiDTO request) {
        log.debug("Request to update moderation status of report with id {}", id);

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Report with id " + id + " does not exist."
                ));

        ModerationStatus currentModStatus = report.getModerationStatus();
        ModerationStatus newModStatus = ModerationStatus.valueOf(request.getStatus().getValue());

        if (currentModStatus != ModerationStatus.PENDING_REVIEW) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot update moderation status. Current status is " + currentModStatus +
                            ". Only PENDING_REVIEW reports can be moderated."
            );
        }

        switch (newModStatus) {
            case ACCEPTED:
                report.setModerationStatus(ModerationStatus.ACCEPTED);
                report.setReportStatus(ReportStatus.PENDING); // ← CORRIGÉ: reste PENDING, pas IN_PROGRESS
                report.setRejectionReason(null); // Effacer toute ancienne raison
                log.debug("Report {} accepted", id);
                break;

            case REJECTED:
                String rejectionReason = request.getRejectionReason();
                if (rejectionReason == null || rejectionReason.isBlank()) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "A rejection reason is required when rejecting a report."
                    );
                }
                if (rejectionReason.length() < 5 || rejectionReason.length() > 500) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Rejection reason must be between 5 and 500 characters."
                    );
                }

                report.setModerationStatus(ModerationStatus.REJECTED);
                report.setReportStatus(ReportStatus.PENDING);
                report.setRejectionReason(rejectionReason);
                log.debug("Report {} rejected with reason: {}", id, rejectionReason);
                break;

            default:
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid moderation status: " + newModStatus +
                                ". Allowed values: ACCEPTED, REJECTED"
                );
        }
        Timestamp now = new Timestamp(System.currentTimeMillis());
        report.setReviewedAt(now);
//        report.setUpdatedAt(now);

        Report updated = reportRepository.save(report);
        log.info("Report {} moderation status updated from {} to {}",
                id, currentModStatus, updated.getModerationStatus());

        return reportMapper.toReportDTO(updated);
    }

    @Transactional
    public ReportApiDTO updateReportStatus(Long id, ReportStatus newStatus) {
        log.debug("Request to update progression status of report with id {} to {}", id, newStatus);

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Report with id " + id + " does not exist."
                ));

        ModerationStatus currentModStatus = report.getModerationStatus();
        ReportStatus currentRepStatus = report.getReportStatus();

        if (currentModStatus != ModerationStatus.ACCEPTED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Cannot update progress status. Report is %s. Only ACCEPTED reports can have their status updated.",
                            currentModStatus)
            );
        }

        if (currentRepStatus == ReportStatus.RESOLVED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot update progress status of a RESOLVED report. It's already completed."
            );
        }

        validateStatusTransition(currentRepStatus, newStatus);
        report.setReportStatus(newStatus);
        report.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        Report updated = reportRepository.save(report);
        log.info("Report {} progress status updated from {} to {}",
                id, currentRepStatus, updated.getReportStatus());

        return reportMapper.toReportDTO(updated);
    }

    private void validateStatusTransition(ReportStatus current, ReportStatus target) {
        boolean isValidTransition = switch (current) {
            case PENDING -> target == ReportStatus.IN_PROGRESS;
            case IN_PROGRESS -> target == ReportStatus.RESOLVED;
            case RESOLVED -> false;
        };

        if (!isValidTransition) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Invalid status transition from %s to %s. Allowed: PENDING → IN_PROGRESS → RESOLVED",
                            current, target)
            );
        }
    }

    @Transactional
    public void deleteReport(Long id, User currentUser) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Report not found with id: " + id));

        if (!report.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the author of this report");
        }

        if (!ReportStatus.PENDING.equals(report.getReportStatus())) {
            throw new BadRequestException("Report cannot be deleted because its status is not PENDING");
        }

        if (report.getMedia() != null) {
            report.getMedia().forEach(media -> media.setReport(null));
            report.getMedia().clear();
        }

        reportRepository.delete(report);
    }
}