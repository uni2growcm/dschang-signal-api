package u2g.codylab.dschang_signal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
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

    @Mock
    private UserService userService;

    @Mock
    private I18nService i18nService;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ReportService reportService;



    @Test
    void shouldCreateReportSuccessfullyWithoutCategory() {
        ReportRequestApiDTO dto = new ReportRequestApiDTO();
        dto.setTitle("Pothole on Kennedy Avenue");
        dto.setDescription("Large dangerous pothole on the roadway");
        dto.setLocationText("Kennedy Avenue, in front of the university");

        User currentUser = new User();
        currentUser.setId(1L);

        Report savedReport = new Report();
        savedReport.setId(1L);

        ReportApiDTO reportApiDTO = new ReportApiDTO();

        when(reportRepository.existsByTitleAndLocationText(
                dto.getTitle(), dto.getLocationText())).thenReturn(false);
        when(reportRepository.save(any(Report.class))).thenReturn(savedReport);
        when(reportMapper.toReportDTO(savedReport)).thenReturn(reportApiDTO);

        ReportApiDTO result = reportService.createReport(dto, currentUser);

        assertNotNull(result);
        verify(reportRepository).existsByTitleAndLocationText(dto.getTitle(), dto.getLocationText());
        verify(reportRepository).save(any(Report.class));
        verify(reportMapper).toReportDTO(savedReport);
    }

    @Test
    void shouldCreateReportSuccessfullyWithCategory() {
        ReportRequestApiDTO dto = new ReportRequestApiDTO();
        dto.setTitle("Broken street light");
        dto.setDescription("The street light has been broken for two weeks");
        dto.setLocationText("Avenue des Palmiers");
        dto.setCategoryIds(List.of(1L));

        User currentUser = new User();
        currentUser.setId(1L);

        Category category = new Category();
        category.setId(1);
        category.setName("Roads & Potholes");

        Report savedReport = new Report();
        savedReport.setId(2L);

        ReportApiDTO reportApiDTO = new ReportApiDTO();

        when(reportRepository.existsByTitleAndLocationText(
                dto.getTitle(), dto.getLocationText())).thenReturn(false);
        when(categoryRepository.findAllById(dto.getCategoryIds()))
                .thenReturn(List.of(category));
        when(reportRepository.save(any(Report.class))).thenReturn(savedReport);
        when(reportMapper.toReportDTO(savedReport)).thenReturn(reportApiDTO);

        ReportApiDTO result = reportService.createReport(dto, currentUser);

        assertNotNull(result);
        verify(categoryRepository).findAllById(dto.getCategoryIds());
        verify(reportRepository).save(any(Report.class));
    }

    @Test
    void shouldThrowConflictWhenReportAlreadyExists() {
        ReportRequestApiDTO dto = new ReportRequestApiDTO();
        dto.setTitle("Pothole on Kennedy Avenue");
        dto.setDescription("Large dangerous pothole on the roadway");
        dto.setLocationText("Kennedy Avenue, in front of the university");

        User currentUser = new User();
        currentUser.setId(1L);

        when(reportRepository.existsByTitleAndLocationText(
                dto.getTitle(), dto.getLocationText())).thenReturn(true);

        assertThrows(ConflictException.class,
                () -> reportService.createReport(dto, currentUser));

        verify(reportRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenCategoryDoesNotExist() {
        ReportRequestApiDTO dto = new ReportRequestApiDTO();
        dto.setTitle("Broken street light");
        dto.setDescription("The street light has been broken for two weeks");
        dto.setLocationText("Avenue des Palmiers");
        dto.setCategoryIds(List.of(99999L));

        User currentUser = new User();
        currentUser.setId(1L);

        when(reportRepository.existsByTitleAndLocationText(
                dto.getTitle(), dto.getLocationText())).thenReturn(false);
        when(categoryRepository.findAllById(dto.getCategoryIds())).thenReturn(List.of());

        assertThrows(NotFoundException.class,
                () -> reportService.createReport(dto, currentUser));

        verify(reportRepository, never()).save(any());
    }



    @Test
    void shouldGetReportByIdSuccessfully() {
        Report report = new Report();
        report.setId(1L);

        ReportApiDTO dto = new ReportApiDTO();

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));
        when(reportMapper.toReportDTO(report)).thenReturn(dto);

        ReportApiDTO result = reportService.getReportById(1L);

        assertNotNull(result);
        verify(reportRepository).findById(1L);
        verify(reportMapper).toReportDTO(report);
    }

    @Test
    void shouldThrowExceptionWhenReportNotFound() {
        when(reportRepository.findById(1L)).thenReturn(Optional.empty());
        when(i18nService.get(eq("report.error.notFound"), any()))
                .thenReturn("Report not found with ID: 1");

        assertThrows(NotFoundException.class, () -> reportService.getReportById(1L));

        verify(reportRepository).findById(1L);
    }


    @Test
    void shouldGetPublicReportsSuccessfully() {
        Pageable pageable = PageRequest.of(0, 10);

        Report report = new Report();
        ReportApiDTO dto = new ReportApiDTO();
        Page<Report> page = new PageImpl<>(List.of(report));

        when(reportRepository.findByModerationStatus(ModerationStatus.RESOLVED, pageable))
                .thenReturn(page);
        when(reportMapper.toReportDTO(report)).thenReturn(dto);

        Page<ReportApiDTO> result = reportService.getPublicReports(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reportRepository).findByModerationStatus(ModerationStatus.RESOLVED, pageable);
        verify(reportMapper).toReportDTO(report);
    }

    @Test
    void shouldThrowBadRequestWhenPaginationFails() {
        Pageable pageable = PageRequest.of(0, 10);

        when(reportRepository.findByModerationStatus(ModerationStatus.RESOLVED, pageable))
                .thenThrow(new RuntimeException());

        assertThrows(BadRequestException.class,
                () -> reportService.getPublicReports(pageable));
    }



    @Test
    void shouldDeleteReportSuccessfully() {
        User currentUser = new User();
        currentUser.setId(1L);

        Report report = new Report();
        report.setId(1L);
        report.setCreatedBy(currentUser);
        report.setReportStatus(ReportStatus.PENDING);

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        reportService.deleteReport(1L, currentUser);

        verify(reportRepository).delete(report);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingNonExistentReport() {
        User currentUser = new User();
        currentUser.setId(1L);

        when(reportRepository.findById(99L)).thenReturn(Optional.empty());
        when(i18nService.get(eq("report.error.notFound"), any()))
                .thenReturn("Report not found with ID: 99");

        assertThrows(NotFoundException.class,
                () -> reportService.deleteReport(99L, currentUser));

        verify(reportRepository, never()).delete(any());
    }

    @Test
    void shouldThrowForbiddenWhenDeletingOtherUserReport() {
        User currentUser = new User();
        currentUser.setId(1L);

        User otherUser = new User();
        otherUser.setId(2L);

        Report report = new Report();
        report.setId(1L);
        report.setCreatedBy(otherUser);
        report.setReportStatus(ReportStatus.PENDING);

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        assertThrows(ForbiddenException.class,
                () -> reportService.deleteReport(1L, currentUser));

        verify(reportRepository, never()).delete(any());
    }

    @Test
    void shouldThrowBadRequestWhenDeletingNonPendingReport() {
        User currentUser = new User();
        currentUser.setId(1L);

        Report report = new Report();
        report.setId(1L);
        report.setCreatedBy(currentUser);
        report.setReportStatus(ReportStatus.IN_PROGRESS);

        when(reportRepository.findById(1L)).thenReturn(Optional.of(report));

        assertThrows(BadRequestException.class,
                () -> reportService.deleteReport(1L, currentUser));

        verify(reportRepository, never()).delete(any());
    }
}