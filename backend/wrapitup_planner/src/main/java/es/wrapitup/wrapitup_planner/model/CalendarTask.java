package es.wrapitup.wrapitup_planner.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "calendar_tasks")
@Getter
@Setter
@NoArgsConstructor
public class CalendarTask {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private UserModel user;
    
    @Column(nullable = false, length = 100)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "task_date", nullable = false)
    private LocalDate taskDate;
    
    @Column(nullable = false)
    private Boolean completed = false;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    public CalendarTask(UserModel user, String title, String description, LocalDate taskDate) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.taskDate = taskDate;
        this.completed = false;
        this.createdAt = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }
    
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastModified == null) {
            lastModified = LocalDateTime.now();
        }
        if (completed == null) {
            completed = false;
        }
    }
    
    
    public void setCompleted(Boolean completed) {
        this.completed = completed;
        if (completed != null && completed) {
            this.completedAt = LocalDateTime.now();
        } else {
            this.completedAt = null;
        }
    }
}
