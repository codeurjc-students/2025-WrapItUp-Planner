package es.wrapitup.wrapitup_planner.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
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
    
    @Column(nullable = false)
    private boolean isReported = false;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    public Comment(String content, Note note, UserModel user) {
        this.content = content;
        this.note = note;
        this.user = user;
        this.isReported = false;
        if (user != null) {
            this.userProfilePicUrl = user.getImage();
        }
    }
    
    
    public void setUser(UserModel user) {
        this.user = user;
        if (user != null) {
            this.userProfilePicUrl = user.getImage();
        }
    }
}
