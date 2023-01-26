package model;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;

public class Task {
    private int id;
    private final String summary;
    private final String description;
    private Status status;
    private long duration;
    private final LocalDateTime startTime;

    public Task(String summary, String description, Status status, long duration, LocalDateTime startTime) {
        this.summary = summary;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getEndTime() {
        return startTime.plusMinutes(duration);
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return getClass().getName() +
                "{" +
                "id=" + id + '\'' +
                "summary='" + summary + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", duration=" + duration + '\'' +
                ", startTime=" + startTime + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(summary, task.summary)
                && Objects.equals(description, task.description) && status == task.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(summary, description, status, id);
    }
}