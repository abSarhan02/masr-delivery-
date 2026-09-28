package exception;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

public class BusyRiderException extends PlatformException {
    public BusyRiderException() {
        super("Rider is busy or off duty");
    }
}
