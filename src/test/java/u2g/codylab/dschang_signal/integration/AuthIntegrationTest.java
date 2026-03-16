package u2g.codylab.dschang_signal.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import u2g.codylab.dschang_signal.dto.ChangeRoleRequestApiDTO;
import u2g.codylab.dschang_signal.dto.LoginRequestApiDTO;
import u2g.codylab.dschang_signal.dto.RegisterRequestApiDTO;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
        dto.setEmail("user@test.com");
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
    void shouldChangeRoleWithValidToken() throws Exception {

        RegisterRequestApiDTO register = new RegisterRequestApiDTO();
        register.setEmail("admin@test.com");
        register.setPassword("123456");
        register.setFullName("Admin User");

        mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        LoginRequestApiDTO login = new LoginRequestApiDTO();
        login.setEmail("admin@test.com");
        login.setPassword("123456");

        String loginResponse = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();

        ChangeRoleRequestApiDTO changeRole = new ChangeRoleRequestApiDTO();
        changeRole.setRole(ChangeRoleRequestApiDTO.RoleEnum.ADMIN);

        mockMvc.perform(patch("/api/users/1/role")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRole)))
                .andExpect(status().isOk());
    }
}