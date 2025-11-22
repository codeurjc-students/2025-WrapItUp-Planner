package es.wrapitup.wrapitup_planner.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Blob;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import es.wrapitup.wrapitup_planner.dto.UserMapper;
import es.wrapitup.wrapitup_planner.dto.UserModelDTO;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import es.wrapitup.wrapitup_planner.service.UserService;

@Tag("unit")
public class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByIdReturnsDTO() {
        UserModel user = new UserModel();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        UserModelDTO dto = new UserModelDTO();
        dto.setId(1L);
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        Optional<UserModelDTO> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(dto.getId(), result.get().getId());
        assertEquals(dto.getUsername(), result.get().getUsername());
        verify(userRepository).findById(1L);
    }

    @Test
    void testFindByIdReturnsEmptyWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<UserModelDTO> result = userService.findById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void testGetAllUsers() {
        UserModel user1 = new UserModel();
        user1.setId(1L);
        user1.setUsername("user1");

        UserModel user2 = new UserModel();
        user2.setId(2L);
        user2.setUsername("user2");

        UserModelDTO dto1 = new UserModelDTO();
        dto1.setId(1L);
        dto1.setUsername("user1");

        UserModelDTO dto2 = new UserModelDTO();
        dto2.setId(2L);
        dto2.setUsername("user2");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));
        when(userMapper.toDto(user1)).thenReturn(dto1);
        when(userMapper.toDto(user2)).thenReturn(dto2);

        List<UserModelDTO> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
    }

    @Test
    void testFindByNameReturnsDTO() {
        UserModel user = new UserModel();
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        UserModelDTO dto = new UserModelDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@example.com");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        UserModelDTO result = userService.findByName("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testFindByNameReturnsNullWhenNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        UserModelDTO result = userService.findByName("nonexistent");

        assertNull(result);
    }

    @Test
    void testCreateUserEncodesPasswordAndSetsDefaults() {
        UserModelDTO dto = new UserModelDTO();
        dto.setUsername("newuser");
        dto.setEmail("new@example.com");
        dto.setPassword("plainPassword");

        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");

        userService.createUser(dto);

        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository).save(captor.capture());

        UserModel savedUser = captor.getValue();
        assertEquals("newuser", savedUser.getUsername());
        assertEquals("newuser", savedUser.getDisplayName()); // displayName defaults to username
        assertEquals("new@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals(UserStatus.ACTIVE, savedUser.getStatus());
    }

    @Test
    void testUpdateUserUpdatesFields() {
        UserModel existingUser = new UserModel();
        existingUser.setId(1L);
        existingUser.setUsername("testuser");
        existingUser.setEmail("old@example.com");
        existingUser.setDisplayName("Old Name");

        UserModelDTO updateDTO = new UserModelDTO();
        updateDTO.setDisplayName("New Name");
        updateDTO.setEmail("new@example.com");
        updateDTO.setImage("/path/to/image.jpg");

        UserModel updatedUser = new UserModel();
        updatedUser.setId(1L);
        updatedUser.setUsername("testuser");
        updatedUser.setDisplayName("New Name");
        updatedUser.setEmail("new@example.com");
        updatedUser.setImage("/path/to/image.jpg");

        UserModelDTO updatedDTO = new UserModelDTO();
        updatedDTO.setId(1L);
        updatedDTO.setUsername("testuser");
        updatedDTO.setDisplayName("New Name");
        updatedDTO.setEmail("new@example.com");
        updatedDTO.setImage("/path/to/image.jpg");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(updatedUser);
        when(userMapper.toDto(updatedUser)).thenReturn(updatedDTO);

        UserModelDTO result = userService.updateUser(1L, updateDTO);

        assertNotNull(result);
        assertEquals("New Name", result.getDisplayName());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("/path/to/image.jpg", result.getImage());
    }

    @Test
    void testUpdateUserReturnsNullWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        UserModelDTO updateDTO = new UserModelDTO();
        updateDTO.setEmail("test@example.com");

        UserModelDTO result = userService.updateUser(99L, updateDTO);

        assertNull(result);
    }

    @Test
    void testUpdateUserDoesNotUpdateEmptyFields() {
        UserModel existingUser = new UserModel();
        existingUser.setId(1L);
        existingUser.setUsername("testuser");
        existingUser.setEmail("old@example.com");
        existingUser.setDisplayName("Old Name");

        UserModelDTO updateDTO = new UserModelDTO();
        updateDTO.setDisplayName(""); // empty should not update
        updateDTO.setEmail("new@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(existingUser);

        userService.updateUser(1L, updateDTO);

        ArgumentCaptor<UserModel> captor = ArgumentCaptor.forClass(UserModel.class);
        verify(userRepository).save(captor.capture());

        UserModel savedUser = captor.getValue();
        assertEquals("Old Name", savedUser.getDisplayName()); // should not change
        assertEquals("new@example.com", savedUser.getEmail());
    }

    @Test
    void testUsernameExistsReturnsTrue() {
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(new UserModel()));

        boolean result = userService.usernameExists("existinguser");

        assertTrue(result);
    }

    @Test
    void testUsernameExistsReturnsFalse() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());

        boolean result = userService.usernameExists("newuser");

        assertFalse(result);
    }

    @Test
    void testUpdateUserWithBlobUpdatesProfilePic() throws Exception {
        UserModel existingUser = new UserModel();
        existingUser.setId(1L);
        existingUser.setUsername("testuser");

        Blob mockBlob = mock(Blob.class);

        UserModelDTO updateDTO = new UserModelDTO();
        updateDTO.setEmail("new@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserModel.class))).thenReturn(existingUser);
        when(userMapper.toDto(any(UserModel.class))).thenReturn(new UserModelDTO());

        UserModelDTO result = userService.updateUserWithBlob(1L, updateDTO, mockBlob);

        assertNotNull(result);
        verify(userRepository).save(argThat(user -> user.getProfilePic() == mockBlob));
    }

    @Test
    void testGetProfileImageReturnsBlob() throws Exception {
        Blob mockBlob = mock(Blob.class);
        UserModel user = new UserModel();
        user.setId(1L);
        user.setProfilePic(mockBlob);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Blob result = userService.getProfileImage(1L);

        assertEquals(mockBlob, result);
    }

    @Test
    void testGetProfileImageReturnsNullWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        Blob result = userService.getProfileImage(99L);

        assertNull(result);
    }

    @Test
    void testGetProfileImageReturnsNullWhenNoPicture() {
        UserModel user = new UserModel();
        user.setId(1L);
        user.setProfilePic(null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Blob result = userService.getProfileImage(1L);

        assertNull(result);
    }
}
