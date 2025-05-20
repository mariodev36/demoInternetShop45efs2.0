package org.demointernetshop45efs.controller;

import org.demointernetshop45efs.entity.ConfirmationCode;
import org.demointernetshop45efs.entity.User;
import org.demointernetshop45efs.repository.ConfirmationCodeRepository;
import org.demointernetshop45efs.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
class UserControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfirmationCodeRepository confirmationCodeRepository;

    @BeforeEach
    void setUp() {
        User testUser = new User();
        testUser.setFirstName("user1");
        testUser.setLastName("user1");
        testUser.setEmail("user1@company.com");
        testUser.setRole(User.Role.USER);
        testUser.setStatus(User.Status.NOT_CONFIRMED);
        User savedUser = userRepository.save(testUser);

        ConfirmationCode confirmationCode = new ConfirmationCode();
        confirmationCode.setCode("code for test");
        confirmationCode.setUser(savedUser);
        confirmationCode.setExpireDataTime(LocalDateTime.now().plusDays(1));
        confirmationCodeRepository.save(confirmationCode);
    }

    @AfterEach
    void drop() {
        confirmationCodeRepository.deleteAll();
        userRepository.deleteAll();
    }


    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetUserByIdWhenIdCorrect() throws Exception {
        String requestPath = "/api/users/1";

        mockMvc.perform(get(requestPath)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user1@company.com"));


    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetUserByIdWhenIdNotExist() throws Exception {
        String requestPath = "/api/users/2";
        String errorMessage = "User with ID = 2 not found";

        mockMvc.perform(get(requestPath)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(errorMessage)));
    }


    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testGetUserByIdWhenIdNotCorrect() throws Exception {
        String requestPath = "/api/users/2a";
        String errorMessage = "id";

        mockMvc.perform(get(requestPath)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.parameter").value(errorMessage));
    }




}