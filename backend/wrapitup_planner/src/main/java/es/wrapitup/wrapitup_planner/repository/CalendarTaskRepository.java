package es.wrapitup.wrapitup_planner.repository;

import es.wrapitup.wrapitup_planner.model.CalendarTask;
import es.wrapitup.wrapitup_planner.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CalendarTaskRepository extends JpaRepository<CalendarTask, Long> {
    
    @Query("SELECT t FROM CalendarTask t WHERE t.user.id = :userId " +
           "AND t.taskDate = :date " +
           "ORDER BY t.completed ASC, t.createdAt ASC")
    List<CalendarTask> findByUserAndTaskDateOrderByCompletedAndCreated(
        @Param("userId") Long userId,
        @Param("date") LocalDate date
    );
    
    @Query("SELECT t FROM CalendarTask t WHERE t.user.id = :userId " +
           "AND t.taskDate BETWEEN :startDate AND :endDate " +
           "ORDER BY t.taskDate ASC, t.completed ASC, t.createdAt ASC")
    List<CalendarTask> findByUserAndDateRangeOrderByDate(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    List<CalendarTask> findByUserAndCompletedFalseOrderByTaskDateAsc(UserModel user);
    
    List<CalendarTask> findByUserOrderByTaskDateDescCreatedAtDesc(UserModel user);
    
    void deleteByUser(UserModel user);
}
