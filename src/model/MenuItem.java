package model;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

import java.math.*;

public abstract class MenuItem extends BaseEntity {
    public final String name, category;
    public final int preparationMinutes;
    public boolean available = true;
    public double stock;

    public MenuItem(String id, String name, String category, int minutes, int stock) {
        super(id);
        if (minutes < 0 || stock < 0) throw new IllegalArgumentException("Invalid preparation time or stock");
        this.name = name;
        this.category = category;
        this.preparationMinutes = minutes;
        this.stock = stock;
    }

    public abstract BigDecimal price(BigDecimal quantity);

    public abstract BigDecimal displayedPrice();

    public void check(BigDecimal q) {
        if (q.signum() <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (!available) throw new UnavailableItemException();
        if (BigDecimal.valueOf(stock).compareTo(q) < 0) throw new StockException();
    }
}
