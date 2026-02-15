import com.gabrielgavrilov.mocha.MochaRequest;
import com.gabrielgavrilov.mocha.MochaResponse;
import com.gabrielgavrilov.mocha.annotations.*;

@Controller
@Route("/api/v1/todo")
public class TodoController {

    @Dependency
    private TodoService todoService;

    @Get
    public Todo[] get(@Request MochaRequest request, @Response MochaResponse response) {
        System.out.println(request.header);
        response.addHeader("Working", "true");
        return todoService.getAll();
    }

    @Post
    public Todo addTodo(@Body Todo todo) {
        return todoService.addTodo(todo);
    }

}
