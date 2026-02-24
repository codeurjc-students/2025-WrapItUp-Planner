package es.wrapitup.wrapitup_planner.repository;

import es.wrapitup.wrapitup_planner.model.CalendarEvent;
import es.wrapitup.wrapitup_planner.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    
    @Query("SELECT e FROM CalendarEvent e WHERE e.user.id = :userId " +
           "AND e.startDate <= :endDate AND e.endDate >= :startDate " +
           "ORDER BY e.startDate ASC")
    List<CalendarEvent> findEventsByUserAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    List<CalendarEvent> findByUserOrderByStartDateAsc(UserModel user);
    
    List<CalendarEvent> findByUserAndStartDateGreaterThanEqualOrderByStartDateAsc(
        UserModel user, LocalDateTime startDate
    );
    
    void deleteByUser(UserModel user);
}
