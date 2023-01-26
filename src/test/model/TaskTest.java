package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {

    Task task;
    LocalDateTime zonedDateTime = LocalDateTime.now();

    @BeforeEach
    public void beforeEach() {
        task = new Task("Summary",
                "Description",
                Status.NEW,
                30,
                zonedDateTime);
    }

    @Test
    void setIdAndGetId() {
        task.setId(1);
        assertEquals(1, task.getId());
    }

    @Test
    void getSummaryTest() {
        assertEquals("Summary", task.getSummary());
    }

    @Test
    void getDescriptionTest() {
        assertEquals("Description", task.getDescription());
    }

    @Test
    void getStatusTest() {
        assertEquals(Status.NEW, task.getStatus());
    }

    @Test
    void setStatusTest() {
        task.setStatus(Status.IN_PROGRESS);
        assertEquals(Status.IN_PROGRESS, task.getStatus());
    }

    @Test
    void setDurationGeturationAndGetEndTime() {
        int duration = 30;
        task.setDuration(duration);
        assertEquals(duration, task.getDuration(), "Duration is wrong");

        assertEquals(zonedDateTime.plusMinutes(duration), task.getEndTime());
    }

    @Test
    void getStartTime() {
        assertEquals(zonedDateTime, task.getStartTime());
    }
}