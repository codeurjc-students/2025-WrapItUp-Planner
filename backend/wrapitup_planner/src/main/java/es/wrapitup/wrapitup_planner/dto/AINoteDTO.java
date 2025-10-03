package es.wrapitup.wrapitup_planner.dto;

public class AINoteDTO {
    private Long idSummary;
    private String overview;
    private String summary;
    private String jsonQuestions;
    private Boolean visibility;
    private Long userId;
    
    public Long getIdSummary() {
        return idSummary;
    }
    public void setIdSummary(Long idSummary) {
        this.idSummary = idSummary;
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
    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
