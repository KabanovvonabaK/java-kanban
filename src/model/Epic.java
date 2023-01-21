package model;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private ArrayList<Integer> subTasksIds;
    private long duration;
    private ZonedDateTime startTime;

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

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    @Override
    public ZonedDateTime getEndTime() {
        if (startTime != null) {
            return startTime.plusMinutes(duration);
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