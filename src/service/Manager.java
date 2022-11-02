package service;

import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Manager {
    private int id = 0;
    private final HashMap<Integer, Task> listOfTasks;
    private final HashMap<Integer, Epic> listOfEpics;
    private final HashMap<Integer, SubTask> listOfSubTasks;

    public Manager() {
        this.listOfTasks = new HashMap<>();
        this.listOfEpics = new HashMap<>();
        this.listOfSubTasks = new HashMap<>();
    }

    public void createNewTask(Task task) {
        if (task != null) {
            id++;
            task.setId(id);
            listOfTasks.put(id, task);
        }
    }

    public void createNewEpic(Epic epic) {
        if (epic != null) {
            id++;
            epic.setId(id);
            listOfEpics.put(id, epic);
        }
    }

    public void createNewSubTask(SubTask subTask) {
        if (subTask != null) {
            id++;
            subTask.setId(id);
            listOfSubTasks.put(id, subTask);
            linkEpicToSubTask(subTask);
            updateEpicStatus(subTask.getEpicId());
        }
    }

    public void dropListsOfTasksEpicsAndSubTasks() {
        dropListOfTasks();
        dropListOfEpicsAndSubTasks();
        id = 0;
    }

    public void dropListOfTasks() {
        listOfTasks.clear();
    }

    public void dropListOfEpicsAndSubTasks() {
        dropListOfSubTasks();
        listOfEpics.clear();
    }

    public void dropListOfSubTasks() {
        listOfSubTasks.clear();
        listOfEpics.forEach((id, epic) -> epic.setSubTasksIds(new ArrayList<>()));
        listOfEpics.forEach((id, epic) -> updateEpicStatus(epic.getId()));
    }

    public HashMap<Integer, Task> getListOfTasks() {
        return listOfTasks;
    }

    public HashMap<Integer, Epic> getListOfEpics() {
        return listOfEpics;
    }

    public HashMap<Integer, SubTask> getListOfSubTasks() {
        return listOfSubTasks;
    }

    public Task getTaskById(int id) {
        if (listOfTasks.containsKey(id)) {
            return listOfTasks.get(id);
        } else {
            throw new RuntimeException("Can't find task with id " + id);
        }
    }

    public Epic getEpicById(int id) {
        if (listOfEpics.containsKey(id)) {
            return listOfEpics.get(id);
        } else {
            throw new RuntimeException("Can't find epic with id " + id);
        }
    }

    public SubTask getSubTaskById(int id) {
        if (listOfSubTasks.containsKey(id)) {
            return listOfSubTasks.get(id);
        } else {
            throw new RuntimeException("Can't find subtask with id " + id);
        }
    }


    public void updateTask(int id, Task task) {
        if (listOfTasks.containsKey(id)) {
            listOfTasks.replace(id, task);
        } else {
            throw new RuntimeException("Can't update task with id " + id + ", no task with such id.");
        }
    }

    public void updateEpic(int id, Epic epic) {
        if (listOfEpics.containsKey(id)) {
            epic.setId(id);
            epic.setSubTasksIds(listOfEpics.get(id).getSubTasksIds());
            listOfEpics.replace(id, epic);
            updateEpicStatus(id);
        } else {
            throw new RuntimeException("Can't update epic with id " + id + ", no epic with such id.");
        }
    }

    public void updateSubTask(int id, SubTask subTask) {
        if (listOfSubTasks.containsKey(id)) {
            subTask.setId(id);
            if (listOfSubTasks.get(id).getEpicId() != subTask.getEpicId()) {
                linkEpicToSubTask(subTask);
                updateEpicStatus(subTask.getEpicId());
                Epic epic = listOfEpics.get(subTask.getEpicId());
                ArrayList<Integer> subTasksIds = epic.getSubTasksIds();
                subTasksIds.remove((Integer) id);
                epic.setSubTasksIds(subTasksIds);
                updateEpic(subTask.getEpicId(), epic);
            }
            listOfSubTasks.replace(id, subTask);
            updateEpicStatus(subTask.getEpicId());
        } else {
            throw new RuntimeException("Can't update subtask with id " + id + ", no subtask with such id.");
        }
    }

    public void removeTaskById(int id) {
        if (listOfTasks.containsKey(id)) {
            listOfTasks.remove(id);
        } else {
            throw new RuntimeException("Task with id " + id + " don't exist.");
        }
    }

    public void removeEpicById(int id) {
        if (listOfEpics.containsKey(id)) {
            Epic epic = listOfEpics.get(id);
            listOfEpics.remove(id);
            ArrayList<Integer> subTasksIds = epic.getSubTasksIds();
            if (subTasksIds != null) {
                for (Integer subTasksId : subTasksIds) {
                    listOfSubTasks.remove(subTasksId);
                }
            }
        } else {
            throw new RuntimeException("Epic with id " + id + " don't exist.");
        }
    }

    public void removeSubTaskById(int id) {
        if (listOfSubTasks.containsKey(id)) {
            SubTask subTask = listOfSubTasks.get(id);
            Epic epic = listOfEpics.get(subTask.getEpicId());
            ArrayList<Integer> subTasksIds = epic.getSubTasksIds();
            listOfSubTasks.remove(id);
            subTasksIds.remove((Integer) id);
            epic.setSubTasksIds(subTasksIds);
            updateEpic(subTask.getEpicId(), epic);
            updateEpicStatus(subTask.getEpicId());
        } else {
            throw new RuntimeException("SubTask with id " + id + " don't exist.");
        }
    }

    private void linkEpicToSubTask(SubTask subTask) {
        if (listOfEpics.containsKey(subTask.getEpicId())) {
            Epic epic = listOfEpics.get(subTask.getEpicId());
            epic.addSubTaskId(subTask.getId());
            listOfEpics.replace(subTask.getEpicId(), epic);
        } else {
            throw new RuntimeException("Can't find epic with id " + id);
        }
    }

    private void updateEpicStatus(int id) {
        if (listOfEpics.containsKey(id)) {
            Epic epic = listOfEpics.get(id);
            ArrayList<Integer> subTasksIds = epic.getSubTasksIds();
            int counterNew = 0;
            int counterDone = 0;

            for (int subTasksId : subTasksIds) {
                if (listOfSubTasks.get(subTasksId).getStatus().equals(Status.NEW)) {
                    counterNew++;
                } else if (listOfSubTasks.get(subTasksId).getStatus().equals(Status.DONE)) {
                    counterDone++;
                }
            }
            if (subTasksIds.size() == 0 || counterNew == subTasksIds.size()) {
                epic.setStatus(Status.NEW);
            } else if (counterDone == subTasksIds.size()) {
                epic.setStatus(Status.DONE);
            } else {
                epic.setStatus(Status.IN_PROGRESS);
            }
            listOfEpics.replace(id, epic);
        } else {
            throw new RuntimeException("Can't update status for epic with id " + id + ", can't find epic with such id");
        }
    }
}