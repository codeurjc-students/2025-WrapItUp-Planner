package es.wrapitup.wrapitup_planner.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CommentDTO {
    private Long id;
    private String content;
    private Long noteId;
    private Long userId;
    private String username;
    private String userProfilePicUrl;
    private LocalDateTime createdAt;
    private boolean isReported;
}
