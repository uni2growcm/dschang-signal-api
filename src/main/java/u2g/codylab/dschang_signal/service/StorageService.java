package u2g.codylab.dschang_signal.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface StorageService {

    /**
     * Sauvegarde le fichier et retourne l'URL publique générée.
     */
    String store(MultipartFile file, String mediaType, String hash);

    /**
     * Supprime le fichier physique correspondant à l'URL.
     */
    void delete(String url);
}
