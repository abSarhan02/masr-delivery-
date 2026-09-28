package factory;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

import java.math.*;
import java.util.*;

public class ItemFactory {
    public static MenuItem create(String type, String id, String name, String category, BigDecimal price, int minutes, int stock, List<MenuItem> parts, BigDecimal discount) {
        return switch (type.toUpperCase()) {
            case "STANDARD" -> new StandardItem(id, name, category, price, minutes, stock);
            case "WEIGHTED" -> new WeightedItem(id, name, category, price, minutes, stock);
            case "COMBO" -> new ComboItem(id, name, category, parts, discount, minutes, stock);
            default -> throw new IllegalArgumentException("Unknown item type");
        };
    }
}
