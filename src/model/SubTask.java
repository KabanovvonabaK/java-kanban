package model;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;

public class SubTask extends Task {
    private final int epicId;

    public SubTask(String summary, String description, Status status, int epicId, long duration, LocalDateTime startTime) {
        super(summary, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return getClass().getName() +
                "{" +
                "id=" + super.getId() + '\'' +
                "summary='" + super.getSummary() + '\'' +
                ", description='" + super.getDescription() + '\'' +
                ", status='" + super.getStatus() + '\'' +
                ", epicId='" + epicId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubTask subTask = (SubTask) o;
        return epicId == subTask.epicId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(epicId) + super.hashCode();
    }
}