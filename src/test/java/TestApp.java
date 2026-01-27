import com.gabrielgavrilov.mocha.Mocha;

public class TestApp extends Mocha {
    public static void main(String[] args) {
        attach(TodoController.class);
        listen(8080);
    }
}
