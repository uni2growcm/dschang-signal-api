package u2g.codylab.dschang_signal.service;


import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import u2g.codylab.dschang_signal.exception.BadRequestException;
import u2g.codylab.dschang_signal.exception.NotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;

@Service
@Profile("!s3")
public class StorageServiceImpl implements StorageService {

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
            throw new BadRequestException("Failed to save media in: " + relativePath);
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
            throw new BadRequestException("Failed to delete media in : " + relativePath);
        }
    }

    @Override
    public Resource load(String url) {
        String relativePath = url.replace(baseUrl + "/", "");
        Path filePath = Paths.get(basePath).resolve(relativePath);
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new NotFoundException("Fichier introuvable : " + relativePath);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new NotFoundException("URL invalide : " + relativePath);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

}

