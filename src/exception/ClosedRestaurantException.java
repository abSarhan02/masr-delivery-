package exception;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

public class ClosedRestaurantException extends PlatformException {
    public ClosedRestaurantException() {
        super("Restaurant is closed");
    }
}
