package es.wrapitup.wrapitup_planner.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import es.wrapitup.wrapitup_planner.model.CalendarEvent;
import es.wrapitup.wrapitup_planner.model.CalendarTask;
import es.wrapitup.wrapitup_planner.model.Comment;
import es.wrapitup.wrapitup_planner.model.EventColor;
import es.wrapitup.wrapitup_planner.model.Note;
import es.wrapitup.wrapitup_planner.model.NoteCategory;
import es.wrapitup.wrapitup_planner.model.NoteVisibility;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.CalendarEventRepository;
import es.wrapitup.wrapitup_planner.repository.CalendarTaskRepository;
import es.wrapitup.wrapitup_planner.repository.CommentRepository;
import es.wrapitup.wrapitup_planner.repository.NoteRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import jakarta.annotation.PostConstruct;

@Service
public class DatabaseInitizalizer {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final CommentRepository commentRepository;
    private final CalendarEventRepository eventRepository;
    private final CalendarTaskRepository taskRepository;
    private final PasswordEncoder passwordEncoder;
    
    public DatabaseInitizalizer(UserRepository userRepository, NoteRepository noteRepository, 
                                CommentRepository commentRepository, CalendarEventRepository eventRepository,
                                CalendarTaskRepository taskRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
        this.commentRepository = commentRepository;
        this.eventRepository = eventRepository;
        this.taskRepository = taskRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Blob saveImageFromFile(String resourcePath) throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
        if (inputStream == null) {
            throw new IOException("File not found in classpath: " + resourcePath);
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }
        
        return BlobProxy.generateProxy(buffer.toByteArray());
    }
}

    @PostConstruct
    public void init() throws IOException{

        // User 1: genericUser
        UserModel user = new UserModel(
            "genericUser", 
            "genericUser@example.com", 
            passwordEncoder.encode("12345678"), 
            UserStatus.ACTIVE, 
            "USER"
        );
        user.setProfilePic(saveImageFromFile("images/calendar.jpg"));
        userRepository.save(user);
        user.setImage("/api/v1/users/profile-image/" + user.getId());
        userRepository.save(user);

        // User 2: secondUser
        UserModel secondUser = new UserModel(
            "secondUser", 
            "secondUser@example.com", 
            passwordEncoder.encode("12345678"), 
            UserStatus.ACTIVE, 
            "USER"
        );
        secondUser.setProfilePic(saveImageFromFile("images/notebook.jpg"));
        userRepository.save(secondUser);
        secondUser.setImage("/api/v1/users/profile-image/" + secondUser.getId());
        userRepository.save(secondUser);

        // admin
        UserModel admin = new UserModel(
            "admin", 
            "admin@example.com", 
            passwordEncoder.encode("12345678"), 
            UserStatus.ACTIVE, 
            "ADMIN", "USER"
        );

        userRepository.save(admin);

        // Notes for genericUser
        Note note1 = new Note(
            user,
            "Pythagorean Theorem",
            "Fundamentals of geometry",
            "The Pythagorean theorem states that in any right triangle, the square of the hypotenuse equals the sum of the squares of the other two sides.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note1.setCategory(NoteCategory.MATHS);

        Note note2 = new Note(
            user,
            "Photosynthesis",
            "Biological process in plants",
            "Photosynthesis is the process by which plants convert sunlight into chemical energy.",
            "{}",
            NoteVisibility.PRIVATE
        );
        note2.setCategory(NoteCategory.SCIENCE);

        Note note3 = new Note(
            user,
            "French Revolution",
            "18th century historical event",
            "The French Revolution was a period of radical social and political change in France that had a lasting impact on the country's and the world's history.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note3.setCategory(NoteCategory.HISTORY);

        Note note4 = new Note(
            user,
            "Italian Renaissance",
            "Cultural and artistic movement",
            "The Renaissance was a period of great cultural flourishing that originated in Italy during the 14th century.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note4.setCategory(NoteCategory.ART);

        Note note5 = new Note(
            user,
            "English Grammar",
            "Basic English rules",
            "Verb tenses in English: simple present, present continuous, simple past, and past continuous.",
            "{}",
            NoteVisibility.PRIVATE
        );
        note5.setCategory(NoteCategory.LANGUAGES);

        Note note12 = new Note(
            user,
            "Newton's Laws",
            "Fundamental principles of motion",
            "Newton's three laws of motion describe the relationship between forces and motion.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note12.setCategory(NoteCategory.SCIENCE);

        Note note13 = new Note(
            user,
            "Roman Empire",
            "Ancient civilization",
            "The Roman Empire was one of the most influential civilizations in world history.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note13.setCategory(NoteCategory.HISTORY);

        Note note14 = new Note(
            user,
            "Calculus Basics",
            "Introduction to derivatives",
            "Calculus is the mathematical study of continuous change, focusing on limits, derivatives, and integrals.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note14.setCategory(NoteCategory.MATHS);

        Note note15 = new Note(
            user,
            "DNA Structure",
            "Molecular biology",
            "DNA is a double helix structure that contains genetic information for all living organisms.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note15.setCategory(NoteCategory.SCIENCE);

        Note note16 = new Note(
            user,
            "Baroque Art",
            "17th century artistic style",
            "Baroque art is characterized by dramatic use of light and shadow, intense emotions, and grandeur.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note16.setCategory(NoteCategory.ART);

        Note note17 = new Note(
            user,
            "German Pronunciation",
            "Language learning guide",
            "Guide to proper German pronunciation including umlauts and consonant combinations.",
            "{}",
            NoteVisibility.PRIVATE
        );
        note17.setCategory(NoteCategory.LANGUAGES);

        Note note18 = new Note(
            user,
            "Study Techniques",
            "Effective learning methods",
            "Various study techniques including spaced repetition, active recall, and the Feynman technique.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note18.setCategory(NoteCategory.OTHERS);

        // Notes for secondUser
        Note note6 = new Note(
            secondUser,
            "Quadratic Equations",
            "Solving second-degree equations",
            "A quadratic equation is a second-degree polynomial equation. The general formula is ax² + bx + c = 0.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note6.setCategory(NoteCategory.MATHS);

        Note note7 = new Note(
            secondUser,
            "Periodic Table",
            "Organization of chemical elements",
            "The periodic table organizes chemical elements according to their atomic number, electron configuration, and chemical properties.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note7.setCategory(NoteCategory.SCIENCE);

        Note note8 = new Note(
            secondUser,
            "World War II",
            "Global military conflict",
            "World War II was a global military conflict that took place between 1939 and 1945.",
            "{}",
            NoteVisibility.PRIVATE
        );
        note8.setCategory(NoteCategory.HISTORY);

        Note note9 = new Note(
            secondUser,
            "Impressionism",
            "19th century art movement",
            "Impressionism is an art movement that emerged in France in the 19th century, characterized by visible brushstrokes and emphasis on light.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note9.setCategory(NoteCategory.ART);

        Note note10 = new Note(
            secondUser,
            "Spanish Irregular Verbs",
            "Conjugation of irregular verbs",
            "List of the most common irregular verbs in Spanish and their conjugations in different tenses.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note10.setCategory(NoteCategory.LANGUAGES);

        Note note11 = new Note(
            secondUser,
            "General Notes",
            "Miscellaneous topics",
            "Various notes on different topics that don't fit into other categories.",
            "{}",
            NoteVisibility.PRIVATE
        );
        note11.setCategory(NoteCategory.OTHERS);

        noteRepository.save(note1);
        noteRepository.save(note2);
        noteRepository.save(note3);
        noteRepository.save(note4);
        noteRepository.save(note5);
        noteRepository.save(note6);
        noteRepository.save(note7);
        noteRepository.save(note8);
        noteRepository.save(note9);
        noteRepository.save(note10);
        noteRepository.save(note11);
        noteRepository.save(note12);
        noteRepository.save(note13);
        noteRepository.save(note14);
        noteRepository.save(note15);
        noteRepository.save(note16);
        noteRepository.save(note17);
        noteRepository.save(note18);

        // Share some notes
        // note1 (genericUser's Pythagorean Theorem) shared with secondUser
        Set<UserModel> sharedWith1 = new HashSet<>();
        sharedWith1.add(secondUser);
        note1.setSharedWith(sharedWith1);
        noteRepository.save(note1);

        // note6 (secondUser's Quadratic Equations) shared with genericUser
        Set<UserModel> sharedWith6 = new HashSet<>();
        sharedWith6.add(user);
        note6.setSharedWith(sharedWith6);
        noteRepository.save(note6);

        // note9 (secondUser's Impresionismo) shared with genericUser
        Set<UserModel> sharedWith9 = new HashSet<>();
        sharedWith9.add(user);
        note9.setSharedWith(sharedWith9);
        noteRepository.save(note9);


        Comment comment1 = new Comment("Useful, thanks", note1, secondUser);
        Comment comment2 = new Comment("Great tips!", note1, user);
        Comment comment3 = new Comment("Very helpful", note1, secondUser);
        comment3.setReported(true);
        Comment comment4 = new Comment("Useful, thanks", note1, user);
        Comment comment5 = new Comment("Nice summary", note1, secondUser);
        Comment comment6 = new Comment("Useful, thanks", note1, user);
        Comment comment7 = new Comment("Good information", note1, secondUser);
        comment7.setReported(true);
        Comment comment8 = new Comment("Useful, thanks", note1, user);
        Comment comment9 = new Comment("Well explained", note1, secondUser);
        Comment comment10 = new Comment("Useful, thanks", note1, user);
        Comment comment11 = new Comment("Thanks for sharing", note1, secondUser);
        Comment comment12 = new Comment("Useful, thanks", note1, user);

        commentRepository.save(comment1);
        commentRepository.save(comment2);
        commentRepository.save(comment3);
        commentRepository.save(comment4);
        commentRepository.save(comment5);
        commentRepository.save(comment6);
        commentRepository.save(comment7);
        commentRepository.save(comment8);
        commentRepository.save(comment9);
        commentRepository.save(comment10);
        commentRepository.save(comment11);
        commentRepository.save(comment12);

        CalendarEvent event1 = new CalendarEvent(
            user,
            "Math Study Session",
            "Group study session for calculus exam",
            LocalDateTime.of(2026, 3, 3, 14, 0),
            LocalDateTime.of(2026, 3, 3, 16, 0),
            EventColor.BLUE,
            false
        );

        CalendarEvent event4 = new CalendarEvent(
            user,
            "History Lecture",
            "Lecture on World War II",
            LocalDateTime.of(2026, 3, 10, 9, 0),
            LocalDateTime.of(2026, 3, 10, 11, 0),
            EventColor.YELLOW,
            false
        );

        CalendarEvent event6 = new CalendarEvent(
            user,
            "Language Workshop",
            "Multi-day German pronunciation workshop",
            LocalDateTime.of(2026, 3, 14, 9, 0),
            LocalDateTime.of(2026, 3, 15, 17, 0),
            EventColor.GREEN,
            false
        );

        CalendarEvent event7 = new CalendarEvent(
            user,
            "Spring Festival",
            "Spring festival celebration",
            LocalDateTime.of(2026, 3, 14, 0, 0),
            LocalDateTime.of(2026, 3, 14, 23, 59),
            EventColor.RED,
            true
        );

        CalendarEvent event11 = new CalendarEvent(
            user,
            "Physics Workshop",
            "Multi-day Newton's laws practical workshop",
            LocalDateTime.of(2026, 3, 21, 9, 0),
            LocalDateTime.of(2026, 3, 22, 16, 0),
            EventColor.GREEN,
            false
        );

        CalendarEvent event13 = new CalendarEvent(
            user,
            "Project Deadline",
            "Multi-day final project submission period",
            LocalDateTime.of(2026, 3, 24, 0, 0),
            LocalDateTime.of(2026, 3, 26, 23, 59),
            EventColor.RED,
            true
        );

        CalendarEvent event14 = new CalendarEvent(
            user,
            "Study Review",
            "Multi-day comprehensive review session before exams",
            LocalDateTime.of(2026, 3, 27, 9, 0),
            LocalDateTime.of(2026, 3, 28, 17, 0),
            EventColor.BLUE,
            false
        );

        eventRepository.save(event1);
        eventRepository.save(event4);
        eventRepository.save(event6);
        eventRepository.save(event7);
        eventRepository.save(event11);
        eventRepository.save(event13);
        eventRepository.save(event14);

        CalendarTask task1 = new CalendarTask(
            user,
            "Read Chapter 5 - Calculus",
            "Read and take notes on differential equations",
            LocalDate.of(2026, 3, 2)
        );
        task1.setCompleted(true);

        CalendarTask task2 = new CalendarTask(
            user,
            "Prepare Lab Report",
            "Write lab report for biology experiment",
            LocalDate.of(2026, 3, 6)
        );
        task2.setCompleted(true);

        CalendarTask task3 = new CalendarTask(
            user,
            "Review History Notes",
            "Review notes from last week's lecture",
            LocalDate.of(2026, 3, 8)
        );
        task3.setCompleted(true);

        CalendarTask task4 = new CalendarTask(
            user,
            "Practice German Vocabulary",
            "Study 50 new German words",
            LocalDate.of(2026, 3, 11)
        );
        task4.setCompleted(true);

        CalendarTask task5 = new CalendarTask(
            user,
            "Complete Math Assignment",
            "Solve problems 1-20 from textbook",
            LocalDate.of(2026, 3, 13)
        );
        task5.setCompleted(true);

        CalendarTask task6 = new CalendarTask(
            user,
            "Buy Art Supplies",
            "Purchase canvas and paints for art project",
            LocalDate.of(2026, 3, 15)
        );
        task6.setCompleted(true);

        CalendarTask task7 = new CalendarTask(
            user,
            "Research Renaissance Art",
            "Research for art history presentation",
            LocalDate.of(2026, 3, 17)
        );
        task7.setCompleted(false);

        CalendarTask task8 = new CalendarTask(
            user,
            "Study Physics Formulas",
            "Memorize Newton's laws and formulas",
            LocalDate.of(2026, 3, 19)
        );
        task8.setCompleted(false);

        CalendarTask task9 = new CalendarTask(
            user,
            "Create Project Presentation",
            "Prepare slides for group project",
            LocalDate.of(2026, 3, 22)
        );
        task9.setCompleted(false);

        CalendarTask task10 = new CalendarTask(
            user,
            "Submit Project Report",
            "Final submission of group project report",
            LocalDate.of(2026, 3, 25)
        );
        task10.setCompleted(false);

        CalendarTask task11 = new CalendarTask(
            user,
            "Review All Course Material",
            "Comprehensive review for final exams",
            LocalDate.of(2026, 3, 26)
        );
        task11.setCompleted(false);

        CalendarTask task12 = new CalendarTask(
            user,
            "Organize Study Materials",
            "Organize all notes and materials for exam week",
            LocalDate.of(2026, 3, 28)
        );
        task12.setCompleted(false);

        
        CalendarTask task13 = new CalendarTask(
            user,
            "Morning Exercise",
            "",
            LocalDate.of(2026, 3, 5)
        );
        task13.setCompleted(false);

        CalendarTask task14 = new CalendarTask(
            user,
            "Email Professor",
            "",
            LocalDate.of(2026, 3, 5)
        );
        task14.setCompleted(false);

        CalendarTask task15 = new CalendarTask(
            user,
            "Library Books Return",
            "",
            LocalDate.of(2026, 3, 5)
        );
        task15.setCompleted(true);

        CalendarTask task16 = new CalendarTask(
            user,
            "Grocery Shopping",
            "",
            LocalDate.of(2026, 3, 12)
        );
        task16.setCompleted(false);

        CalendarTask task17 = new CalendarTask(
            user,
            "Call Family",
            "",
            LocalDate.of(2026, 3, 12)
        );
        task17.setCompleted(false);
        

        CalendarTask task18 = new CalendarTask(
            user,
            "Submit assignment",
            "",
            LocalDate.of(2026, 3, 12)
        );

        task18.setCompleted(false);

        taskRepository.save(task1);
        taskRepository.save(task2);
        taskRepository.save(task3);
        taskRepository.save(task4);
        taskRepository.save(task5);
        taskRepository.save(task6);
        taskRepository.save(task7);
        taskRepository.save(task8);
        taskRepository.save(task9);
        taskRepository.save(task10);
        taskRepository.save(task11);
        taskRepository.save(task12);
        taskRepository.save(task13);
        taskRepository.save(task14);
        taskRepository.save(task15);
        taskRepository.save(task16);
        taskRepository.save(task17);
        taskRepository.save(task18);
    }
}
