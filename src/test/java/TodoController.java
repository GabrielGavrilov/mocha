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

//    @Get("/add")
//    public Todo addTodo(@Body Todo todo) {
//        return todoService.addTodo(todo);
//    }

//    @Get("/{firstName}/{lastName}")
//    public String test(@Param String firstName, @Param String lastName) {
//        return firstName + " " + lastName;
//    }

}
