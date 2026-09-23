# Mocha

## Getting Started

You can create a controller by adding the `@Controller` and `@Route` annotations:

```java
import com.gabrielgavrilov.mocha.*;

@Controller
@Route("/greet")
public class GreetingController {

    @Get("/{name}")
    public String greetPerson(@Param String name) {
        return String.format("Hello, %s!", name);
    }

}
```

Attach all your controllers to main and give your microservice a port:

```java
import com.gabrielgavrilov.mocha.Mocha;
import service.controller.GreetingController;

public class GreetingApp extends Mocha {
    public static void main(String[] args) {
        attach(GreetingController.class);
        listen(8080);
    }
}
```

View at `http://localhost:8080/greet/john`.

