import com.gabrielgavrilov.mocha.Mocha;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import service.TodoClient;
import service.controller.TodoController;
import service.dto.TodoDto;
import service.todo.TodoService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class MochaControllerTest {

    private static int port;
    private static TodoClient client;

    private TodoDto FOO = createTodoDto(new UUID(0,8),"Foo", false);
    private TodoDto BAR = createTodoDto(new UUID(0,9),"Bar", false);

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
    public void testGetAllTodoItems_withNoTodoItemsPresent_shouldReturnEmptyListOfTodoDto() {
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
                () -> assertEquals(FOO.id, todos.get(0).id),
                () -> assertEquals(FOO.title, todos.get(0).title),
                () -> assertEquals(FOO.isCompleted, todos.get(0).isCompleted)
        );
    }

    public TodoDto createTodoDto(UUID id, String title, boolean isCompleted) {
        TodoDto todo = new TodoDto();
        todo.id = id;
        todo.title = title;
        todo.isCompleted = isCompleted;
        return todo;
    }

}
