package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubTaskTest {

    SubTask subTask;
    LocalDateTime zonedDateTime = LocalDateTime.now();

    @BeforeEach
    public void beforeEach() {
        subTask = new SubTask("Summary",
                "Description",
                Status.NEW,
                1,
                30,
                zonedDateTime);
    }

    @Test
    void getEpicIdTest() {
        assertEquals(1, subTask.getEpicId());
    }
}