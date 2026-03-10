package u2g.codylab.dschang_signal.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class TokenBlacklistService {

    // tokens explicitement blacklistés (logout)
    private final Set<String> blacklist = new HashSet<>();

    // token actif par utilisateur
    private final Map<String, String> activeTokens = new HashMap<>();

    public void blacklist(String token) {
        blacklist.add(token);
    }

    public void registerActiveToken(String email, String token) {
        // si un ancien token existe pour cet email, on le blackliste
        String oldToken = activeTokens.get(email);
        if (oldToken != null) {
            blacklist.add(oldToken);
        }
        activeTokens.put(email, token);
    }

    public boolean isBlacklisted(String token) {
        if (token == null) return false;
        return blacklist.contains(token);
    }
}