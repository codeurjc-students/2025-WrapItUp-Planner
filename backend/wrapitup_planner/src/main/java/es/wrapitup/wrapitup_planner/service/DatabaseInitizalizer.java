package es.wrapitup.wrapitup_planner.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.wrapitup.wrapitup_planner.model.Note;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.NoteRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import jakarta.annotation.PostConstruct;

@Service
public class DatabaseInitizalizer {

    @Autowired
	private UserRepository userRepository;

	@Autowired
	private NoteRepository noteRepository;

    @PostConstruct
    public void init(){

        UserModel user = new UserModel(
        "genericUser", 
        "genericUser@example.com", 
        "12345678", 
        UserStatus.ACTIVE, 
        "USER"
        );

        Note note = new Note(
        user,
        "Resumen general de la sesión",
        "Este es el contenido detallado del resumen",
        "{\"questions\": [\"¿Qué es esto?\", \"¿Cómo funciona?\"]}",
        true
        );

        userRepository.save(user);
        noteRepository.save(note);

    }

    

}
