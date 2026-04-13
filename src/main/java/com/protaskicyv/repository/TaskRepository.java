package com.protaskicyv.repository;

import com.protaskicyv.domain.Task;
import java.util.List;
import com.protaskicyv.domain.enumeration.TaskStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Task entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("select task from Task task where task.assignedTo.login = ?#{authentication.name}")
    List<Task> findByAssignedToIsCurrentUser();

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.login = :login")
    Long countByAssignedTo_Login(@Param("login") String login);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.login = :login AND t.status = :status")
    Long countByAssignedTo_LoginAndStatus(@Param("login") String login, @Param("status") TaskStatus status);
}