import model.*;
import service.Manager;

public class Main {

    public static void main(String[] args) {

        Manager manager = new Manager();

        Task epicFirst = new Epic("Название первого эпика", "Тестируем приложение", Status.NEW);
        Task subTaskFirstForFirstEpic = new SubTask("Название первого сабтаска для первого эпика", "Тестируем приложение", Status.NEW, 1);
        Task subTaskSecondForFirstEpic = new SubTask("Название второго сабтаска для первого эпика", "Тестируем приложение", Status.NEW, 1);

        Task epicSecond = new Epic("Название второго эпика", "Тестируем приложение", Status.NEW);
        Task subTaskFirstForSecondEpic = new SubTask("Название первого сабтаска для второго эпика", "Тестируем приложение", Status.NEW, 4);

        manager.createNewTask(epicFirst);
        manager.createNewTask(subTaskFirstForFirstEpic);
        manager.createNewTask(subTaskSecondForFirstEpic);
        manager.createNewTask(epicSecond);
        manager.createNewTask(subTaskFirstForSecondEpic);

        System.out.println(manager.getListOfTasks());

        subTaskFirstForFirstEpic.setStatus(Status.DONE);
        subTaskFirstForSecondEpic.setStatus(Status.DONE);
        manager.updateTask(2, subTaskFirstForFirstEpic);
        manager.updateTask(5, subTaskFirstForSecondEpic);

        System.out.println(manager.getListOfTasks());

        manager.removeTaskById(5);
        System.out.println(manager.getListOfTasks());

        manager.removeTaskById(1);
        System.out.println(manager.getListOfTasks());
    }
}
