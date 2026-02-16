import com.gabrielgavrilov.mocha.annotations.*;

import java.util.ArrayList;

@Controller
@Route("/api/v1/todo")
public class TodoController {

    @Dependency
    private TodoService todoService;

    @Get
    public Todo[] get() {
        return todoService.getAll();
    }

    @Post
    public Todo addTodo(@Body Todo todo) {
        return todoService.addTodo(todo);
    }

}
