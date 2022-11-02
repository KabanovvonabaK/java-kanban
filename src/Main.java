import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
import service.Manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Main {
    // For using asset please don't forget to add -enableassertions or -ea as VM option
    public static void main(String[] args) {
        // Creation test section
        Manager managerCreation = getNewManager();
        // check task created successfully
        Task taskCheck = managerCreation.getTaskById(6);
        assert Objects.equals(taskCheck.getSummary(), "Summary of simple task")
                : "Task's summary is broken";
        assert Objects.equals(taskCheck.getDescription(), "Application testing")
                : "Task's description is broken";
        assert Objects.equals(taskCheck.getStatus(), Status.NEW)
                : "Task's status is broken";

        // check epic created successfully
        Epic epicFirstCheck = managerCreation.getEpicById(1);
        assert Objects.equals(epicFirstCheck.getSummary(), "Summary of first epic")
                : "Epic's summary is broken";
        assert Objects.equals(epicFirstCheck.getDescription(), "Application testing")
                : "Epic's description is broken";
        assert Objects.equals(epicFirstCheck.getStatus(), Status.NEW)
                : "Epic's status is broken";

        // check subtask created successfully
        SubTask subTaskFirstForFirstEpicCheck = managerCreation.getSubTaskById(2);
        assert Objects.equals(subTaskFirstForFirstEpicCheck.getSummary(),
                "Summary for first subtask of first epic")
                : "SubTasks's summary is broken";
        assert Objects.equals(subTaskFirstForFirstEpicCheck.getDescription(), "Application testing")
                : "SubTasks's description is broken";
        assert Objects.equals(subTaskFirstForFirstEpicCheck.getStatus(), Status.NEW)
                : "SubTasks's status is broken";
        assert Objects.equals(subTaskFirstForFirstEpicCheck.getEpicId(), 1)
                : "SubTasks's epicId is broken";

        // getListOf%() test section
        Manager managerGetList = getNewManager();
        // check listOfTasks()
        HashMap<Integer, Task> recreatedListOfTasks = new HashMap<>();
        Task task = new Task("Summary of simple task",
                "Application testing", Status.NEW);
        task.setId(6);
        recreatedListOfTasks.put(6, task);
        assert Objects.equals(managerGetList.getListOfTasks().get(6).hashCode(), recreatedListOfTasks.get(6).hashCode())
                : "listOfTasks() not the same as the original";

        // check listOfEpics()
        HashMap<Integer, Epic> recreatedListOfEpics = new HashMap<>();
        Epic epicFirstRecreated = new Epic("Summary of first epic", "Application testing",
                Status.NEW);
        Epic epicSecondRecreated = new Epic("Summary of second epic", "Application testing",
                Status.NEW);
        epicFirstRecreated.setId(1);
        epicFirstRecreated.addSubTaskId(2);
        epicFirstRecreated.addSubTaskId(3);
        epicSecondRecreated.setId(4);
        epicSecondRecreated.addSubTaskId(5);
        recreatedListOfEpics.put(1, epicFirstRecreated);
        recreatedListOfEpics.put(4, epicSecondRecreated);
        assert Objects.equals(recreatedListOfEpics.get(1).hashCode(), managerGetList.getListOfEpics().get(1).hashCode())
                : "First epic from listOfEpics() not the same as the original";
        assert Objects.equals(recreatedListOfEpics.get(4).hashCode(), managerGetList.getListOfEpics().get(4).hashCode())
                : "First epic from listOfEpics() not the same as the original";

        // check listOfSubTasks()
        HashMap<Integer, SubTask> recreatedListOfSubTasks = new HashMap<>();
        SubTask subTask2 = new SubTask("Summary for first subtask of first epic",
                "Application testing", Status.NEW, 1);
        subTask2.setId(2);
        SubTask subTask3 = new SubTask("Summary for second subtask of first epic",
                "Application testing", Status.NEW, 1);
        subTask3.setId(3);
        SubTask subTask5 = new SubTask("Summary for first subtask of second epic",
                "Application testing", Status.NEW, 4);
        subTask5.setId(5);
        recreatedListOfSubTasks.put(2, subTask2);
        recreatedListOfSubTasks.put(3, subTask3);
        recreatedListOfSubTasks.put(5, subTask5);
        assert Objects.equals(recreatedListOfSubTasks.get(2).hashCode(),
                managerGetList.getListOfSubTasks().get(2).hashCode())
                : "First subtask from listOfSubTasks() not the same as the original";
        assert Objects.equals(recreatedListOfSubTasks.get(3).hashCode(),
                managerGetList.getListOfSubTasks().get(3).hashCode())
                : "Second subtask from listOfSubTasks() not the same as the original";
        assert Objects.equals(recreatedListOfSubTasks.get(5).hashCode(),
                managerGetList.getListOfSubTasks().get(5).hashCode())
                : "Third subtask from listOfSubTasks() not the same as the original";

        // getTask, getEpic, getSubTask test section
        Manager managerGet = getNewManager();
        Task task1 = new Task("Summary of simple task", "Application testing", Status.NEW);
        task1.setId(6);
        assert Objects.equals(managerGet.getTaskById(6).hashCode(), task1.hashCode())
                : "Returned task not the same as original";

        Epic epic = new Epic("Summary of first epic", "Application testing", Status.NEW);
        epic.setId(1);
        epic.addSubTaskId(2);
        epic.addSubTaskId(3);
        assert Objects.equals(managerGet.getEpicById(1).hashCode(), epic.hashCode())
                : "Returned epic not the same as original";

        SubTask subTask = new SubTask("Summary for first subtask of first epic",
                "Application testing", Status.NEW, 1);
        subTask.setId(2);
        assert Objects.equals(managerGet.getSubTaskById(2).hashCode(), subTask.hashCode())
                : "Returned subtask not the same as original";

        // 'Update' test section
        Manager managerUpdate = getNewManager();
        Task newTask = new Task("Updated summary of simple task", "Application testing",
                Status.NEW);
        newTask.setId(6);
        managerUpdate.updateTask(6, newTask);
        assert Objects.equals(managerUpdate.getTaskById(6).hashCode(), newTask.hashCode())
                : "Task update failed";

        Epic newEpic = new Epic("Updated summary of first epic", "Application testing",
                Status.NEW);
        newEpic.setId(1);
        newEpic.addSubTaskId(2);
        newEpic.addSubTaskId(3);
        int newEpicHashCode = newEpic.hashCode();
        newEpic.setStatus(Status.DONE);
        managerUpdate.updateEpic(1, newEpic);
        assert Objects.equals(managerUpdate.getEpicById(1).hashCode(), newEpicHashCode)
                : "Epic update failed";

        SubTask subTask1 = new SubTask("Updated summary for first subtask of first epic",
                "Application testing", Status.DONE, 1);
        subTask1.setId(2);
        int newSubTaskHashCode = subTask1.hashCode();
        managerUpdate.updateSubTask(2, subTask1);
        assert Objects.equals(managerUpdate.getSubTaskById(2).hashCode(), newSubTaskHashCode)
                : "SubTask update failed";
        assert Objects.equals(managerUpdate.getEpicById(1).getStatus(), Status.IN_PROGRESS)
                : "Epic status update failed during subtask with id " + subTask1.getId() + " update";

        // Drop test section
        Manager managerDrop = getNewManager();
        managerDrop.dropListsOfTasksEpicsAndSubTasks();
        assert managerDrop.getListOfTasks().size() == 0 : "Tasks list wasn't cleared but should";
        assert managerDrop.getListOfEpics().size() == 0 : "Epics list wasn't cleared but should";
        assert managerDrop.getListOfSubTasks().size() == 0 : "SubTasks list wasn't cleared but should";

        managerDrop = getNewManager();
        managerDrop.dropListOfTasks();
        assert managerDrop.getListOfTasks().size() == 0 : "Tasks list wasn't cleared but should";
        assert managerDrop.getListOfEpics().size() == 2 : "Epics list was cleared but shouldn't";
        assert managerDrop.getListOfSubTasks().size() == 3 : "SubTasks list was cleared but shouldn't";

        managerDrop = getNewManager();
        managerDrop.dropListOfEpicsAndSubTasks();
        assert managerDrop.getListOfTasks().size() == 1 : "Tasks list was cleared but shouldn't";
        assert managerDrop.getListOfEpics().size() == 0 : "Epics list wasn't cleared but should";
        assert managerDrop.getListOfSubTasks().size() == 0 : "SubTasks list wasn't cleared but should";

        managerDrop = getNewManager();
        managerDrop.dropListOfSubTasks();
        assert managerDrop.getListOfTasks().size() == 1 : "Tasks list was cleared but shouldn't";
        assert managerDrop.getListOfEpics().size() == 2 : "Epics list was cleared but shouldn't";
        assert managerDrop.getListOfSubTasks().size() == 0 : "SubTasks list wasn't cleared but should";
        ArrayList<ArrayList<Integer>> subTasksIds = new ArrayList<>();
        managerDrop.getListOfEpics().forEach((Integer, Epic) -> subTasksIds.add(Epic.getSubTasksIds()));
        assert subTasksIds.get(0).size() == 0 : "Not all subtasks ids were cleared from epics";
        assert subTasksIds.get(1).size() == 0 : "Not all subtasks ids were cleared from epics";

        // Remove test section
        Manager managerRemove = getNewManager();

        managerRemove.removeTaskById(6);
        assert Objects.equals(managerRemove.getListOfTasks().size(), 0)
                : "Remove task by id 6 failed";

        managerRemove.removeEpicById(1);
        assert Objects.equals(managerRemove.getListOfEpics().size(), 1)
                : "Remove epic by id 1 failed";
        assert Objects.equals(managerRemove.getListOfSubTasks().size(), 1)
                : "Remove subtasks after removing epic with id 1 failed";

        SubTask subTask4 = new SubTask("Summary for first subtask of second epic",
                "Application testing", Status.IN_PROGRESS, 4);
        managerRemove.updateSubTask(5, subTask4);
        managerRemove.removeSubTaskById(5);
        assert Objects.equals(managerRemove.getListOfSubTasks().size(), 0)
                : "Remove last one subtask by id 5 failed";
        assert Objects.equals(managerRemove.getListOfEpics().get(4).getStatus(), Status.NEW)
                : "After removing last subtask linked to epic - " +
                "status if such epic didn't change from IN_PROGRESS to NEW";
    }

    private static Manager getNewManager() {
        Manager manager = new Manager();

        Epic epicFirst = new Epic("Summary of first epic", "Application testing", Status.NEW);
        SubTask subTaskFirstForFirstEpic = new SubTask("Summary for first subtask of first epic",
                "Application testing", Status.NEW, 1);
        SubTask subTaskSecondForFirstEpic = new SubTask("Summary for second subtask of first epic",
                "Application testing", Status.NEW, 1);
        Epic epicSecond = new Epic("Summary of second epic", "Application testing", Status.NEW);
        SubTask subTaskFirstForSecondEpic = new SubTask("Summary for first subtask of second epic",
                "Application testing", Status.NEW, 4);
        Task task = new Task("Summary of simple task", "Application testing", Status.NEW);

        manager.createNewEpic(epicFirst);
        manager.createNewSubTask(subTaskFirstForFirstEpic);
        manager.createNewSubTask(subTaskSecondForFirstEpic);
        manager.createNewEpic(epicSecond);
        manager.createNewSubTask(subTaskFirstForSecondEpic);
        manager.createNewTask(task);

        return manager;
    }
}
