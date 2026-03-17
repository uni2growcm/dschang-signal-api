package u2g.codylab.dschang_signal.service;


import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface StorageService {

    String store(MultipartFile file, String mediaType, String hash);

    void delete(String url);

    Resource load(String url);

}
