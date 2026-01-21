import com.gabrielgavrilov.mocha.annotations.Controller;
import com.gabrielgavrilov.mocha.annotations.Get;
import com.gabrielgavrilov.mocha.annotations.Param;
import com.gabrielgavrilov.mocha.annotations.Route;

@Controller
@Route("/api/v1/test")
public class ControllerTest {

    @Get
    public ModelTest getModel() {
        return new ModelTest("This is a test item", false);
    }

    @Get("/{id}")
    public ModelTest hello(@Param String id) {
        return new ModelTest(id, false);
    }

}
