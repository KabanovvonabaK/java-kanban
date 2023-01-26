package http;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import http.utils.LocalDateTimeAdapter;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
import service.Managers;
import service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static java.net.HttpURLConnection.*;

public class HttpTaskServer {
    private static final int PORT = 8080;

    private HttpServer httpServer;

    private static final TaskManager TASK_MANAGER = Managers.getFileBackedTaskManager();

    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";

    private void createServerAndContext() throws IOException {
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(PORT), 0);
        httpServer.createContext("/tasks", new AllTaskHandler());
        httpServer.createContext("/tasks/tasks", new TaskHandler());
        httpServer.createContext("/tasks/epics", new EpicHandler());
        httpServer.createContext("/tasks/subtasks", new SubTaskHandler());
        httpServer.createContext("/tasks/history", new TaskHistoryHandler());
    }

    public void startServer() throws IOException {
        createServerAndContext();
        httpServer.start();
    }

    public void stopServer() {
        httpServer.stop(1);
    }

    private static void sendResponse(HttpExchange exchange, String response, int code) throws IOException {
        exchange.sendResponseHeaders(code, 0);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response.getBytes());
            exchange.close();
        }
    }

    private static void sendIncorrectEndpointResponse(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "Incorrect endpoint", HTTP_BAD_REQUEST);
        exchange.close();
    }

    private static void sendBadMethodResponse(HttpExchange exchange, String... method) throws IOException {
        sendResponse(exchange, "Only " + Arrays.toString(method) + " supported", HTTP_BAD_METHOD);
        exchange.close();
    }

    private static class AllTaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath().replace("/", "");
            String method = exchange.getRequestMethod();

            try {
                switch (method) {
                    case GET:
                        if (path.equals("tasks")) {
                            List<Task> allTasks = new ArrayList<>();
                            allTasks.addAll(TASK_MANAGER.getCatalogOfTasks().values());
                            allTasks.addAll(TASK_MANAGER.getCatalogOfEpics().values());
                            allTasks.addAll(TASK_MANAGER.getCatalogOfSubTasks().values());

                            String response = GSON.toJson(allTasks);

                            sendResponse(exchange, response, HTTP_OK);
                            exchange.close();
                        } else {
                            sendIncorrectEndpointResponse(exchange);
                        }
                    default:
                        sendBadMethodResponse(exchange, GET);
                }
            } catch (Throwable e) {
                sendResponse(exchange, "Something went wrong. Please try again later.", HTTP_INTERNAL_ERROR);
            }
        }
    }

    private static class TaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().toString();
            String method = exchange.getRequestMethod();

            switch (method) {
                case GET:
                    if (path.equals("/tasks/tasks/") || path.equals("/tasks/tasks")) {
                        sendResponse(exchange, GSON.toJson(TASK_MANAGER.getCatalogOfTasks().values()), HTTP_OK);
                    } else if (path.contains("/tasks/tasks/?id=")) {
                        try {
                            int id = Integer.parseInt(path.split("=")[1]);
                            if (TASK_MANAGER.getCatalogOfTasks().containsKey(id)) {
                                sendResponse(exchange,
                                        GSON.toJson(TASK_MANAGER.getCatalogOfTasks().get(id)),
                                        HTTP_OK);
                            } else {
                                sendResponse(exchange,
                                        "Task with id " + id + " don't exist.",
                                        HTTP_NOT_FOUND);
                            }
                        } catch (NumberFormatException e) {
                            sendResponse(exchange, "Id of task not an integer", HTTP_BAD_REQUEST);
                        }
                    } else {
                        sendIncorrectEndpointResponse(exchange);
                    }
                    break;
                case POST:
                    if (path.equals("/tasks/tasks") || path.equals("/tasks/tasks/")) {
                        InputStream inputStream = exchange.getRequestBody();
                        String jsonToString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                        JsonElement jsonElement = JsonParser.parseString(jsonToString);

                        try {
                            JsonObject jsonObject = jsonElement.getAsJsonObject();
                            try {
                                Task task = GSON.fromJson(jsonObject, Task.class);
                                if (!TASK_MANAGER.getCatalogOfTasks().containsKey(task.getId())) {
                                    TASK_MANAGER.createNewTask(task);
                                    sendResponse(exchange,
                                            "Task was successfully created with id " + task.getId(),
                                            HTTP_CREATED);
                                } else {
                                    TASK_MANAGER.updateTask(task);
                                    sendResponse(exchange,
                                            "Task with id " + task.getId() + " was updated",
                                            HTTP_OK);
                                }
                            } catch (RuntimeException t) {
                                sendResponse(exchange,
                                        "Oops, something went wrong\n" + t.getMessage(),
                                        HTTP_INTERNAL_ERROR);
                            }
                        } catch (IllegalStateException | JsonSyntaxException e) {
                            sendResponse(exchange, "Incorrect body of request", HTTP_BAD_REQUEST);
                        }
                    } else {
                        sendIncorrectEndpointResponse(exchange);
                    }
                    break;
                case DELETE:
                    if (path.equals("/tasks/tasks") || path.equals("/tasks/tasks/")) {
                        TASK_MANAGER.dropListOfTasks();
                        sendResponse(exchange, "Catalog of tasks removed successfully", HTTP_OK);
                    } else if (path.contains("/tasks/tasks/?id=")) {
                        try {
                            int id = Integer.parseInt(path.split("=")[1]);
                            if (TASK_MANAGER.getCatalogOfTasks().containsKey(id)) {
                                TASK_MANAGER.removeTaskById(id);
                                sendResponse(exchange,
                                        "Task with id " + id + " removed successfully",
                                        HTTP_OK);
                            } else {
                                sendResponse(exchange,
                                        "Task with id " + id + " don't exist.",
                                        HTTP_NOT_FOUND);
                            }
                        } catch (NumberFormatException e) {
                            sendResponse(exchange, "Id of task not an integer", HTTP_BAD_REQUEST);
                        }
                    } else {
                        sendIncorrectEndpointResponse(exchange);
                    }
                    break;
                default:
                    sendBadMethodResponse(exchange, GET, POST, DELETE);
                    break;
            }
        }
    }

    private static class EpicHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().toString();
            String method = exchange.getRequestMethod();

            switch (method) {
                case GET:
                    if (path.equals("/tasks/epics/") || path.equals("/tasks/epics")) {
                        sendResponse(exchange, GSON.toJson(TASK_MANAGER.getCatalogOfEpics().values()), HTTP_OK);
                    } else if (path.contains("/tasks/epics/?id=")) {
                        try {
                            int id = Integer.parseInt(path.split("=")[1]);
                            if (TASK_MANAGER.getCatalogOfEpics().containsKey(id)) {
                                sendResponse(exchange,
                                        GSON.toJson(TASK_MANAGER.getCatalogOfEpics().get(id)),
                                        HTTP_OK);
                            } else {
                                sendResponse(exchange,
                                        "Epic with id " + id + " don't exist.",
                                        HTTP_NOT_FOUND);
                            }
                        } catch (NumberFormatException e) {
                            sendResponse(exchange, "Id of epic not an integer", HTTP_BAD_REQUEST);
                        }
                    } else {
                        sendIncorrectEndpointResponse(exchange);
                    }
                    break;
                case POST:
                    if (path.equals("/tasks/epics") || path.equals("/tasks/epics/")) {
                        InputStream inputStream = exchange.getRequestBody();
                        String jsonToString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                        JsonElement jsonElement = JsonParser.parseString(jsonToString);

                        try {
                            JsonObject jsonObject = jsonElement.getAsJsonObject();
                            try {
                                Epic epic = GSON.fromJson(jsonObject, Epic.class);
                                if (!TASK_MANAGER.getCatalogOfEpics().containsKey(epic.getId())) {
                                    TASK_MANAGER.createNewEpic(epic);
                                    sendResponse(exchange,
                                            "Epic was successfully created with id " + epic.getId(),
                                            HTTP_CREATED);
                                } else {
                                    TASK_MANAGER.updateEpic(epic);
                                    sendResponse(exchange,
                                            "Epic with id " + epic.getId() + " was updated",
                                            HTTP_OK);
                                }
                            } catch (RuntimeException t) {
                                sendResponse(exchange,
                                        "Oops, something went wrong\n" + t.getMessage(),
                                        HTTP_INTERNAL_ERROR);
                            }
                        } catch (IllegalStateException | JsonSyntaxException e) {
                            sendResponse(exchange, "Incorrect body of request", HTTP_BAD_REQUEST);
                        }
                    } else {
                        sendIncorrectEndpointResponse(exchange);
                    }
                    break;
                case DELETE:
                    if (path.equals("/tasks/epics") || path.equals("/tasks/epics/")) {
                        TASK_MANAGER.dropListOfEpicsAndSubTasks();
                        sendResponse(exchange, "Catalog of epics removed successfully", HTTP_OK);
                    } else if (path.contains("/tasks/epics/?id=")) {
                        try {
                            int id = Integer.parseInt(path.split("=")[1]);
                            List<Integer> subTasksIds = TASK_MANAGER.getEpicById(id).getSubTasksIds();
                            if (TASK_MANAGER.getCatalogOfEpics().containsKey(id)) {
                                TASK_MANAGER.removeEpicById(id);
                                sendResponse(exchange,
                                        "Epic with id " + id +
                                                "and it's subtasks " + subTasksIds +
                                                " removed successfully",
                                        HTTP_OK);
                            } else {
                                sendResponse(exchange,
                                        "Epic with id " + id + " don't exist.",
                                        HTTP_NOT_FOUND);
                            }
                        } catch (NumberFormatException e) {
                            sendResponse(exchange, "Id of epic not an integer", HTTP_BAD_REQUEST);
                        }
                    } else {
                        sendIncorrectEndpointResponse(exchange);
                    }
                    break;
                default:
                    sendBadMethodResponse(exchange, GET, POST, DELETE);
                    break;
            }
        }
    }

    private static class SubTaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().toString();
            String method = exchange.getRequestMethod();

            switch (method) {
                case GET:
                    if (path.equals("/tasks/subtasks/") || path.equals("/tasks/subtasks")) {
                        sendResponse(exchange, GSON.toJson(TASK_MANAGER.getCatalogOfSubTasks().values()), HTTP_OK);
                    } else if (path.contains("/tasks/subtasks/?id=")) {
                        try {
                            int id = Integer.parseInt(path.split("=")[1]);
                            if (TASK_MANAGER.getCatalogOfSubTasks().containsKey(id)) {
                                sendResponse(exchange,
                                        GSON.toJson(TASK_MANAGER.getCatalogOfSubTasks().get(id)),
                                        HTTP_OK);
                            } else {
                                sendResponse(exchange,
                                        "Subtask with id " + id + " don't exist.",
                                        HTTP_NOT_FOUND);
                            }
                        } catch (NumberFormatException e) {
                            sendResponse(exchange, "Id of subtask not an integer", HTTP_BAD_REQUEST);
                        }
                    } else {
                        sendIncorrectEndpointResponse(exchange);
                    }
                    break;
                case POST:
                    if (path.equals("/tasks/subtasks") || path.equals("/tasks/subtasks/")) {
                        InputStream inputStream = exchange.getRequestBody();
                        String jsonToString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                        JsonElement jsonElement = JsonParser.parseString(jsonToString);

                        try {
                            JsonObject jsonObject = jsonElement.getAsJsonObject();
                            try {
                                SubTask subTask = GSON.fromJson(jsonObject, SubTask.class);
                                if (!TASK_MANAGER.getCatalogOfSubTasks().containsKey(subTask.getId())) {
                                    TASK_MANAGER.createNewSubTask(subTask);
                                    sendResponse(exchange,
                                            "Subtask was successfully created with id " + subTask.getId(),
                                            HTTP_CREATED);
                                } else {
                                    TASK_MANAGER.updateSubTask(subTask);
                                    sendResponse(exchange,
                                            "Subtask with id " + subTask.getId() + " was updated",
                                            HTTP_OK);
                                }
                            } catch (RuntimeException t) {
                                sendResponse(exchange,
                                        "Oops, something went wrong\n" + t.getMessage(),
                                        HTTP_INTERNAL_ERROR);
                            }
                        } catch (IllegalStateException | JsonSyntaxException e) {
                            sendResponse(exchange, "Incorrect body of request", HTTP_BAD_REQUEST);
                        }
                    } else {
                        sendIncorrectEndpointResponse(exchange);
                    }
                    break;
                case DELETE:
                    if (path.equals("/tasks/subtasks") || path.equals("/tasks/subtasks/")) {
                        TASK_MANAGER.dropListOfSubTasks();
                        sendResponse(exchange, "Catalog of subtasks removed successfully", HTTP_OK);
                    } else if (path.contains("/tasks/subtasks/?id=")) {
                        try {
                            int id = Integer.parseInt(path.split("=")[1]);
                            if (TASK_MANAGER.getCatalogOfSubTasks().containsKey(id)) {
                                TASK_MANAGER.removeSubTaskById(id);
                                sendResponse(exchange,
                                        "Subtask with id " + id + " removed successfully",
                                        HTTP_OK);
                            } else {
                                sendResponse(exchange,
                                        "Subtask with id " + id + " don't exist.",
                                        HTTP_NOT_FOUND);
                            }
                        } catch (NumberFormatException e) {
                            sendResponse(exchange, "Id of subtask not an integer", HTTP_BAD_REQUEST);
                        }
                    } else {
                        sendIncorrectEndpointResponse(exchange);
                    }
                    break;
                default:
                    sendBadMethodResponse(exchange, GET, POST, DELETE);
                    break;
            }
        }
    }

    private static class TaskHistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            try {
                switch (method) {
                    case GET:
                        if (path.equals("/tasks/history") || path.equals("/tasks/history/")) {
                            sendResponse(exchange, GSON.toJson(TASK_MANAGER.getHistory()), HTTP_OK);
                        } else {
                            sendIncorrectEndpointResponse(exchange);
                        }
                    default:
                        sendBadMethodResponse(exchange, GET);
                }
            } catch (Throwable e) {
                sendResponse(exchange, "Something went wrong. Please try again later.", HTTP_INTERNAL_ERROR);
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        HttpTaskServer httpTaskServer = new HttpTaskServer();

        httpTaskServer.startServer();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

        Task task = new Task("Task from main",
                "Description",
                Status.NEW,
                10,
                LocalDateTime.parse("2023-02-19T00:00"));

        URI uri = URI.create("http://localhost:8080/tasks" + "/tasks");
        String body = GSON.toJson(task);
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(uri).version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json").build();
        client.send(request, handler);
    }
}
