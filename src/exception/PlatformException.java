package exception;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

public class PlatformException extends RuntimeException {
    public PlatformException(String message) {
        super(message);
    }
}
