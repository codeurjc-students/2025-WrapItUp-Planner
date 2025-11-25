package es.wrapitup.wrapitup_planner.system;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Blob;
import java.util.Arrays;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import es.wrapitup.wrapitup_planner.dto.UserModelDTO;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import es.wrapitup.wrapitup_planner.security.jwt.AuthResponse;
import es.wrapitup.wrapitup_planner.security.jwt.AuthResponse.Status;
import es.wrapitup.wrapitup_planner.security.jwt.LoginRequest;
import es.wrapitup.wrapitup_planner.security.jwt.UserLoginService;
import es.wrapitup.wrapitup_planner.service.UserService;
import jakarta.servlet.http.Cookie;

@Tag("system")
@Transactional
@SpringBootTest
public class ServerSystemTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserLoginService userLoginService;

    @Test
    void loginUserLoginService() throws Exception {
        String username = "currentUser";
        String rawPassword = "Password123";

        UserModel user = new UserModel();
        user.setUsername(username);
        user.setEmail("currentuser@example.com");
        user.setRoles(java.util.Arrays.asList("USER"));
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode(rawPassword));

        userRepository.save(user);

        MockHttpServletResponse response = new MockHttpServletResponse();
        LoginRequest loginRequest = new LoginRequest(username, rawPassword);

        var respEntity = userLoginService.login(response, loginRequest);
        AuthResponse body = respEntity.getBody();

        assertEquals(Status.SUCCESS, body.getStatus());
        assertTrue(response.getCookies().length >= 1);
    }

    @Test
    void createUserPersistsToDatabase() {
        UserModelDTO dto = new UserModelDTO();
        dto.setUsername("currentUser");
        dto.setEmail("currentuser@example.com");
        dto.setPassword("Password123");

        userService.createUser(dto);

        Optional<UserModel> savedUser = userRepository.findByUsername("currentUser");
        assertTrue(savedUser.isPresent());
        assertEquals("currentUser", savedUser.get().getUsername());
        assertEquals("currentuser@example.com", savedUser.get().getEmail());
        assertEquals("currentUser", savedUser.get().getDisplayName()); 
        assertEquals(UserStatus.ACTIVE, savedUser.get().getStatus());
        assertTrue(passwordEncoder.matches("Password123", savedUser.get().getPassword()));
    }

    @Test
    void updateUserPersistsChanges() {
        UserModel user = new UserModel();
        user.setUsername("currentUser");
        user.setEmail("currentuser@example.com");
        user.setDisplayName("Old Name");
        user.setRoles(Arrays.asList("USER"));
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode("Password123"));
        userRepository.save(user);

        UserModelDTO updateDTO = new UserModelDTO();
        updateDTO.setDisplayName("New Name");
        updateDTO.setEmail("updated@example.com");

        UserModelDTO result = userService.updateUser(user.getId(), updateDTO);

        assertNotNull(result);
        assertEquals("New Name", result.getDisplayName());
        assertEquals("updated@example.com", result.getEmail());

        // Verify in database
        UserModel updatedUser = userRepository.findById(user.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals("New Name", updatedUser.getDisplayName());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals("currentUser", updatedUser.getUsername());
    }


    @Test
    void findByNameReturnsCorrectUser() {
        UserModel user = new UserModel();
        user.setUsername("currentUser");
        user.setEmail("currentuser@example.com");
        user.setRoles(Arrays.asList("USER"));
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode("Password123"));
        userRepository.save(user);

        UserModelDTO result = userService.findByName("currentUser");

        assertNotNull(result);
        assertEquals("currentUser", result.getUsername());
        assertEquals("currentuser@example.com", result.getEmail());
    }

    @Test
    void usernameExistsChecksDatabase() {
        UserModel user = new UserModel();
        user.setUsername("currentUser");
        user.setEmail("currentuser@example.com");
        user.setRoles(Arrays.asList("USER"));
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode("Password123"));
        userRepository.save(user);

        assertTrue(userService.usernameExists("currentUser"));
        assertFalse(userService.usernameExists("nonexistentuser"));
    }

    @Test
    void updateUserWithBlobSavesImageToDatabase() throws Exception {
        UserModel user = new UserModel();
        user.setUsername("currentUser");
        user.setEmail("currentuser@example.com");
        user.setRoles(Arrays.asList("USER"));
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode("Password123"));
        userRepository.save(user);

        byte[] imageData = "fake-image-data".getBytes();
        Blob imageBlob = new SerialBlob(imageData);

        UserModelDTO updateDTO = new UserModelDTO();
        updateDTO.setImage("/api/v1/users/profile-image/" + user.getId());

        userService.updateUserWithBlob(user.getId(), updateDTO, imageBlob);

        // Verify in database
        UserModel updatedUser = userRepository.findById(user.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertNotNull(updatedUser.getProfilePic());
        
        byte[] savedImageData = updatedUser.getProfilePic().getBytes(1, (int) updatedUser.getProfilePic().length());
        assertArrayEquals(imageData, savedImageData);
    }

    @Test
    void getProfileImageRetrievesFromDatabase() throws Exception {
        UserModel user = new UserModel();
        user.setUsername("currentUser");
        user.setEmail("currentuser@example.com");
        user.setRoles(Arrays.asList("USER"));
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode("Password123"));
        
        byte[] imageData = "test-image".getBytes();
        user.setProfilePic(new SerialBlob(imageData));
        userRepository.save(user);

        Blob result = userService.getProfileImage(user.getId());

        assertNotNull(result);
        byte[] resultData = result.getBytes(1, (int) result.length());
        assertArrayEquals(imageData, resultData);
    }

    @Test
    void logoutClearsCookies() {
        MockHttpServletResponse response = new MockHttpServletResponse();

        userLoginService.logout(response);

        Cookie[] cookies = response.getCookies();
        assertTrue(cookies.length >= 2);
        
        boolean authTokenCleared = false;
        boolean refreshTokenCleared = false;
        
        for (Cookie cookie : cookies) {
            if ("AuthToken".equals(cookie.getName())) {
                authTokenCleared = cookie.getMaxAge() == 0;
            }
            if ("RefreshToken".equals(cookie.getName())) {
                refreshTokenCleared = cookie.getMaxAge() == 0;
            }
        }
        
        assertTrue(authTokenCleared);
        assertTrue(refreshTokenCleared);
    }

}

