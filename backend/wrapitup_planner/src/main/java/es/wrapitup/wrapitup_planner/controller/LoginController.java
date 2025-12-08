package es.wrapitup.wrapitup_planner.controller;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.wrapitup.wrapitup_planner.dto.UserModelDTO;
import es.wrapitup.wrapitup_planner.security.jwt.AuthResponse;
import es.wrapitup.wrapitup_planner.security.jwt.AuthResponse.Status;
import es.wrapitup.wrapitup_planner.security.jwt.LoginRequest;
import es.wrapitup.wrapitup_planner.security.jwt.UserLoginService;
import es.wrapitup.wrapitup_planner.service.UserService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

    private final UserService userService;
    private final UserLoginService userLoginService;
    
    public LoginController(UserService userService, UserLoginService userLoginService) {
        this.userService = userService;
        this.userLoginService = userLoginService;
    }

    @PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
			@RequestBody LoginRequest loginRequest,
			HttpServletResponse response) {
		
		return userLoginService.login(response, loginRequest);
	}

    @PostMapping("/refresh")
	public ResponseEntity<AuthResponse> refreshToken(
			@CookieValue(name = "RefreshToken", required = false) String refreshToken, HttpServletResponse response) {

		return userLoginService.refresh(response, refreshToken);
	}

    @PostMapping("/logout")
	public ResponseEntity<AuthResponse> logOut(HttpServletResponse response) {
		return ResponseEntity.ok(new AuthResponse(Status.SUCCESS, userLoginService.logout(response)));
	}

    @PostMapping("/user")
    public ResponseEntity<?> register(@RequestBody UserModelDTO userDTO) {

        if (userDTO.getUsername() == null || userDTO.getUsername().isBlank() ||
            userDTO.getEmail() == null || userDTO.getEmail().isBlank() ||
            userDTO.getPassword() == null || userDTO.getPassword().isBlank()) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Missing or blank fields"));
        }

        if (userDTO.getPassword().length() < 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Password must be at least 8 characters long"));
        }

        if (userService.findByName(userDTO.getUsername()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "User already exists"));
        }

        userService.createUser(userDTO);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/v1/users/{username}")
                .buildAndExpand(userDTO.getUsername())
                .toUri();

        return ResponseEntity.status(HttpStatus.CREATED)
                .location(location)
                .body(Map.of("message", "User registered successfully"));
    }



}
