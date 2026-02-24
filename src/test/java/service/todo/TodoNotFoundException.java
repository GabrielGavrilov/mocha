package service.todo;

import com.gabrielgavrilov.mocha.exceptions.NotFound;

public class TodoNotFoundException extends NotFound {
    public TodoNotFoundException(String message) {
        super(message);
    }
}
