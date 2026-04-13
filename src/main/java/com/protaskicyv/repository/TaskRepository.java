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

    Long countByAssignedTo_Login(String login);

    Long countByAssignedTo_LoginAndStatus(String login, TaskStatus status);
}
