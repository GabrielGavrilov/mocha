package tests;

import com.gabrielgavrilov.mocha.Mocha;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.TodoClient;
import service.controller.TodoController;
import service.dto.TodoDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MochaControllerTest {

    private static int port;
    private static TodoClient client;

    @BeforeEach
    public void before() {
        port = 8327;

        Mocha.attach(TodoController.class);
        Mocha.listen(port);

        client = new TodoClient();
        client.setBaseUri("http://localhost:" + port);
    }

    @Test
    public void testGetAllTodoItems_withNoTodoItemsPresent_shouldReturnEmptyListOfTodoDto() {
        List<TodoDto> items = client.getAllTodos();
        assertEquals(0, items.size());
    }

}
