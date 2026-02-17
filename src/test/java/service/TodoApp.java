package service;

import service.controller.TodoController;
import com.gabrielgavrilov.mocha.Mocha;

public class TodoApp extends Mocha {
    public static void main(String[] args) {
        attach(TodoController.class);
        listen(8080);
    }
}
