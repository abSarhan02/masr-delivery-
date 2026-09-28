package model;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

import java.math.*;
import java.util.*;

public class ComboItem extends MenuItem {
    public final List<MenuItem> parts;
    public final BigDecimal discountPercent;

    public ComboItem(String id, String name, String category, List<MenuItem> parts, BigDecimal discount, int minutes, int stock) {
        super(id, name, category, minutes, stock);
        if (parts.isEmpty() || discount.signum() < 0 || discount.compareTo(new BigDecimal("100")) >= 0)
            throw new IllegalArgumentException("Invalid combo");
        this.parts = List.copyOf(parts);
        this.discountPercent = discount;
    }

    public BigDecimal displayedPrice() {
        BigDecimal sum = parts.stream().map(MenuItem::displayedPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.multiply(BigDecimal.ONE.subtract(discountPercent.divide(new BigDecimal("100")))).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal price(BigDecimal q) {
        return displayedPrice().multiply(q).setScale(2, RoundingMode.HALF_UP);
    }
}
