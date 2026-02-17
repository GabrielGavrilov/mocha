package service.todo;

import service.dto.TodoDto;

public class TodoAssembler {

    public TodoDto assemble(Todo entity) {
        TodoDto dto = new TodoDto();
        dto.id = entity.getId();
        dto.title = entity.getTitle();
        dto.isCompleted = entity.isCompleted();
        return dto;
    }

    public Todo disassemble(TodoDto dto) {
        return new Todo(dto.title, dto.isCompleted);
    }

    public Todo disassembleInto(Todo entity, TodoDto dto) {
        entity.setTitle(dto.title);
        entity.setCompleted(dto.isCompleted);
        return entity;
    }

}
