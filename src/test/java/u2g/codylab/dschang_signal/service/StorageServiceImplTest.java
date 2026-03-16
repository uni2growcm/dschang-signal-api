package u2g.codylab.dschang_signal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class StorageServiceImplTest {

    private StorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        storageService = new StorageServiceImpl();

        ReflectionTestUtils.setField(storageService, "basePath", "test-storage");
        ReflectionTestUtils.setField(storageService, "baseUrl", "http://localhost/files");
    }

    @Test
    void shouldStoreFileSuccessfully() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "image.jpg",
                        "image/jpeg",
                        "hello".getBytes()
                );

        String url = storageService.store(file, "image", "hash123");

        assertNotNull(url);
        assertTrue(url.contains("hash123"));
    }

    @Test
    void shouldDeleteFileSuccessfully() throws Exception {

        Path folder = Path.of("test-storage/image");
        Files.createDirectories(folder);

        Path file = folder.resolve("hash123.jpg");

        if (!Files.exists(file)) {
            Files.createFile(file);
        }

        String url = "http://localhost/files/image/hash123.jpg";

        storageService.delete(url);

        assertFalse(Files.exists(file));
    }
}