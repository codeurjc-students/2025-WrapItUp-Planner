package es.wrapitup.wrapitup_planner.model;

import java.util.List;

import jakarta.persistence.*;

@Entity
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String passwordHashed;
    private List<String> roles; 
    private String status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AINote> notes;
    public UserModel(String username, String email, String passwordHashed, String status, String... roles) {
        this.username = username;
        this.email = email;
        this.passwordHashed = passwordHashed;
        this.roles = List.of(roles);
        this.status = status;
        this.notes = null;
    }
    public UserModel() {
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
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
    public List<String> getRoles() {
		return roles;
	}

    public void setRoles(List<String> roles) {
        this.roles = roles;
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