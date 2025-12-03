package es.wrapitup.wrapitup_planner.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.wrapitup.wrapitup_planner.model.Note;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long>{
    
    List<Note> findByUserId(Long userId);
    Page<Note> findByUserId(Long userId, Pageable pageable);
}
