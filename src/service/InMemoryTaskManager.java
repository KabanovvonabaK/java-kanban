package service;

import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private int id = 0;
    private final HashMap<Integer, Task> catalogOfTasks;
    private final HashMap<Integer, Epic> catalogOfEpics;
    private final HashMap<Integer, SubTask> catalogOfSubTasks;
    private final HistoryManager history;

    public InMemoryTaskManager() {
        this.catalogOfTasks = new HashMap<>();
        this.catalogOfEpics = new HashMap<>();
        this.catalogOfSubTasks = new HashMap<>();
        this.history = Managers.getDefaultHistory();
    }

    @Override
    public void createNewTask(Task task) {
        if (task != null) {
            id++;
            task.setId(id);
            catalogOfTasks.put(id, task);
        }
    }

    @Override
    public void createNewEpic(Epic epic) {
        if (epic != null) {
            id++;
            epic.setId(id);
            catalogOfEpics.put(id, epic);
        }
    }

    @Override
    public void createNewSubTask(SubTask subTask) {
        if (subTask != null) {
            id++;
            subTask.setId(id);
            catalogOfSubTasks.put(id, subTask);
            linkEpicToSubTask(subTask);
            updateEpicStatus(subTask.getEpicId());
        }
    }

    @Override
    public void dropListsOfTasksEpicsAndSubTasks() {
        dropListOfTasks();
        dropListOfEpicsAndSubTasks();
        id = 0;
    }

    @Override
    public void dropListOfTasks() {
        catalogOfTasks.clear();
    }

    @Override
    public void dropListOfEpicsAndSubTasks() {
        dropListOfSubTasks();
        catalogOfEpics.clear();
    }

    @Override
    public void dropListOfSubTasks() {
        catalogOfSubTasks.clear();
        catalogOfEpics.forEach((id, epic) -> epic.setSubTasksIds(new ArrayList<>()));
        catalogOfEpics.forEach((id, epic) -> updateEpicStatus(epic.getId()));
    }

    @Override
    public HashMap<Integer, Task> getCatalogOfTasks() {
        return catalogOfTasks;
    }

    @Override
    public HashMap<Integer, Epic> getCatalogOfEpics() {
        return catalogOfEpics;
    }

    @Override
    public HashMap<Integer, SubTask> getCatalogOfSubTasks() {
        return catalogOfSubTasks;
    }

    @Override
    public Task getTaskById(int id) {
        if (catalogOfTasks.containsKey(id)) {
            history.add(catalogOfTasks.get(id));
            return catalogOfTasks.get(id);
        } else {
            throw new RuntimeException("Can't find task with id " + id);
        }
    }

    @Override
    public Epic getEpicById(int id) {
        if (catalogOfEpics.containsKey(id)) {
            history.add(catalogOfEpics.get(id));
            return catalogOfEpics.get(id);
        } else {
            throw new RuntimeException("Can't find epic with id " + id);
        }
    }

    @Override
    public SubTask getSubTaskById(int id) {
        if (catalogOfSubTasks.containsKey(id)) {
            history.add(catalogOfSubTasks.get(id));
            return catalogOfSubTasks.get(id);
        } else {
            throw new RuntimeException("Can't find subtask with id " + id);
        }
    }

    @Override
    public List<Task> getHistory() {
        return history.getHistory();
    }

    @Override
    public void updateTask(Task task) {
        if (catalogOfTasks.containsKey(task.getId())) {
            catalogOfTasks.replace(task.getId(), task);
        } else {
            throw new RuntimeException("Can't update task with id " + task.getId() + ", no task with such id.");
        }
    }

    @Override
    public void updateEpic(int id, Epic epic) {
        if (catalogOfEpics.containsKey(id)) {
            epic.setId(id);
            epic.setSubTasksIds(catalogOfEpics.get(id).getSubTasksIds());
            catalogOfEpics.replace(id, epic);
            updateEpicStatus(id);
        } else {
            throw new RuntimeException("Can't update epic with id " + id + ", no epic with such id.");
        }
    }

    @Override
    public void updateSubTask(int id, SubTask subTask) {
        if (catalogOfSubTasks.containsKey(id)) {
            subTask.setId(id);
            if (catalogOfSubTasks.get(id).getEpicId() != subTask.getEpicId()) {
                linkEpicToSubTask(subTask);
                updateEpicStatus(subTask.getEpicId());
                Epic epic = catalogOfEpics.get(subTask.getEpicId());
                ArrayList<Integer> subTasksIds = epic.getSubTasksIds();
                subTasksIds.remove((Integer) id);
                epic.setSubTasksIds(subTasksIds);
                updateEpic(subTask.getEpicId(), epic);
            }
            catalogOfSubTasks.replace(id, subTask);
            updateEpicStatus(subTask.getEpicId());
        } else {
            throw new RuntimeException("Can't update subtask with id " + id + ", no subtask with such id.");
        }
    }

    @Override
    public void removeTaskById(int id) {
        if (catalogOfTasks.containsKey(id)) {
            catalogOfTasks.remove(id);
        } else {
            throw new RuntimeException("Task with id " + id + " don't exist.");
        }
    }

    @Override
    public void removeEpicById(int id) {
        if (catalogOfEpics.containsKey(id)) {
            Epic epic = catalogOfEpics.get(id);
            catalogOfEpics.remove(id);
            ArrayList<Integer> subTasksIds = epic.getSubTasksIds();
            if (subTasksIds != null) {
                for (Integer subTasksId : subTasksIds) {
                    catalogOfSubTasks.remove(subTasksId);
                }
            }
        } else {
            throw new RuntimeException("Epic with id " + id + " don't exist.");
        }
    }

    @Override
    public void removeSubTaskById(int id) {
        if (catalogOfSubTasks.containsKey(id)) {
            SubTask subTask = catalogOfSubTasks.get(id);
            Epic epic = catalogOfEpics.get(subTask.getEpicId());
            ArrayList<Integer> subTasksIds = epic.getSubTasksIds();
            catalogOfSubTasks.remove(id);
            subTasksIds.remove((Integer) id);
            epic.setSubTasksIds(subTasksIds);
            updateEpic(subTask.getEpicId(), epic);
            updateEpicStatus(subTask.getEpicId());
        } else {
            throw new RuntimeException("SubTask with id " + id + " don't exist.");
        }
    }

    private void linkEpicToSubTask(SubTask subTask) {
        if (catalogOfEpics.containsKey(subTask.getEpicId())) {
            Epic epic = catalogOfEpics.get(subTask.getEpicId());
            epic.addSubTaskId(subTask.getId());
            catalogOfEpics.replace(subTask.getEpicId(), epic);
        } else {
            throw new RuntimeException("Can't find epic with id " + id);
        }
    }

    private void updateEpicStatus(int id) {
        if (catalogOfEpics.containsKey(id)) {
            Epic epic = catalogOfEpics.get(id);
            ArrayList<Integer> subTasksIds = epic.getSubTasksIds();
            int counterNew = 0;
            int counterDone = 0;

            for (int subTasksId : subTasksIds) {
                if (catalogOfSubTasks.get(subTasksId).getStatus().equals(Status.NEW)) {
                    counterNew++;
                } else if (catalogOfSubTasks.get(subTasksId).getStatus().equals(Status.DONE)) {
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
            catalogOfEpics.replace(id, epic);
        } else {
            throw new RuntimeException("Can't update status for epic with id " + id + ", can't find epic with such id");
        }
    }
}