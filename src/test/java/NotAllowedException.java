import com.gabrielgavrilov.mocha.exceptions.BadRequest;

public class NotAllowedException extends BadRequest {
    public NotAllowedException(String message) {
        super(message);
    }
}
