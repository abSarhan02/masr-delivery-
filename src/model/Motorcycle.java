package model;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

public class Motorcycle implements Vehicle {
    public boolean canDeliver(double km, int size) {
        return km <= 30 && size <= 10;
    }

    public double speed() {
        return 35;
    }
}
