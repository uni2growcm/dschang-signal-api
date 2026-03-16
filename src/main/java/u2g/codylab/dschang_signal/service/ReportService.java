package u2g.codylab.dschang_signal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;
import u2g.codylab.dschang_signal.dto.UserApiDTO;
import u2g.codylab.dschang_signal.entity.ModerationStatus;
import u2g.codylab.dschang_signal.entity.Report;
import u2g.codylab.dschang_signal.entity.ReportStatus;
import u2g.codylab.dschang_signal.entity.User;
import u2g.codylab.dschang_signal.exception.BadRequestException;
import u2g.codylab.dschang_signal.exception.NotFoundException;
import u2g.codylab.dschang_signal.mapper.ReportMapper;
import u2g.codylab.dschang_signal.repository.ReportRepository;

@Slf4j
@Service
@Transactional
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final UserService userService;

    public ReportService(ReportRepository reportRepository, ReportMapper reportMapper, UserService userService) {
        this.reportRepository = reportRepository;
        this.reportMapper = reportMapper;
        this.userService = userService;
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

    public Page<ReportApiDTO> getPublicReports(Pageable pageable) {
        log.debug("Request to fetch all reports by page {}", pageable);
        try {
            Page<ReportApiDTO> dtos = reportRepository.findByModerationStatus(ModerationStatus.RESOLVED, pageable)
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