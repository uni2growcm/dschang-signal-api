package u2g.codylab.dschang_signal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import u2g.codylab.dschang_signal.dto.MediaResponseApiDTO;
import u2g.codylab.dschang_signal.entity.Media;
import u2g.codylab.dschang_signal.entity.Report;
import u2g.codylab.dschang_signal.entity.User;
import u2g.codylab.dschang_signal.exception.BadRequestException;
import u2g.codylab.dschang_signal.exception.NotFoundException;
import u2g.codylab.dschang_signal.mapper.MediaMapper;
import u2g.codylab.dschang_signal.repository.MediaRepository;
import u2g.codylab.dschang_signal.repository.ReportRepository;
import u2g.codylab.dschang_signal.repository.UserRepository;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MediaService {

    private final MediaMapper mediaMapper;
    private final MediaRepository mediaRepository;
    private final StorageService storageService;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    public MediaResponseApiDTO upload(Long reportId,MultipartFile file, String description) {
        log.info("Uploading file: {}, size: {}",
                file.getOriginalFilename(), file.getSize());

        // Utilisateur connecté
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found: " + email));

        // Report
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException("Report not found: " + reportId));

        String mimeType = detectMimeType(file);
        String mediaType = resolveMediaType(mimeType);
        String hash = generateHash(file);
        String url = storageService.store(file, mediaType, hash);

        Media media = new Media();
        media.setDescription(description != null ? description : "");
        media.setType(mediaType);
        media.setMimeType(mimeType);
        media.setUrl(url);
        media.setOriginalName(file.getOriginalFilename());
        media.setFileSize(file.getSize());
        media.setReport(report);
        media.setCreatedBy(currentUser);

        Media savedMedia = mediaRepository.save(media);
        log.info("Media saved with url: {}", savedMedia.getUrl());
        return mediaMapper.toMediaDTO(savedMedia);
    }

    @Transactional(readOnly = true)
    public List<MediaResponseApiDTO> getAllMedias() {
        return mediaRepository.findAll()
                .stream().map(mediaMapper::toMediaDTO).toList();
    }

    public ResponseEntity<Resource> getById(Integer mediaId) {
        Media media = mediaRepository.findById(mediaId.longValue())
                .orElseThrow(() -> new NotFoundException(
                        "Media avec l'id " + mediaId + " introuvable"));

        Resource resource = storageService.load(media.getUrl());

        String contentType = media.getMimeType();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + media.getOriginalName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }


    public void delete(Integer mediaId) {
        Media media = mediaRepository.findById(mediaId.longValue())
                .orElseThrow(() -> new BadRequestException(
                        "Media with id " + mediaId + " not found"));
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
            throw new BadRequestException("Failed to generate media hash");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}


