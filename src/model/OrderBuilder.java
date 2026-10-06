package model;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

import java.math.BigDecimal;
import java.util.*;

public class OrderBuilder {

    private final String id;
    private final Customer customer;
    private final Restaurant restaurant;
    private final Address address;
    private final double km;

    // Items added to the order
    private final List<Line> lines = new ArrayList<>();

    private Promotion promotion;
    private String notes = "";


    public OrderBuilder(
            String id,
            Customer customer,
            Restaurant restaurant,
            Address address,
            double km) {

        this.id = id;
        this.customer = customer;
        this.restaurant = restaurant;
        this.address = address;
        this.km = km;
    }


    // Add an item and return the builder to continue building
    public OrderBuilder add(
            MenuItem item,
            BigDecimal quantity) {

        lines.add(new Line(item, quantity));

        return this;
    }


    public OrderBuilder promotion(Promotion promotion) {
        this.promotion = promotion;
        return this;
    }


    public OrderBuilder notes(String notes) {
        this.notes = notes;
        return this;
    }


    // Create the final order
    public Order build() {

        return new Order(
                id,
                customer,
                restaurant,
                address,
                lines,
                promotion,
                notes,
                km
        );
    }
}
