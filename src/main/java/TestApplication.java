import com.gabrielgavrilov.mocha.Mocha;

public class TestApplication extends Mocha {

    public static void main(String[] args) {

        attach(ControllerTest.class);

        listen(8080);
    }

}
