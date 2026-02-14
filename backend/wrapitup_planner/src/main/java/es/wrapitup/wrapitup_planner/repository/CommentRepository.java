package es.wrapitup.wrapitup_planner.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.wrapitup.wrapitup_planner.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    List<Comment> findByNoteIdOrderByCreatedAtDesc(Long noteId);
    
    Page<Comment> findByNoteIdOrderByCreatedAtDesc(Long noteId, Pageable pageable);
    
    Page<Comment> findByIsReportedTrueOrderByCreatedAtDesc(Pageable pageable);
}
