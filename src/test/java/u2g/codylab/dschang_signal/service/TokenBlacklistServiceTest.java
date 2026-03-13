package u2g.codylab.dschang_signal.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenBlacklistServiceTest {

    private final TokenBlacklistService tokenBlacklistService = new TokenBlacklistService();

    @Test
    void shouldBlacklistToken() {

        String token = "jwt-token";

        tokenBlacklistService.blacklist(token);

        assertTrue(tokenBlacklistService.isBlacklisted(token));
    }

    @Test
    void shouldReturnFalseWhenTokenNotBlacklisted() {

        String token = "jwt-token";

        assertFalse(tokenBlacklistService.isBlacklisted(token));
    }

    @Test
    void shouldBlacklistOldTokenWhenRegisteringNewToken() {

        String email = "test@test.com";

        String oldToken = "old-token";
        String newToken = "new-token";

        tokenBlacklistService.registerActiveToken(email, oldToken);
        tokenBlacklistService.registerActiveToken(email, newToken);

        assertTrue(tokenBlacklistService.isBlacklisted(oldToken));
        assertFalse(tokenBlacklistService.isBlacklisted(newToken));
    }
}