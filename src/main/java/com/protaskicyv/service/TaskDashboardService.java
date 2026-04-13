package com.protaskicyv.service;

import com.protaskicyv.domain.enumeration.TaskStatus;
import com.protaskicyv.repository.TaskRepository;
import com.protaskicyv.security.SecurityUtils;
import com.protaskicyv.service.dto.TaskDashboardDTO;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskDashboardService {

    private final Logger log = LoggerFactory.getLogger(TaskDashboardService.class);

    private final TaskRepository taskRepository;

    public TaskDashboardService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public Optional<TaskDashboardDTO> getTaskDashboardForCurrentUser() {
        return SecurityUtils
            .getCurrentUserLogin()
            .map(currentUserLogin -> {
                Long totalTasks = taskRepository.countByAssignedTo_Login(currentUserLogin);

                Map<TaskStatus, Long> tasksByStatus = Arrays
                    .stream(TaskStatus.values())
                    .collect(Collectors.toMap(status -> status, status -> taskRepository.countByAssignedTo_LoginAndStatus(currentUserLogin, status)));

                return new TaskDashboardDTO(totalTasks, tasksByStatus);
            });
    }
}
