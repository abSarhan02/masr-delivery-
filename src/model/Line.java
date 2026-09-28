package model;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

import java.math.BigDecimal;

public record Line(MenuItem item, BigDecimal quantity) {
    public Line {
        if (quantity.signum() <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (!(item instanceof WeightedItem) && quantity.stripTrailingZeros().scale() > 0)
            throw new IllegalArgumentException("Count must be a whole number");
    }

    public BigDecimal total() {
        return item.price(quantity);
    }
}
