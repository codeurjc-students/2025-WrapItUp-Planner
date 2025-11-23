package es.wrapitup.wrapitup_planner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.wrapitup.wrapitup_planner.model.Note;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long>{

}
