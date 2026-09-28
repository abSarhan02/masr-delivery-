package service;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

import java.math.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class Platform {
    public final Map<String, Restaurant> restaurants = new HashMap<>();
    public final Map<String, Customer> customers = new HashMap<>();
    public final Map<String, Rider> riders = new HashMap<>();
    public final Map<String, Order> orders = new HashMap<>();
    public final Map<String, Promotion> promotions = new HashMap<>();
    final PriorityQueue<Order> ready = new PriorityQueue<>(Comparator
            .comparing((Order o) -> o.customer.tier() != Tier.GOLD)
            .thenComparing(o -> o.readyAt));
    public final List<String> audit = new ArrayList<>();

    public void addRestaurant(Restaurant value) {
        if (!(restaurants.putIfAbsent(value.id, value) == null))
            throw new IllegalArgumentException("Duplicate restaurant");
    }

    public void addCustomer(Customer value) {
        if (!(customers.putIfAbsent(value.id, value) == null)) throw new IllegalArgumentException("Duplicate customer");
    }

    public void addRider(Rider value) {
        if (!(riders.putIfAbsent(value.id, value) == null)) throw new IllegalArgumentException("Duplicate rider");
    }

    public void addPromotion(Promotion value) {
        if (!(promotions.putIfAbsent(value.id, value) == null))
            throw new IllegalArgumentException("Duplicate promotion");
    }

    public List<Restaurant> browse(Predicate<Restaurant> filter) {
        return restaurants.values().stream().filter(r -> r.open).filter(filter)
                .sorted(Comparator.comparingDouble((Restaurant r) -> r.rating).reversed().thenComparing(r -> r.name)).toList();
    }

    public Set<String> cuisines() {
        return restaurants.values().stream().flatMap(r -> r.cuisines.stream()).collect(Collectors.toSet());
    }

    public Order place(Order order) {
        if (!(!orders.containsKey(order.id))) throw new IllegalArgumentException("Duplicate order");
        // Check the combined quantity before changing any stock.
        Map<MenuItem, BigDecimal> needed = order.lines.stream().collect(Collectors.toMap(Line::item, Line::quantity, BigDecimal::add));
        needed.forEach(MenuItem::check);
        needed.forEach((item, qty) -> item.stock -= qty.doubleValue());
        order.listen(o -> {
            audit.add(o.id + " -> " + o.status);
            System.out.println("Order update: " + o.id + " " + o.status);
        });
        orders.put(order.id, order);
        return order;
    }

    public void pay(Order order) {
        if (!(!order.paid && order.status != Status.CANCELLED))
            throw new IllegalArgumentException("Cannot pay this order");
        order.customer.debit(order.total);
        order.paid = true;
    }

    public void cancel(Order order) {
        order.change(Status.CANCELLED);
        if (order.paid) {
            order.customer.refund(order.total);
            order.paid = false;
        }
        order.lines.forEach(line -> line.item().stock += line.quantity().doubleValue());
        if (order.rider != null) order.rider.activeOrder = null;
        ready.remove(order);
    }

    public void change(Order order, Status next) {
        order.change(next);
        if (next == Status.READY) ready.add(order);
    }

    public void dispatch(Rider rider) {
        Order order = ready.stream().filter(o -> o.status == Status.READY && rider.vehicle.canDeliver(o.distanceKm, o.lines.size()))
                .min(ready.comparator()).orElseThrow(() -> new PlatformException("No eligible ready order"));
        rider.assign(order, order.distanceKm);
        ready.remove(order);
    }

    public List<Order> history(Customer c) {
        return orders.values().stream().filter(o -> o.customer.equals(c))
                .sorted(Comparator.comparing((Order o) -> o.placedAt).reversed()).toList();
    }

    public BigDecimal spent(Customer c) {
        return history(c).stream().filter(o -> o.paid || o.status == Status.DELIVERED)
                .map(o -> o.total).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    public List<Order> delivered() {
        return orders.values().stream().filter(o -> o.status == Status.DELIVERED).toList();
    }

    public BigDecimal revenue(LocalDate from, LocalDate to) {
        return delivered().stream()
                .filter(o -> !o.deliveredAt.toLocalDate().isBefore(from) && !o.deliveredAt.toLocalDate().isAfter(to))
                .map(o -> o.total).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }

    public Map<Restaurant, BigDecimal> topFive(YearMonth month) {
        return delivered().stream()
                .filter(o -> YearMonth.from(o.deliveredAt).equals(month))
                .collect(Collectors.groupingBy(o -> o.restaurant, Collectors.reducing(BigDecimal.ZERO.setScale(2), o -> o.total, BigDecimal::add)))
                .entrySet().stream().sorted(Map.Entry.<Restaurant, BigDecimal>comparingByValue().reversed()).limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    public Map<String, Double> averageByDistrict() {
        return orders.values().stream().collect(Collectors.groupingBy(o -> o.address.district(), Collectors.averagingDouble(o -> o.total.doubleValue())));
    }

    public List<Restaurant> popularRestaurants() {
        return restaurants.values().stream().filter(r -> r.rating > 4.5)
                .filter(r -> delivered().stream().filter(o -> o.restaurant.equals(r)).count() >= 20).toList();
    }

    public Map<Status, Long> countByStatus() {
        return orders.values().stream().collect(Collectors.groupingBy(o -> o.status, () -> new EnumMap<>(Status.class), Collectors.counting()));
    }

    public List<Rider> riderReport() {
        return riders.values().stream().sorted(Comparator.comparingInt((Rider r) -> r.completedDeliveries).reversed()).toList();
    }

    public Optional<MenuItem> mostOrdered() {
        return orders.values().stream().flatMap(o -> o.lines.stream())
                .collect(Collectors.groupingBy(Line::item, Collectors.reducing(BigDecimal.ZERO.setScale(2), Line::quantity, BigDecimal::add)))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
    }

    public Optional<Integer> peakHour() {
        return orders.values().stream().collect(Collectors.groupingBy(o -> o.placedAt.getHour(), Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
    }

    public List<Customer> inactiveCustomers() {
        return customers.values().stream().filter(c -> history(c).stream()
                .noneMatch(o -> o.placedAt.isAfter(LocalDateTime.now().minusDays(30)))).toList();
    }
}
