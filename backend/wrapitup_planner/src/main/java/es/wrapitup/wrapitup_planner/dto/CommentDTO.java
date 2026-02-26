package es.wrapitup.wrapitup_planner.dto;

import java.time.LocalDateTime;

public class CommentDTO {
    
    private Long id;
    private String content;
    private Long noteId;
    private Long userId;
    private String username;
    private String userProfilePicUrl;
    private LocalDateTime createdAt;
    private boolean isReported;
    
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
    
    public Long getNoteId() {
        return noteId;
    }
    
    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getUserProfilePicUrl() {
        return userProfilePicUrl;
    }
    
    public void setUserProfilePicUrl(String userProfilePicUrl) {
        this.userProfilePicUrl = userProfilePicUrl;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public boolean isReported() {
        return isReported;
    }
    
    public void setReported(boolean reported) {
        this.isReported = reported;
    }
}
