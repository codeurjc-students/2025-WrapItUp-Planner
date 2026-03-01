package es.wrapitup.wrapitup_planner.dto;

import es.wrapitup.wrapitup_planner.model.NoteCategory;
import es.wrapitup.wrapitup_planner.model.NoteVisibility;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class NoteDTO {
    private Long id;
    private String title;
    private String overview;
    private String summary;
    private String jsonQuestions;
    private NoteVisibility visibility;
    private NoteCategory category;
    private LocalDateTime lastModified;
    private Long userId;
    private List<Long> sharedWithUserIds;
    private List<CommentDTO> comments = new ArrayList<>();
}
