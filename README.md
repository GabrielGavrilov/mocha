# Mocha

## Getting Started

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

