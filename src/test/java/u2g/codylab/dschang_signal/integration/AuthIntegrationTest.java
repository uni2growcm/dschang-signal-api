package u2g.codylab.dschang_signal.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import u2g.codylab.dschang_signal.dto.ChangeRoleRequestApiDTO;
import u2g.codylab.dschang_signal.dto.LoginRequestApiDTO;
import u2g.codylab.dschang_signal.dto.RegisterRequestApiDTO;
import u2g.codylab.dschang_signal.entity.Role;
import u2g.codylab.dschang_signal.entity.User;
import u2g.codylab.dschang_signal.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        userRepository.findByEmail("user@test.com").ifPresent(userRepository::delete);
        userRepository.findByEmail("admin@test.com").ifPresent(userRepository::delete);

        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setFullName("Admin User");
        admin.setRole(Role.ADMIN);
        admin.setIsActive(true);

        Timestamp now = new Timestamp(System.currentTimeMillis());
        admin.setCreatedAt(now);
        admin.setUpdatedAt(now);

        userRepository.save(admin);
    }

    @Test
    void shouldRegisterUser() throws Exception {
        RegisterRequestApiDTO dto = new RegisterRequestApiDTO();
        dto.setEmail("user@test.com");
        dto.setPassword("123456");
        dto.setFullName("Test User");

        mockMvc.perform(post("/api/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldLoginUser() throws Exception {
        LoginRequestApiDTO dto = new LoginRequestApiDTO();
        dto.setEmail("admin@test.com");
        dto.setPassword("123456");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void shouldRejectInvalidToken() throws Exception {
        ChangeRoleRequestApiDTO dto = new ChangeRoleRequestApiDTO();
        dto.setRole(ChangeRoleRequestApiDTO.RoleEnum.ADMIN);

        mockMvc.perform(patch("/api/users/1/role")
                        .header("Authorization", "Bearer invalidToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void shouldChangeRoleWithValidToken() throws Exception {

        Long adminId = userRepository.findByEmail("admin@test.com").get().getId();

        ChangeRoleRequestApiDTO changeRole = new ChangeRoleRequestApiDTO();
        changeRole.setRole(ChangeRoleRequestApiDTO.RoleEnum.ADMIN);

        mockMvc.perform(patch("/api/users/" + adminId + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRole)))
                .andExpect(status().isOk());
    }
}