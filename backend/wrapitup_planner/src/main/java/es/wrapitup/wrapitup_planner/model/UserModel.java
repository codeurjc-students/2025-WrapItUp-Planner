package es.wrapitup.wrapitup_planner.model;

import java.util.List;
import jakarta.persistence.*;
import java.sql.Blob;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String displayName;
    private String email;
    private String password;
    private String image;
    
    @Lob
    private Blob profilePic;
    
    private List<String> roles;
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Note> notes;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CalendarEvent> calendarEvents;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CalendarTask> calendarTasks;
    
    public UserModel(String username, String email, String password, UserStatus status, String... roles) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.roles = List.of(roles);
        this.status = status;
        this.notes = null;
    }
}