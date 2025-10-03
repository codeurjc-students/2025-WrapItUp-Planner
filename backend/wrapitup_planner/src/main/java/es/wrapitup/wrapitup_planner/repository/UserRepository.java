package es.wrapitup.wrapitup_planner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import es.wrapitup.wrapitup_planner.model.UserModel;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
    
}
