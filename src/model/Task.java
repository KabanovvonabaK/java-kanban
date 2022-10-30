package model;

public class Task {
    private String summary;
    private String description;
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

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return getClass().getName() +
                "{" +
                "summary='" + summary + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
