package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import http.HttpTaskServer;
import http.KVServer;
import http.utils.LocalDateTimeAdapter;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {

    /*
        Ваня, привет! Приличными словами описать что я испытал во время работы над заданием не получится.
        Человеку который принял решение поместить жесткий дедлайн аккурат после этого спринта чувства юмора не занимать.
        Не представляю как ты будешь это ревьюить, так как я сам не уверен, что всё и везде понимаю.
        Работа заняла >20 часов чистого рабочего времени, включая бессонную ночь, так что возможны глупые ошибки и
        помарки. Отправляю так как уже время жмёт. Кое-что забрал с гугла, что-то сделал после обсуждения на Q&A,
        что-то подглядел в общих каналах. Абсолютно точно где-то можно написать лучше/красивее/оптимальнее, но мне так
        тяжело далась эта работа, что по большей части я делал что бы оно хоть как то работало. Если захочешь разнести
        в пух и прах работу я пойму. :D
     */

    private static KVServer server;
    private static HttpTaskServer taskServer;
    static HttpResponse.BodyHandler<String> handler;
    HttpClient client = HttpClient.newHttpClient();

    Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    @Override
    HttpTaskManager createTaskManager() {
        try {
            return Managers.getDefault(URI.create("http://localhost:" + KVServer.PORT));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    public static void beforeAll() {
        try {
            server = new KVServer();
            server.start();
            taskServer = new HttpTaskServer();
            taskServer.startServer();
            handler = HttpResponse.BodyHandlers.ofString();
        } catch (IOException e) {
            System.out.println("Ошибка при создании менеджера");
        }
    }

    @AfterAll
    public static void stopServer() throws IOException {
        server.stop();
        taskServer.stopServer();
        restoreEnv();
    }

    @Test
    @Order(1)
    void saveNewTaskTest() {
        Task task = new Task("Summary",
                "Description",
                Status.NEW,
                10,
                LocalDateTime.parse("2023-02-24T00:00"));
        String body = GSON.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(getUrlTaskWithoutParameter())
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        try {
            HttpResponse<String> response = client.send(request, handler);
            assertEquals("Task was successfully created with id ",
                    response.body().substring(0, response
                            .body()
                            .indexOf(response.body().split(" ")[6])));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(2)
    void loadOldTaskTest() {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(getUrlTaskWithParameter(3))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        try {
            HttpResponse<String> response = client.send(request, handler);
            Task task = GSON.fromJson(response.body(), Task.class);

            assertEquals(LocalDateTime.parse("2023-01-19T00:00"), task.getStartTime());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(3)
    void saveAndLoadNewTaskTest() {
        Task task = new Task("Summary",
                "Description",
                Status.NEW,
                10,
                LocalDateTime.parse("2023-02-25T00:00"));
        String body = GSON.toJson(task);

        HttpRequest requestPost = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(getUrlTaskWithoutParameter())
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        try {
            HttpResponse<String> responsePost = client.send(requestPost, handler);
            assertEquals("Task was successfully created with id ",
                    responsePost.body().substring(0, responsePost
                            .body()
                            .indexOf(responsePost.body().split(" ")[6])));
            int taskId = Integer.parseInt(responsePost.body().split(" ")[6]);

            HttpRequest requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(getUrlTaskWithParameter(taskId))
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            HttpResponse<String> responseGet = client.send(requestGet, handler);

            Task taskFromResponse = GSON.fromJson(responseGet.body(), Task.class);

            assertEquals(LocalDateTime.parse("2023-02-25T00:00"), taskFromResponse.getStartTime());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(4)
    void saveLoadAndDeleteNewTaskTest() {
        Task task = new Task("Summary",
                "Description",
                Status.NEW,
                10,
                LocalDateTime.parse("2023-02-26T00:00"));
        String body = GSON.toJson(task);

        HttpRequest requestPost = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(getUrlTaskWithoutParameter())
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        try {
            HttpResponse<String> responsePost = client.send(requestPost, handler);
            assertEquals("Task was successfully created with id ",
                    responsePost.body().substring(0, responsePost
                            .body()
                            .indexOf(responsePost.body().split(" ")[6])));
            int taskId = Integer.parseInt(responsePost.body().split(" ")[6]);

            HttpRequest requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(getUrlTaskWithParameter(taskId))
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            HttpResponse<String> responseGet = client.send(requestGet, handler);

            Task taskFromResponse = GSON.fromJson(responseGet.body(), Task.class);

            assertEquals(LocalDateTime.parse("2023-02-26T00:00"), taskFromResponse.getStartTime());

            HttpRequest requestDelete = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(getUrlTaskWithParameter(taskId))
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            HttpResponse<String> responseDelete = client.send(requestDelete, handler);

            assertEquals("Task with id " + taskId + " removed successfully", responseDelete.body());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(5)
    void loadOldEpic() {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(getUrlEpicWithParameter(1))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        try {
            HttpResponse<String> response = client.send(request, handler);
            Epic epic = GSON.fromJson(response.body(), Epic.class);

            assertEquals("Description epic2 from file", epic.getDescription());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(6)
    void saveAndLoadNewEpicTest() {
        Epic epic = new Epic("Summary",
                "Unique Description",
                Status.NEW);
        String body = GSON.toJson(epic);

        HttpRequest requestPost = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(getUrlEpicWithoutParameter())
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        try {
            HttpResponse<String> responsePost = client.send(requestPost, handler);
            assertEquals("Epic was successfully created with id ",
                    responsePost.body().substring(0, responsePost
                            .body()
                            .indexOf(responsePost.body().split(" ")[6])));
            int epicId = Integer.parseInt(responsePost.body().split(" ")[6]);

            HttpRequest requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(getUrlEpicWithParameter(epicId))
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            HttpResponse<String> responseGet = client.send(requestGet, handler);

            Epic epicFromResponse = GSON.fromJson(responseGet.body(), Epic.class);

            assertEquals("Unique Description", epicFromResponse.getDescription());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(7)
    void saveLoadAndDeleteNewEpicTest() {
        Epic epic = new Epic("Summary",
                "Unique Description",
                Status.NEW);
        String body = GSON.toJson(epic);

        HttpRequest requestPost = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(getUrlEpicWithoutParameter())
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        try {
            HttpResponse<String> responsePost = client.send(requestPost, handler);
            assertEquals("Epic was successfully created with id ",
                    responsePost.body().substring(0, responsePost
                            .body()
                            .indexOf(responsePost.body().split(" ")[6])));
            int epicId = Integer.parseInt(responsePost.body().split(" ")[6]);

            HttpRequest requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(getUrlEpicWithParameter(epicId))
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            HttpResponse<String> responseGet = client.send(requestGet, handler);

            Epic epicFromResponse = GSON.fromJson(responseGet.body(), Epic.class);

            assertEquals("Unique Description", epicFromResponse.getDescription());

            HttpRequest requestDelete = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(getUrlEpicWithParameter(epicId))
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            HttpResponse<String> responseDelete = client.send(requestDelete, handler);

            assertEquals("Epic with id " + epicId + " and it's subtasks [] removed successfully",
                    responseDelete.body());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(8)
    void loadOldSubTasksTest() {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(getUrlSubTasksWithParameter(4))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        try {
            HttpResponse<String> response = client.send(request, handler);
            SubTask subTask = GSON.fromJson(response.body(), SubTask.class);

            assertEquals("Description sub task3", subTask.getDescription());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(9)
    void saveAndLoadSubTaskTest() {
        SubTask subTask = new SubTask("Summary",
                "Unique SubTask Description",
                Status.NEW,
                1,
                10,
                LocalDateTime.parse("2023-03-01T00:30"));
        String body = GSON.toJson(subTask);

        HttpRequest requestPost = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(getUrlSubTasksWithoutParameter())
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        try {
            HttpResponse<String> responsePost = client.send(requestPost, handler);
            assertEquals("Subtask was successfully created with id ",
                    responsePost.body().substring(0, responsePost
                            .body()
                            .indexOf(responsePost.body().split(" ")[6])));
            int subTaskId = Integer.parseInt(responsePost.body().split(" ")[6]);

            HttpRequest requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(getUrlSubTasksWithParameter(subTaskId))
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            HttpResponse<String> responseGet = client.send(requestGet, handler);

            Epic subTaskFromResponse = GSON.fromJson(responseGet.body(), Epic.class);

            assertEquals("Unique SubTask Description", subTaskFromResponse.getDescription());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(10)
    void saveLoadAndDeleteNewSubTaskTest() {
        SubTask subTask = new SubTask("Summary",
                "Unique SubTask Description",
                Status.NEW,
                1,
                10,
                LocalDateTime.parse("2023-03-01T04:30"));
        String body = GSON.toJson(subTask);

        HttpRequest requestPost = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(getUrlSubTasksWithoutParameter())
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        try {
            HttpResponse<String> responsePost = client.send(requestPost, handler);
            assertEquals("Subtask was successfully created with id ",
                    responsePost.body().substring(0, responsePost
                            .body()
                            .indexOf(responsePost.body().split(" ")[6])));
            int subTaskId = Integer.parseInt(responsePost.body().split(" ")[6]);

            HttpRequest requestGet = HttpRequest.newBuilder()
                    .GET()
                    .uri(getUrlSubTasksWithParameter(subTaskId))
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            HttpResponse<String> responseGet = client.send(requestGet, handler);

            SubTask subTaskFromResponse = GSON.fromJson(responseGet.body(), SubTask.class);

            assertEquals("Unique SubTask Description", subTaskFromResponse.getDescription());

            HttpRequest requestDelete = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(getUrlSubTasksWithParameter(subTaskId))
                    .version(HttpClient.Version.HTTP_1_1)
                    .build();

            HttpResponse<String> responseDelete = client.send(requestDelete, handler);

            assertEquals("Subtask with id " + subTaskId + " removed successfully",
                    responseDelete.body());

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(11)
    void deleteTest() {
        HttpRequest requestTaskDelete = HttpRequest.newBuilder()
                .DELETE()
                .uri(getUrlTaskWithoutParameter())
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        try {
            HttpResponse<String> responseDelete = client.send(requestTaskDelete, handler);
            assertEquals("Catalog of tasks removed successfully", responseDelete.body());
            restoreEnv();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        HttpRequest requestSubTaskDelete = HttpRequest.newBuilder()
                .DELETE()
                .uri(getUrlSubTasksWithoutParameter())
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        try {
            HttpResponse<String> responseDelete = client.send(requestSubTaskDelete, handler);
            assertEquals("Catalog of subtasks removed successfully", responseDelete.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        HttpRequest requestEpicDelete = HttpRequest.newBuilder()
                .DELETE()
                .uri(getUrlEpicWithoutParameter())
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        try {
            HttpResponse<String> responseDelete = client.send(requestEpicDelete, handler);
            assertEquals("Catalog of epics removed successfully", responseDelete.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private URI getUrlTaskWithParameter(int id) {
        return URI.create("http://localhost:8080/tasks/tasks/?id=" + id);
    }

    private URI getUrlTaskWithoutParameter() {
        return URI.create("http://localhost:8080/tasks/tasks/");
    }

    private URI getUrlEpicWithParameter(int id) {
        return URI.create("http://localhost:8080/tasks/epics/?id=" + id);
    }

    private URI getUrlEpicWithoutParameter() {
        return URI.create("http://localhost:8080/tasks/epics/");
    }

    private URI getUrlSubTasksWithParameter(int id) {
        return URI.create("http://localhost:8080/tasks/subtasks/?id=" + id);
    }

    private URI getUrlSubTasksWithoutParameter() {
        return URI.create("http://localhost:8080/tasks/subtasks/");
    }

    private static void restoreEnv() throws IOException {
        File dbForTests = new File("resources" + File.separator + "dbForTests.csv");
        Files.delete(dbForTests.toPath());
        File source = new File("resources" + File.separator + "db.csv");
        File destination = new File("resources" + File.separator + "dbForTests.csv");

        copyFile(source, destination);
    }

    // https://stackoverflow.com/a/5388232
    private static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new RandomAccessFile(sourceFile, "rw").getChannel();
            destination = new RandomAccessFile(destFile, "rw").getChannel();

            long position = 0;
            long count = source.size();

            source.transferTo(position, count, destination);
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
}