package u2g.codylab.dschang_signal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import u2g.codylab.dschang_signal.dto.MediaResponseApiDTO;
import u2g.codylab.dschang_signal.entity.Media;
import u2g.codylab.dschang_signal.repository.MediaRepository;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MediaService {

    private final MediaRepository mediaRepository;
    private final StorageService storageService;


    public Media upload(MultipartFile file, String description) {

        String mimeType   = detectMimeType(file);
        String mediaType  = resolveMediaType(mimeType);
        String hash       = generateHash(file);
        String url        = storageService.store(file, mediaType, hash);

        Media media = new Media();
        media.setDescription(description);
        media.setType(mediaType);
        media.setMimeType(mimeType);
        media.setUrl(url);
        media.setOriginalName(file.getOriginalFilename());
        media.setFileSize(file.getSize());

        return mediaRepository.save(media);
    }


    @Transactional(readOnly = true)
    public List<Media> getAll() {
        return mediaRepository.findAll();
    }


    public void delete(Integer mediaId) {
        Media media = mediaRepository.findById(mediaId.longValue())
                .orElseThrow(() -> new RuntimeException(
                        "Media avec l'id " + mediaId + " introuvable"));
        storageService.delete(media.getUrl());
        mediaRepository.delete(media);
    }



    private String detectMimeType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType != null && !contentType.equals("application/octet-stream")) {
            return contentType;
        }
        String name = file.getOriginalFilename();
        if (name != null) {
            return switch (getExtension(name).toLowerCase()) {
                case "jpg", "jpeg" -> "image/jpeg";
                case "png"         -> "image/png";
                case "webp"        -> "image/webp";
                case "gif"         -> "image/gif";
                case "mp4"         -> "video/mp4";
                case "mov"         -> "video/quicktime";
                case "avi"         -> "video/x-msvideo";
                case "pdf"         -> "application/pdf";
                default            -> "application/octet-stream";
            };
        }
        return "application/octet-stream";
    }

    private String resolveMediaType(String mimeType) {
        if (mimeType.startsWith("image/")) return "image";
        if (mimeType.startsWith("video/")) return "video";
        if (mimeType.startsWith("audio/")) return "audio";
        return "document";
    }

    private String generateHash(MultipartFile file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(file.getBytes());
            return HexFormat.of().formatHex(hashBytes).substring(0, 20);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException("Impossible de générer le hash du fichier", e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}


