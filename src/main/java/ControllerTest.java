import com.gabrielgavrilov.mocha.MochaRequest;
import com.gabrielgavrilov.mocha.MochaResponse;
import com.gabrielgavrilov.mocha.annotations.Controller;
import com.gabrielgavrilov.mocha.annotations.Get;
import com.gabrielgavrilov.mocha.annotations.Route;

@Controller
@Route(value = "/api/v1/test")
public class ControllerTest {

    @Get
    public String hello() {
        System.out.println("Hello is called!");
        return "Hello, World!";
    }

}
