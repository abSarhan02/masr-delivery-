package exception;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

public class InvalidStatusException extends PlatformException {
    public InvalidStatusException() {
        super("Illegal order status change");
    }
}
