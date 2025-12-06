package es.wrapitup.wrapitup_planner.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import es.wrapitup.wrapitup_planner.model.Note;
import es.wrapitup.wrapitup_planner.model.NoteCategory;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long>{
    
    List<Note> findByUserId(Long userId);
    Page<Note> findByUserId(Long userId, Pageable pageable);
    Page<Note> findByUserIdOrderByLastModifiedDesc(Long userId, Pageable pageable);
    
    //filter by category
    Page<Note> findByUserIdAndCategoryOrderByLastModifiedDesc(Long userId, NoteCategory category, Pageable pageable);
    
    //search (title)
    @Query("SELECT n FROM Note n WHERE n.user.id = :userId AND LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY n.lastModified DESC")
    Page<Note> findByUserIdAndTitleContainingOrderByLastModifiedDesc(@Param("userId") Long userId, @Param("search") String search, Pageable pageable);
    
    //filter by both category and search
    @Query("SELECT n FROM Note n WHERE n.user.id = :userId AND n.category = :category AND LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) ORDER BY n.lastModified DESC")
    Page<Note> findByUserIdAndCategoryAndTitleContainingOrderByLastModifiedDesc(@Param("userId") Long userId, @Param("category") NoteCategory category, @Param("search") String search, Pageable pageable);
}
