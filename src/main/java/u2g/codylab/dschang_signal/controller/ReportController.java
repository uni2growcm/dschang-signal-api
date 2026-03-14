package u2g.codylab.dschang_signal.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import u2g.codylab.dschang_signal.api.ReportApi;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;

import u2g.codylab.dschang_signal.service.ReportService;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
public class ReportController implements ReportApi {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportApiDTO>> getAllReports(Integer page, Integer size, String sort, String moderationStatus, String reportStatus, String category, OffsetDateTime fromDate, OffsetDateTime toDate) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<ReportApiDTO> reports = reportService.getAllReports(pageable);
        return new ResponseEntity<>(reports.getContent(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ReportApiDTO> getReportById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @Override
    public ResponseEntity<List<ReportApiDTO>> getPublicReports(
            Integer page,
            Integer size,
            String sort
    ) {
        String sortField = "created_at".equals(sort) ? "createdAt" : sort != null ? sort : "createdAt";

        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 20,
                Sort.by(sortField).descending()
        );
        Page<ReportApiDTO> reports = reportService.getPublicReports(pageable);
        return new ResponseEntity<>(reports.getContent(), HttpStatus.OK);
    }
}
