package es.wrapitup.wrapitup_planner.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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

            UserModel user = new UserModel(
            "genericUser",
            "genericUser@example.com",
            passwordEncoder.encode("12345678"),
            UserStatus.ACTIVE,
            "USER"
        );

        user.setProfilePic(saveImageFromFile("images/calendar.jpg"));

        user = userRepository.save(user);

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

        // User 3: thirdUser
        UserModel thirdUser = new UserModel(
            "thirdUser",
            "thirdUser@example.com",
            passwordEncoder.encode("12345678"),
            UserStatus.ACTIVE,
            "USER"
        );
        thirdUser.setProfilePic(saveImageFromFile("images/calendar.jpg"));
        userRepository.save(thirdUser);
        thirdUser.setImage("/api/v1/users/profile-image/" + thirdUser.getId());
        userRepository.save(thirdUser);

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
                String note1JsonQuestions = """
                        {
                            "questions": [
                                {
                                    "question": "What kind of triangle does the Pythagorean theorem apply to?",
                                    "options": ["Equilateral triangle", "Right triangle", "Isosceles triangle", "Scalene triangle"],
                                    "correctOptionIndex": 1
                                },
                                {
                                    "question": "What relationship does the theorem describe?",
                                    "options": ["Angles and area", "Sides and the hypotenuse", "Perimeter and radius", "Base and height"],
                                    "correctOptionIndex": 1
                                },
                                {
                                    "question": "Which formula represents the theorem?",
                                    "options": ["a + b = c", "a^2 + b^2 = c^2", "a^2 - b^2 = c^2", "2a + 2b = c"],
                                    "correctOptionIndex": 1
                                },
                                {
                                    "question": "What is the hypotenuse?",
                                    "options": ["The shortest side", "The side opposite the right angle", "Any side in the triangle", "The line inside the triangle"],
                                    "correctOptionIndex": 1
                                },
                                {
                                    "question": "If the legs are 3 and 4, what is the hypotenuse?",
                                    "options": ["5", "6", "7", "8"],
                                    "correctOptionIndex": 0
                                }
                            ]
                        }
                        """;

                String note2JsonQuestions = """
                        {
                            "questions": [
                                {
                                    "question": "What do plants convert sunlight into during photosynthesis?",
                                    "options": ["Mechanical energy", "Chemical energy", "Electrical energy", "Thermal energy"],
                                    "correctOptionIndex": 1
                                },
                                {
                                    "question": "Which organelle is mainly responsible for photosynthesis?",
                                    "options": ["Nucleus", "Mitochondria", "Chloroplast", "Ribosome"],
                                    "correctOptionIndex": 2
                                },
                                {
                                    "question": "Which gas do plants take in for photosynthesis?",
                                    "options": ["Oxygen", "Nitrogen", "Carbon dioxide", "Helium"],
                                    "correctOptionIndex": 2
                                },
                                {
                                    "question": "Which substance is commonly produced and stored as energy?",
                                    "options": ["Glucose", "Salt", "Protein", "Water"],
                                    "correctOptionIndex": 0
                                },
                                {
                                    "question": "What is released as a byproduct of photosynthesis?",
                                    "options": ["Carbon monoxide", "Oxygen", "Hydrogen", "Methane"],
                                    "correctOptionIndex": 1
                                }
                            ]
                        }
                        """;

                String note3JsonQuestions = """
                        {
                            "questions": [
                                {
                                    "question": "In which country did the French Revolution begin?",
                                    "options": ["Italy", "France", "Spain", "Germany"],
                                    "correctOptionIndex": 1
                                },
                                {
                                    "question": "During which century did the French Revolution take place?",
                                    "options": ["16th century", "17th century", "18th century", "19th century"],
                                    "correctOptionIndex": 2
                                },
                                {
                                    "question": "What kind of change did the French Revolution bring?",
                                    "options": ["Only economic change", "Radical social and political change", "Only artistic change", "No lasting change"],
                                    "correctOptionIndex": 1
                                },
                                {
                                    "question": "What historical system was strongly challenged during the revolution?",
                                    "options": ["Feudalism", "Monarchy", "Republic", "Democracy"],
                                    "correctOptionIndex": 1
                                },
                                {
                                    "question": "The French Revolution had an impact on",
                                    "options": ["Only France", "Only Europe", "France and world history", "Only the military"],
                                    "correctOptionIndex": 2
                                }
                            ]
                        }
                        """;

                String note4JsonQuestions = """
                        {
                            "questions": [
                                {
                                    "question": "Where did the Italian Renaissance originate?",
                                    "options": ["France", "Italy", "England", "Portugal"],
                                    "correctOptionIndex": 1
                                },
                                {
                                    "question": "During which century did the Renaissance begin?",
                                    "options": ["12th century", "13th century", "14th century", "16th century"],
                                    "correctOptionIndex": 2
                                },
                                {
                                    "question": "What was the Renaissance characterized by?",
                                    "options": ["Cultural flourishing", "Industrial decline", "Only military growth", "Religious isolation"],
                                    "correctOptionIndex": 0
                                },
                                {
                                    "question": "Which area was strongly associated with the Renaissance?",
                                    "options": ["Art and culture", "Space exploration", "Computer science", "Automobile design"],
                                    "correctOptionIndex": 0
                                },
                                {
                                    "question": "The Renaissance is best described as a period of",
                                    "options": ["Cultural stagnation", "Great cultural flourishing", "Only political unrest", "Economic collapse"],
                                    "correctOptionIndex": 1
                                }
                            ]
                        }
                        """;

                Note note1 = new Note(
            user,
            "Pythagorean Theorem",
            "Fundamentals of geometry",
            "The Pythagorean theorem states that in any right triangle, the square of the hypotenuse equals the sum of the squares of the other two sides.",
                        note1JsonQuestions,
            NoteVisibility.PUBLIC
        );
        note1.setCategory(NoteCategory.MATHS);

        Note note2 = new Note(
            user,
            "Photosynthesis",
            "Biological process in plants",
            "Photosynthesis is the process by which plants convert sunlight into chemical energy.",
            note2JsonQuestions,
            NoteVisibility.PRIVATE
        );
        note2.setCategory(NoteCategory.SCIENCE);

        Note note3 = new Note(
            user,
            "French Revolution",
            "18th century historical event",
            "The French Revolution was a period of radical social and political change in France that had a lasting impact on the country's and the world's history.",
            note3JsonQuestions,
            NoteVisibility.PUBLIC
        );
        note3.setCategory(NoteCategory.HISTORY);

        Note note4 = new Note(
            user,
            "Italian Renaissance",
            "Cultural and artistic movement",
            "The Renaissance was a period of great cultural flourishing that originated in Italy during the 14th century.",
            note4JsonQuestions,
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

        Note note21 = new Note(
            user,
            "Creative Writing",
            "Short writing ideas",
            "Ideas for stories, essays, and imaginative writing exercises.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note21.setCategory(NoteCategory.OTHERS);

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

        Note note22 = new Note(
            secondUser,
            "Organic Chemistry",
            "Functional groups and reactions",
            "Overview of alkanes, alkenes, alcohols, acids, and common reaction patterns.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note22.setCategory(NoteCategory.SCIENCE);

        Note note23 = new Note(
            secondUser,
            "World History Timeline",
            "Major historical milestones",
            "A timeline of major events from antiquity to the modern era.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note23.setCategory(NoteCategory.HISTORY);

        Note note19 = new Note(
            thirdUser,
            "Project Ideas",
            "Brainstorming notes",
            "A collection of ideas for upcoming projects and experiments.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note19.setCategory(NoteCategory.OTHERS);

        Note note20 = new Note(
            thirdUser,
            "Machine Learning Basics",
            "Introductory concepts",
            "Basic concepts about supervised learning, classification, and model evaluation.",
            "{}",
            NoteVisibility.PRIVATE
        );
        note20.setCategory(NoteCategory.SCIENCE);

        Note note24 = new Note(
            thirdUser,
            "Data Structures",
            "Core programming concepts",
            "Notes about arrays, linked lists, stacks, queues, trees, and hash maps.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note24.setCategory(NoteCategory.OTHERS);

        Note note25 = new Note(
            thirdUser,
            "Statistics Basics",
            "Intro to data analysis",
            "Core ideas about averages, variance, probability, and data interpretation.",
            "{}",
            NoteVisibility.PUBLIC
        );
        note25.setCategory(NoteCategory.SCIENCE);

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
        noteRepository.save(note21);
        noteRepository.save(note22);
        noteRepository.save(note23);
        noteRepository.save(note19);
        noteRepository.save(note20);
        noteRepository.save(note24);
        noteRepository.save(note25);
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

        // thirdUser comments on notes from genericUser and secondUser
        Comment comment13 = new Comment("Nice explanation, very clear", note1, thirdUser);
        Comment comment14 = new Comment("This helped me understand it better", note6, thirdUser);
        Comment comment15 = new Comment("I like how this is structured", note3, thirdUser);
        Comment comment16 = new Comment("Great overview, thanks for sharing", note22, user);
        Comment comment17 = new Comment("The timeline format works well", note23, user);
        Comment comment18 = new Comment("Good intro to the topic", note24, user);

        commentRepository.save(comment13);
        commentRepository.save(comment14);
        commentRepository.save(comment15);
        commentRepository.save(comment16);
        commentRepository.save(comment17);
        commentRepository.save(comment18);


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
        Comment comment19 = new Comment("Very complete notes", note22, secondUser);
        Comment comment20 = new Comment("This is a solid summary", note23, thirdUser);
        Comment comment21 = new Comment("These ideas are very useful", note21, secondUser);
        Comment comment22 = new Comment("Could use a little more detail", note24, user);
        Comment comment23 = new Comment("This explanation feels incomplete", note25, secondUser);
        Comment comment24 = new Comment("I do not agree with this approach", note19, thirdUser);

        comment22.setReported(true);
        comment23.setReported(true);
        comment24.setReported(true);

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
        commentRepository.save(comment19);
        commentRepository.save(comment20);
        commentRepository.save(comment21);
        commentRepository.save(comment22);
        commentRepository.save(comment23);
        commentRepository.save(comment24);

        CalendarEvent event1 = new CalendarEvent(
            user,
            "Math Study Session",
            "Group study session for calculus exam",
            currentMonthDateTime(3, 14, 0),
            currentMonthDateTime(3, 16, 0),
            EventColor.BLUE,
            false
        );

        CalendarEvent event4 = new CalendarEvent(
            user,
            "History Lecture",
            "Lecture on World War II",
            currentMonthDateTime(10, 9, 0),
            currentMonthDateTime(10, 11, 0),
            EventColor.YELLOW,
            false
        );

        CalendarEvent event6 = new CalendarEvent(
            user,
            "Language Workshop",
            "Multi-day German pronunciation workshop",
            currentMonthDateTime(14, 9, 0),
            currentMonthDateTime(15, 17, 0),
            EventColor.GREEN,
            false
        );

        CalendarEvent event7 = new CalendarEvent(
            user,
            "Spring Festival",
            "Spring festival celebration",
            currentMonthDateTime(14, 0, 0),
            currentMonthDateTime(14, 23, 59),
            EventColor.RED,
            true
        );

        CalendarEvent event11 = new CalendarEvent(
            user,
            "Physics Workshop",
            "Multi-day Newton's laws practical workshop",
            currentMonthDateTime(21, 9, 0),
            currentMonthDateTime(22, 16, 0),
            EventColor.GREEN,
            false
        );

        CalendarEvent event13 = new CalendarEvent(
            user,
            "Project Deadline",
            "Multi-day final project submission period",
            currentMonthDateTime(24, 0, 0),
            currentMonthDateTime(26, 23, 59),
            EventColor.RED,
            true
        );

        CalendarEvent event14 = new CalendarEvent(
            user,
            "Study Review",
            "Multi-day comprehensive review session before exams",
            currentMonthDateTime(27, 9, 0),
            currentMonthDateTime(28, 17, 0),
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

        CalendarEvent event15 = new CalendarEvent(
            secondUser,
            "Chemistry Lab Prep",
            "Prepare materials for the organic chemistry lab",
            currentMonthDateTime(8, 8, 30),
            currentMonthDateTime(8, 10, 30),
            EventColor.YELLOW,
            false
        );

        CalendarEvent event16 = new CalendarEvent(
            secondUser,
            "History Debate",
            "Debate session about key historical events",
            currentMonthDateTime(19, 16, 0),
            currentMonthDateTime(19, 18, 0),
            EventColor.BLUE,
            false
        );

        CalendarEvent event17 = new CalendarEvent(
            thirdUser,
            "Coding Practice",
            "Practice session for data structures and algorithms",
            currentMonthDateTime(9, 11, 0),
            currentMonthDateTime(9, 13, 0),
            EventColor.GREEN,
            false
        );

        CalendarEvent event18 = new CalendarEvent(
            thirdUser,
            "Statistics Revision",
            "Review probability and statistics basics",
            currentMonthDateTime(23, 17, 0),
            currentMonthDateTime(23, 18, 30),
            EventColor.RED,
            false
        );

        eventRepository.save(event15);
        eventRepository.save(event16);
        eventRepository.save(event17);
        eventRepository.save(event18);

        CalendarTask task1 = new CalendarTask(
            user,
            "Read Chapter 5 - Calculus",
            "Read and take notes on differential equations",
            currentMonthDate(2)
        );
        task1.setCompleted(true);

        CalendarTask task2 = new CalendarTask(
            user,
            "Prepare Lab Report",
            "Write lab report for biology experiment",
            currentMonthDate(6)
        );
        task2.setCompleted(true);

        CalendarTask task3 = new CalendarTask(
            user,
            "Review History Notes",
            "Review notes from last week's lecture",
            currentMonthDate(8)
        );
        task3.setCompleted(true);

        CalendarTask task4 = new CalendarTask(
            user,
            "Practice German Vocabulary",
            "Study 50 new German words",
            currentMonthDate(11)
        );
        task4.setCompleted(true);

        CalendarTask task5 = new CalendarTask(
            user,
            "Complete Math Assignment",
            "Solve problems 1-20 from textbook",
            currentMonthDate(13)
        );
        task5.setCompleted(true);

        CalendarTask task6 = new CalendarTask(
            user,
            "Buy Art Supplies",
            "Purchase canvas and paints for art project",
            currentMonthDate(15)
        );
        task6.setCompleted(true);

        CalendarTask task7 = new CalendarTask(
            user,
            "Research Renaissance Art",
            "Research for art history presentation",
            currentMonthDate(17)
        );
        task7.setCompleted(false);

        CalendarTask task8 = new CalendarTask(
            user,
            "Study Physics Formulas",
            "Memorize Newton's laws and formulas",
            currentMonthDate(19)
        );
        task8.setCompleted(false);

        CalendarTask task9 = new CalendarTask(
            user,
            "Create Project Presentation",
            "Prepare slides for group project",
            currentMonthDate(22)
        );
        task9.setCompleted(false);

        CalendarTask task10 = new CalendarTask(
            user,
            "Submit Project Report",
            "Final submission of group project report",
            currentMonthDate(25)
        );
        task10.setCompleted(false);

        CalendarTask task11 = new CalendarTask(
            user,
            "Review All Course Material",
            "Comprehensive review for final exams",
            currentMonthDate(26)
        );
        task11.setCompleted(false);

        CalendarTask task12 = new CalendarTask(
            user,
            "Organize Study Materials",
            "Organize all notes and materials for exam week",
            currentMonthDate(28)
        );
        task12.setCompleted(false);

        
        CalendarTask task13 = new CalendarTask(
            user,
            "Morning Exercise",
            "",
            currentMonthDate(5)
        );
        task13.setCompleted(false);

        CalendarTask task14 = new CalendarTask(
            user,
            "Email Professor",
            "",
            currentMonthDate(5)
        );
        task14.setCompleted(false);

        CalendarTask task15 = new CalendarTask(
            user,
            "Library Books Return",
            "",
            currentMonthDate(5)
        );
        task15.setCompleted(true);

        CalendarTask task16 = new CalendarTask(
            user,
            "Grocery Shopping",
            "",
            currentMonthDate(12)
        );
        task16.setCompleted(false);

        CalendarTask task17 = new CalendarTask(
            user,
            "Call Family",
            "",
            currentMonthDate(12)
        );
        task17.setCompleted(false);
        

        CalendarTask task18 = new CalendarTask(
            user,
            "Submit assignment",
            "",
            currentMonthDate(12)
        );

        task18.setCompleted(false);

        CalendarTask task19 = new CalendarTask(
            secondUser,
            "Review Chemistry Notes",
            "Go through organic chemistry reactions and examples",
            currentMonthDate(7)
        );
        task19.setCompleted(true);

        CalendarTask task20 = new CalendarTask(
            secondUser,
            "Prepare History Summary",
            "Summarize the main milestones from the world history timeline",
            currentMonthDate(14)
        );
        task20.setCompleted(false);

        CalendarTask task21 = new CalendarTask(
            secondUser,
            "Language Review",
            "Practice irregular verbs and vocabulary",
            currentMonthDate(21)
        );
        task21.setCompleted(true);

        CalendarTask task22 = new CalendarTask(
            thirdUser,
            "Build Practice App",
            "Create a small app using stacks and queues",
            currentMonthDate(9)
        );
        task22.setCompleted(false);

        CalendarTask task23 = new CalendarTask(
            thirdUser,
            "Statistics Exercises",
            "Solve exercises about averages and variance",
            currentMonthDate(16)
        );
        task23.setCompleted(true);

        CalendarTask task24 = new CalendarTask(
            thirdUser,
            "Machine Learning Reading",
            "Read about supervised learning and evaluation metrics",
            currentMonthDate(24)
        );
        task24.setCompleted(false);

        CalendarEvent thirdUserEvent1 = new CalendarEvent(
            thirdUser,
            "Third User Workshop",
            "Hands-on practice session for project ideas",
            currentMonthDateTime(7, 10, 0),
            currentMonthDateTime(7, 12, 0),
            EventColor.YELLOW,
            false
        );

        CalendarEvent thirdUserEvent2 = new CalendarEvent(
            thirdUser,
            "Model Review Meeting",
            "Review meeting for machine learning basics",
            currentMonthDateTime(18, 15, 0),
            currentMonthDateTime(18, 16, 30),
            EventColor.BLUE,
            false
        );

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
        taskRepository.save(task19);
        taskRepository.save(task20);
        taskRepository.save(task21);
        taskRepository.save(task22);
        taskRepository.save(task23);
        taskRepository.save(task24);

        eventRepository.save(thirdUserEvent1);
        eventRepository.save(thirdUserEvent2);
    }

    private LocalDate currentMonthDate(int dayOfMonth) {
        YearMonth currentMonth = YearMonth.now();
        int safeDay = Math.min(dayOfMonth, currentMonth.lengthOfMonth());
        return LocalDate.of(currentMonth.getYear(), currentMonth.getMonth(), safeDay);
    }

    private LocalDateTime currentMonthDateTime(int dayOfMonth, int hour, int minute) {
        LocalDate date = currentMonthDate(dayOfMonth);
        return LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), hour, minute);
    }
}
