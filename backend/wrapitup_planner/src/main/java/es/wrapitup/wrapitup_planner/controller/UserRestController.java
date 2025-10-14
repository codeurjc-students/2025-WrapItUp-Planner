package es.wrapitup.wrapitup_planner.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.wrapitup.wrapitup_planner.dto.UserModelDTO;
import es.wrapitup.wrapitup_planner.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {
    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserModelDTO> getUser(@PathVariable Long id) {
        Optional<UserModelDTO> user = userService.findById(id);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

}
