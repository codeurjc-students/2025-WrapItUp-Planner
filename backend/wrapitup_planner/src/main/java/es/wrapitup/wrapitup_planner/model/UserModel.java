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
    private String password;
    private List<String> roles; 
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AINote> notes;
    public UserModel(String username, String email, String password, UserStatus status, String... roles) {
        this.username = username;
        this.email = email;
        this.password = password;
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
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public List<String> getRoles() {
		return roles;
	}

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    public UserStatus getStatus() {
        return status;
    }
    public void setStatus(UserStatus status) {
        this.status = status;
    }
    public List<AINote> getNotes() {
        return notes;
    }
    public void setNotes(List<AINote> notes) {
        this.notes = notes;
    }

}