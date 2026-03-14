package u2g.codylab.dschang_signal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import u2g.codylab.dschang_signal.dto.MediaResponseApiDTO;
import u2g.codylab.dschang_signal.entity.Media;
import u2g.codylab.dschang_signal.mapper.MediaMapper;
import u2g.codylab.dschang_signal.repository.MediaRepository;

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

    @Test
    void shouldUploadMediaSuccessfully() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "data".getBytes()
        );

        String url = "http://localhost/files/image/hash.jpg";

        when(storageService.store(any(), any(), any()))
                .thenReturn(url);

        Media media = new Media();
        media.setUrl(url);

        when(mediaRepository.save(any(Media.class)))
                .thenReturn(media);

        when(mediaMapper.toMediaDTO(any(Media.class)))
                .thenReturn(new MediaResponseApiDTO());

        MediaResponseApiDTO result = mediaService.upload(file, "test");

        assertNotNull(result);

        verify(storageService).store(any(), any(), any());
        verify(mediaRepository).save(any(Media.class));
        verify(mediaMapper).toMediaDTO(any(Media.class));
    }
}