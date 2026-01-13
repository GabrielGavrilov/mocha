import com.gabrielgavrilov.mocha.annotations.Controller;
import com.gabrielgavrilov.mocha.annotations.Get;
import com.gabrielgavrilov.mocha.annotations.Route;

@Controller
@Route("/api/v1/test")
public class ControllerTest {

    @Get
    public String hello() {
        return "Hello, World!";
    }

    @Get("/goodbye")
    public String goodbye() {
        return "Goodbye, World!";
    }

}
