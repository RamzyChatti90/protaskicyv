package com.protaskicyv.service.dto;

import com.protaskicyv.domain.enumeration.TaskStatus;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class TaskDashboardDTO implements Serializable {

    private Long totalTasks;
    private Map<TaskStatus, Long> tasksByStatus;

    public TaskDashboardDTO() {
        // Empty constructor needed for Jackson
    }

    public TaskDashboardDTO(Long totalTasks, Map<TaskStatus, Long> tasksByStatus) {
        this.totalTasks = totalTasks;
        this.tasksByStatus = tasksByStatus;
    }

    public Long getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(Long totalTasks) {
        this.totalTasks = totalTasks;
    }

    public Map<TaskStatus, Long> getTasksByStatus() {
        return tasksByStatus;
    }

    public void setTasksByStatus(Map<TaskStatus, Long> tasksByStatus) {
        this.tasksByStatus = tasksByStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskDashboardDTO that = (TaskDashboardDTO) o;
        return Objects.equals(totalTasks, that.totalTasks) && Objects.equals(tasksByStatus, that.tasksByStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalTasks, tasksByStatus);
    }

    @Override
    public String toString() {
        return "TaskDashboardDTO{" + "totalTasks=" + totalTasks + ", tasksByStatus=" + tasksByStatus + '}';
    }
}
