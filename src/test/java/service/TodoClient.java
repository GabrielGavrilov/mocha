package service;

import com.fasterxml.jackson.core.util.JacksonFeature;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import service.dto.TodoDto;

import java.util.List;

public class TodoClient {

    private static final String DEFAULT_BASE_URI = "http://localhost";

    private final Client client;
    private String baseUri = DEFAULT_BASE_URI;

    public TodoClient() {
        this.client = ClientBuilder.newBuilder()
                .register(JacksonFeature.class)
                .build();
    }

    public TodoClient(Client client) {
        this.client = client;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

    public List<TodoDto> getAllTodos() {
        return todoTarget()
                .request()
                .get(new GenericType<List<TodoDto>>() {});
    }

    public TodoDto getTodoById(String id) {
        return todoTarget()
                .path(id)
                .request()
                .get(new GenericType<TodoDto>() {});
    }

    public TodoDto createTodo(TodoDto todoDto) {
        return todoTarget()
                .request()
                .post(Entity.json(todoDto), new GenericType<TodoDto>() {});
    }

    public TodoDto updateTodo(String id, TodoDto todoDto) {
        return todoTarget()
                .path(id)
                .request()
                .put(Entity.json(todoDto), new GenericType<TodoDto>() {});
    }

    public void deleteTodo(String id) {
        todoTarget()
                .path(id)
                .request()
                .delete();
    }

    private WebTarget todoTarget() {
        return client.target(baseUri)
                .path("/api/v1/todo");
    }


}
