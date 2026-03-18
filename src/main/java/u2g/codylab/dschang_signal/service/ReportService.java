package u2g.codylab.dschang_signal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;
import u2g.codylab.dschang_signal.dto.ReportRequestApiDTO;
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
    private final UserService userService;
    private final I18nService i18nService;
    private final CategoryRepository categoryRepository;

    public ReportService(ReportRepository reportRepository,
                         ReportMapper reportMapper,
                         UserService userService,
                         I18nService i18nService,
                         CategoryRepository categoryRepository) {
        this.reportRepository = reportRepository;
        this.reportMapper = reportMapper;
        this.userService = userService;
        this.i18nService = i18nService;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ReportApiDTO createReport(ReportRequestApiDTO dto, User currentUser) {
        log.debug("Creating report: {}", dto.getTitle());

        if (reportRepository.existsByTitleAndLocationText(
                dto.getTitle(), dto.getLocationText())) {
            throw new ConflictException(
                    "A report with the same title and location already exists"
            );
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

    public Page<ReportApiDTO> getPublicReports(Pageable pageable) {
        log.debug("Request to fetch all reports by page {}", pageable);
        try {
            Page<ReportApiDTO> dtos = reportRepository
                    .findByModerationStatus(ModerationStatus.RESOLVED, pageable)
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
            Page<ReportApiDTO> reportsDTO = reports.map(reportMapper::toReportDTO);
            log.debug("Reports found: {}", reportsDTO);
            return reportsDTO;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BadRequestException("Error occurred while fetching reports");
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
            throw new BadRequestException(
                    "Report cannot be deleted because its status is not PENDING"
            );
        }

        if (report.getMedia() != null) {
            report.getMedia().forEach(media -> media.setReport(null));
            report.getMedia().clear();
        }

        reportRepository.delete(report);
    }
}