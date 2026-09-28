package model;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

import java.math.*;

public class WeightedItem extends StandardItem {
    public WeightedItem(String id, String name, String category, BigDecimal perKg, int minutes, int stock) {
        super(id, name, category, perKg, minutes, stock);
    }
}
