package exception;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

public class InsufficientWalletException extends PlatformException {
    public InsufficientWalletException() {
        super("Not enough wallet balance");
    }
}
