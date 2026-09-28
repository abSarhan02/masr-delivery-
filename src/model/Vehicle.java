package model;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

public interface Vehicle {
    boolean canDeliver(double km, int size);

    public double speed();
}
