# Mocha

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

Then attach all your controllers to main and give your microservice a port:

```java
import com.gabrielgavrilov.mocha.Mocha;
import service.controller.GreetingController;

public class GreetingApp extends Mocha {
    public static void main(String[] args) {
        attach(GreetingController.class);
        listen(8080);
        // or listen(8080, "0.0.0.0")
    }
}
```

View at <a href="http://localhost:8080/greet/john">http://localhost:8080/greet/john<a/>.

You can check out and the <a href="https://github.com/GabrielGavrilov/mocha/tree/master/src/test/java/service">Todo microservice<a/> example in the source code as a reference.

### Annotations

#### Dependencies 

Mocha has a built-in dependency manager. All dependencies are singleton to the application's lifecycle and can be added by using the `@Dependency` annotation in the controller. 

```java
    @Dependency
    private TodoService todoService;
```

#### Routes

Mocha supports all CRUD routes and can be added to the controller with their respective annotations:
`@Get`
`@Post`
`@Put`
`@Delete`

#### Payloads

You can accept payloads in `Post`, `Put`, and `Delete` routes by adding the `@Body` annotation.

```Java
@Post
public TodoDto create(@Body TodoDto todo) {
    return todoService.create(todo);
}
```

#### Route Params

As shown previously, routes can have parameters by using the `@Param` annotation.

**All route parameters must be a String!**

```Java
@Get("/{id}")
public TodoDto getById(@Param String id) {
    return todoService.getById(id);
}
```

