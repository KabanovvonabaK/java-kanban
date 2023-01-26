package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import http.KVTaskClient;
import http.utils.LocalDateTimeAdapter;
import model.Epic;
import model.SubTask;
import model.Task;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class HttpTaskManager extends FileBackedTasksManager {
    public static Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .serializeNulls().create();
    private final URI uri;

    private final static String FILE_PATH = "resources" + File.separator + "dbForTests.csv";

    private final KVTaskClient kvTaskClient;

    public HttpTaskManager(URI uri) throws IOException, InterruptedException {
        super(new File(FILE_PATH));
        this.uri = uri;
        kvTaskClient = new KVTaskClient(uri);
    }

    @Override
    protected void save() {
        try {
            kvTaskClient.put("Tasks", gson.toJson(getCatalogOfTasks()));
            kvTaskClient.put("Epics", gson.toJson(getCatalogOfEpics()));
            kvTaskClient.put("Subtasks", gson.toJson(getCatalogOfSubTasks()));
            kvTaskClient.put("History", gson.toJson(getHistory()));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try {
            String tasks = kvTaskClient.load("Tasks");
            String epics = kvTaskClient.load("Epics");
            String subTasks = kvTaskClient.load("Subtasks");
            String history = kvTaskClient.load("History");

            Map<Integer, Task> taskMap = gson.fromJson(tasks,
                    new TypeToken<Map<Integer, Task>>() {}.getType());
            Map<Integer, Epic> epicsMap = gson.fromJson(epics,
                    new TypeToken<Map<Integer, Epic>>() {}.getType());
            Map<Integer, SubTask> subTasksMap = gson.fromJson(subTasks,
                    new TypeToken<Map<Integer, SubTask>>() {}.getType());
            List<Task> historyList = gson.fromJson(tasks,
                    new TypeToken<List<Task>>() {}.getType());

            for (Task t : taskMap.values()) {
                createNewTask(t);
            }

            for (Epic e : epicsMap.values()) {
                createNewTask(e);
            }

            for (SubTask s : subTasksMap.values()) {
                createNewTask(s);
            }

            for (Task t : historyList) {
                if (getCatalogOfTasks().containsKey(t.getId())) {
                    getTaskById(t.getId());
                } else if (getCatalogOfEpics().containsKey(t.getId())) {
                    getEpicById(t.getId());
                } else {
                    getSubTaskById(t.getId());
                }
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
