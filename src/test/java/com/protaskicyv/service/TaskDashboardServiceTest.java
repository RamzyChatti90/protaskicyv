package com.protaskicyv.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.protaskicyv.domain.enumeration.TaskStatus;
import com.protaskicyv.repository.TaskRepository;
import com.protaskicyv.security.SecurityUtils;
import com.protaskicyv.service.dto.TaskDashboardDTO;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskDashboardServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskDashboardService taskDashboardService;

    private String currentUserLogin;

    @BeforeEach
    void setUp() {
        currentUserLogin = "testuser";
    }

    @Test
    void getTaskDashboardForCurrentUser_shouldReturnDashboardData() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of(currentUserLogin));

            when(taskRepository.countByAssignedTo_Login(currentUserLogin)).thenReturn(10L);
            when(taskRepository.countByAssignedTo_LoginAndStatus(currentUserLogin, TaskStatus.TODO)).thenReturn(5L);
            when(taskRepository.countByAssignedTo_LoginAndStatus(currentUserLogin, TaskStatus.IN_PROGRESS)).thenReturn(3L);
            when(taskRepository.countByAssignedTo_LoginAndStatus(currentUserLogin, TaskStatus.DONE)).thenReturn(2L);

            TaskDashboardDTO result = taskDashboardService.getTaskDashboardForCurrentUser();

            assertThat(result).isNotNull();
            assertThat(result.getTotalTasks()).isEqualTo(10L);
            assertThat(result.getTasksByStatus().get(TaskStatus.TODO)).isEqualTo(5L);
            assertThat(result.getTasksByStatus().get(TaskStatus.IN_PROGRESS)).isEqualTo(3L);
            assertThat(result.getTasksByStatus().get(TaskStatus.DONE)).isEqualTo(2L);

            verify(taskRepository, times(1)).countByAssignedTo_Login(currentUserLogin);
            verify(taskRepository, times(1)).countByAssignedTo_LoginAndStatus(currentUserLogin, TaskStatus.TODO);
            verify(taskRepository, times(1)).countByAssignedTo_LoginAndStatus(currentUserLogin, TaskStatus.IN_PROGRESS);
            verify(taskRepository, times(1)).countByAssignedTo_LoginAndStatus(currentUserLogin, TaskStatus.DONE);
        }
    }

    @Test
    void getTaskDashboardForCurrentUser_shouldReturnEmptyWhenNoUser() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.empty());

            TaskDashboardDTO result = taskDashboardService.getTaskDashboardForCurrentUser();

            assertThat(result).isNotNull();
            assertThat(result.getTotalTasks()).isZero();
            assertThat(result.getTasksByStatus()).isEmpty();

            verifyNoInteractions(taskRepository);
        }
    }
}
