import java.util.ArrayList;

public class TodoService {

    private final ArrayList<Todo> todos = new ArrayList<>();

    public TodoService() {}

    public Todo[] getAll() {
        return this.todos.toArray(new Todo[todos.size()]);
    }

    public Todo addTodo(Todo todo) {
        this.todos.add(todo);
        return todo;
    }

}
