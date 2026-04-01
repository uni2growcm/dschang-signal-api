package u2g.codylab.dschang_signal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import u2g.codylab.dschang_signal.dto.ReportApiDTO;
import u2g.codylab.dschang_signal.dto.ReportRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UpdateModerationStatusRequestApiDTO;
import u2g.codylab.dschang_signal.entity.*;
import u2g.codylab.dschang_signal.exception.BadRequestException;
import u2g.codylab.dschang_signal.exception.ConflictException;
import u2g.codylab.dschang_signal.exception.ForbiddenException;
import u2g.codylab.dschang_signal.exception.NotFoundException;
import u2g.codylab.dschang_signal.mapper.ReportMapper;
import u2g.codylab.dschang_signal.repository.CategoryRepository;
import u2g.codylab.dschang_signal.repository.ReportRepository;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashSet;
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
    private I18nService i18nService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private NotificationService notificationService;

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
        verify(notificationService, never()).createReportStatusChangedNotification(any(), any(), any());
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

        when(reportRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(reportMapper.toReportDTO(report)).thenReturn(dto);

        Page<ReportApiDTO> result = reportService.getPublicReports(pageable, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reportRepository).findAll(any(Specification.class), eq(pageable));
        verify(reportMapper).toReportDTO(report);
    }

    @Test
    void shouldThrowBadRequestWhenPaginationFails() {
        Pageable pageable = PageRequest.of(0, 10);

        when(reportRepository.findAll(any(Specification.class), eq(pageable)))
                .thenThrow(new RuntimeException());

        assertThrows(BadRequestException.class,
                () -> reportService.getPublicReports(pageable, null, null));
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

        verify(reportRepository, never()).delete(any(Report.class));
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

        verify(reportRepository, never()).delete(any(Report.class));
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

        verify(reportRepository, never()).delete(any(Report.class));
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
        verify(notificationService, never()).createReportStatusChangedNotification(any(), any(), any());
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

        assertThrows(BadRequestException.class,
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
        report.setTitle("Test Report");

        User user = new User();
        user.setId(1L);
        report.setCreatedBy(user);

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
        verify(notificationService).createReportStatusChangedNotification(report, ReportStatus.PENDING, ReportStatus.IN_PROGRESS);
    }

    @Test
    void shouldUpdateReportProgressToResolvedSuccessfully() {
        Long reportId = 1L;
        ReportStatus newStatus = ReportStatus.RESOLVED;

        Report report = new Report();
        report.setId(reportId);
        report.setModerationStatus(ModerationStatus.ACCEPTED);
        report.setReportStatus(ReportStatus.IN_PROGRESS);
        report.setTitle("Test Report");

        User user = new User();
        user.setId(1L);
        report.setCreatedBy(user);

        ReportApiDTO expectedDto = new ReportApiDTO();

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenReturn(report);
        when(reportMapper.toReportDTO(report)).thenReturn(expectedDto);

        ReportApiDTO result = reportService.updateReportStatus(reportId, newStatus);

        assertNotNull(result);
        assertEquals(ReportStatus.RESOLVED, report.getReportStatus());

        verify(reportRepository).findById(reportId);
        verify(reportRepository).save(report);
        verify(notificationService).createReportStatusChangedNotification(report, ReportStatus.IN_PROGRESS, ReportStatus.RESOLVED);
    }

    @Test
    void shouldCreateNotificationWhenStatusChanges() {
        Long reportId = 1L;
        ReportStatus oldStatus = ReportStatus.PENDING;
        ReportStatus newStatus = ReportStatus.IN_PROGRESS;

        Report report = new Report();
        report.setId(reportId);
        report.setModerationStatus(ModerationStatus.ACCEPTED);
        report.setReportStatus(oldStatus);
        report.setTitle("Test Report");

        User user = new User();
        user.setId(1L);
        report.setCreatedBy(user);

        ReportApiDTO expectedDto = new ReportApiDTO();

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(reportRepository.save(any(Report.class))).thenReturn(report);
        when(reportMapper.toReportDTO(report)).thenReturn(expectedDto);

        reportService.updateReportStatus(reportId, newStatus);

        verify(notificationService, times(1)).createReportStatusChangedNotification(report, oldStatus, newStatus);
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

        assertThrows(BadRequestException.class,
                () -> reportService.updateReportStatus(reportId, newStatus));

        verify(reportRepository, never()).save(any());
        verify(notificationService, never()).createReportStatusChangedNotification(any(), any(), any());
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

        assertThrows(BadRequestException.class,
                () -> reportService.updateReportStatus(reportId, newStatus));

        verify(reportRepository, never()).save(any());
        verify(notificationService, never()).createReportStatusChangedNotification(any(), any(), any());
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

        assertThrows(BadRequestException.class,
                () -> reportService.updateReportStatus(reportId, newStatus));

        verify(reportRepository, never()).save(any());
        verify(notificationService, never()).createReportStatusChangedNotification(any(), any(), any());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingProgressForNonExistentReport() {
        Long reportId = 999L;
        ReportStatus newStatus = ReportStatus.IN_PROGRESS;

        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> reportService.updateReportStatus(reportId, newStatus));

        verify(reportRepository).findById(reportId);
        verify(reportRepository, never()).save(any());
        verify(notificationService, never()).createReportStatusChangedNotification(any(), any(), any());
    }

    @Test
    void shouldUpdateReportSuccessfully() {
        Long reportId = 1L;

        User currentUser = new User();
        currentUser.setId(1L);

        Report report = new Report();
        report.setId(reportId);
        report.setCreatedBy(currentUser);
        report.setModerationStatus(ModerationStatus.PENDING_REVIEW);
        report.setReportStatus(ReportStatus.PENDING);
        report.setCategories(new HashSet<>());

        ReportRequestApiDTO dto = new ReportRequestApiDTO();
        dto.setTitle("Updated title");
        dto.setDescription("Updated description valid");
        dto.setLocationText("Updated location");
        dto.setCategoryIds(List.of(1L));

        Category category = new Category();
        category.setId(1);

        ReportApiDTO expectedDto = new ReportApiDTO();

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(categoryRepository.findAllById(dto.getCategoryIds()))
                .thenReturn(List.of(category));
        when(reportRepository.save(any(Report.class))).thenReturn(report);
        when(reportMapper.toReportDTO(report)).thenReturn(expectedDto);

        ReportApiDTO result = reportService.updateReport(reportId, dto, currentUser);

        assertNotNull(result);
        assertEquals("Updated title", report.getTitle());
        assertEquals("Updated description valid", report.getDescription());
        assertEquals("Updated location", report.getLocationText());

        verify(reportRepository).findById(reportId);
        verify(reportRepository).save(report);
        verify(reportMapper).toReportDTO(report);
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingNonExistentReport() {
        Long reportId = 99L;

        User currentUser = new User();
        currentUser.setId(1L);

        ReportRequestApiDTO dto = new ReportRequestApiDTO();
        dto.setTitle("Title");
        dto.setLocationText("Location");

        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());
        when(i18nService.get(eq("report.error.notFound"), any()))
                .thenReturn("Report not found");

        assertThrows(NotFoundException.class,
                () -> reportService.updateReport(reportId, dto, currentUser));

        verify(reportRepository).findById(reportId);
    }

    @Test
    void shouldThrowForbiddenWhenUpdatingOtherUserReport() {
        Long reportId = 1L;

        User owner = new User();
        owner.setId(2L);

        User currentUser = new User();
        currentUser.setId(1L);

        Report report = new Report();
        report.setId(reportId);
        report.setCreatedBy(owner);
        report.setModerationStatus(ModerationStatus.PENDING_REVIEW);
        report.setReportStatus(ReportStatus.PENDING);

        ReportRequestApiDTO dto = new ReportRequestApiDTO();
        dto.setTitle("Updated");
        dto.setLocationText("Location");

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        assertThrows(ForbiddenException.class,
                () -> reportService.updateReport(reportId, dto, currentUser));

        verify(reportRepository, never()).save(any());
    }

    @Test
    void shouldThrowBadRequestWhenReportStatusNotPending() {
        Long reportId = 1L;

        User currentUser = new User();
        currentUser.setId(1L);

        Report report = new Report();
        report.setId(reportId);
        report.setCreatedBy(currentUser);
        report.setModerationStatus(ModerationStatus.PENDING_REVIEW);
        report.setReportStatus(ReportStatus.IN_PROGRESS);

        ReportRequestApiDTO dto = new ReportRequestApiDTO();
        dto.setTitle("Updated");
        dto.setLocationText("Location");

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        assertThrows(BadRequestException.class,
                () -> reportService.updateReport(reportId, dto, currentUser));

        verify(reportRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingWithInvalidCategory() {
        Long reportId = 1L;

        User currentUser = new User();
        currentUser.setId(1L);

        Report report = new Report();
        report.setId(reportId);
        report.setCreatedBy(currentUser);
        report.setModerationStatus(ModerationStatus.PENDING_REVIEW);
        report.setReportStatus(ReportStatus.PENDING);
        report.setCategories(new HashSet<>());

        ReportRequestApiDTO dto = new ReportRequestApiDTO();
        dto.setTitle("Updated");
        dto.setLocationText("Location");
        dto.setCategoryIds(List.of(999L));

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(categoryRepository.findAllById(dto.getCategoryIds()))
                .thenReturn(List.of());

        assertThrows(NotFoundException.class,
                () -> reportService.updateReport(reportId, dto, currentUser));

        verify(reportRepository, never()).save(any());
    }

    @Test
    void shouldGetPublicReportByIdWhenReportIsAccepted() {
        Long reportId = 1L;

        Report acceptedReport = new Report();
        acceptedReport.setId(reportId);
        acceptedReport.setModerationStatus(ModerationStatus.ACCEPTED);

        ReportApiDTO expectedDto = new ReportApiDTO();
        expectedDto.setId(reportId);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(acceptedReport));
        when(reportMapper.toReportDTO(acceptedReport)).thenReturn(expectedDto);

        ReportApiDTO result = reportService.getPublicReportById(reportId);

        assertNotNull(result);
        assertEquals(reportId, result.getId());
        verify(reportRepository).findById(reportId);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenPublicReportIdNotFound() {
        Long reportId = 999L;

        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());
        when(i18nService.get(eq("report.error.notFound"), any()))
                .thenReturn("Report not found");

        assertThrows(NotFoundException.class,
                () -> reportService.getPublicReportById(reportId));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenReportExistsButNotAccepted() {
        Long reportId = 1L;

        Report pendingReport = new Report();
        pendingReport.setId(reportId);
        pendingReport.setModerationStatus(ModerationStatus.PENDING_REVIEW);

        when(reportRepository.findById(reportId)).thenReturn(Optional.of(pendingReport));
        when(i18nService.get(eq("report.error.notFound"), any()))
                .thenReturn("Report not found");

        assertThrows(NotFoundException.class,
                () -> reportService.getPublicReportById(reportId));
    }

    @Test
    void shouldGetPublicReportsFilteredByStatus() {
        Pageable pageable = PageRequest.of(0, 10);

        Report report = new Report();
        ReportApiDTO dto = new ReportApiDTO();
        Page<Report> page = new PageImpl<>(List.of(report));

        when(reportRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(reportMapper.toReportDTO(report)).thenReturn(dto);

        Page<ReportApiDTO> result = reportService.getPublicReports(pageable, "PENDING", null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reportRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldGetPublicReportsFilteredByCategory() {
        Pageable pageable = PageRequest.of(0, 10);

        Report report = new Report();
        ReportApiDTO dto = new ReportApiDTO();
        Page<Report> page = new PageImpl<>(List.of(report));

        when(reportRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(reportMapper.toReportDTO(report)).thenReturn(dto);

        Page<ReportApiDTO> result = reportService.getPublicReports(pageable, null, "Drainage");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reportRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldReturnEmptyPageWhenNoPublicReportsMatchFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> emptyPage = new PageImpl<>(List.of());

        when(reportRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(emptyPage);

        Page<ReportApiDTO> result = reportService.getPublicReports(pageable, "RESOLVED", "Roads");

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(reportRepository).findAll(any(Specification.class), eq(pageable));
        verify(reportMapper, never()).toReportDTO(any());
    }

    @Test
    void shouldGetAllReportsWithoutFilters() {
        Pageable pageable = PageRequest.of(0, 10);

        Report report = new Report();
        ReportApiDTO dto = new ReportApiDTO();
        Page<Report> page = new PageImpl<>(List.of(report));

        when(reportRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(reportMapper.toReportDTO(report)).thenReturn(dto);

        Page<ReportApiDTO> result = reportService.getAllReports(
                pageable, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reportRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldGetAllReportsFilteredByModerationStatus() {
        Pageable pageable = PageRequest.of(0, 10);

        Report report = new Report();
        report.setModerationStatus(ModerationStatus.PENDING_REVIEW);
        ReportApiDTO dto = new ReportApiDTO();
        Page<Report> page = new PageImpl<>(List.of(report));

        when(reportRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(reportMapper.toReportDTO(report)).thenReturn(dto);

        Page<ReportApiDTO> result = reportService.getAllReports(
                pageable, "PENDING_REVIEW", null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reportRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldGetAllReportsFilteredByReportStatus() {
        Pageable pageable = PageRequest.of(0, 10);

        Report report = new Report();
        report.setReportStatus(ReportStatus.IN_PROGRESS);
        ReportApiDTO dto = new ReportApiDTO();
        Page<Report> page = new PageImpl<>(List.of(report));

        when(reportRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(reportMapper.toReportDTO(report)).thenReturn(dto);

        Page<ReportApiDTO> result = reportService.getAllReports(
                pageable, null, "IN_PROGRESS", null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reportRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldGetAllReportsFilteredByCategory() {
        Pageable pageable = PageRequest.of(0, 10);

        Report report = new Report();
        ReportApiDTO dto = new ReportApiDTO();
        Page<Report> page = new PageImpl<>(List.of(report));

        when(reportRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(reportMapper.toReportDTO(report)).thenReturn(dto);

        Page<ReportApiDTO> result = reportService.getAllReports(
                pageable, null, null, "Roads", null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reportRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldReturnEmptyPageWhenNoAdminReportsMatchFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Report> emptyPage = new PageImpl<>(List.of());

        when(reportRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(emptyPage);

        Page<ReportApiDTO> result = reportService.getAllReports(
                pageable, "ACCEPTED", "RESOLVED", "Drainage", null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(reportMapper, never()).toReportDTO(any());
    }


    @Test
    void shouldGetMyReportsWithoutFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        User currentUser = new User();
        currentUser.setId(1L);

        Report report = new Report();
        ReportApiDTO dto = new ReportApiDTO();
        Page<Report> page = new PageImpl<>(List.of(report));

        when(reportRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(reportMapper.toReportDTO(report)).thenReturn(dto);

        Page<ReportApiDTO> result = reportService.getMyReports(
                currentUser, pageable, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reportRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldGetMyReportsFilteredByStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        User currentUser = new User();
        currentUser.setId(1L);

        Report report = new Report();
        report.setReportStatus(ReportStatus.PENDING);
        ReportApiDTO dto = new ReportApiDTO();
        Page<Report> page = new PageImpl<>(List.of(report));

        when(reportRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(reportMapper.toReportDTO(report)).thenReturn(dto);

        Page<ReportApiDTO> result = reportService.getMyReports(
                currentUser, pageable, "PENDING", null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reportRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldGetMyReportsFilteredByCategory() {
        Pageable pageable = PageRequest.of(0, 10);
        User currentUser = new User();
        currentUser.setId(1L);

        Report report = new Report();
        ReportApiDTO dto = new ReportApiDTO();
        Page<Report> page = new PageImpl<>(List.of(report));

        when(reportRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(page);
        when(reportMapper.toReportDTO(report)).thenReturn(dto);

        Page<ReportApiDTO> result = reportService.getMyReports(
                currentUser, pageable, null, "Drainage");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(reportRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void shouldReturnEmptyPageWhenNoMyReportsMatchFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        User currentUser = new User();
        currentUser.setId(1L);

        Page<Report> emptyPage = new PageImpl<>(List.of());

        when(reportRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(emptyPage);

        Page<ReportApiDTO> result = reportService.getMyReports(
                currentUser, pageable, "RESOLVED", "Roads");

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(reportMapper, never()).toReportDTO(any());
    }
}