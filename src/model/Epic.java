package model;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private ArrayList<Integer> subTasksIds;
    private long durationEpic;
    private LocalDateTime startTimeEpic;

    public Epic(String summary, String description, Status status) {
        super(summary, description, status, 0, null);
        subTasksIds = new ArrayList<>();
    }

    public ArrayList<Integer> getSubTasksIds() {
        return subTasksIds;
    }

    public void addSubTaskId(int id) {
        subTasksIds.add(id);
    }

    public void setSubTasksIds(ArrayList<Integer> subTasksIds) {
        this.subTasksIds = subTasksIds;
    }

    public void setStartTimeEpic(LocalDateTime startTimeEpic) {
        this.startTimeEpic = startTimeEpic;
    }

    @Override
    public void setDuration(long duration) {
        this.durationEpic = duration;
    }

    @Override
    public long getDuration() {
        return durationEpic;
    }

    public LocalDateTime getStartTime() {
        return startTimeEpic;
    }

    @Override
    public LocalDateTime getEndTime() {
        if (startTimeEpic != null) {
            return startTimeEpic.plusMinutes(durationEpic);
        } else {
            throw new RuntimeException("End time is null for epic with id " + getId());
        }
    }

    @Override
    public String toString() {
        return getClass().getName() +
                "{" +
                "id=" + super.getId() + '\'' +
                "summary='" + super.getSummary() + '\'' +
                ", description='" + super.getDescription() + '\'' +
                ", status='" + super.getStatus() + '\'' +
                ", subTasksIds=" + subTasksIds +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subTasksIds, epic.subTasksIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subTasksIds) + super.hashCode();
    }
}