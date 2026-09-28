package model;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

import java.time.*;
import java.util.*;

public class Rider extends BaseEntity {
    public final String name;
    public final Vehicle vehicle;
    String district;
    public boolean onDuty;
    public Order activeOrder;
    public int completedDeliveries;
    public final List<Long> deliveryMinutes = new ArrayList<>();

    public Rider(String id, String name, Vehicle vehicle, String district) {
        super(id);
        this.name = name;
        this.vehicle = vehicle;
        this.district = district;
    }

    public void assign(Order o, double km) {
        if (!onDuty || activeOrder != null || !vehicle.canDeliver(km, o.lines.size())) throw new BusyRiderException();
        o.change(Status.ASSIGNED);
        activeOrder = o;
        o.rider = this;
    }

    public void delivered() {
        if (activeOrder == null) throw new IllegalArgumentException("No active order");
        Order o = activeOrder;
        o.change(Status.DELIVERED);
        completedDeliveries++;
        deliveryMinutes.add(Duration.between(o.placedAt, o.deliveredAt).toMinutes());
        activeOrder = null;
    }

    public double averageMinutes() {
        return deliveryMinutes.stream().mapToLong(Long::longValue).average().orElse(0);
    }
}
