package es.wrapitup.wrapitup_planner.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import es.wrapitup.wrapitup_planner.model.Note;
import es.wrapitup.wrapitup_planner.model.NoteCategory;
import es.wrapitup.wrapitup_planner.model.NoteVisibility;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.model.Comment;
import es.wrapitup.wrapitup_planner.repository.NoteRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import es.wrapitup.wrapitup_planner.repository.CommentRepository;
import jakarta.annotation.PostConstruct;

@Service
public class DatabaseInitizalizer {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;
    
    public DatabaseInitizalizer(UserRepository userRepository, NoteRepository noteRepository, 
                                CommentRepository commentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
        this.commentRepository = commentRepository;
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
        Comment comment4 = new Comment("Useful, thanks", note1, user);
        Comment comment5 = new Comment("Nice summary", note1, secondUser);
        Comment comment6 = new Comment("Useful, thanks", note1, user);
        Comment comment7 = new Comment("Good information", note1, secondUser);
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
    }
}
