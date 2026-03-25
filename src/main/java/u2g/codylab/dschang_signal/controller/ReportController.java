package u2g.codylab.dschang_signal.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import u2g.codylab.dschang_signal.dto.UpdateModerationStatusRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UpdateReportStatusRequestApiDTO;
import u2g.codylab.dschang_signal.api.ReportApi;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;
import u2g.codylab.dschang_signal.dto.ReportRequestApiDTO;
import u2g.codylab.dschang_signal.entity.ReportStatus;
import u2g.codylab.dschang_signal.entity.User;
import u2g.codylab.dschang_signal.exception.NotFoundException;
import u2g.codylab.dschang_signal.repository.UserRepository;
import u2g.codylab.dschang_signal.service.ReportService;
import u2g.codylab.dschang_signal.service.UserService;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
public class ReportController implements ReportApi {

    private final ReportService reportService;
    private final UserService userService;
    private final UserRepository userRepository;

    public ReportController(ReportService reportService, UserService userService, UserRepository userRepository) {
        this.reportService = reportService;
        this.userService = userService;
        this.userRepository = userRepository;

    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReportApiDTO> createReport(ReportRequestApiDTO reportRequestApiDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getUserEntityByEmail(email);
        ReportApiDTO created = reportService.createReport(reportRequestApiDTO, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportApiDTO>> getAllReports(
            Integer page, Integer size, String sort,
            String moderationStatus, String reportStatus,
            String category, OffsetDateTime fromDate, OffsetDateTime toDate) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<ReportApiDTO> reports = reportService.getAllReports(pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count",  String.valueOf(reports.getTotalElements()));
        headers.add("X-Page-Size",    String.valueOf(reports.getSize()));
        headers.add("X-Page-Number",  String.valueOf(reports.getNumber()));
        return new ResponseEntity<>(reports.getContent(),headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ReportApiDTO> getReportById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @Override
    public ResponseEntity<List<ReportApiDTO>> getPublicReports(
            Integer page, Integer size, String sort, String category, String status) {
        String sortField = "created_at".equals(sort) ? "createdAt" : sort != null ? sort : "createdAt";
        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20,
                Sort.by(sortField).descending()
        );
        Page<ReportApiDTO> reports = reportService.getPublicReports(pageable);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(reports.getTotalElements()));
        headers.add("X-Page-Size", String.valueOf(reports.getSize()));
        headers.add("X-Page-Number", String.valueOf(reports.getNumber()));
        return new ResponseEntity<>(reports.getContent(), headers, HttpStatus.OK);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReportApiDTO> updateReport(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReportRequestApiDTO reportRequestApiDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getUserEntityByEmail(email);
        return ResponseEntity.ok(reportService.updateReport(id, reportRequestApiDTO, currentUser));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportApiDTO> updateModerationStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateModerationStatusRequestApiDTO updateModerationStatusRequest) {
        return ResponseEntity.ok(reportService.updateModerationStatus(id, updateModerationStatusRequest));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportApiDTO> updateReportStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateReportStatusRequestApiDTO updateReportStatusRequest) {

        String statusValue = updateReportStatusRequest.getStatus().getValue();
        ReportStatus reportStatus = ReportStatus.valueOf(statusValue);

        return ResponseEntity.ok(reportService.updateReportStatus(id, reportStatus));
    }

    @Override
    public ResponseEntity<List<ReportApiDTO>> getMyReports(
            Integer page,
            Integer size,
            String sort
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));

        String sortField = "created_at".equals(sort) ? "createdAt" : sort != null ? sort : "createdAt";
        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20,
                Sort.by(sortField).descending()
        );
        Page<ReportApiDTO> reports = reportService.getMyReports(currentUser, pageable);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count",  String.valueOf(reports.getTotalElements()));
        headers.add("X-Page-Size",    String.valueOf(reports.getSize()));
        headers.add("X-Page-Number",  String.valueOf(reports.getNumber()));
        return new ResponseEntity<>(reports.getContent(),headers, HttpStatus.OK);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteReport(@PathVariable("id") Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getUserEntityByEmail(email);
        reportService.deleteReport(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}