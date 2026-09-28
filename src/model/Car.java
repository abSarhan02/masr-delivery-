package model;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

public class Car implements Vehicle {
    public boolean canDeliver(double km, int size) {
        return km <= 50 && size <= 30;
    }

    public double speed() {
        return 30;
    }
}
