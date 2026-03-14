package u2g.codylab.dschang_signal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(
                jwtService,
                "secret",
                "12345678901234567890123456789012"
        );

        ReflectionTestUtils.setField(
                jwtService,
                "expiration",
                "3600000"
        );
    }

    @Test
    void shouldGenerateToken() {

        String email = "admin@test.com";

        String token = jwtService.generateToken(email);

        assertNotNull(token);
    }

    @Test
    void shouldExtractEmailFromToken() {

        String email = "admin@test.com";

        String token = jwtService.generateToken(email);

        String extractedEmail = jwtService.extractEmail(token);

        assertEquals(email, extractedEmail);
    }

    @Test
    void shouldValidateToken() {

        String token = jwtService.generateToken("admin@test.com");

        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void shouldReturnFalseForInvalidToken() {

        assertFalse(jwtService.validateToken("invalid.token"));
    }
}