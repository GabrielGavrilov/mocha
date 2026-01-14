import com.gabrielgavrilov.mocha.annotations.Controller;
import com.gabrielgavrilov.mocha.annotations.Get;
import com.gabrielgavrilov.mocha.annotations.Route;

@Controller
@Route("/api/v1/test")
public class ControllerTest {

    @Get
    public ModelTest getModel() {
        return new ModelTest("This is a test item", false);
    }

}
