import com.gabrielgavrilov.mocha.annotations.*;

@Controller
@Route("/api/v1/test")
public class ControllerTest {

    @Get
    public ModelTest getModel() {
        return new ModelTest("This is a test item", false);
    }

    @Get("/{id}")
    public ModelTest hello(@Param String id, @Body ModelTest model) {
        return model;
    }

}
