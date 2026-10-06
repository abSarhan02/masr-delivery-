package model;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

import java.util.*;

public class Restaurant extends BaseEntity {

    public final String name, district;

    // Set avoids duplicate cuisine types
    public final Set<String> cuisines;

    // Keep menu items in insertion order
    public final LinkedHashMap<String, MenuItem> menu =
            new LinkedHashMap<>();

    public double rating;
    public boolean open = true;


    public Restaurant(
            String id,
            String name,
            String district,
            Set<String> cuisines,
            double rating) {

        super(id);

        // Rating must be between 0 and 5
        if (rating < 0 || rating > 5 || cuisines.isEmpty())
            throw new IllegalArgumentException(
                    "Invalid restaurant"
            );

        this.name = name;
        this.district = district;
        this.cuisines = Set.copyOf(cuisines);
        this.rating = rating;
    }


    public void addItem(MenuItem i) {

        // Every menu item needs a unique ID
        if (menu.containsKey(i.id))
            throw new IllegalArgumentException(
                    "Duplicate menu item ID"
            );

        menu.put(i.id, i);
    }


    public void removeItem(String id) {
        menu.remove(id);
    }


    public List<MenuItem> menu() {

        // Don't expose the original collection
        return List.copyOf(menu.values());
    }


    public Set<String> cuisines() {
        return Set.copyOf(cuisines);
    }
}
