package u2g.codylab.dschang_signal.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import u2g.codylab.dschang_signal.dto.MediaResponseApiDTO;
import u2g.codylab.dschang_signal.entity.Media;
import u2g.codylab.dschang_signal.entity.Report;
import u2g.codylab.dschang_signal.entity.User;
import u2g.codylab.dschang_signal.mapper.MediaMapper;
import u2g.codylab.dschang_signal.repository.MediaRepository;
import u2g.codylab.dschang_signal.repository.ReportRepository;
import u2g.codylab.dschang_signal.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private MediaRepository mediaRepository;

    @Mock
    private MediaMapper mediaMapper;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private MediaService mediaService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    // Mock SecurityContextHolder
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUpSecurityContext() {
        when(authentication.getName()).thenReturn("user@test.cm");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldUploadMediaSuccessfully() {

        Long reportId = 1L;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "data".getBytes()
        );

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("user@test.cm");

        Report mockReport = new Report();
        mockReport.setId(reportId);

        String url = "http://localhost/files/image/hash.jpg";

        when(userRepository.findByEmail("user@test.cm"))
                .thenReturn(Optional.of(mockUser));

        when(reportRepository.findById(reportId))
                .thenReturn(Optional.of(mockReport));

        when(storageService.store(any(), any(), any()))
                .thenReturn(url);

        Media media = new Media();
        media.setUrl(url);

        when(mediaRepository.save(any(Media.class)))
                .thenReturn(media);

        when(mediaMapper.toMediaDTO(any(Media.class)))
                .thenReturn(new MediaResponseApiDTO());

        MediaResponseApiDTO result = mediaService.upload(reportId, file, "test");

        assertNotNull(result);
        verify(userRepository).findByEmail("user@test.cm");
        verify(reportRepository).findById(reportId);
        verify(storageService).store(any(), any(), any());
        verify(mediaRepository).save(any(Media.class));
        verify(mediaMapper).toMediaDTO(any(Media.class));
    }

    @Test
    void shouldThrowExceptionWhenReportNotFound() {

        Long reportId = 99L;

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "data".getBytes()
        );

        User mockUser = new User();
        mockUser.setEmail("user@test.cm");

        when(userRepository.findByEmail("user@test.cm"))
                .thenReturn(Optional.of(mockUser));

        when(reportRepository.findById(reportId))
                .thenReturn(Optional.empty());


        assertThrows(Exception.class,
                () -> mediaService.upload(reportId, file, "test"));

        verify(mediaRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {

        Long reportId = 1L;

        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "data".getBytes()
        );

        when(userRepository.findByEmail("user@test.cm"))
                .thenReturn(Optional.empty());


        assertThrows(Exception.class,
                () -> mediaService.upload(reportId, file, "test"));

        verify(reportRepository, never()).findById(any());
        verify(mediaRepository, never()).save(any());
    }
}