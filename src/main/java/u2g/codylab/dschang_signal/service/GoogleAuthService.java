package u2g.codylab.dschang_signal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import u2g.codylab.dschang_signal.exception.BadRequestException;

import java.util.Map;

@Slf4j
@Service
public class GoogleAuthService {

    @Value("${google.client-id}")
    private String googleClientId;

    private final RestTemplate restTemplate = new RestTemplate();

    public GoogleUserInfo verifyToken(String token) {
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + token;

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Invalid token");
            }

            Map<String, Object> payload = response.getBody();

            String audience = (String) payload.get("aud");
            if (!googleClientId.equals(audience)) {
                throw new RuntimeException("Token not valid for this application");
            }

            GoogleUserInfo userInfo = new GoogleUserInfo();
            userInfo.setEmail((String) payload.get("email"));
            userInfo.setFullName((String) payload.get("name"));
            userInfo.setAvatar((String) payload.get("picture"));
            userInfo.setGoogleId((String) payload.get("sub"));

            log.info("Google token verified for: {}", userInfo.getEmail());

            return userInfo;

        } catch (Exception e) {
            log.error("Error verifying Google token", e);
            throw new BadRequestException("Failed to verify Google token: " + e.getMessage());
        }
    }

    public static class GoogleUserInfo {
        private String email;
        private String fullName;
        private String avatar;
        private String googleId;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        public String getGoogleId() { return googleId; }
        public void setGoogleId(String googleId) { this.googleId = googleId; }
    }
}