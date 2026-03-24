package u2g.codylab.dschang_signal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;
import u2g.codylab.dschang_signal.dto.ReportRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UpdateModerationStatusRequestApiDTO;
import u2g.codylab.dschang_signal.entity.*;
import u2g.codylab.dschang_signal.exception.BadRequestException;
import u2g.codylab.dschang_signal.exception.ConflictException;
import u2g.codylab.dschang_signal.exception.ForbiddenException;
import u2g.codylab.dschang_signal.exception.NotFoundException;
import u2g.codylab.dschang_signal.mapper.ReportMapper;
import u2g.codylab.dschang_signal.repository.CategoryRepository;
import u2g.codylab.dschang_signal.repository.ReportRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;

@Slf4j
@Service
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final I18nService i18nService;
    private final CategoryRepository categoryRepository;

    public ReportService(ReportRepository reportRepository,
                         ReportMapper reportMapper,
                         I18nService i18nService,
                         CategoryRepository categoryRepository) {
        this.reportRepository = reportRepository;
        this.reportMapper = reportMapper;
        this.i18nService = i18nService;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ReportApiDTO createReport(ReportRequestApiDTO dto, User currentUser) {
        log.debug("Creating report: {}", dto.getTitle());

        if (reportRepository.existsByTitleAndLocationText(
                dto.getTitle(), dto.getLocationText())) {
            throw new ConflictException(
                    i18nService.get("report.error.exist"));
        }

        Report report = new Report();
        report.setTitle(dto.getTitle());
        report.setDescription(dto.getDescription());
        report.setLocationText(dto.getLocationText());
        report.setModerationStatus(ModerationStatus.PENDING_REVIEW);
        report.setReportStatus(ReportStatus.PENDING);
        report.setCreatedBy(currentUser);
        report.setCreatedAt(Timestamp.from(Instant.now()));
        report.setUpdatedAt(Timestamp.from(Instant.now()));

        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(dto.getCategoryIds());
            if (categories.size() != dto.getCategoryIds().size()) {
                throw new NotFoundException("One or more categories not found");
            }
            report.setCategories(new HashSet<>(categories));
        }

        Report saved = reportRepository.save(report);
        log.debug("Report created with id: {}", saved.getId());
        return reportMapper.toReportDTO(saved);
    }

    @Transactional(readOnly = true)
    public ReportApiDTO getReportById(Long id) {
        log.debug("Request to fetch report by id {}", id);
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        i18nService.get("report.error.notFound", id)
                ));
        log.debug("Report with id {} found", id);
        return reportMapper.toReportDTO(report);
    }

    @Transactional(readOnly = true)
    public ReportApiDTO getPublicReportById(Long id) {
        log.debug("Request to fetch public report by id {}", id);
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        i18nService.get("report.error.notFound", id)
                ));
        if (report.getModerationStatus() != ModerationStatus.ACCEPTED) {
            throw new NotFoundException(
                    i18nService.get("report.error.notFound", id)
            );
        }
        log.debug("Public report with id {} found", id);
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
            throw new BadRequestException(i18nService.get("report.error.pagination"));
        }

    }

    @Transactional(readOnly = true)
    public Page<ReportApiDTO> getAllReports(Pageable pageable) {
        log.debug("Request to get all reports");
        try {
            Page<Report> reports = reportRepository.findAll(pageable);
            Page<ReportApiDTO> reportsDTO = reports.map(reportMapper::toReportDTO);
            log.debug("Reports found: {}", reportsDTO);
            return reportsDTO;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BadRequestException(i18nService.get("report.error.unknown"));
        }
    }

    @Transactional
    public ReportApiDTO updateReport(Long id, ReportRequestApiDTO dto, User currentUser) {
        log.debug("Request to update report with id {}", id);

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        i18nService.get("report.error.notFound", id)
                ));

        if (!report.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new ForbiddenException(i18nService.get("category.error.forbidden"));
        }

        if (report.getModerationStatus() != ModerationStatus.PENDING_REVIEW) {
            throw new BadRequestException(
                    i18nService.get("report.error.update.moderationStatus",
                            report.getModerationStatus())
            );
        }

        if (report.getReportStatus() != ReportStatus.PENDING) {
            throw new BadRequestException(
                    i18nService.get("report.error.update.reportStatus",
                            report.getReportStatus())
            );
        }

        report.setTitle(dto.getTitle());
        report.setDescription(dto.getDescription());
        report.setLocationText(dto.getLocationText());
        report.setUpdatedAt(Timestamp.from(Instant.now()));

        if (dto.getCategoryIds() != null && !dto.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(dto.getCategoryIds());
            if (categories.size() != dto.getCategoryIds().size()) {
                throw new NotFoundException(
                        i18nService.get("report.error.categories.notFound")
                );
            }
            report.setCategories(new HashSet<>(categories));
        } else {
            report.getCategories().clear();
        }

        Report updated = reportRepository.save(report);
        log.debug("Report with id {} updated", id);
        return reportMapper.toReportDTO(updated);
    }

    @Transactional
    public ReportApiDTO updateModerationStatus(Long id, UpdateModerationStatusRequestApiDTO request) {
        log.debug("Request to update moderation status of report with id {}", id);

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        i18nService.get("report.error.notFound", id)
                ));

        ModerationStatus currentModStatus = report.getModerationStatus();
        ModerationStatus newModStatus = ModerationStatus.valueOf(request.getStatus().getValue());

        if (currentModStatus != ModerationStatus.PENDING_REVIEW) {
            throw new BadRequestException(
                    i18nService.get("report.error.moderation.invalidStatus", currentModStatus)
            );
        }

        switch (newModStatus) {
            case ACCEPTED:
                report.setModerationStatus(ModerationStatus.ACCEPTED);
                report.setReportStatus(ReportStatus.PENDING);
                report.setRejectionReason(null);
                log.debug("Report {} accepted", id);
                break;

            case REJECTED:
                String rejectionReason = request.getRejectionReason();
                if (rejectionReason == null || rejectionReason.isBlank()) {
                    throw new BadRequestException(
                            i18nService.get("report.error.moderation.rejectionReasonRequired")
                    );
                }
                if (rejectionReason.length() < 5 || rejectionReason.length() > 500) {
                    throw new BadRequestException(
                            i18nService.get("report.error.moderation.rejectionReason.length")
                    );
                }

                report.setModerationStatus(ModerationStatus.REJECTED);
                report.setReportStatus(ReportStatus.PENDING);
                report.setRejectionReason(rejectionReason);
                log.debug("Report {} rejected with reason: {}", id, rejectionReason);
                break;

            default:
                throw new BadRequestException(
                        i18nService.get("report.error.moderation.invalidValue", newModStatus)
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
                .orElseThrow(() -> new NotFoundException(
                        i18nService.get("report.error.notFound", id)
                ));

        ModerationStatus currentModStatus = report.getModerationStatus();
        ReportStatus currentRepStatus = report.getReportStatus();

        if (currentModStatus != ModerationStatus.ACCEPTED) {
            throw new BadRequestException(
                    i18nService.get("report.error.status.notAccepted",
                            currentModStatus)
            );
        }

        if (currentRepStatus == ReportStatus.RESOLVED) {
            throw new BadRequestException(
                    i18nService.get("report.error.status.alreadyResolved")
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
            throw new BadRequestException(
                    i18nService.get("report.error.status.invalidTransition", current, target)
            );
        }
    }

    @Transactional
    public void deleteReport(Long id, User currentUser) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        i18nService.get("report.error.notFound", id)
                ));

        if (!report.getCreatedBy().getId().equals(currentUser.getId())) {
            throw new ForbiddenException(i18nService.get("category.error.forbidden"));
        }

        if (!ReportStatus.PENDING.equals(report.getReportStatus())) {
            throw new BadRequestException(i18nService.get("report.error.delete.notPending"));
        }

        if (report.getMedia() != null) {
            report.getMedia().forEach(media -> media.setReport(null));
            report.getMedia().clear();
        }

        reportRepository.delete(report);
    }

    @Transactional(readOnly = true)
    public Page<ReportApiDTO> getMyReports(User currentUser, Pageable pageable) {
        log.debug("Request to fetch reports for user {}", currentUser.getEmail());

            Page<ReportApiDTO> dtos = reportRepository.findByCreatedBy(currentUser, pageable)
                    .map(reportMapper::toReportDTO);
            log.debug("Found {} reports for user {}", dtos.getTotalElements(), currentUser.getEmail());
            return dtos;

    }
}