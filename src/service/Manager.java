package service;

import model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Manager {

    int id = 0;
    private HashMap<Integer, Task> listOfTasks;

    public Manager() {
        this.listOfTasks = new HashMap<>();
    }

    public HashMap<Integer, Task> getListOfTasks() {
        return listOfTasks;
    }

    public void dropListOfTasks() {
        listOfTasks.clear();
        id = 0;
    }

    public Task getTaskById(int id) {
        return listOfTasks.get(id);
    }

    public void createNewTask(Task task) {
        if (task != null) {
            id++;
            listOfTasks.put(id, task);
            if (task.getClass().getName().equals("model.SubTask")) {
                linkEpicToSubTask((SubTask) task);
                updateEpicStatus(((SubTask) task).getEpicId());
            }
        }
    }

    public void updateTask(int id, Task task) {
        listOfTasks.replace(id, task);
        if (task.getClass().getName().equals("model.SubTask")) {
            updateEpicStatus(((SubTask) task).getEpicId());
        }
    }

    public void removeTaskById(int id) {
        Task task = listOfTasks.get(id);
        listOfTasks.remove(id);
        if (task.getClass().getName().equals("model.SubTask")) {
            SubTask subTask = (SubTask) task;
            Epic epic = (Epic) listOfTasks.get(subTask.getEpicId());
            ArrayList<Integer> subTasksIds = epic.getSubTasksIds();

            subTasksIds.remove((Integer) id);
            epic.setSubTasksIds(subTasksIds);
            updateTask(((SubTask) task).getEpicId(), epic);
            updateEpicStatus(((SubTask) task).getEpicId());
        }
    }

    public ArrayList<SubTask> getListOfSubTasks(int id) {
        if (listOfTasks.get(id).getClass().getName().equals("model.Epic")) {
            Epic epic = (Epic) listOfTasks.get(id);
            ArrayList<Integer> listOfSubTasksIds = epic.getSubTasksIds();
            ArrayList<SubTask> listOfSubTasks = new ArrayList<>();

            for (Integer listOfSubTasksId : listOfSubTasksIds) {
                listOfSubTasks.add((SubTask) listOfTasks.get(listOfSubTasksId));
            }
            return listOfSubTasks;
        } else return null;
    }

    private void linkEpicToSubTask(SubTask subTask) {
        Epic epic = (Epic) listOfTasks.get(subTask.getEpicId());
        epic.setSubTasksIds(id);
        listOfTasks.replace(subTask.getEpicId(), epic);
    }

    private void updateEpicStatus(int id) {
        if (listOfTasks.get(id) != null) {
            Epic epic = (Epic) listOfTasks.get(id);
            ArrayList<SubTask> subTasks = getListOfSubTasks(id);
            int counterNew = 0;
            int counterDone = 0;

            for (SubTask subTask : subTasks) {
                if (subTask.getStatus().equals(Status.NEW)) counterNew++;
                if (subTask.getStatus().equals(Status.DONE)) counterDone++;
            }

            if (subTasks.size() == 0 || counterNew == subTasks.size()) epic.setStatus(Status.NEW);
            else if (counterDone == subTasks.size()) epic.setStatus(Status.DONE);
            else epic.setStatus(Status.IN_PROGRESS);

            updateTask(id, epic);
        }
    }
}