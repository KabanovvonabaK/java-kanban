package http;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {

    private final String apiToken;

    private final URI uri;

    private final HttpClient httpClient;

    private final HttpResponse.BodyHandler<String> handler;

    public KVTaskClient(URI uri) throws IOException, InterruptedException {
        this.uri = uri;

        httpClient = HttpClient.newHttpClient();
        handler = HttpResponse.BodyHandlers.ofString();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(this.uri + "/register"))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, handler);
        apiToken = response.body();
    }

    public void put(String key, String json) throws IOException, InterruptedException {
        // POST /save/<ключ>?API_TOKEN=
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(URI.create(uri + "/save/" + key + "?API_TOKEN=" + apiToken))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        httpClient.send(httpRequest, handler);
    }

    public String load(String key) throws IOException, InterruptedException {
        // GET /load/<ключ>?API_TOKEN=
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(uri + "/load/" + key + "?API_TOKEN=" + apiToken))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        return httpClient.send(httpRequest, handler).body();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new KVServer().start();
        KVTaskClient kvTaskClient = new KVTaskClient(URI.create("http://localhost:8079"));
        kvTaskClient.put("1", "{json1}");
        kvTaskClient.load("1");
    }
}