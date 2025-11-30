package es.wrapitup.wrapitup_planner.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Comment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @ManyToOne
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private String userProfilePicUrl;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public Comment() {
    }
    
    public Comment(String content, Note note, UserModel user) {
        this.content = content;
        this.note = note;
        this.user = user;
        if (user != null) {
            this.userProfilePicUrl = user.getImage();
        }
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Note getNote() {
        return note;
    }
    
    public void setNote(Note note) {
        this.note = note;
    }
    
    public UserModel getUser() {
        return user;
    }
    
    public void setUser(UserModel user) {
        this.user = user;
        if (user != null) {
            this.userProfilePicUrl = user.getImage();
        }
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getUserProfilePicUrl() {
        return userProfilePicUrl;
    }
    
    public void setUserProfilePicUrl(String userProfilePicUrl) {
        this.userProfilePicUrl = userProfilePicUrl;
    }
}
