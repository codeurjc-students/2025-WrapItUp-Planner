package es.wrapitup.wrapitup_planner.model;

import jakarta.persistence.*;
import java.util.HashSet;
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


    public Note(UserModel user, String title, String overview, String summary, String jsonQuestions, NoteVisibility visibility) {
        this.user = user;
        this.title = title;
        this.overview = overview;
        this.summary = summary;
        this.jsonQuestions = jsonQuestions;
        this.visibility = visibility;
        this.sharedWith = new HashSet<>();
    }



    public Note() {
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
}
