package model;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

public interface OrderListener {
    void changed(Order order);
}
