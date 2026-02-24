import com.gabrielgavrilov.mocha.Mocha;
import com.gabrielgavrilov.mocha.exceptions.NotFound;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import service.TodoClient;
import service.controller.TodoController;
import service.dto.TodoDto;
import service.todo.Todo;
import service.todo.TodoNotFoundException;
import service.todo.TodoService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class MochaControllerTest {

    private static int port;
    private static TodoClient client;

    private TodoDto FOO = createTodoDto("Foo", false);
    private TodoDto BAR = createTodoDto("Bar", false);

    @BeforeEach
    public void before() {
        port = 8327;

        Mocha.attach(TodoController.class);
        Mocha.listen(port);

        client = new TodoClient();
        client.setBaseUri("http://localhost:" + port);
    }

    @AfterEach
    public void after() {
        Mocha.stop();
    }

    @Test
    public void testGetAllTodoItems_withNoTodoItemsPresent_shouldReturnAListContainingOneTodoDto() {
        List<TodoDto> items = client.getAllTodos();
        assertEquals(0, items.size());
    }

    @Test
    public void testGetAllTodoItems_withTwoTodoItemsPresent_shouldReturnAListOfTodoDtos() {
        client.createTodo(FOO);
        client.createTodo(BAR);
        List<TodoDto> todos = client.getAllTodos();
        assertEquals(2, todos.size());
        assertAll(
                () -> assertEquals(FOO.title, todos.get(0).title),
                () -> assertEquals(FOO.isCompleted, todos.get(0).isCompleted)
        );
    }

    @Test
    public void testGetTodoById_shouldReturnTodoDto() {
        TodoDto created = client.createTodo(FOO);
        TodoDto todo = client.getTodoById(created.id.toString());
        assertAll(
                () -> assertEquals(created.title, todo.title),
                () -> assertEquals(created.isCompleted, todo.isCompleted)
        );
    }

    @Test
    public void testGetTodoByIdService_withAnIdThatDoesntExist_shouldThrowTodoNotFoundException() {
        TodoService service = new TodoService();
        assertThrows(TodoNotFoundException.class, () -> service.getById(UUID.randomUUID().toString()));
    }

    @Test
    public void testCreateTodoItem_shouldReturnCreatedTodoDto() {
        TodoDto todoDto = client.createTodo(FOO);
        assertEquals(FOO.title, todoDto.title);
        assertEquals(FOO.isCompleted, todoDto.isCompleted);
    }

    @Test
    public void testUpdateTodoItem_shouldReturnUpdatedTodoDto() {
        TodoDto todoDto = client.createTodo(FOO);
        TodoDto updatedTodo = client.updateTodo(todoDto.id.toString(), BAR);
        assertAll(
                () -> assertEquals(BAR.title, updatedTodo.title),
                () -> assertEquals(BAR.isCompleted, updatedTodo.isCompleted)
        );
    }

    @Test
    public void testDeleteTodoItem_shouldReturnAnEmptyList() {
        TodoDto todoDto = client.createTodo(FOO);
        List<TodoDto> todos = client.getAllTodos();
        assertEquals(1, todos.size());
        client.deleteTodo(todoDto.id.toString());
        todos = client.getAllTodos();
        assertEquals(0, todos.size());
    }

    private TodoDto createTodoDto(String title, boolean isCompleted) {
        TodoDto todo = new TodoDto();
        todo.title = title;
        todo.isCompleted = isCompleted;
        return todo;
    }

}
