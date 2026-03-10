package u2g.codylab.dschang_signal.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import u2g.codylab.dschang_signal.api.MediaApi;
import u2g.codylab.dschang_signal.dto.MediaResponseApiDTO;
import u2g.codylab.dschang_signal.entity.Media;
import u2g.codylab.dschang_signal.service.MediaService;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
public class MediaController implements MediaApi {

    private final MediaService mediaService;
    public MediaController(MediaService mediaService){
        this.mediaService = mediaService;
    }

    @Override
    public ResponseEntity<MediaResponseApiDTO> uploadMedia(MultipartFile file, String description
    ) {
        Media media = mediaService.upload(file,
                description != null ? description : "");
        return ResponseEntity.status(HttpStatus.CREATED).body(toDTO(media));
    }


    @Override
    public ResponseEntity<List<MediaResponseApiDTO>> getAllMedia() {
        return ResponseEntity.ok(
                mediaService.getAll().stream().map(this::toDTO).toList()
        );
    }


    @Override
    public ResponseEntity<Void> deleteMedia(Integer mediaId) {
        mediaService.delete(mediaId);
        return ResponseEntity.noContent().build();
    }


    private MediaResponseApiDTO toDTO(Media media) {
        MediaResponseApiDTO dto = new MediaResponseApiDTO();
        dto.setId(media.getId().intValue());
        dto.setDescription(media.getDescription());
        dto.setType(media.getType());
        dto.setMimeType(media.getMimeType());
        dto.setUrl(media.getUrl());
        dto.setOriginalName(media.getOriginalName());
        dto.setFileSize(media.getFileSize().intValue());
        dto.setCreatedAt(OffsetDateTime.from(media.getCreatedAt()));
        return dto;
    }
}


