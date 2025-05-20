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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.core.StringContains.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
class PublicControllerTest {

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
    void testRegisterUser() throws Exception {
        String newUserJson = """
                {
                "firstName":"John",
                "lastName":"John",
                "email":"john@company.com",
                "hashPassword":"Pass12345!"
                }
                """;
        String requestPath = "/api/public/new";

        mockMvc.perform(
                        post(requestPath)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(newUserJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("john@company.com"))
                .andExpect(jsonPath("$.role").value("USER"));



    }

    @Test
    void testReturn400ForBadEmailFormat() throws Exception {
        String newUserJson = """
                {
                "firstName":"John",
                "lastName":"John",
                "email":"badFormatForEmail",
                "hashPassword":"Pass12345!"
                }
                """;
        String requestPath = "/api/public/new";

        mockMvc.perform(
                        post(requestPath)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(newUserJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("должно иметь формат адреса электронной почты")));

    }


    @Test
    void testReturn409ForExistEmail() throws Exception {
        String newUserJson = """
                {
                "firstName":"John",
                "lastName":"John",
                "email":"user1@company.com",
                "hashPassword":"Pass12345!"
                }
                """;
        String requestPath = "/api/public/new";
        String errorMessage = "User with email: user1@company.com is already exist";

        mockMvc.perform(post(requestPath)
                .contentType(MediaType.APPLICATION_JSON)
                .content(newUserJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(errorMessage));
    }

    @Test
    void testConfirmRegistration() throws Exception {
        String requestPath = "/api/public/code/confirmation";
        String requestParamName = "codeConfirmation";
        String requestParamValue = "code for test";
        String expectedValue = "user1@company.com";

        mockMvc.perform(get(requestPath)
                .param(requestParamName, requestParamValue))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(expectedValue));
    }

}