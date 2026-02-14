package es.wrapitup.wrapitup_planner.controller;

import java.io.IOException;
import java.security.Principal;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.rowset.serial.SerialBlob;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.wrapitup.wrapitup_planner.dto.UserModelDTO;
import es.wrapitup.wrapitup_planner.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {
    
    private final UserService userService;
    
    public UserRestController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id, HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("You must be authenticated");
        }

        UserModelDTO loggedUser = userService.findByName(principal.getName());

        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Logged user not found");
        }

        // Allow admins to view any user profile
        boolean isAdmin = loggedUser.getRoles() != null && loggedUser.getRoles().contains("ADMIN");
        if (!loggedUser.getId().equals(id) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You cannot access other user's data");
        }

        Optional<UserModelDTO> user = userService.findById(id);
        
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found");
        }

        return ResponseEntity.ok(user.get());
    }

    @GetMapping("")
    public ResponseEntity<?> getLoggedUser(HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("You must be authenticated");
        }

        UserModelDTO loggedUser = userService.findByName(principal.getName());

        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Logged user not found");
        }

        return ResponseEntity.ok(loggedUser);
    }

    @PutMapping("")
    public ResponseEntity<?> updateUser(@RequestBody UserModelDTO userDTO, HttpServletRequest request, HttpServletResponse response) {

        Principal principal = request.getUserPrincipal();

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("You must be authenticated");
        }

        UserModelDTO loggedUser = userService.findByName(principal.getName());

        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Logged user not found");
        }

        // Validate email
        if (userDTO.getEmail() == null || userDTO.getEmail().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email is required");
        }

        UserModelDTO updatedUser = userService.updateUser(loggedUser.getId(), userDTO);

        if (updatedUser == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating user");
        }

        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("image") MultipartFile file,
            HttpServletRequest request) {

        Principal principal = request.getUserPrincipal();

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("You must be authenticated");
        }

        UserModelDTO loggedUser = userService.findByName(principal.getName());

        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Logged user not found");
        }

        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Please select a file to upload");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Only image files are allowed");
        }

        try {
            // Convert file to Blob
            byte[] bytes = file.getBytes();
            Blob blob = new SerialBlob(bytes);

            // Generate URL for the image
            String imageUrl = "/api/v1/users/profile-image/" + loggedUser.getId();

            // Update user's image
            UserModelDTO updatedUserDTO = new UserModelDTO();
            updatedUserDTO.setImage(imageUrl);
            UserModelDTO updatedUser = userService.updateUserWithBlob(loggedUser.getId(), updatedUserDTO, blob);

            return ResponseEntity.ok(updatedUser);

        } catch (IOException | SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload image: " + e.getMessage());
        }
    }

    @GetMapping("/profile-image/{userId}")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable Long userId) {
        try {
            Blob imageBlob = userService.getProfileImage(userId);
            
            if (imageBlob == null) {
                return ResponseEntity.notFound().build();
            }

            byte[] imageBytes = imageBlob.getBytes(1, (int) imageBlob.length());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) 
                    .body(imageBytes);

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
