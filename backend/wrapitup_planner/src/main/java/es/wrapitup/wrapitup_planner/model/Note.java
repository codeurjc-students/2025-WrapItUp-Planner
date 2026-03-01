package es.wrapitup.wrapitup_planner.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 100)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String overview;
    
    @Column(columnDefinition = "TEXT")
    private String summary;
    
    @Column(columnDefinition = "TEXT")
    private String jsonQuestions;
    
    @Enumerated(EnumType.STRING)
    private NoteVisibility visibility;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NoteCategory category;
    
    @Column(name = "last_modified", nullable = false)
    private LocalDateTime lastModified;
    
    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private UserModel user;
    
    @ManyToMany
    @JoinTable(
        name = "note_shared_users",
        joinColumns = @JoinColumn(name = "note_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserModel> sharedWith = new HashSet<>();
    
    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    public Note(UserModel user, String title, String overview, String summary, String jsonQuestions, NoteVisibility visibility) {
        this.user = user;
        this.title = title;
        this.overview = overview;
        this.summary = summary;
        this.jsonQuestions = jsonQuestions;
        this.visibility = visibility;
        this.category = NoteCategory.OTHERS;
        this.lastModified = LocalDateTime.now();
        this.sharedWith = new HashSet<>();
        this.comments = new ArrayList<>();
    }
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        if (lastModified == null) {
            lastModified = LocalDateTime.now();
        }
    }
}
