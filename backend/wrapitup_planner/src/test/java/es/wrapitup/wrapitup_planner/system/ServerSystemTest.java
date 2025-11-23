package es.wrapitup.wrapitup_planner.system;

import es.wrapitup.wrapitup_planner.dto.NoteDTO;
import es.wrapitup.wrapitup_planner.dto.UserModelDTO;
import es.wrapitup.wrapitup_planner.model.Note;
import es.wrapitup.wrapitup_planner.model.NoteVisibility;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.NoteRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import es.wrapitup.wrapitup_planner.service.NoteService;
import es.wrapitup.wrapitup_planner.service.UserService;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockHttpServletResponse;

import es.wrapitup.wrapitup_planner.security.jwt.UserLoginService;
import es.wrapitup.wrapitup_planner.security.jwt.LoginRequest;
import es.wrapitup.wrapitup_planner.security.jwt.AuthResponse;
import es.wrapitup.wrapitup_planner.security.jwt.AuthResponse.Status;
import jakarta.servlet.http.Cookie;

import java.sql.Blob;
import javax.sql.rowset.serial.SerialBlob;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("system")
@Transactional
@SpringBootTest
public class ServerSystemTest {

    @Autowired
    private NoteService noteService;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserLoginService userLoginService;

    @Test
    void testFindByIdWithSqlDatabase() {

        UserModel user = new UserModel();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setRoles(Arrays.asList("USER"));
        user.setStatus(UserStatus.ACTIVE);


        userRepository.save(user);

        Note note = new Note();
        note.setOverview("Resumen general de la sesión de IA");
        note.setSummary("Este es el contenido detallado del resumen");
        note.setJsonQuestions("{\"questions\": [\"¿Qué es IA?\", \"¿Cómo funciona?\"]}");
        note.setVisibility(NoteVisibility.PUBLIC);
        note.setUser(user);
        noteRepository.save(note);
        Optional<NoteDTO> result = noteService.findById(note.getId());

        assertEquals(true, result.isPresent());
        assertEquals("Resumen general de la sesión de IA", result.get().getOverview());
        assertEquals("Este es el contenido detallado del resumen", result.get().getSummary());
        assertEquals("{\"questions\": [\"¿Qué es IA?\", \"¿Cómo funciona?\"]}", result.get().getJsonQuestions());
        assertEquals(NoteVisibility.PUBLIC, result.get().getVisibility());
    }

    @Test
    void loginViaUserLoginService() throws Exception {
        String username = "sysuser";
        String rawPassword = "sysPass123";

        UserModel user = new UserModel();
        user.setUsername(username);
        user.setEmail("sys@example.com");
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
    void testCreateUserPersistsToDatabase() {
        UserModelDTO dto = new UserModelDTO();
        dto.setUsername("newdbuser");
        dto.setEmail("newdb@example.com");
        dto.setPassword("password123");

        userService.createUser(dto);

        Optional<UserModel> savedUser = userRepository.findByUsername("newdbuser");
        assertTrue(savedUser.isPresent());
        assertEquals("newdbuser", savedUser.get().getUsername());
        assertEquals("newdb@example.com", savedUser.get().getEmail());
        assertEquals("newdbuser", savedUser.get().getDisplayName()); // displayName defaults to username
        assertEquals(UserStatus.ACTIVE, savedUser.get().getStatus());
        assertTrue(passwordEncoder.matches("password123", savedUser.get().getPassword()));
    }

    @Test
    void testUpdateUserPersistsChanges() {
        UserModel user = new UserModel();
        user.setUsername("updatetest");
        user.setEmail("old@example.com");
        user.setDisplayName("Old Name");
        user.setRoles(Arrays.asList("USER"));
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);

        UserModelDTO updateDTO = new UserModelDTO();
        updateDTO.setDisplayName("New Name");
        updateDTO.setEmail("new@example.com");

        UserModelDTO result = userService.updateUser(user.getId(), updateDTO);

        assertNotNull(result);
        assertEquals("New Name", result.getDisplayName());
        assertEquals("new@example.com", result.getEmail());

        // Verify in database
        UserModel updatedUser = userRepository.findById(user.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals("New Name", updatedUser.getDisplayName());
        assertEquals("new@example.com", updatedUser.getEmail());
        assertEquals("updatetest", updatedUser.getUsername()); // username should not change
    }


    @Test
    void testFindByNameReturnsCorrectUser() {
        UserModel user = new UserModel();
        user.setUsername("findmeuser");
        user.setEmail("findme@example.com");
        user.setRoles(Arrays.asList("USER"));
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);

        UserModelDTO result = userService.findByName("findmeuser");

        assertNotNull(result);
        assertEquals("findmeuser", result.getUsername());
        assertEquals("findme@example.com", result.getEmail());
    }

    @Test
    void testUsernameExistsChecksDatabase() {
        UserModel user = new UserModel();
        user.setUsername("existinguser");
        user.setEmail("existing@example.com");
        user.setRoles(Arrays.asList("USER"));
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode("password"));
        userRepository.save(user);

        assertTrue(userService.usernameExists("existinguser"));
        assertFalse(userService.usernameExists("nonexistentuser"));
    }

    @Test
    void testUpdateUserWithBlobSavesImageToDatabase() throws Exception {
        UserModel user = new UserModel();
        user.setUsername("imageuser");
        user.setEmail("image@example.com");
        user.setRoles(Arrays.asList("USER"));
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode("password"));
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
    void testGetProfileImageRetrievesFromDatabase() throws Exception {
        UserModel user = new UserModel();
        user.setUsername("picuser");
        user.setEmail("pic@example.com");
        user.setRoles(Arrays.asList("USER"));
        user.setStatus(UserStatus.ACTIVE);
        user.setPassword(passwordEncoder.encode("password"));
        
        byte[] imageData = "test-image".getBytes();
        user.setProfilePic(new SerialBlob(imageData));
        userRepository.save(user);

        Blob result = userService.getProfileImage(user.getId());

        assertNotNull(result);
        byte[] resultData = result.getBytes(1, (int) result.length());
        assertArrayEquals(imageData, resultData);
    }

    @Test
    void testLogoutClearsCookies() {
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

