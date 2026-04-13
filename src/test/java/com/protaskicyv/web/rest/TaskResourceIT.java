package com.protaskicyv.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.protaskicyv.IntegrationTest;
import com.protaskicyv.domain.Task;
import com.protaskicyv.domain.User;
import com.protaskicyv.domain.enumeration.TaskStatus;
import com.protaskicyv.repository.TaskRepository;
import com.protaskicyv.repository.UserRepository;
import com.protaskicyv.security.AuthoritiesConstants;
import com.protaskicyv.security.SecurityUtils;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link TaskResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TaskResourceIT {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREATED_AT = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREATED_AT = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS);

    private static final TaskStatus DEFAULT_STATUS = TaskStatus.TODO;
    private static final TaskStatus UPDATED_STATUS = TaskStatus.IN_PROGRESS;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTaskMockMvc;

    private Task task;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Task createEntity(EntityManager em) {
        Task task = new Task()
            .title(DEFAULT_TITLE)
            .description(DEFAULT_DESCRIPTION)
            .createdAt(DEFAULT_CREATED_AT)
            .status(DEFAULT_STATUS);
        return task;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Task createUpdatedEntity(EntityManager em) {
        Task task = new Task()
            .title(UPDATED_TITLE)
            .description(UPDATED_DESCRIPTION)
            .createdAt(UPDATED_CREATED_AT)
            .status(UPDATED_STATUS);
        return task;
    }

    @BeforeEach
    public void initTest() {
        task = createEntity(em);
    }

    @Test
    @Transactional
    @WithMockUser(username = "testuser", authorities = { AuthoritiesConstants.USER })
    void getTaskDashboardForCurrentUser() throws Exception {
        // Initialize the database
        User currentUser = SecurityUtils
            .getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .orElseThrow(() -> new IllegalArgumentException("User 'testuser' not found"));

        task.setAssignedTo(currentUser);
        taskRepository.saveAndFlush(task);

        Task task2 = createEntity(em);
        task2.setStatus(TaskStatus.DONE);
        task2.setAssignedTo(currentUser);
        taskRepository.saveAndFlush(task2);

        restTaskMockMvc
            .perform(get("/api/tasks/dashboard").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath(".totalTasks").value(2))
            .andExpect(jsonPath(".tasksByStatus.TODO").value(1))
            .andExpect(jsonPath(".tasksByStatus.DONE").value(1))
            .andExpect(jsonPath(".tasksByStatus.IN_PROGRESS").value(0));
    }

    @Test
    @Transactional
    @WithMockUser(username = "otheruser", authorities = { AuthoritiesConstants.USER })
    void getTaskDashboardForOtherUser() throws Exception {
        // Initialize the database with tasks for 'testuser'
        User testUser = userRepository.findOneByLogin("testuser").orElseGet(() -> {
            User newUser = new User();
            newUser.setLogin("testuser");
            newUser.setPassword("$2a$10$gSAhZrxMllrbgj/kkK9UceBPchw.rwQ4GwYp1LzVDs3SJ1ebYKmQR"); // test
            newUser.setActivated(true);
            newUser.setEmail("testuser@localhost");
            newUser.setFirstName("testuser");
            newUser.setLastName("testuser");
            newUser.setLangKey("en");
            em.persist(newUser);
            return newUser;
        });

        task.setAssignedTo(testUser);
        taskRepository.saveAndFlush(task);

        Task task2 = createEntity(em);
        task2.setStatus(TaskStatus.DONE);
        task2.setAssignedTo(testUser);
        taskRepository.saveAndFlush(task2);

        // As 'otheruser', we should see no tasks
        restTaskMockMvc
            .perform(get("/api/tasks/dashboard").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath(".totalTasks").value(0))
            .andExpect(jsonPath(".tasksByStatus").isEmpty());
    }
}
