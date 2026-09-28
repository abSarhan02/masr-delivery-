package model;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

public class Bicycle implements Vehicle {
    public boolean canDeliver(double km, int size) {
        return km <= 5 && size <= 3;
    }

    public double speed() {
        return 15;
    }
}
