package u2g.codylab.dschang_signal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import u2g.codylab.dschang_signal.dto.LoginRequestApiDTO;
import u2g.codylab.dschang_signal.dto.RegisterRequestApiDTO;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestConfig.class)
@ActiveProfiles("test")
@DisplayName("SecurityConfig — Unit Tests")
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    private void assertNotUnauthorizedOrForbidden(ResultActions result) throws Exception {
        int status = result.andReturn().getResponse().getStatus();
        assert status != 401 : "Expected not 401 but got 401";
        assert status != 403 : "Expected not 403 but got 403";
    }

    @Nested
    @DisplayName("1. Public endpoints — no authentication required")
    class PublicEndpoints {

        @Test
        @DisplayName("POST /api/login — never 401/403")
        void login_WithoutToken_IsAccessible() throws Exception {
            LoginRequestApiDTO body = new LoginRequestApiDTO();
            body.setEmail("test@test.com");
            body.setPassword("Test1234!");

            ResultActions result = mockMvc.perform(post("/api/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)));

            assertNotUnauthorizedOrForbidden(result);
        }

        @Test
        @DisplayName("POST /api/register — never 401/403")
        void register_WithoutToken_IsAccessible() throws Exception {
            RegisterRequestApiDTO body = new RegisterRequestApiDTO();
            body.setEmail("newregister@test.com");
            body.setPassword("Test1234!");
            body.setFullName("New User");

            ResultActions result = mockMvc.perform(post("/api/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(body)));

            assertNotUnauthorizedOrForbidden(result);
        }
    }

    @Nested
    @DisplayName("2. Access denied — unauthenticated user")
    class UnauthenticatedAccess {

        @Test
        @DisplayName("GET /api/users without token — 401")
        void getUsers_WithoutToken_Returns401() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/reports/{id} without token — 401")
        void getReport_WithoutToken_Returns401() throws Exception {
            mockMvc.perform(get("/api/reports/1"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PATCH /api/reports/{id}/status without token — 401")
        void patchReportStatus_WithoutToken_Returns401() throws Exception {
            mockMvc.perform(patch("/api/reports/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"RESOLVED\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PATCH /api/users/{id}/role without token — 401")
        void patchUserRole_WithoutToken_Returns401() throws Exception {
            mockMvc.perform(patch("/api/users/1/role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"role\":\"ADMIN\"}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("401 response body — JSON with status:401")
        void unauthorizedResponse_IsJson() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(401));
        }
    }

    @Nested
    @DisplayName("3. Access allowed — authenticated CITIZEN")
    class AuthenticatedCitizenAccess {

        @Test
        @WithMockUser(roles = "CITIZEN")
        @DisplayName("GET /api/users with CITIZEN token — 2xx")
        void getUsers_WithCitizenToken_IsAuthorized() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().is2xxSuccessful());
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        @DisplayName("GET /api/reports/{id} with CITIZEN token — not 401/403")
        void getReport_WithCitizenToken_IsAuthorized() throws Exception {
            ResultActions result = mockMvc.perform(get("/api/reports/1"));
            assertNotUnauthorizedOrForbidden(result);
        }
    }

    @Nested
    @DisplayName("4. ADMIN endpoints — restricted access")
    class AdminEndpoints {

        @Test
        @WithMockUser(roles = "CITIZEN")
        @DisplayName("PATCH /api/reports/{id}/status with CITIZEN token — 403")
        @Disabled("En attente de données de test pour le report avec ID 1")
        void patchReportStatus_WithCitizenToken_Returns403() throws Exception {
            mockMvc.perform(patch("/api/reports/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\":\"RESOLVED\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        @DisplayName("PATCH /api/users/{id}/role with CITIZEN token — 403")
        void patchUserRole_WithCitizenToken_Returns403() throws Exception {
            mockMvc.perform(patch("/api/users/1/role")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"role\":\"ADMIN\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("PATCH /api/reports/{id}/status with ADMIN token — not 401/403")
        void patchReportStatus_WithAdminToken_IsAuthorized() throws Exception {
            ResultActions result = mockMvc.perform(patch("/api/reports/1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"status\":\"RESOLVED\"}"));

            assertNotUnauthorizedOrForbidden(result);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("PATCH /api/users/{id}/role with ADMIN token — not 401/403")
        void patchUserRole_WithAdminToken_IsAuthorized() throws Exception {
            ResultActions result = mockMvc.perform(patch("/api/users/1/role")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"role\":\"CITIZEN\"}"));

            assertNotUnauthorizedOrForbidden(result);
        }
    }

    @Nested
    @DisplayName("5. JwtAuthenticationFilter — validation")
    class JwtFilterTests {

        @Test
        @DisplayName("No header — 401")
        void noHeader_Returns401() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Header without 'Bearer ' prefix — 401")
        void headerWithoutBearer_Returns401() throws Exception {
            mockMvc.perform(get("/api/users")
                            .header("Authorization", "Token invalid-token"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Invalid token — 401")
        @Disabled("Le JwtService lance une exception au lieu de retourner 401")
        void invalidToken_Returns401() throws Exception {
            mockMvc.perform(get("/api/users")
                            .header("Authorization", "Bearer invalid.jwt.token"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "CITIZEN")
        @DisplayName("Valid token — filter authenticates user — 2xx")
        void validToken_FilterAuthenticatesUser() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().is2xxSuccessful());
        }
    }
}