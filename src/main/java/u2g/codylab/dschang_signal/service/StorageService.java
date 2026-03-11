package u2g.codylab.dschang_signal.service;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface StorageService {

    String store(MultipartFile file, String mediaType, String hash);

    void delete(String url);
}
