package service.todo;

import service.dto.TodoDto;
import com.gabrielgavrilov.mocha.exceptions.NotFound;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TodoService {

    private final TodoAssembler todoAssembler = new TodoAssembler();
    private final ArrayList<Todo> todos = new ArrayList<>();

    public TodoService() {}

    public List<TodoDto> getAll() {
        return this.todos
                .stream()
                .map(todoAssembler::assemble)
                .toList();
    }

    public TodoDto getById(String id) {
        return this.todos
                .stream()
                .filter(todo -> todo.getId().equals(UUID.fromString(id)))
                .findFirst()
                .map(todoAssembler::assemble)
                .orElseThrow(() -> new NotFound(String.format("No todo item exists with the id of '%s'", id)));
    }

    public TodoDto create(TodoDto dto) {
        Todo todo = todoAssembler.disassemble(dto);
        todos.add(todo);
        return getById(todo.getId().toString());
    }

    public TodoDto update(String id, TodoDto dto) {
        Todo entity = this.todos
                .stream()
                .filter(todo -> todo.getId().equals(UUID.fromString(id)))
                .findFirst()
                .orElseThrow(() -> new NotFound(String.format("No todo item exists with the id of '%s'", id)));
        delete(id);
        todos.add(todoAssembler.disassembleInto(entity, dto));
        return getById(id);
    }

    public void delete(String id) {
        Todo delete = this.todos
                .stream()
                .filter(todo -> todo.getId().equals(UUID.fromString(id)))
                .findFirst()
                .get();
        this.todos.remove(delete);
    }

}
