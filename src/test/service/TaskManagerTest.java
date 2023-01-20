package service;

import jdk.jfr.Description;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.*;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    public T taskManager;
    public static final ZonedDateTime ZONED_DATE_TIME = ZonedDateTime.now();
    public static final int TASK_DEFAULT_DURATION = 10;

    abstract T createTaskManager();

    @BeforeEach
    public void beforeEach() {
        taskManager = createTaskManager();
    }

    @AfterEach
    public void afterAll() {
        ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION);
    }

    @Test
    @Order(0)
    public void createNewNullTaskTest() {
        taskManager.createNewTask(null);
        assertEquals(0, taskManager.getCatalogOfTasks().size(),
                "Expected catalogOfTasks.size() == 0 after creating null task");
    }

    @Test
    public void createNewTaskTest() {
        Task taskToAdd = new Task("Summary",
                "Description",
                Status.NEW,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);

        taskManager.createNewTask(taskToAdd);
        Task addedTask = taskManager.getCatalogOfTasks().get(1);

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfTasks().size()),
                () -> assertEquals(1, addedTask.getId(), "id"),
                () -> assertEquals(taskToAdd.getSummary(), addedTask.getSummary()),
                () -> assertEquals(taskToAdd.getDescription(), addedTask.getDescription()),
                () -> assertEquals(Status.NEW, addedTask.getStatus()),
                () -> assertEquals(TASK_DEFAULT_DURATION, addedTask.getDuration()),
                () -> assertEquals(ZONED_DATE_TIME, addedTask.getStartTime())
        );
    }

    @Test
    public void createNewTaskTimeConflictTest() {
        Task taskToAdd = new Task("Summary",
                "Description",
                Status.NEW,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        taskManager.createNewTask(taskToAdd);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> taskManager.createNewTask(taskToAdd));

        assertEquals("Impossible to add a task - time conflict", exception.getMessage());
    }

    @Test
    void createNewNullEpicTest() {
        taskManager.createNewEpic(null);
        assertEquals(0, taskManager.getCatalogOfEpics().size(),
                "Expected catalogOfEpics.size() == 0 after creating null epic");
    }

    @Test
    void createNewEpicTest() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.NEW);

        taskManager.createNewEpic(epicToAdd);
        Task addedEpic = taskManager.getCatalogOfEpics().get(1);

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(1, addedEpic.getId()),
                () -> assertEquals(epicToAdd.getSummary(), addedEpic.getSummary()),
                () -> assertEquals(epicToAdd.getDescription(), addedEpic.getDescription()),
                () -> assertEquals(epicToAdd.getStatus(), addedEpic.getStatus())
        );
    }

    @Test
    void createNewNullSubTask() {
        taskManager.createNewSubTask(null);
        assertEquals(0, taskManager.getCatalogOfSubTasks().size(),
                "Expected catalogOfSubTasks.size() == 0 after creating null subTask");
    }

    @Test
    void createNewSubTask() {
        Epic epic = new Epic("Summary",
                "Description",
                Status.NEW);
        SubTask subTaskToAdd = new SubTask("Summary",
                "Description",
                Status.NEW,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);

        taskManager.createNewEpic(epic);
        taskManager.createNewSubTask(subTaskToAdd);
        SubTask addedSubTask = taskManager.getCatalogOfSubTasks().get(2);

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfSubTasks().size()),
                () -> assertEquals(2, addedSubTask.getId(), "id"),
                () -> assertEquals(subTaskToAdd.getSummary(), addedSubTask.getSummary(), "Summary"),
                () -> assertEquals(subTaskToAdd.getDescription(), addedSubTask.getDescription()),
                () -> assertEquals(subTaskToAdd.getStatus(), addedSubTask.getStatus()),
                () -> assertEquals(subTaskToAdd.getEpicId(), addedSubTask.getEpicId()),
                () -> assertEquals(subTaskToAdd.getDuration(), addedSubTask.getDuration()),
                () -> assertEquals(subTaskToAdd.getStartTime(), addedSubTask.getStartTime())
        );
    }

    @Test
    void createNewSubTaskTimeConflictTest() {
        Epic epic = new Epic("Summary",
                "Description",
                Status.NEW);
        SubTask subTaskToAdd = new SubTask("Summary",
                "Description",
                Status.NEW,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);

        taskManager.createNewEpic(epic);
        taskManager.createNewSubTask(subTaskToAdd);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> taskManager.createNewSubTask(subTaskToAdd));

        assertEquals("Impossible to add a subTask - time conflict", exception.getMessage());
    }

    @Test
    void dropListsOfTasksEpicsAndSubTasks() {
        Task taskToAdd = new Task("Summary",
                "Description",
                Status.NEW,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        Epic epic = new Epic("Summary",
                "Description",
                Status.NEW);
        SubTask subTaskToAdd = new SubTask("Summary",
                "Description",
                Status.NEW,
                2,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));

        taskManager.createNewTask(taskToAdd);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubTask(subTaskToAdd);

        taskManager.dropListsOfTasksEpicsAndSubTasks();

        assertAll(
                () -> assertEquals(0, taskManager.getCatalogOfTasks().size()),
                () -> assertEquals(0, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(0, taskManager.getCatalogOfSubTasks().size())
        );
    }

    @Test
    void dropListOfTasksTest() {
        Task taskToAdd = new Task("Summary",
                "Description",
                Status.NEW,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        Epic epic = new Epic("Summary",
                "Description",
                Status.NEW);
        SubTask subTaskToAdd = new SubTask("Summary",
                "Description",
                Status.NEW,
                2,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));

        taskManager.createNewTask(taskToAdd);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubTask(subTaskToAdd);

        taskManager.dropListOfTasks();

        assertAll(
                () -> assertEquals(0, taskManager.getCatalogOfTasks().size()),
                () -> assertEquals(1, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(1, taskManager.getCatalogOfSubTasks().size())
        );
    }

    @Test
    void dropListOfEpicsAndSubTasksTest() {
        Task taskToAdd = new Task("Summary",
                "Description",
                Status.NEW,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        Epic epic = new Epic("Summary",
                "Description",
                Status.NEW);
        SubTask subTaskToAdd = new SubTask("Summary",
                "Description",
                Status.NEW,
                2,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));

        taskManager.createNewTask(taskToAdd);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubTask(subTaskToAdd);

        taskManager.dropListOfEpicsAndSubTasks();

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfTasks().size()),
                () -> assertEquals(0, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(0, taskManager.getCatalogOfSubTasks().size())
        );
    }

    @Test
    void dropListOfSubTasksTest() {
        Task taskToAdd = new Task("Summary",
                "Description",
                Status.NEW,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        Epic epic = new Epic("Summary",
                "Description",
                Status.IN_PROGRESS);
        SubTask subTaskToAdd = new SubTask("Summary",
                "Description",
                Status.IN_PROGRESS,
                2,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));

        taskManager.createNewTask(taskToAdd);
        taskManager.createNewEpic(epic);
        taskManager.createNewSubTask(subTaskToAdd);

        taskManager.dropListOfSubTasks();

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfTasks().size()),
                () -> assertEquals(1, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(0, taskManager.getCatalogOfSubTasks().size()),
                () -> assertEquals(0, taskManager.getCatalogOfEpics().get(2).getSubTasksIds().size()),
                () -> assertEquals(Status.NEW, taskManager.getCatalogOfEpics().get(2).getStatus())
        );
    }

    @Test
    void getTaskByIdNotInCatalogTest() {
        Task taskToAdd = new Task("Summary",
                "Description",
                Status.NEW,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        taskManager.createNewTask(taskToAdd);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> taskManager.getTaskById(2));

        assertAll(
                () -> assertEquals("Can't find task with id 2", exception.getMessage()),
                () -> assertEquals(0, taskManager.getHistory().size())
        );
    }

    @Test
    void getTaskByIdTest() {
        Task taskToAdd = new Task("Summary",
                "Description",
                Status.NEW,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        taskManager.createNewTask(taskToAdd);

        Task addedTask = taskManager.getTaskById(1);

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfTasks().size()),
                () -> assertEquals(taskToAdd.getSummary(), addedTask.getSummary()),
                () -> assertEquals(taskToAdd.getDescription(), addedTask.getDescription()),
                () -> assertEquals(taskToAdd.getStatus(), addedTask.getStatus()),
                () -> assertEquals(taskToAdd.getStartTime(), addedTask.getStartTime()),
                () -> assertEquals(1, taskManager.getHistory().size()),
                () -> assertEquals(1, taskManager.getHistory().get(0).getId())
        );
    }

    @Test
    void getEpicByIdNotInCatalogTest() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.NEW);
        taskManager.createNewEpic(epicToAdd);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> taskManager.getEpicById(2));

        assertAll(
                () -> assertEquals("Can't find epic with id 2", exception.getMessage()),
                () -> assertEquals(0, taskManager.getHistory().size())
        );
    }

    @Test
    void getEpicByIdTest() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.NEW);
        taskManager.createNewEpic(epicToAdd);

        Epic addedEpic = taskManager.getEpicById(1);

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(epicToAdd.getSummary(), addedEpic.getSummary()),
                () -> assertEquals(epicToAdd.getDescription(), addedEpic.getDescription()),
                () -> assertEquals(epicToAdd.getStatus(), addedEpic.getStatus()),
                () -> assertEquals(epicToAdd.getStartTime(), addedEpic.getStartTime()),
                () -> assertEquals(1, taskManager.getHistory().size()),
                () -> assertEquals(1, taskManager.getHistory().get(0).getId())
        );
    }

    @Test
    void getSubTaskByIdNotInCatalogTest() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.IN_PROGRESS);
        SubTask subTaskToAdd = new SubTask("Summary",
                "Description",
                Status.IN_PROGRESS,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));

        taskManager.createNewEpic(epicToAdd);
        taskManager.createNewSubTask(subTaskToAdd);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> taskManager.getSubTaskById(3));

        assertAll(
                () -> assertEquals("Can't find subtask with id 3", exception.getMessage()),
                () -> assertEquals(0, taskManager.getHistory().size())
        );
    }

    @Test
    void getSubTaskByIdTest() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.IN_PROGRESS);
        SubTask subTaskToAdd = new SubTask("Summary",
                "Description",
                Status.IN_PROGRESS,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));

        taskManager.createNewEpic(epicToAdd);
        taskManager.createNewSubTask(subTaskToAdd);

        SubTask addedSubTask = taskManager.getSubTaskById(2);

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(1, taskManager.getCatalogOfSubTasks().size()),
                () -> assertEquals(subTaskToAdd.getSummary(), addedSubTask.getSummary()),
                () -> assertEquals(subTaskToAdd.getDescription(), addedSubTask.getDescription()),
                () -> assertEquals(subTaskToAdd.getStatus(), addedSubTask.getStatus()),
                () -> assertEquals(subTaskToAdd.getStartTime(), addedSubTask.getStartTime()),
                () -> assertEquals(1, taskManager.getHistory().size()),
                () -> assertEquals(2, taskManager.getHistory().get(0).getId())
        );
    }

    @Test
    void getHistory() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.IN_PROGRESS);
        SubTask subTaskToAdd = new SubTask("Summary",
                "Description",
                Status.IN_PROGRESS,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));

        taskManager.createNewEpic(epicToAdd);
        taskManager.createNewSubTask(subTaskToAdd);
        taskManager.getSubTaskById(2);
        taskManager.dropListOfSubTasks();

        List<Task> history = taskManager.getHistory();

        assertAll(
                () -> assertEquals(1, history.size()),
                () -> assertEquals(2, history.get(0).getId())
        );
    }

    @Test
    void updateTaskNotInCatalogTest() {
        Task taskToAdd = new Task("Summary",
                "Description",
                Status.NEW,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        taskManager.createNewTask(taskToAdd);
        Task taskForUpdate = new Task("Summary",
                "Description",
                Status.NEW,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        taskForUpdate.setId(2);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> taskManager.updateTask(taskForUpdate));

        assertEquals("Can't update task with id 2, no task with such id.", exception.getMessage());
    }

    @Test
    void updateTaskTimeConflictTest() {
        Task taskToAdd = new Task("Summary",
                "Description",
                Status.NEW,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        taskManager.createNewTask(taskToAdd);

        Task taskForUpdate = new Task("Summary",
                "Description",
                Status.IN_PROGRESS,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(1));
        taskForUpdate.setId(1);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> taskManager.updateTask(taskForUpdate));

        assertAll(
                () -> assertEquals("Impossible to update a task - time conflict", exception.getMessage()),
                () -> assertEquals(Status.NEW, taskManager.getCatalogOfTasks().get(1).getStatus())
        );
    }

    @Test
    void updateTaskTest() {
        Task taskToAdd = new Task("Summary",
                "Description",
                Status.NEW,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        taskManager.createNewTask(taskToAdd);

        Task taskForUpdate = new Task("Summary",
                "Description",
                Status.IN_PROGRESS,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        taskForUpdate.setId(1);
        taskManager.updateTask(taskForUpdate);

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfTasks().size()),
                () -> assertEquals(Status.IN_PROGRESS, taskManager.getCatalogOfTasks().get(1).getStatus())
        );
    }

    @Test
    void updateEpicTest() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.NEW);
        taskManager.createNewEpic(epicToAdd);

        Epic epicToUpdate = new Epic("Summary",
                "New Description",
                Status.IN_PROGRESS);
        epicToUpdate.setId(1);

        taskManager.updateEpic(epicToUpdate);

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(Status.NEW, taskManager.getCatalogOfEpics().get(1).getStatus(),
                        "If status changed looks like status logic for epic was fucked"),
                () -> assertEquals("New Description", taskManager.getCatalogOfEpics()
                        .get(1).getDescription())
        );
    }

    @Test
    void updateEpicNotInCatalogTest() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.NEW);
        taskManager.createNewEpic(epicToAdd);

        Epic epicToUpdate = new Epic("Summary",
                "New Description",
                Status.IN_PROGRESS);
        epicToUpdate.setId(2);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> taskManager.updateEpic(epicToUpdate));

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals("Can't update epic with id 2, no epic with such id.", exception.getMessage())
        );
    }

    @Test
    void updateSubTaskTest() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.NEW);
        SubTask subTaskToAdd = new SubTask("Summary",
                "Description",
                Status.NEW,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);

        taskManager.createNewEpic(epicToAdd);
        taskManager.createNewSubTask(subTaskToAdd);

        SubTask subTaskToUpdate = new SubTask("Summary",
                "Description",
                Status.IN_PROGRESS,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));
        subTaskToUpdate.setId(2);

        taskManager.updateSubTask(subTaskToUpdate);

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(1, taskManager.getCatalogOfSubTasks().size()),
                () -> assertEquals(Status.IN_PROGRESS, taskManager.getCatalogOfSubTasks().get(2).getStatus()),
                () -> assertEquals(ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION), taskManager
                        .getCatalogOfSubTasks().get(2).getStartTime()),
                () -> assertEquals(Status.IN_PROGRESS, taskManager.getCatalogOfEpics().get(1).getStatus()),
                () -> assertEquals(ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION), taskManager.getCatalogOfEpics()
                        .get(1).getStartTime())
        );
    }

    @Test
    void updateSubTaskNotInCatalogTest() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.NEW);
        SubTask subTaskToAdd = new SubTask("Summary",
                "Description",
                Status.NEW,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);

        taskManager.createNewEpic(epicToAdd);
        taskManager.createNewSubTask(subTaskToAdd);

        SubTask subTaskToUpdate = new SubTask("Summary",
                "Description",
                Status.IN_PROGRESS,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));
        subTaskToUpdate.setId(3);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> taskManager.updateSubTask(subTaskToUpdate));

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(1, taskManager.getCatalogOfSubTasks().size()),
                () -> assertEquals("Can't update subtask with id 3, no subtask with such id.",
                        exception.getMessage()),
                () -> assertEquals(Status.NEW, taskManager.getCatalogOfEpics().get(1).getStatus()),
                () -> assertEquals(Status.NEW, taskManager.getCatalogOfSubTasks().get(2).getStatus())
        );
    }

    @Test
    void updateSubTaskTimeConflictTest() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.NEW);
        SubTask subTaskToAdd = new SubTask("Summary",
                "Description",
                Status.NEW,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        SubTask subTaskToAddConflict = new SubTask("Summary",
                "Description",
                Status.NEW,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));

        taskManager.createNewEpic(epicToAdd);
        taskManager.createNewSubTask(subTaskToAdd);
        taskManager.createNewSubTask(subTaskToAddConflict);

        SubTask subTaskToUpdate = new SubTask("Summary",
                "Description",
                Status.IN_PROGRESS,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));
        subTaskToUpdate.setId(2);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> taskManager.updateSubTask(subTaskToUpdate));

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(2, taskManager.getCatalogOfSubTasks().size()),
                () -> assertEquals("Impossible to update a subTask - time conflict",
                        exception.getMessage()),
                () -> assertEquals(Status.NEW, taskManager.getCatalogOfEpics().get(1).getStatus()),
                () -> assertEquals(Status.NEW, taskManager.getCatalogOfSubTasks().get(2).getStatus())
        );
    }

    @Test
    void removeTaskByIdTest() {
        Task taskToAdd = new Task("Summary",
                "Description",
                Status.NEW,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        taskManager.createNewTask(taskToAdd);

        assertEquals(1, taskManager.getCatalogOfTasks().size(),
                "Ooops, looks like task wasn't created");

        taskManager.removeTaskById(1);

        assertEquals(0, taskManager.getCatalogOfTasks().size());
    }

    @Test
    void removeTaskByIdNotInCatalogTest() {
        Task taskToAdd = new Task("Summary",
                "Description",
                Status.NEW,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        taskManager.createNewTask(taskToAdd);

        assertEquals(1, taskManager.getCatalogOfTasks().size(),
                "Ooops, looks like task wasn't created");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> taskManager.removeTaskById(2));

        assertEquals("Task with id 2 don't exist.", exception.getMessage());
    }

    @Test
    void removeEpicByIdTest() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.NEW);
        SubTask subTaskToAdd = new SubTask("Summary",
                "Description",
                Status.NEW,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);

        taskManager.createNewEpic(epicToAdd);
        taskManager.createNewSubTask(subTaskToAdd);

        assertEquals(1, taskManager.getCatalogOfEpics().size(),
                "Ooops, looks like epic wasn't created");
        assertEquals(1, taskManager.getCatalogOfSubTasks().size(),
                "Ooops, looks like subtask wasn't created");

        taskManager.removeEpicById(1);

        assertAll(
                () -> assertEquals(0, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(0, taskManager.getCatalogOfSubTasks().size())
        );
    }

    @Test
    void removeEpicByIdNotInCatalogTest() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.NEW);
        SubTask subTaskToAdd = new SubTask("Summary",
                "Description",
                Status.NEW,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);

        taskManager.createNewEpic(epicToAdd);
        taskManager.createNewSubTask(subTaskToAdd);

        assertEquals(1, taskManager.getCatalogOfEpics().size(),
                "Ooops, looks like epic wasn't created");
        assertEquals(1, taskManager.getCatalogOfSubTasks().size(),
                "Ooops, looks like subtask wasn't created");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> taskManager.removeEpicById(2));

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(1, taskManager.getCatalogOfSubTasks().size()),
                () -> assertEquals("Epic with id 2 don't exist.", exception.getMessage())
        );
    }

    @Test
    void removeSubTaskByIdTest() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.IN_PROGRESS);
        SubTask subTaskToAdd = new SubTask("Summary",
                "First subTask description",
                Status.NEW,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        SubTask subTaskToAddToDelete = new SubTask("Summary",
                "Second subTask description",
                Status.IN_PROGRESS,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));

        taskManager.createNewEpic(epicToAdd);
        taskManager.createNewSubTask(subTaskToAdd);
        taskManager.createNewSubTask(subTaskToAddToDelete);

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(2, taskManager.getCatalogOfSubTasks().size())
        );

        taskManager.removeSubTaskById(3);

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(1, taskManager.getCatalogOfSubTasks().size()),
                () -> assertEquals("First subTask description",
                        taskManager.getCatalogOfSubTasks().get(2).getDescription()),
                () -> assertEquals(Status.NEW, taskManager.getCatalogOfEpics().get(1).getStatus(),
                        "Looks like logic for epic status update was fucked")
        );
    }

    @Test
    @Description("Для расчёта статуса Epic. Граничные условия: a.   Пустой список подзадач.")
    void epicStatusCalculatingEmptyList() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.NEW);
        SubTask subTaskToAdd = new SubTask("Summary",
                "First subTask description",
                Status.NEW,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        SubTask subTaskToAddNumberTwo = new SubTask("Summary",
                "Second subTask description",
                Status.IN_PROGRESS,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));

        taskManager.createNewEpic(epicToAdd);
        taskManager.createNewSubTask(subTaskToAdd);
        taskManager.createNewSubTask(subTaskToAddNumberTwo);

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(2, taskManager.getCatalogOfSubTasks().size()),
                () -> assertEquals(Status.IN_PROGRESS, taskManager.getCatalogOfEpics().get(1).getStatus())
        );

        taskManager.dropListOfSubTasks();

        assertEquals(Status.NEW, taskManager.getCatalogOfEpics().get(1).getStatus());
    }

    @Test
    @Description("Для расчёта статуса Epic. Граничные условия:  b.   Все подзадачи со статусом NEW.")
    void epicStatusCalculatingAllSubTasksNew() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.NEW);
        SubTask subTaskToAdd = new SubTask("Summary",
                "First subTask description",
                Status.IN_PROGRESS,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        SubTask subTaskToAddNumberTwo = new SubTask("Summary",
                "Second subTask description",
                Status.IN_PROGRESS,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));

        taskManager.createNewEpic(epicToAdd);
        taskManager.createNewSubTask(subTaskToAdd);
        taskManager.createNewSubTask(subTaskToAddNumberTwo);

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(2, taskManager.getCatalogOfSubTasks().size()),
                () -> assertEquals(Status.IN_PROGRESS, taskManager.getCatalogOfEpics().get(1).getStatus())
        );

        SubTask subTaskToAddUpdate = new SubTask("Summary",
                "First subTask description",
                Status.NEW,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        SubTask subTaskToAddNumberTwoUpdate = new SubTask("Summary",
                "Second subTask description",
                Status.NEW,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));
        subTaskToAddUpdate.setId(2);
        subTaskToAddNumberTwoUpdate.setId(3);

        taskManager.updateSubTask(subTaskToAddUpdate);
        taskManager.updateSubTask(subTaskToAddNumberTwoUpdate);

        assertEquals(Status.NEW, taskManager.getCatalogOfEpics().get(1).getStatus());
    }

    @Test
    @Description("Для расчёта статуса Epic. Граничные условия:  c.    Все подзадачи со статусом DONE.")
    void epicStatusCalculatingAllSubTasksDone() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.NEW);
        SubTask subTaskToAdd = new SubTask("Summary",
                "First subTask description",
                Status.IN_PROGRESS,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        SubTask subTaskToAddNumberTwo = new SubTask("Summary",
                "Second subTask description",
                Status.IN_PROGRESS,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));

        taskManager.createNewEpic(epicToAdd);
        taskManager.createNewSubTask(subTaskToAdd);
        taskManager.createNewSubTask(subTaskToAddNumberTwo);

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(2, taskManager.getCatalogOfSubTasks().size()),
                () -> assertEquals(Status.IN_PROGRESS, taskManager.getCatalogOfEpics().get(1).getStatus())
        );

        SubTask subTaskToAddUpdate = new SubTask("Summary",
                "First subTask description",
                Status.DONE,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        SubTask subTaskToAddNumberTwoUpdate = new SubTask("Summary",
                "Second subTask description",
                Status.DONE,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));
        subTaskToAddUpdate.setId(2);
        subTaskToAddNumberTwoUpdate.setId(3);

        taskManager.updateSubTask(subTaskToAddUpdate);
        taskManager.updateSubTask(subTaskToAddNumberTwoUpdate);

        assertEquals(Status.DONE, taskManager.getCatalogOfEpics().get(1).getStatus());
    }

    @Test
    @Description("Для расчёта статуса Epic. Граничные условия:  d.    Подзадачи со статусами NEW и DONE. ")
    void epicStatusCalculatingAllSubTasksNewAndDone() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.IN_PROGRESS);
        SubTask subTaskToAdd = new SubTask("Summary",
                "First subTask description",
                Status.IN_PROGRESS,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        SubTask subTaskToAddNumberTwo = new SubTask("Summary",
                "Second subTask description",
                Status.IN_PROGRESS,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));

        taskManager.createNewEpic(epicToAdd);
        taskManager.createNewSubTask(subTaskToAdd);
        taskManager.createNewSubTask(subTaskToAddNumberTwo);

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(2, taskManager.getCatalogOfSubTasks().size()),
                () -> assertEquals(Status.IN_PROGRESS, taskManager.getCatalogOfEpics().get(1).getStatus())
        );

        SubTask subTaskToAddUpdate = new SubTask("Summary",
                "First subTask description",
                Status.NEW,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        SubTask subTaskToAddNumberTwoUpdate = new SubTask("Summary",
                "Second subTask description",
                Status.DONE,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));
        subTaskToAddUpdate.setId(2);
        subTaskToAddNumberTwoUpdate.setId(3);

        taskManager.updateSubTask(subTaskToAddUpdate);
        taskManager.updateSubTask(subTaskToAddNumberTwoUpdate);

        assertEquals(Status.IN_PROGRESS, taskManager.getCatalogOfEpics().get(1).getStatus());
    }

    @Test
    @Description("Для расчёта статуса Epic. Граничные условия: e.    Подзадачи со статусом IN_PROGRESS.")
    void epicStatusCalculatingAllSubTasksInProgress() {
        Epic epicToAdd = new Epic("Summary",
                "Description",
                Status.DONE);
        SubTask subTaskToAdd = new SubTask("Summary",
                "First subTask description",
                Status.DONE,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        SubTask subTaskToAddNumberTwo = new SubTask("Summary",
                "Second subTask description",
                Status.DONE,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));

        taskManager.createNewEpic(epicToAdd);
        taskManager.createNewSubTask(subTaskToAdd);
        taskManager.createNewSubTask(subTaskToAddNumberTwo);

        assertAll(
                () -> assertEquals(1, taskManager.getCatalogOfEpics().size()),
                () -> assertEquals(2, taskManager.getCatalogOfSubTasks().size()),
                () -> assertEquals(Status.DONE, taskManager.getCatalogOfEpics().get(1).getStatus())
        );

        SubTask subTaskToAddUpdate = new SubTask("Summary",
                "First subTask description",
                Status.IN_PROGRESS,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME);
        SubTask subTaskToAddNumberTwoUpdate = new SubTask("Summary",
                "Second subTask description",
                Status.IN_PROGRESS,
                1,
                TASK_DEFAULT_DURATION,
                ZONED_DATE_TIME.plusMinutes(TASK_DEFAULT_DURATION));
        subTaskToAddUpdate.setId(2);
        subTaskToAddNumberTwoUpdate.setId(3);

        taskManager.updateSubTask(subTaskToAddUpdate);
        taskManager.updateSubTask(subTaskToAddNumberTwoUpdate);

        assertEquals(Status.IN_PROGRESS, taskManager.getCatalogOfEpics().get(1).getStatus());
    }
}