package es.wrapitup.wrapitup_planner.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.wrapitup.wrapitup_planner.dto.UserMapper;
import es.wrapitup.wrapitup_planner.dto.UserModelDTO;
import es.wrapitup.wrapitup_planner.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
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

}
