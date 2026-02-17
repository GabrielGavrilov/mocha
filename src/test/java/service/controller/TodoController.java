package service.controller;

import service.dto.TodoDto;
import service.todo.TodoService;
import com.gabrielgavrilov.mocha.MochaResponse;
import com.gabrielgavrilov.mocha.annotations.*;

import java.util.List;

@Controller
@Route("/api/v1/todo")
public class TodoController {

    @Dependency
    private TodoService todoService;

    @Get
    public List<TodoDto> get() {
        return todoService.getAll();
    }

    @Get("/{id}")
    public TodoDto getById(@Param String id) {
        return todoService.getById(id);
    }

    @Post
    public TodoDto create(@Body TodoDto todo) {
        return todoService.create(todo);
    }

    @Put("/{id}")
    public TodoDto update(@Param String id, @Body TodoDto todo) {
        return todoService.update(id, todo);
    }

    @Delete("/{id}")
    public void delete(@Response MochaResponse response, @Param String id) {
        todoService.delete(id);
        response.setStatus(204, "No Content");
    }

}
