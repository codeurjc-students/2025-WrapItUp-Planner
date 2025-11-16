package es.wrapitup.wrapitup_planner.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.wrapitup.wrapitup_planner.dto.UserMapper;
import es.wrapitup.wrapitup_planner.dto.UserModelDTO;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

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
        userRepository.save(user);
    }

}
