package es.wrapitup.wrapitup_planner.dto;

import es.wrapitup.wrapitup_planner.model.NoteVisibility;
import java.util.List;

public class NoteDTO {
    private Long id;
    private String title;
    private String overview;
    private String summary;
    private String jsonQuestions;
    private NoteVisibility visibility;
    private Long userId;
    private List<Long> sharedWithUserIds;
    
    
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
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
    public List<Long> getSharedWithUserIds() {
        return sharedWithUserIds;
    }
    public void setSharedWithUserIds(List<Long> sharedWithUserIds) {
        this.sharedWithUserIds = sharedWithUserIds;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
}
