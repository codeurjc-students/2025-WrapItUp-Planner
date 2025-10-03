package es.wrapitup.wrapitup_planner.model;

import java.util.List;

import jakarta.persistence.*;

@Entity
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUser;
    private String username;
    private String email;
    private String passwordHashed;
    private String role;  
    private String status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AINote> notes;
    public UserModel(String username, String email, String passwordHashed, String role, String status) {
        this.username = username;
        this.email = email;
        this.passwordHashed = passwordHashed;
        this.role = role;
        this.status = status;
        this.notes = null;
    }
    public UserModel() {
    }
    public Long getIdUser() {
        return idUser;
    }
    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPasswordHashed() {
        return passwordHashed;
    }
    public void setPasswordHashed(String passwordHashed) {
        this.passwordHashed = passwordHashed;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public List<AINote> getNotes() {
        return notes;
    }
    public void setNotes(List<AINote> notes) {
        this.notes = notes;
    }

}