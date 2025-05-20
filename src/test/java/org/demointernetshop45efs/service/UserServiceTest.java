package org.demointernetshop45efs.service;

import org.demointernetshop45efs.dto.UserRequestDto;
import org.demointernetshop45efs.entity.ConfirmationCode;
import org.demointernetshop45efs.entity.User;
import org.demointernetshop45efs.repository.ConfirmationCodeRepository;
import org.demointernetshop45efs.repository.UserRepository;
import org.demointernetshop45efs.service.exception.AlreadyExistException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class UserServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

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
    void testWhenDuplicatedEmail(){
        UserRequestDto requestDto = new UserRequestDto(
                "firstUserName",
                "lastUserName",
                "user1@company.com",
                "pass12345!"
        );

        assertThrows(AlreadyExistException.class, () -> userService.registration(requestDto));
    }

}