package es.wrapitup.wrapitup_planner.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import es.wrapitup.wrapitup_planner.dto.UserMapper;
import es.wrapitup.wrapitup_planner.dto.UserModelDTO;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.sql.Blob;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }
    

    public Optional<UserModelDTO> findById(Long id) {
        return userRepository.findById(id)
                             .map(userMapper::toDto);
    }

    public List<UserModelDTO> getAllUsers() {
        return userRepository.findAll()
                             .stream()
                             .map(userMapper::toDto)
                             .toList();
    }


    public UserModelDTO findByName(String username) {
    return userRepository.findByUsername(username)
            .map(userMapper::toDto)
            .orElse(null);
    }

    public void createUser(UserModelDTO userDTO) {
        UserModel user = new UserModel(userDTO.getUsername(), userDTO.getEmail(), passwordEncoder.encode(userDTO.getPassword()), UserStatus.ACTIVE , "USER");
        // Set displayName to username by default
        user.setDisplayName(userDTO.getUsername());
        userRepository.save(user);
    }

    public UserModelDTO updateUser(Long id, UserModelDTO userDTO) {
        Optional<UserModel> existingUser = userRepository.findById(id);
        
        if (existingUser.isEmpty()) {
            return null;
        }
        
        UserModel user = existingUser.get();
        
        // Username cannot be changed - it's used for login
        if (userDTO.getDisplayName() != null && !userDTO.getDisplayName().isEmpty()) {
            user.setDisplayName(userDTO.getDisplayName());
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().isEmpty()) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getImage() != null) {
            user.setImage(userDTO.getImage());
        }
        
        UserModel updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public UserModelDTO updateUserWithBlob(Long id, UserModelDTO userDTO, Blob profilePic) {
        Optional<UserModel> existingUser = userRepository.findById(id);
        
        if (existingUser.isEmpty()) {
            return null;
        }
        
        UserModel user = existingUser.get();
        
        if (userDTO.getDisplayName() != null && !userDTO.getDisplayName().isEmpty()) {
            user.setDisplayName(userDTO.getDisplayName());
        }
        if (userDTO.getEmail() != null && !userDTO.getEmail().isEmpty()) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getImage() != null) {
            user.setImage(userDTO.getImage());
        }
        if (profilePic != null) {
            user.setProfilePic(profilePic);
        }
        
        UserModel updatedUser = userRepository.save(user);
        return userMapper.toDto(updatedUser);
    }

    public Blob getProfileImage(Long userId) {
        Optional<UserModel> user = userRepository.findById(userId);
        return user.map(UserModel::getProfilePic).orElse(null);
    }

}
