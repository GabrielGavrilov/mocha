package service.todo;

import java.util.UUID;

public class Todo {

    private UUID id;
    private String title;
    private boolean isCompleted;

    public Todo() {}

    public Todo(String title, boolean isCompleted) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.isCompleted = isCompleted;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
