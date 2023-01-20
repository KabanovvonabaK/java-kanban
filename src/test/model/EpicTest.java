package model;

import java.time.ZonedDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    Epic epic;

    @BeforeEach
    public void beforeEach() {
        epic = new Epic("Summary",
                "Description",
                Status.NEW);
        epic.addSubTaskId(2);
    }

    @Test
    void getSubTasksIdsTest() {
        ArrayList<Integer> subTaskIds = epic.getSubTasksIds();

        assertAll(
                () -> assertEquals(1, subTaskIds.size(), "Wrong size"),
                () -> assertEquals(2, subTaskIds.get(0), "Wrong subTask id")
        );
    }

    @Test
    void addSubTaskIdTest() {
        epic.addSubTaskId(3);
        ArrayList<Integer> subTaskIds = epic.getSubTasksIds();

        assertAll(
                () -> assertEquals(2, subTaskIds.size(), "Wrong size"),
                () -> assertEquals(2, subTaskIds.get(0), "Wrong subTask id"),
                () -> assertEquals(3, subTaskIds.get(1), "Wrong subTask id")
        );
    }

    @Test
    void setSubTasksIdsTest() {
        ArrayList<Integer> subTaskIds = new ArrayList<>();
        subTaskIds.add(8);
        subTaskIds.add(9);
        subTaskIds.add(10);

        epic.setSubTasksIds(subTaskIds);

        assertAll(
                () -> assertEquals(3, subTaskIds.size(), "Wrong size"),
                () -> assertEquals(8, subTaskIds.get(0), "Wrong subTask id"),
                () -> assertEquals(9, subTaskIds.get(1), "Wrong subTask id"),
                () -> assertEquals(10, subTaskIds.get(2), "Wrong subTask id")
        );
    }

    @Test
    void setStartTimeTest() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        epic.setStartTime(zonedDateTime);

        assertEquals(zonedDateTime, epic.getStartTime());
    }
}