package u2g.codylab.dschang_signal.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import u2g.codylab.dschang_signal.dto.ReportResponseApiDTO;
import u2g.codylab.dschang_signal.entity.Report;
import u2g.codylab.dschang_signal.exception.BadRequestException;
import u2g.codylab.dschang_signal.mapper.ReportMapper;
import u2g.codylab.dschang_signal.repository.ReportRepository;

@Slf4j
@Transactional
@Service
public class ReportService {
    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;

    public ReportService(ReportRepository reportRepository, ReportMapper reportMapper) {
        this.reportRepository = reportRepository;
        this.reportMapper = reportMapper;
    }

    public Page<ReportResponseApiDTO> getAllReports(Pageable pageable) {
        log.debug("Request to get all reports");
        try {
            Page<Report> reports = reportRepository.findAll(pageable);
            Page<ReportResponseApiDTO> reportsDTO = reports
                    .map(reportMapper::toReportDTO);
            log.debug("Reports found: {}", reportsDTO);
            return reportsDTO;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BadRequestException("Error occurred while fetching reports");
        }
    }
}
