package service;

import model.Status;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

abstract class HistoryManagerTest<T extends HistoryManager> {

    public T historyManager;

    abstract T createHistoryManager();

    @BeforeEach
    public void beforeEach() {
        historyManager = createHistoryManager();
    }
    @Test
    void addOneTaskTest() {
        Task taskToAdd = new Task("Summary",
                "Description",
                Status.NEW,
                10,
                LocalDateTime.now());
        taskToAdd.setId(1);

        historyManager.add(taskToAdd);

        assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    void addThreeTasksTest() {
        Task taskToAddOne = new Task("Summary",
                "Description",
                Status.NEW,
                10,
                LocalDateTime.now());
        taskToAddOne.setId(1);
        Task taskToAddTwo = new Task("Summary",
                "Description",
                Status.NEW,
                10,
                LocalDateTime.now().plusMinutes(10));
        taskToAddTwo.setId(2);
        Task taskToAddThree = new Task("Summary",
                "Description",
                Status.NEW,
                10,
                LocalDateTime.now().plusMinutes(10));
        taskToAddThree.setId(3);

        historyManager.add(taskToAddOne);
        historyManager.add(taskToAddTwo);
        historyManager.add(taskToAddThree);

        assertEquals(3, historyManager.getHistory().size());
    }

    @Test
    void addThreeTasksAndDuplicateomeOfThemTest() {
        Task taskToAddOne = new Task("Summary",
                "Description",
                Status.NEW,
                10,
                LocalDateTime.parse("2023-01-20T23:51"));
        taskToAddOne.setId(1);
        Task taskToAddTwo = new Task("Summary",
                "Description",
                Status.NEW,
                10,
                LocalDateTime.parse("2023-01-20T23:51"));
        taskToAddTwo.setId(2);
        Task taskToAddThree = new Task("Summary",
                "Description",
                Status.NEW,
                10,
                LocalDateTime.parse("2023-01-20T23:41"));
        taskToAddThree.setId(3);

        historyManager.add(taskToAddOne);
        historyManager.add(taskToAddTwo);
        historyManager.add(taskToAddThree);
        historyManager.add(taskToAddOne);

        System.out.println(historyManager.getHistory());
        assertEquals(3, historyManager.getHistory().size());
        assertEquals("[model.Task{id=2'summary='Summary', description='Description', status='NEW', " +
                "duration=10', startTime=2023-01-20T23:51'}, model.Task{id=3'" +
                "summary='Summary', description='Description', status='NEW', duration=10', " +
                "startTime=2023-01-20T23:41'}, model.Task{id=1'summary='Summary', " +
                "description='Description', status='NEW', duration=10', " +
                "startTime=2023-01-20T23:51'}]", historyManager.getHistory().toString());
    }

    @Test
    void remove() {
        Task taskToAddOne = new Task("Summary",
                "Description",
                Status.NEW,
                10,
                LocalDateTime.parse("2023-01-20T23:51"));
        taskToAddOne.setId(1);
        Task taskToAddTwo = new Task("Summary",
                "Description",
                Status.NEW,
                10,
                LocalDateTime.parse("2023-01-20T23:51"));
        taskToAddTwo.setId(2);
        Task taskToAddThree = new Task("Summary",
                "Description",
                Status.NEW,
                10,
                LocalDateTime.parse("2023-01-20T23:41"));
        taskToAddThree.setId(3);

        historyManager.add(taskToAddOne);
        historyManager.add(taskToAddTwo);
        historyManager.add(taskToAddThree);
        historyManager.add(taskToAddOne);

        assertEquals(3, historyManager.getHistory().size());

        historyManager.remove(2);

        assertEquals(2, historyManager.getHistory().size());
    }
}