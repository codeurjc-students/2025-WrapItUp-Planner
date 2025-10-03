package es.wrapitup.wrapitup_planner.model;

import jakarta.persistence.*;

@Entity
public class AINote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSummary;
    private String overview;
    private String summary;
    private String jsonQuestions;
    private Boolean visibility;
    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private UserModel user;
    
    
    public AINote(UserModel user, String overview, String summary, String jsonQuestions, Boolean visibility) {
        this.user = user;
        this.overview = overview;
        this.summary = summary;
        this.jsonQuestions = jsonQuestions;
        this.visibility = visibility;
    }
    public AINote() {
    }
    public Long getIdSummary() {
        return idSummary;
    }
    public void setIdSummary(Long idSummary) {
        this.idSummary = idSummary;
    }
    public UserModel getUser() {
        return user;
    }
    public void setUser(UserModel user) {
        this.user = user;
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
    public Boolean getVisibility() {
        return visibility;
    }
    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }
}
