package u2g.codylab.dschang_signal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import u2g.codylab.dschang_signal.api.ReportApi;
import u2g.codylab.dschang_signal.dto.CreateReportRequestApiDTO;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;
import u2g.codylab.dschang_signal.entity.Report;
import u2g.codylab.dschang_signal.mapper.ReportMapper;
import u2g.codylab.dschang_signal.service.ReportService;


@RestController
public class ReportController implements ReportApi {
    private final ReportService reportService;
    private final ReportMapper reportMapper;
    public ReportController(ReportService reportService, ReportMapper reportMapper){
        this.reportService = reportService;
        this.reportMapper = reportMapper;
    }

    @Override
    public ResponseEntity<ReportApiDTO> createReport(CreateReportRequestApiDTO request){
        Report report = reportService.createReport(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reportMapper.toDTO(report));
    }

}
