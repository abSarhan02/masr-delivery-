package exception;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

public class UnavailableItemException extends PlatformException {
    public UnavailableItemException() {
        super("Item is unavailable");
    }
}
