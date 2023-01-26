package service;

import http.HttpTaskServer;
import http.KVServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {

    private KVServer server;
    private TaskManager manager = createTaskManager();

    HttpResponse.BodyHandler<String> handler;

    @Override
    HttpTaskManager createTaskManager() {
        try {
            return Managers.getDefault(URI.create("http://localhost:" + KVServer.PORT));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @BeforeEach
    public void beforeEach() {
        try {
            server = new KVServer();
            server.start();
        } catch (IOException e) {
            System.out.println("Ошибка при создании менеджера");
        }
    }

    @AfterEach
    public void stopServer() {
        server.stop();
    }

    @Test
    @Order(1)
    void save() {
    }

    @Test
    @Order(2)
    void load() {
    }
}