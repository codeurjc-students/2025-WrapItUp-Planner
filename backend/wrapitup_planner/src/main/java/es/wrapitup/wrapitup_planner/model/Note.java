package es.wrapitup.wrapitup_planner.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
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



    public Note() {
        this.lastModified = LocalDateTime.now();
    }
    
    public UserModel getUser() {
        return user;
    }
    public void setUser(UserModel user) {
        this.user = user;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getOverview() {
        return overview;
    }
    public void setOverview(String overview) {
        this.overview = overview;
    }
    public String getSummary() {
        return summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }
    public String getJsonQuestions() {
        return jsonQuestions;
    }
    public void setJsonQuestions(String jsonQuestions) {
        this.jsonQuestions = jsonQuestions;
    }
    public NoteVisibility getVisibility() {
        return visibility;
    }
    public void setVisibility(NoteVisibility visibility) {
        this.visibility = visibility;
    }
    
    public Set<UserModel> getSharedWith() {
        return sharedWith;
    }
    public void setSharedWith(Set<UserModel> sharedWith) {
        this.sharedWith = sharedWith;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public List<Comment> getComments() {
        return comments;
    }
    
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
    
    public NoteCategory getCategory() {
        return category;
    }
    
    public void setCategory(NoteCategory category) {
        this.category = category;
    }
    
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
}
