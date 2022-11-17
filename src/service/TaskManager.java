package service;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.HashMap;
import java.util.List;

public interface TaskManager {
    void createNewTask(Task task);

    void createNewEpic(Epic epic);

    void createNewSubTask(SubTask subTask);

    void dropListsOfTasksEpicsAndSubTasks();

    void dropListOfTasks();

    void dropListOfEpicsAndSubTasks();

    void dropListOfSubTasks();

    HashMap<Integer, Task> getCatalogOfTasks();

    HashMap<Integer, Epic> getCatalogOfEpics();

    HashMap<Integer, SubTask> getCatalogOfSubTasks();

    Task getTaskById(int id);

    Epic getEpicById(int id);

    SubTask getSubTaskById(int id);

    List<Task> getHistory();

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubTask(SubTask subTask);

    void removeTaskById(int id);

    void removeEpicById(int id);

    void removeSubTaskById(int id);
}