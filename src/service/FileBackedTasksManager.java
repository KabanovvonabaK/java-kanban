package service;

import model.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.*;

import static model.Types.*;

public class FileBackedTasksManager extends InMemoryTaskManager {

    private File file;

    public FileBackedTasksManager(File file) {
        super();
        this.file = file;
    }

    @Override
    public void createNewTask(Task task) {
        super.createNewTask(task);
        save();
    }

    @Override
    public void createNewEpic(Epic epic) {
        super.createNewEpic(epic);
        save();
    }

    @Override
    public void createNewSubTask(SubTask subTask) {
        super.createNewSubTask(subTask);
        save();
    }

    @Override
    public void dropListsOfTasksEpicsAndSubTasks() {
        super.dropListsOfTasksEpicsAndSubTasks();
        save();
    }

    @Override
    public void dropListOfTasks() {
        super.dropListOfTasks();
        save();
    }

    @Override
    public void dropListOfEpicsAndSubTasks() {
        super.dropListOfEpicsAndSubTasks();
        save();
    }

    @Override
    public void dropListOfSubTasks() {
        super.dropListOfSubTasks();
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        super.updateSubTask(subTask);
        save();
    }

    @Override
    public void removeTaskById(int id) {
        super.removeTaskById(id);
        save();
    }

    @Override
    public void removeEpicById(int id) {
        super.removeEpicById(id);
        save();
    }

    @Override
    public void removeSubTaskById(int id) {
        super.removeSubTaskById(id);
        save();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = super.getEpicById(id);
        save();
        return epic;
    }

    @Override
    public SubTask getSubTaskById(int id) {
        SubTask subTask = super.getSubTaskById(id);
        save();
        return subTask;
    }

