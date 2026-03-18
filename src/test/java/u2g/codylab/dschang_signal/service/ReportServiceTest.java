package u2g.codylab.dschang_signal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.web.server.ResponseStatusException;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;
import u2g.codylab.dschang_signal.dto.UpdateModerationStatusRequestApiDTO;
import u2g.codylab.dschang_signal.entity.ModerationStatus;
import u2g.codylab.dschang_signal.entity.Report;
import u2g.codylab.dschang_signal.entity.ReportStatus;
import u2g.codylab.dschang_signal.mapper.ReportMapper;
import u2g.codylab.dschang_signal.repository.ReportRepository;

import java.time.OffsetDateTime;
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
                ModerationStatus.ACCEPTED,
                pageable
        )).thenReturn(page);

        when(reportMapper.toReportDTO(report)).thenReturn(dto);

        Page<ReportApiDTO> result = reportService.getPublicReports(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(reportRepository)
                .findByModerationStatus(ModerationStatus.ACCEPTED, pageable);

        verify(reportMapper).toReportDTO(report);
    }

    @Test
    void shouldPropagateExceptionWhenRepositoryFails() {
        Pageable pageable = PageRequest.of(0, 10);

        when(reportRepository.findByModerationStatus(
                ModerationStatus.ACCEPTED,
                pageable
        )).thenThrow(new RuntimeException("DB error"));

        assertThrows(
                RuntimeException.class,
                () -> reportService.getPublicReports(pageable)
        );
    }

    @Test
    void shouldAcceptReportSuccessfully() {
        Long reportId = 1L;

        Report report = new Report();
        report.setId(reportId);
        report.setModerationStatus(ModerationStatus.PENDING_REVIEW);
        report.setReportStatus(ReportStatus.PENDING);

        UpdateModerationStatusRequestApiDTO request = mock(UpdateModerationStatusRequestApiDTO.class);
        UpdateModerationStatusRequestApiDTO.StatusEnum statusEnum =
                UpdateModerationStatusRequestApiDTO.StatusEnum.ACCEPTED;

        when(request.getStatus()).thenReturn(statusEnum);
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        ReportApiDTO expectedDto = new ReportApiDTO();
        when(reportMapper.toReportDTO(report)).thenReturn(expectedDto);

        ReportApiDTO result = reportService.updateModerationStatus(reportId, request);

        assertNotNull(result);
        assertEquals(ModerationStatus.ACCEPTED, report.getModerationStatus());
        assertEquals(ReportStatus.PENDING, report.getReportStatus());

        verify(reportRepository).findById(reportId);
        verify(reportRepository).save(report);
    }

    @Test
    void shouldRejectReportWithReason() {
        Long reportId = 1L;
        String rejectionReason = "Hors zone de compétence";

        Report report = new Report();
        report.setId(reportId);
        report.setModerationStatus(ModerationStatus.PENDING_REVIEW);
        report.setReportStatus(ReportStatus.PENDING);

        UpdateModerationStatusRequestApiDTO request = mock(UpdateModerationStatusRequestApiDTO.class);
        UpdateModerationStatusRequestApiDTO.StatusEnum statusEnum =
                UpdateModerationStatusRequestApiDTO.StatusEnum.REJECTED;

        when(request.getStatus()).thenReturn(statusEnum);
        when(request.getRejectionReason()).thenReturn(rejectionReason);
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenReturn(report);

        ReportApiDTO expectedDto = new ReportApiDTO();
        when(reportMapper.toReportDTO(report)).thenReturn(expectedDto);

        ReportApiDTO result = reportService.updateModerationStatus(reportId, request);

        assertNotNull(result);
        assertEquals(ModerationStatus.REJECTED, report.getModerationStatus());
        assertEquals(ReportStatus.PENDING, report.getReportStatus());
        assertEquals(rejectionReason, report.getRejectionReason());

        verify(reportRepository).findById(reportId);
        verify(reportRepository).save(report);
    }

    @Test
    void shouldThrowExceptionWhenRejectingWithoutReason() {
        Long reportId = 1L;

        Report report = new Report();
        report.setId(reportId);
        report.setModerationStatus(ModerationStatus.PENDING_REVIEW);

        UpdateModerationStatusRequestApiDTO request = mock(UpdateModerationStatusRequestApiDTO.class);
        UpdateModerationStatusRequestApiDTO.StatusEnum statusEnum =
                UpdateModerationStatusRequestApiDTO.StatusEnum.REJECTED;

        when(request.getStatus()).thenReturn(statusEnum);
        when(request.getRejectionReason()).thenReturn(null);
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        assertThrows(ResponseStatusException.class,
                () -> reportService.updateModerationStatus(reportId, request));

        verify(reportRepository, never()).save(any());
    }

    @Test
    void shouldUpdateReportProgressToInProgressSuccessfully() {
        Long reportId = 1L;
        ReportStatus newStatus = ReportStatus.IN_PROGRESS;

        Report report = new Report();
        report.setId(reportId);
        report.setModerationStatus(ModerationStatus.ACCEPTED);
        report.setReportStatus(ReportStatus.PENDING);

        ReportApiDTO expectedDto = new ReportApiDTO();

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenReturn(report);
        when(reportMapper.toReportDTO(report)).thenReturn(expectedDto);

        ReportApiDTO result = reportService.updateReportStatus(reportId, newStatus);

        assertNotNull(result);
        assertEquals(ReportStatus.IN_PROGRESS, report.getReportStatus());
        assertNotNull(report.getUpdatedAt());

        verify(reportRepository).findById(reportId);
        verify(reportRepository).save(report);
        verify(reportMapper).toReportDTO(report);
    }

    @Test
    void shouldUpdateReportProgressToResolvedSuccessfully() {
        Long reportId = 1L;
        ReportStatus newStatus = ReportStatus.RESOLVED;

        Report report = new Report();
        report.setId(reportId);
        report.setModerationStatus(ModerationStatus.ACCEPTED);
        report.setReportStatus(ReportStatus.IN_PROGRESS);

        ReportApiDTO expectedDto = new ReportApiDTO();

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenReturn(report);
        when(reportMapper.toReportDTO(report)).thenReturn(expectedDto);

        ReportApiDTO result = reportService.updateReportStatus(reportId, newStatus);

        assertNotNull(result);
        assertEquals(ReportStatus.RESOLVED, report.getReportStatus());

        verify(reportRepository).findById(reportId);
        verify(reportRepository).save(report);
    }

    @Test
    void shouldThrowExceptionWhenProgressWithoutAcceptedModeration() {
        Long reportId = 1L;
        ReportStatus newStatus = ReportStatus.IN_PROGRESS;

        Report report = new Report();
        report.setId(reportId);
        report.setModerationStatus(ModerationStatus.PENDING_REVIEW);
        report.setReportStatus(ReportStatus.PENDING);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        assertThrows(ResponseStatusException.class,
                () -> reportService.updateReportStatus(reportId, newStatus));

        verify(reportRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenMovingFromPendingToResolved() {
        Long reportId = 1L;
        ReportStatus newStatus = ReportStatus.RESOLVED;

        Report report = new Report();
        report.setId(reportId);
        report.setModerationStatus(ModerationStatus.ACCEPTED);
        report.setReportStatus(ReportStatus.PENDING);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        assertThrows(ResponseStatusException.class,
                () -> reportService.updateReportStatus(reportId, newStatus));

        verify(reportRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenMovingToPending() {
        Long reportId = 1L;
        ReportStatus newStatus = ReportStatus.PENDING;

        Report report = new Report();
        report.setId(reportId);
        report.setModerationStatus(ModerationStatus.ACCEPTED);
        report.setReportStatus(ReportStatus.IN_PROGRESS);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        assertThrows(ResponseStatusException.class,
                () -> reportService.updateReportStatus(reportId, newStatus));

        verify(reportRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingProgressForNonExistentReport() {
        Long reportId = 999L;
        ReportStatus newStatus = ReportStatus.IN_PROGRESS;

        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> reportService.updateReportStatus(reportId, newStatus));

        verify(reportRepository).findById(reportId);
        verify(reportRepository, never()).save(any());
    }
}