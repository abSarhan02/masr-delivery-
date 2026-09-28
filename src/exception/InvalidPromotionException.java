package exception;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

public class InvalidPromotionException extends PlatformException {
    public InvalidPromotionException(String message) {
        super(message);
    }
}
