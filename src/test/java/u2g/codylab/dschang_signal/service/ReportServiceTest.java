package u2g.codylab.dschang_signal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.server.ResponseStatusException;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;
import u2g.codylab.dschang_signal.entity.ModerationStatus;
import u2g.codylab.dschang_signal.entity.Report;
import u2g.codylab.dschang_signal.exception.BadRequestException;
import u2g.codylab.dschang_signal.mapper.ReportMapper;
import u2g.codylab.dschang_signal.repository.ReportRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportMapper reportMapper;

    @InjectMocks
    private ReportService reportService;

    @Test
    void shouldGetReportByIdSuccessfully() {

        Report report = new Report();
        report.setId(1L);

        ReportApiDTO dto = new ReportApiDTO();

        when(reportRepository.findById(1L))
                .thenReturn(Optional.of(report));

        when(reportMapper.toReportDTO(report))
                .thenReturn(dto);

        ReportApiDTO result = reportService.getReportById(1L);

        assertNotNull(result);

        verify(reportRepository).findById(1L);
        verify(reportMapper).toReportDTO(report);
    }

    @Test
    void shouldThrowExceptionWhenReportNotFound() {

        when(reportRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> reportService.getReportById(1L)
        );

        verify(reportRepository).findById(1L);
    }

    @Test
    void shouldGetPublicReportsSuccessfully() {

        Pageable pageable = PageRequest.of(0, 10);

        Report report = new Report();
        ReportApiDTO dto = new ReportApiDTO();

        Page<Report> page = new PageImpl<>(List.of(report));

        when(reportRepository.findByModerationStatus(
                ModerationStatus.RESOLVED,
                pageable
        )).thenReturn(page);

        when(reportMapper.toReportDTO(report)).thenReturn(dto);

        Page<ReportApiDTO> result = reportService.getPublicReports(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(reportRepository)
                .findByModerationStatus(ModerationStatus.RESOLVED, pageable);

        verify(reportMapper).toReportDTO(report);
    }

    @Test
    void shouldThrowBadRequestWhenPaginationFails() {

        Pageable pageable = PageRequest.of(0, 10);

        when(reportRepository.findByModerationStatus(
                ModerationStatus.RESOLVED,
                pageable
        )).thenThrow(new RuntimeException());

        assertThrows(
                BadRequestException.class,
                () -> reportService.getPublicReports(pageable)
        );
    }
}