    private void save() {
        File tmpFile = new File("resources" + File.separator + "tmp.csv");
        try {
            Files.delete(tmpFile.toPath());
        } catch (NoSuchFileException e) {
            System.err.println("No such file to delete(), creating new tmp.csv");
        } catch (IOException e) {
            System.err.println(e);
        } finally {
            Map<Integer, Task> allTasks = new TreeMap<>();
            allTasks.putAll(getCatalogOfSubTasks());
            allTasks.putAll(getCatalogOfEpics());
            allTasks.putAll(getCatalogOfTasks());

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile, true))) {
                for (Map.Entry<Integer, Task> task : allTasks.entrySet()) {
                    bw.write(toString(task.getValue()) + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile, true))) {
                /*
                    так как в задании явно указано, что historyToString() должен принимать объект HistoryManager
                    мне придется создать объект этого класса, хотя на мой взгляд можно было просто работать
                    с листом который возвращает getHistory()
                 */
                HistoryManager historyManager = new InMemoryHistoryManager();
                for (Task t : getHistory()) {
                    historyManager.add(t);
                }
                bw.write("\n" + historyToString(historyManager));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String toString(Task task) {
        String toReturn = "";
        Types type = Types.valueOf(task
                .getClass()
                .getName()
                .toUpperCase()
                .replace("MODEL.", ""));
        switch (type) {
            // запятые внутри которых нет значения нужны для формирования одной общей модели хранения данных
            case TASK:
                toReturn = String.format("%s,%s,%s,%s,%s,null",
                        task.getId(),
                        TASK,
                        task.getSummary(),
                        task.getStatus(),
                        task.getDescription());
                break;
            case EPIC:
                // приведение типов необходимо для доступа к уникальному методу getSubTasksIds()
                Epic epic = (Epic) task;
                toReturn = String.format("%s,%s,%s,%s,%s,null",
                        epic.getId(),
                        EPIC,
                        epic.getSummary(),
                        epic.getStatus(),
                        epic.getDescription());
                break;
            case SUBTASK:
                // приведение типов необходимо для доступа к уникальному методу getEpicId()
                SubTask subTask = (SubTask) task;
                toReturn = String.format("%s,%s,%s,%s,%s,%s",
                        subTask.getId(),
                        SUBTASK,
                        subTask.getSummary(),
                        subTask.getStatus(),
                        subTask.getDescription(),
                        subTask.getEpicId());
                break;
        }
        return toReturn;
    }

    public Task fromString(String value) {
        Task task;
        String[] s = value.split(",");
         /*
            Содержимое массива после использования .split():
                0 id
                1 type
                2 summary
                3 status
                4 description
                5 epicId - can be null
         */
        String testString = s[3];
        Status status = Status.valueOf(testString);
        switch (valueOf(s[1])) {
            case TASK:
                task = new Task(s[2], s[4], status);
                task.setId(Integer.parseInt(s[0]));
                break;
            case EPIC:
                Epic epic = new Epic(s[2], s[4], status);
                epic.setId(Integer.parseInt(s[0]));
                task = epic;
                break;
            case SUBTASK:
                /*
                по идее следует проверять что в s[5] действительно лежит epicId, а не пустая строка,
                но так как subtask не может существовать без epicId я позволю себе в данной реализации
                это не проверять
                 */
                task = new SubTask(s[2], s[4], status, Integer.parseInt(s[5]));
                task.setId(Integer.parseInt(s[0]));
                break;
            default:
                throw new IllegalStateException("An error occurred during fromString(String value)");
        }
        return task;
    }

    static String historyToString(HistoryManager manager) {
        String[] arrayStringId = new String[manager.getHistory().size()];
        List<Task> taskList = manager.getHistory();
        int counter = 0;

        for (Task t : taskList) {
            arrayStringId[counter] = String.valueOf(t.getId());
            counter++;
        }

        return String.join(",", arrayStringId);
    }

    static List<Integer> historyFromString(String value) {
        String[] arrayStringId = value.split(",");
        List<Integer> listOfTasksIds = new ArrayList<>();

        for (String str : arrayStringId) {
            listOfTasksIds.add(Integer.parseInt(str));
        }
        return listOfTasksIds;
    }

    static FileBackedTasksManager loadFromFile(File file) throws IOException {
        FileBackedTasksManager manager = new FileBackedTasksManager(file);
        if (file.exists() && !file.isDirectory()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                // section of tasks
                while (!(line = br.readLine()).equals("")) {
                    Task task = manager.fromString(line);
                    // order SubTask, Epic, Task important because of Epic and SubTask extends Task
                    if (task != null) {
                        if (task instanceof SubTask) {
                            manager.createNewSubTask((SubTask) task);
                        } else if (task instanceof Epic) {
                            manager.createNewEpic((Epic) task);
                        } else {
                            manager.createNewTask(task);
                        }
                    }
                }
                // section of history
                List<Integer> idsForHistory = historyFromString(br.readLine());
                for (Integer id : idsForHistory) {
                    if (manager.getCatalogOfEpics().containsKey(id)) {
                        manager.getEpicById(id);
                    } else if (manager.getCatalogOfSubTasks().containsKey(id)) {
                        manager.getSubTaskById(id);
                    } else {
                        manager.getTaskById(id);
                    }
                }
            }
        }
        return manager;
    }

    /*
        Не догадался как правильно сделать так что бы одновременно читать с файла и писать, потоки мы еще не проходили
        (если они вообще тут помогут), поэтому реализовал через два файла.
        Как накладные расходы - этот метод надо принудительно вызывать, похоже на закрытие сессии.
     */
    private void replaceOriginalFileWithTmp() {
        File tmpFile = new File("resources" + File.separator + "tmp.csv");
        try {
            File originalFile = file;
            Files.delete(file.toPath());
            tmpFile.renameTo(originalFile);
        } catch (NoSuchFileException e) {
            System.err.println("No such file to delete(), creating new tmp.csv");
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public static void main(String[] args) throws IOException {
        // For using assert please don't forget to add -enableassertions or -ea as VM option
        FileBackedTasksManager manager = new FileBackedTasksManager(
                new File("resources" + File.separator + "dbForTests.csv"));
        // тестируем fromString()
        Task task = manager.fromString("2,EPIC,Epic2,DONE,Description epic2,null");
        Epic epic = new Epic("Epic2", "Description epic2", Status.DONE);
        epic.setId(2);
        ArrayList<Integer> subTasksIds = new ArrayList<>();
        epic.setSubTasksIds(subTasksIds);
        assert Objects.equals(task.hashCode(), epic.hashCode())
                : "fromString() can't convert String to Task properly";

        // тестируем toString()
        Epic newEpic = new Epic("First epic", "Application testing",
                Status.NEW);
        newEpic.setId(1);
        newEpic.addSubTaskId(2);
        newEpic.addSubTaskId(3);

        assert Objects.equals("1,EPIC,First epic,NEW,Application testing,null",
                manager.toString(newEpic)) :
                "toString() can't convert Task to String properly";

        // тестируем loadFromFile()
        FileBackedTasksManager managerLoadFromFile = loadFromFile(
                new File("resources" + File.separator + "dbForTests.csv"));

        Task taskAnotherOne = new Task("Task created manually1", "desc", Status.NEW);
        managerLoadFromFile.createNewTask(taskAnotherOne);
        managerLoadFromFile.getTaskById(taskAnotherOne.getId());

        assert Objects.equals(managerLoadFromFile.getHistory().size(), 6)
                : "history got wrong size";
        assert Objects.equals(managerLoadFromFile.getCatalogOfTasks().size(), 2)
                : "list of Tasks got wrong size";
        assert Objects.equals(managerLoadFromFile.getCatalogOfEpics().size(), 2)
                : "list of Epics got wrong size";
        assert Objects.equals(managerLoadFromFile.getCatalogOfSubTasks().size(), 2)
                : "list of SubTasks for wrong size";

        Task taskToCompare = new Task("Task1", "Description task1", Status.NEW);
        taskToCompare.setId(3);
        Epic epicToCompare1 = new Epic("Epic2", "Description epic2 from file", Status.DONE);
        epicToCompare1.setId(1);
        Epic epicToCompare2 = new Epic("Epic3", "Description epic3 from file", Status.DONE);
        epicToCompare2.setId(2);
        ArrayList<Integer> subTasksIdsFroEpicToCompare2 = new ArrayList<>();
        subTasksIdsFroEpicToCompare2.add(4);
        subTasksIdsFroEpicToCompare2.add(5);
        epicToCompare2.setSubTasksIds(subTasksIdsFroEpicToCompare2);
        SubTask subTaskToCompare = new SubTask("Sub Task2", "Description sub task3", Status.DONE, 2);
        subTaskToCompare.setId(4);

        Map<Integer, Epic> listOfEpics = managerLoadFromFile.getCatalogOfEpics();
        Map<Integer, Task> listOfTasks = managerLoadFromFile.getCatalogOfTasks();
        Map<Integer, SubTask> listOfSubTasks = managerLoadFromFile.getCatalogOfSubTasks();

        assert Objects.equals(listOfEpics.get(1), epicToCompare1)
                : "error during loading an epic";
        assert Objects.equals(listOfEpics.get(2), epicToCompare2)
                : "error during loading an epic";
        assert Objects.equals(listOfTasks.get(3), taskToCompare)
                : "error during loading a task";
        assert Objects.equals(listOfSubTasks.get(4), subTaskToCompare)
                : "error during loading an subTask";
        assert Objects.equals(listOfTasks.get(6), taskAnotherOne)
                : "error during loading a task";

        // тестируем historyToString()
        HistoryManager historyManager = new InMemoryHistoryManager();
        historyManager.add(taskToCompare);
        historyManager.add(epicToCompare1);
        historyManager.add(epicToCompare2);
        historyManager.add(epicToCompare2);
        historyManager.add(epicToCompare1);
        historyManager.add(taskToCompare);
        historyManager.add(subTaskToCompare);

        assert Objects.equals(historyToString(historyManager), "2,1,3,4")
                : "historyToString is broken";

        // тестируем replaceOriginalFileWithTmp() - tmp.csv уже должен существовать
        File fileToReplace = new File("resources" + File.separator + "dbToReplace.csv");
        try {
            fileToReplace.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileBackedTasksManager managerReplace = new FileBackedTasksManager(fileToReplace);
        // для отслеживания того что пишется в tmp.csv в момент работы приложения необходимо замьютить строку ниже
        managerReplace.replaceOriginalFileWithTmp();
    }
}
