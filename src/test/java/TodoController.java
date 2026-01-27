import com.gabrielgavrilov.mocha.annotations.*;

import java.util.ArrayList;

@Controller
@Route("/api/v1/todo")
public class TodoController {

    @Dependency
    private TodoService todoService;

    @Get
    public Todo get() {
        return new Todo("Lorem Ipsum Dolor sit amet");
    }

}
