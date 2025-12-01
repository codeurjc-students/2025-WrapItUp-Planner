package es.wrapitup.wrapitup_planner.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import es.wrapitup.wrapitup_planner.model.Note;
import es.wrapitup.wrapitup_planner.model.NoteVisibility;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.NoteRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import jakarta.annotation.PostConstruct;

@Service
public class DatabaseInitizalizer {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final PasswordEncoder passwordEncoder;
    
    public DatabaseInitizalizer(UserRepository userRepository, NoteRepository noteRepository, 
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init(){

        // regular user
        UserModel user = new UserModel(
        "genericUser", 
        "genericUser@example.com", 
        passwordEncoder.encode("12345678"), 
        UserStatus.ACTIVE, 
        "USER"
        );

        // admin
        UserModel admin = new UserModel(
        "admin", 
        "admin@example.com", 
        passwordEncoder.encode("admin123"), 
        UserStatus.ACTIVE, 
        "ADMIN", "USER"
        );

        Note note = new Note(
        user,
        "Mi Primera Nota",
        "Resumen general de la sesión",
        "Este es el contenido detallado del resumen",
        "{\"questions\": [\"¿Qué es esto?\", \"¿Cómo funciona?\"]}",
        NoteVisibility.PUBLIC
        );

        userRepository.save(user);
        userRepository.save(admin);
        noteRepository.save(note);

    }

    

}
