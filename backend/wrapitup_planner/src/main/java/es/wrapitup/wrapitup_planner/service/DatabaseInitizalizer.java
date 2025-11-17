package es.wrapitup.wrapitup_planner.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.wrapitup.wrapitup_planner.model.AINote;
import es.wrapitup.wrapitup_planner.model.UserModel;
import es.wrapitup.wrapitup_planner.model.UserStatus;
import es.wrapitup.wrapitup_planner.repository.AINoteRepository;
import es.wrapitup.wrapitup_planner.repository.UserRepository;
import jakarta.annotation.PostConstruct;

@Service
public class DatabaseInitizalizer {

    @Autowired
	private UserRepository userRepository;

	@Autowired
	private AINoteRepository aiNoteRepository;

    @PostConstruct
    public void init(){

        UserModel user = new UserModel(
        "genericUser", 
        "genericUser@example.com", 
        "12345678", 
        UserStatus.ACTIVE, 
        "USER"
        );

        AINote note = new AINote(
        user,
        "Resumen general de la sesión de IA",
        "Este es el contenido detallado del resumen",
        "{\"questions\": [\"¿Qué es IA?\", \"¿Cómo funciona?\"]}",
        true
        );

        userRepository.save(user);
        aiNoteRepository.save(note);

    }

    

}
