package model;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

import java.util.*;

public class Restaurant extends BaseEntity {
    public final String name, district;
    public final Set<String> cuisines;
    public final LinkedHashMap<String, MenuItem> menu = new LinkedHashMap<>();
    public double rating;
    public boolean open = true;

    public Restaurant(String id, String name, String district, Set<String> cuisines, double rating) {
        super(id);
        if (rating < 0 || rating > 5 || cuisines.isEmpty()) throw new IllegalArgumentException("Invalid restaurant");
        this.name = name;
        this.district = district;
        this.cuisines = Set.copyOf(cuisines);
        this.rating = rating;
    }

    public void addItem(MenuItem i) {
        if (menu.containsKey(i.id)) throw new IllegalArgumentException("Duplicate menu item ID");
        menu.put(i.id, i);
    }

    public void removeItem(String id) {
        menu.remove(id);
    }

    public List<MenuItem> menu() {
        return List.copyOf(menu.values());
    }

    public Set<String> cuisines() {
        return Set.copyOf(cuisines);
    }
}
