package es.wrapitup.wrapitup_planner.repository;

import es.wrapitup.wrapitup_planner.model.QuizScore;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizScoreRepository extends JpaRepository<QuizScore, Long> {
    List<QuizScore> findByNoteIdAndUserIdOrderByCreatedAtAsc(Long noteId, Long userId);
}