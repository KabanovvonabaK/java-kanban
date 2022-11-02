package model;

import java.util.Objects;

public class Task {
    private int id;
    private final String summary;
    private final String description;
    private Status status;

    public Task(String summary, String description, Status status) {
        this.summary = summary;
        this.description = description;
        this.status = status;
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

    @Override
    public String toString() {
        return getClass().getName() +
                "{" +
                "id=" + id + '\'' +
                "summary='" + summary + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
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