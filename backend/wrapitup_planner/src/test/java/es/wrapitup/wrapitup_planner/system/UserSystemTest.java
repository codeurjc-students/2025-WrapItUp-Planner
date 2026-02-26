package es.wrapitup.wrapitup_planner.system;

import es.wrapitup.wrapitup_planner.dto.UserModelDTO;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import es.wrapitup.wrapitup_planner.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("system")
@Transactional
@SpringBootTest
public class UserSystemTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserModel testUser;
    private UserModel adminUser;

    @BeforeEach
    void setUp() {
        long timestamp = System.currentTimeMillis();

        testUser = new UserModel();
        testUser.setUsername("usersystemtest_" + timestamp);
        testUser.setEmail("usersystemtest_" + timestamp + "@test.com");
        testUser.setDisplayName("Test User System");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRoles(Arrays.asList("USER"));
        testUser.setStatus(UserStatus.ACTIVE);
        testUser = userRepository.save(testUser);

        adminUser = new UserModel();
        adminUser.setUsername("adminsystemtest_" + timestamp);
        adminUser.setEmail("adminsystemtest_" + timestamp + "@test.com");
        adminUser.setDisplayName("Admin System");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRoles(Arrays.asList("USER", "ADMIN"));
        adminUser.setStatus(UserStatus.ACTIVE);
        adminUser = userRepository.save(adminUser);
    }

    @Test
    void banUserPersistsInDatabase() {
        UserModelDTO bannedUser = userService.banUser(testUser.getId());

        assertNotNull(bannedUser);
        assertEquals(UserStatus.BANNED, bannedUser.getStatus());

        Optional<UserModel> savedUser = userRepository.findById(testUser.getId());
        assertTrue(savedUser.isPresent());
        assertEquals(UserStatus.BANNED, savedUser.get().getStatus());
    }

    @Test
    void unbanUserPersistsInDatabase() {
        testUser.setStatus(UserStatus.BANNED);
        userRepository.save(testUser);

        UserModelDTO unbannedUser = userService.unbanUser(testUser.getId());

        assertNotNull(unbannedUser);
        assertEquals(UserStatus.ACTIVE, unbannedUser.getStatus());

        Optional<UserModel> savedUser = userRepository.findById(testUser.getId());
        assertTrue(savedUser.isPresent());
        assertEquals(UserStatus.ACTIVE, savedUser.get().getStatus());
    }

    @Test
    void banNonExistentUserReturnsNull() {
        UserModelDTO result = userService.banUser(999999L);
        assertNull(result);
    }

    @Test
    void unbanNonExistentUserReturnsNull() {
        UserModelDTO result = userService.unbanUser(999999L);
        assertNull(result);
    }


    @Test
    void unbanAlreadyActiveUserKeepsActiveStatus() {
        UserModelDTO result = userService.unbanUser(testUser.getId());

        assertNotNull(result);
        assertEquals(UserStatus.ACTIVE, result.getStatus());
    }

    @Test
    void adminUserCanBeBanned() {
        UserModelDTO bannedAdmin = userService.banUser(adminUser.getId());

        assertNotNull(bannedAdmin);
        assertEquals(UserStatus.BANNED, bannedAdmin.getStatus());
        assertTrue(bannedAdmin.getRoles().contains("ADMIN"));
    }
}
