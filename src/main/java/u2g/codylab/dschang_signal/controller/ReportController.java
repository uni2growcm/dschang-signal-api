package u2g.codylab.dschang_signal.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import u2g.codylab.dschang_signal.api.ReportApi;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;
import u2g.codylab.dschang_signal.dto.UpdateReportStatusRequestApiDTO;
import u2g.codylab.dschang_signal.service.ReportService;

@RestController
public class ReportController implements ReportApi {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public ResponseEntity<ReportApiDTO> getReportById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @Override
    public ResponseEntity<ReportApiDTO> updateReportStatus(@PathVariable("id") Long id,
                                                           @Valid @RequestBody UpdateReportStatusRequestApiDTO request) {
        return ResponseEntity.ok(reportService.updateReportStatus(id, request));
    }
}