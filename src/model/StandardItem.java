package model;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

import java.math.*;

public class StandardItem extends MenuItem {
    public final BigDecimal unitPrice;

    public StandardItem(String id, String name, String category, BigDecimal price, int minutes, int stock) {
        super(id, name, category, minutes, stock);
        if (price.signum() <= 0) throw new IllegalArgumentException("Price must be positive");
        unitPrice = price;
    }

    public BigDecimal price(BigDecimal q) {
        return unitPrice.multiply(q).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal displayedPrice() {
        return unitPrice;
    }
}
