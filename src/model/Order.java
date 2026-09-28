package model;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

import java.math.*;
import java.time.*;
import java.util.*;

public class Order extends BaseEntity {
    public final Customer customer;
    public final Restaurant restaurant;
    public final Address address;
    public final List<Line> lines;
    public final Promotion promotion;
    public final String notes;
    public final double distanceKm;
    public final LocalDateTime placedAt = LocalDateTime.now();
    public final List<OrderListener> listeners = new ArrayList<>();
    public Status status = Status.PLACED;
    public Rider rider;
    public LocalDateTime deliveredAt, readyAt;
    public boolean paid;
    public BigDecimal subtotal, deliveryFee, serviceFee, discount, total;

    public Order(String id, Customer c, Restaurant r, Address a, List<Line> lines, Promotion p, String notes, double km) {
        super(id);
        if (lines.isEmpty()) throw new IllegalArgumentException("Order needs at least one item");
        if (!c.addresses.contains(a)) throw new IllegalArgumentException("Address does not belong to customer");
        if (km < 0) throw new IllegalArgumentException("Invalid distance");
        if (!r.open) throw new ClosedRestaurantException();
        for (Line line : lines) {
            if (r.menu.get(line.item().id) != line.item())
                throw new IllegalArgumentException("Item is not in this restaurant");
            line.item().check(line.quantity());
        }
        customer = c;
        restaurant = r;
        address = a;
        this.lines = List.copyOf(lines);
        promotion = p;
        this.notes = notes;
        distanceKm = km;
        subtotal = lines.stream().map(Line::total).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
        Config cfg = Config.get();
        deliveryFee = cfg.baseFee.add(cfg.extraKmFee.multiply(BigDecimal.valueOf(Math.max(0, km - 3)))).setScale(2, RoundingMode.HALF_UP);
        if (c.tier() == Tier.SILVER)
            deliveryFee = deliveryFee.multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_UP);
        if (c.tier() == Tier.GOLD) deliveryFee = BigDecimal.ZERO.setScale(2);
        serviceFee = subtotal.multiply(cfg.serviceRate).setScale(2, RoundingMode.HALF_UP);
        discount = p == null ? BigDecimal.ZERO.setScale(2) : p.discount(this);
        total = subtotal.add(deliveryFee).add(serviceFee).subtract(discount).max(BigDecimal.ZERO.setScale(2));
    }

    public void listen(OrderListener l) {
        listeners.add(l);
    }

    public void change(Status next) {
        boolean valid = switch (status) {
            case PLACED -> next == Status.ACCEPTED || next == Status.CANCELLED;
            case ACCEPTED -> next == Status.PREPARING || next == Status.CANCELLED;
            case PREPARING -> next == Status.READY || next == Status.CANCELLED;
            case READY -> next == Status.ASSIGNED || next == Status.CANCELLED;
            case ASSIGNED -> next == Status.OUT_FOR_DELIVERY || next == Status.CANCELLED;
            case OUT_FOR_DELIVERY -> next == Status.DELIVERED;
            default -> false;
        };
        if (!valid) throw new InvalidStatusException();
        status = next;
        if (next == Status.READY) readyAt = LocalDateTime.now();
        if (next == Status.DELIVERED) {
            deliveredAt = LocalDateTime.now();
            customer.completedOrders++;
        }
        listeners.forEach(l -> l.changed(this));
    }
}
