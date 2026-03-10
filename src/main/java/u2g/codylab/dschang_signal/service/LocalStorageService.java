package u2g.codylab.dschang_signal.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

@Service
@Profile("!s3")
public class LocalStorageService implements StorageService {

    @Value("${storage.local.path}")
    private String basePath;

    @Value("${storage.local.base-url}")
    private String baseUrl;


    @Override
    public String store(MultipartFile file, String mediaType, String hash) {
        String extension   = getExtension(file.getOriginalFilename());
        String relativePath = String.format("%s/%s.%s", mediaType, hash, extension);
        Path targetPath     = Paths.get(basePath).resolve(relativePath);

        try {
            Files.createDirectories(targetPath.getParent());
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Échec de la sauvegarde : " + relativePath, e);
        }

        return baseUrl + "/" + relativePath;
    }

    @Override
    public void delete(String url) {
        // Extraire le chemin relatif depuis l'URL
        String relativePath = url.replace(baseUrl + "/", "");
        Path targetPath = Paths.get(basePath).resolve(relativePath);
        try {
            Files.deleteIfExists(targetPath);
        } catch (IOException e) {
            throw new RuntimeException("Échec de la suppression du fichier : " + relativePath, e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

}

