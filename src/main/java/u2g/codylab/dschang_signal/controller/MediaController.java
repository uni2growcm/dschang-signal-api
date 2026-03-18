package u2g.codylab.dschang_signal.controller;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import u2g.codylab.dschang_signal.api.MediaApi;
import u2g.codylab.dschang_signal.dto.MediaResponseApiDTO;
import u2g.codylab.dschang_signal.service.MediaService;

import java.util.List;

@RestController
public class MediaController implements MediaApi {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @Override
    public ResponseEntity<MediaResponseApiDTO> uploadMedia(
            Integer reportId,
            MultipartFile file,
            String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mediaService.upload(reportId.longValue(), file, description));
    }

    @Override
    public ResponseEntity<List<MediaResponseApiDTO>> getReportMedias(Integer reportId) {
        return ResponseEntity.ok(
                mediaService.getMediasByReportId(reportId.longValue())
        );
    }

    @Override
    public ResponseEntity<List<MediaResponseApiDTO>> getAllMedia() {
        return ResponseEntity.ok(mediaService.getAllMedias());
    }

    @Override
    public ResponseEntity<Resource> getMediaById(Integer mediaId) {
        return mediaService.getById(mediaId);
    }

    @Override
    public ResponseEntity<Void> deleteMedia(Integer mediaId) {
        mediaService.delete(mediaId);
        return ResponseEntity.noContent().build();
    }
}