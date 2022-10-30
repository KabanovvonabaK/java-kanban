package model;

public class SubTask extends Task {

    int epicId;

    public SubTask(String summary, String description, Status status, int epicId) {
        super(summary, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return getClass().getName() +
                "{" +
                "summary='" + super.getSummary() + '\'' +
                ", description='" + super.getDescription() + '\'' +
                ", status='" + super.getStatus() + '\'' +
                ", epicId='" + epicId + '\'' +
                '}';
    }
}
