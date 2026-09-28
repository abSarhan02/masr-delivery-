package exception;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

public class StockException extends PlatformException {
    public StockException() {
        super("Not enough stock");
    }
}
