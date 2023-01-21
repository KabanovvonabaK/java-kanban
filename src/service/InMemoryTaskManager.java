package service;

import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int id = 0;
    private final HashMap<Integer, Task> catalogOfTasks;
    private final HashMap<Integer, Epic> catalogOfEpics;
    private final HashMap<Integer, SubTask> catalogOfSubTasks;
    protected final HistoryManager history;

    private final Comparator<Task> priorityComparator = Comparator.comparing(Task::getStartTime);

    private final TreeSet<Task> tasksByPriority = new TreeSet<>(priorityComparator);

    public InMemoryTaskManager() {
        this.catalogOfTasks = new HashMap<>();
        this.catalogOfEpics = new HashMap<>();
        this.catalogOfSubTasks = new HashMap<>();
        this.history = Managers.getDefaultHistory();
    }

    @Override
    public void createNewTask(Task task) {
        if (task != null) {
            if (!checkTimeConflict(task)) {
                id++;
                task.setId(id);
                catalogOfTasks.put(id, task);
                tasksByPriority.add(task);
            } else {
                throw new RuntimeException("Impossible to add a task - time conflict");
            }
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
            if (!checkTimeConflict(subTask)) {
                id++;
                subTask.setId(id);
                catalogOfSubTasks.put(id, subTask);
                linkEpicToSubTask(subTask);
                updateEpicStatusStartTimeAndDuration(subTask.getEpicId());
                tasksByPriority.add(subTask);
            } else {
                throw new RuntimeException("Impossible to add a subTask - time conflict");
            }
        }
    }

    @Override
    public void dropListsOfTasksEpicsAndSubTasks() {
        dropListOfTasks();
        dropListOfEpicsAndSubTasks();
        id = 0;
        tasksByPriority.clear();
    }

    @Override
    public void dropListOfTasks() {
        removeTasksFromPrioritizedList(catalogOfTasks.keySet());
        catalogOfTasks.clear();
    }

    @Override
    public void dropListOfEpicsAndSubTasks() {
        dropListOfSubTasks();
        removeTasksFromPrioritizedList(catalogOfEpics.keySet());
        catalogOfEpics.clear();
    }

    @Override
    public void dropListOfSubTasks() {
        removeTasksFromPrioritizedList(catalogOfSubTasks.keySet());
        catalogOfSubTasks.clear();
        catalogOfEpics.forEach((id, epic) -> epic.setSubTasksIds(new ArrayList<>()));
        catalogOfEpics.forEach((id, epic) -> updateEpicStatusStartTimeAndDuration(epic.getId()));
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
            if (!checkTimePeriod(task, catalogOfTasks.get(task.getId()))) {
                if (!checkTimeConflict(task)) {
                    catalogOfTasks.replace(task.getId(), task);
                    removeTasksFromPrioritizedList(Collections.singleton(task.getId()));
                    tasksByPriority.add(task);
                } else {
                    throw new RuntimeException("Impossible to update a task - time conflict");
                }
            } else {
                catalogOfTasks.replace(task.getId(), task);
                removeTasksFromPrioritizedList(Collections.singleton(task.getId()));
                tasksByPriority.add(task);
            }
        } else {
            throw new RuntimeException("Can't update task with id " + task.getId() + ", no task with such id.");
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (catalogOfEpics.containsKey(epic.getId())) {
            epic.setId(epic.getId());
            epic.setSubTasksIds(catalogOfEpics.get(epic.getId()).getSubTasksIds());
            catalogOfEpics.replace(epic.getId(), epic);
            updateEpicStatusStartTimeAndDuration(epic.getId());
        } else {
            throw new RuntimeException("Can't update epic with id " + epic.getId() + ", no epic with such id.");
        }
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        if (catalogOfSubTasks.containsKey(subTask.getId())) {
            if (!checkTimePeriod(subTask, catalogOfSubTasks.get(subTask.getId()))) {
                if (!checkTimeConflict(subTask)) {
                    subTask.setId(subTask.getId());
                    if (catalogOfSubTasks.get(subTask.getId()).getEpicId() != subTask.getEpicId()) {
                        linkEpicToSubTask(subTask);
                        updateEpicStatusStartTimeAndDuration(subTask.getEpicId());
                        Epic epic = catalogOfEpics.get(subTask.getEpicId());
                        ArrayList<Integer> subTasksIds = epic.getSubTasksIds();
                        subTasksIds.remove((Integer) subTask.getId());
                        epic.setSubTasksIds(subTasksIds);
                        updateEpic(epic);
                    }
                    catalogOfSubTasks.replace(subTask.getId(), subTask);
                    updateEpicStatusStartTimeAndDuration((subTask.getEpicId()));
                    removeTasksFromPrioritizedList(Collections.singleton(subTask.getId()));
                    tasksByPriority.add(subTask);
                    updateEpicStatusStartTimeAndDuration(subTask.getEpicId());
                } else {
                    throw new RuntimeException("Impossible to update a subTask - time conflict");
                }
            } else {
                subTask.setId(subTask.getId());
                if (catalogOfSubTasks.get(subTask.getId()).getEpicId() != subTask.getEpicId()) {
                    linkEpicToSubTask(subTask);
                    updateEpicStatusStartTimeAndDuration(subTask.getEpicId());
                    Epic epic = catalogOfEpics.get(subTask.getEpicId());
                    ArrayList<Integer> subTasksIds = epic.getSubTasksIds();
                    subTasksIds.remove((Integer) subTask.getId());
                    epic.setSubTasksIds(subTasksIds);
                    updateEpic(epic);
                }
                catalogOfSubTasks.replace(subTask.getId(), subTask);
                updateEpicStatusStartTimeAndDuration((subTask.getEpicId()));
                removeTasksFromPrioritizedList(Collections.singleton(subTask.getId()));
                tasksByPriority.add(subTask);
                updateEpicStatusStartTimeAndDuration(subTask.getEpicId());
            }
        } else {
            throw new RuntimeException("Can't update subtask with id " + subTask.getId() + ", no subtask with such id.");
        }
    }

    @Override
    public void removeTaskById(int id) {
        if (catalogOfTasks.containsKey(id)) {
            catalogOfTasks.remove(id);
            removeTasksFromPrioritizedList(Collections.singleton(id));
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
            updateEpic(epic);
            removeTasksFromPrioritizedList(Collections.singleton(id));
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
            throw new RuntimeException("Can't find epic with id " + subTask.getEpicId());
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

    private void updateEpicDuration(int id) {
        if (catalogOfEpics.containsKey(id)) {
            Epic epic = catalogOfEpics.get(id);
            ArrayList<Integer> subTaskIds = epic.getSubTasksIds();
            Duration epicDuration = Duration.ZERO;

            if (subTaskIds == null) {
                throw new RuntimeException("calculateEpicDuration() called for epic with subTaskIds == null");
            } else if (subTaskIds.size() == 0) {

            } else if (subTaskIds.size() == 1) {
                epicDuration = epicDuration.plusMinutes(catalogOfSubTasks.get(subTaskIds.get(0)).getDuration());
            } else {
                ZonedDateTime start = catalogOfSubTasks.get(subTaskIds.get(0)).getStartTime();
                ZonedDateTime end = catalogOfSubTasks.get(subTaskIds.get(0)).getEndTime();
                for (Integer subTaskId : subTaskIds) {
                    if (catalogOfSubTasks.get(subTaskId).getStartTime().isBefore(start)) {
                        start = catalogOfSubTasks.get(subTaskId).getStartTime();
                    } else if (catalogOfSubTasks.get(subTaskId).getEndTime().isAfter(end)) {
                        end = catalogOfSubTasks.get(subTaskId).getEndTime();
                    }
                }
                epicDuration = Duration.between(start, end);
            }

            epic.setDuration(epicDuration.toMinutes());
            catalogOfEpics.replace(id, epic);
        } else {
            throw new RuntimeException("Can't calculate duration for epic with id " + id +
                    ", can't find epic with such id");
        }
    }

    private void updateEpicStartTime(int id) {
        if (catalogOfEpics.containsKey(id)) {
            Epic epic = catalogOfEpics.get(id);
            ArrayList<Integer> subTaskIds = epic.getSubTasksIds();

            if (subTaskIds == null) {
                throw new RuntimeException("findEpicStartTime() called for epic with subTaskIds == null");
            } else if (subTaskIds.size() == 0) {
                // возможно не лучшее решение
                epic.setStartTime(null);
            } else if (subTaskIds.size() == 1) {
                epic.setStartTime(catalogOfSubTasks.get(subTaskIds.get(0)).getStartTime());
            } else {
                ZonedDateTime earliestStart = catalogOfSubTasks.get(subTaskIds.get(0)).getStartTime();
                for (Integer subTaskId : subTaskIds) {
                    if (earliestStart.isAfter(catalogOfSubTasks.get(subTaskId).getStartTime())) {
                        earliestStart = catalogOfSubTasks.get(subTaskId).getStartTime();
                    }
                }
                epic.setStartTime(earliestStart);
                catalogOfEpics.replace(id, epic);
            }
        } else {
            throw new RuntimeException("Can't find startTime for epic with id " + id +
                    ", can't find epic with such id");
        }
    }

    private void updateEpicStatusStartTimeAndDuration(int id) {
        updateEpicStatus(id);
        updateEpicDuration(id);
        updateEpicStartTime(id);
    }

    public TreeSet<Task> getPrioritizedTasks() {
        return tasksByPriority;
    }

    private boolean checkTimeConflict(Task task) {
        ZonedDateTime taskStartTime = task.getStartTime();
        ZonedDateTime taskEndTime = task.getEndTime();
        boolean isTimeConflict = false;

        if (getPrioritizedTasks().size() > 0) {
            for (Task prioritizedTask : getPrioritizedTasks()) {
                ZonedDateTime prioritizedTaskStartTime = prioritizedTask.getStartTime();
                ZonedDateTime prioritizedTaskEndTime = prioritizedTask.getEndTime();

                if (taskStartTime.equals(prioritizedTaskStartTime) ||
                        (taskStartTime.isAfter(prioritizedTaskStartTime)
                                && taskStartTime.isBefore(prioritizedTaskEndTime))) {
                    isTimeConflict = true;
                } else if (taskEndTime.equals(prioritizedTaskEndTime) ||
                        (taskEndTime.isAfter(prioritizedTaskStartTime) &&
                                taskEndTime.isBefore(prioritizedTaskEndTime))) {
                    isTimeConflict = true;
                }
            }
        }
        return isTimeConflict;
    }

    private void removeTasksFromPrioritizedList(Set<Integer> ids) {
        Iterator<Task> iterator = tasksByPriority.iterator();
        while (iterator.hasNext()) {
            Task task = iterator.next();
            for (Integer id : ids) {
                if (task.getId() == id) {
                    iterator.remove();
                }
            }
        }
    }

    private boolean checkTimePeriod(Task t1, Task t2) {
        return t1.getStartTime().equals(t2.getStartTime()) &&
                t1.getEndTime().equals(t2.getEndTime());
    }
}