package app;

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

public class ConsoleApp {
    private final Scanner in = new Scanner(System.in);
    private final Platform app;

    public ConsoleApp(Platform app) {
        this.app = app;
    }

    private String ask(String prompt) {
        System.out.print(prompt + ": ");
        return in.nextLine().trim();
    }

    private int number(String prompt) {
        return Integer.parseInt(ask(prompt));
    }

    private BigDecimal amount(String prompt) {
        return new BigDecimal(ask(prompt));
    }

    private <T> T get(Map<String, T> map, String label) {
        T value = map.get(ask(label + " ID"));
        if (value == null) throw new IllegalArgumentException(label + " not found");
        return value;
    }

    private void show(Order o) {
        System.out.println(o.id + " | " + o.status + " | " + o.placedAt);
        System.out.println("Subtotal: " + o.subtotal + " EGP | Delivery: " + o.deliveryFee + " EGP | Service: " + o.serviceFee + " EGP");
        System.out.println("Discount: " + o.discount + " EGP | Total: " + o.total + " EGP");
    }

    private void menu(Restaurant r) {
        r.menu().stream().collect(Collectors.groupingBy(i -> i.category, LinkedHashMap::new, Collectors.toList())).forEach((category, items) -> {
            System.out.println(category);
            items.forEach(i -> System.out.println("  " + i.id + " " + i.name + " " + i.displayedPrice() + " EGP " + (i.available ? "" : "UNAVAILABLE") + " stock=" + i.stock));
        });
    }

    private void customerArea() {
        System.out.println("1 Browse 2 Search 3 View menu 4 Place order 5 Pay 6 Track 7 Cancel 8 History 0 Back");
        int choice = number("Option");
        if (choice == 0) return;
        if (choice == 1) {
            String district = ask("District (blank for all)"), cuisine = ask("Cuisine (blank for all)");
            String min = ask("Minimum rating (blank for all)"), max = ask("Maximum item price (blank for all)");
            Predicate<Restaurant> filter = r -> (district.isBlank() || r.district.equalsIgnoreCase(district)) && (cuisine.isBlank() || r.cuisines.stream().anyMatch(c -> c.equalsIgnoreCase(cuisine))) && (min.isBlank() || r.rating >= Double.parseDouble(min)) && (max.isBlank() || r.menu().stream().anyMatch(i -> i.displayedPrice().compareTo(new BigDecimal(max)) <= 0));
            List<Restaurant> results = app.browse(filter);
            for (int i = 0; i < results.size(); i++) {
                Restaurant r = results.get(i);
                System.out.println((i + 1) + ". " + r.id + " " + r.name + " | " + r.district + " | " + r.cuisines + " | " + r.rating);
            }
            if (results.isEmpty()) System.out.println("Nothing found");
        } else if (choice == 2) {
            Customer c = get(app.customers, "Customer");
            String q = ask("Search");
            c.search(q);
            List<Restaurant> found = app.browse(r -> r.name.toLowerCase().contains(q.toLowerCase()) || r.cuisines.stream().anyMatch(x -> x.toLowerCase().contains(q.toLowerCase())));
            if (found.isEmpty()) System.out.println("Nothing found");
            else found.forEach(r -> System.out.println(r.id + " " + r.name));
        } else if (choice == 3) menu(get(app.restaurants, "Restaurant"));
        else if (choice == 4) {
            Customer c = get(app.customers, "Customer");
            Restaurant r = get(app.restaurants, "Restaurant");
            c.addresses().forEach(a -> System.out.println(c.addresses().indexOf(a) + " " + a));
            Address address = c.addresses().get(number("Address number"));
            OrderBuilder builder = new OrderBuilder(ask("Order ID"), c, r, address, Double.parseDouble(ask("Distance in km")));
            menu(r);
            while (true) {
                String id = ask("Item ID (blank to finish)");
                if (id.isBlank()) break;
                MenuItem item = r.menu.get(id);
                if (!(item != null)) throw new IllegalArgumentException("Item not found");
                builder.add(item, amount("Quantity (kg for weighted items)"));
            }
            String promo = ask("Promo code (blank for none)");
            if (!promo.isBlank()) {
                Promotion p = app.promotions.get(promo.toUpperCase());
                if (!(p != null)) throw new IllegalArgumentException("Promotion not found");
                builder.promotion(p);
            }
            builder.notes(ask("Delivery notes (optional)"));
            Order order = app.place(builder.build());
            show(order);
        } else if (choice == 5) {
            Order o = get(app.orders, "Order");
            app.pay(o);
            System.out.println("New wallet balance: " + o.customer.wallet);
        } else if (choice == 6) {
            Order o = get(app.orders, "Order");
            System.out.println(o.status + " | " + Duration.between(o.placedAt, LocalDateTime.now()).toMinutes() + " minutes elapsed");
        } else if (choice == 7) {
            app.cancel(get(app.orders, "Order"));
            System.out.println("Order cancelled and payment refunded if paid");
        } else if (choice == 8) {
            Customer c = get(app.customers, "Customer");
            app.history(c).forEach(this::show);
            System.out.println("Lifetime spent: " + app.spent(c));
        } else System.out.println("Invalid option");
    }

    private void restaurantArea() {
        System.out.println("1 Accept 2 Reject 3 Preparing 4 Ready 5 Toggle item 6 Add item 7 Remove item 8 Stock 9 Today orders/revenue 0 Back");
        int choice = number("Option");
        if (choice == 0) return;
        Restaurant r = get(app.restaurants, "Restaurant");
        if (choice >= 1 && choice <= 4) {
            Order o = get(app.orders, "Order");
            if (!(o.restaurant.equals(r))) throw new IllegalArgumentException("Wrong restaurant");
            if (choice == 2) app.cancel(o);
            else app.change(o, choice == 1 ? Status.ACCEPTED : choice == 3 ? Status.PREPARING : Status.READY);
        } else if (choice == 5) {
            MenuItem item = r.menu.get(ask("Item ID"));
            if (!(item != null)) throw new IllegalArgumentException("Item not found");
            item.available = !item.available;
        } else if (choice == 6) {
            String type = ask("Type (STANDARD / WEIGHTED / COMBO)");
            String id = ask("Item ID"), name = ask("Name"), category = ask("Category");
            BigDecimal price = new BigDecimal("1"), discount = BigDecimal.ZERO.setScale(2);
            List<MenuItem> parts = new ArrayList<>();
            if (type.equalsIgnoreCase("COMBO")) {
                while (true) {
                    String part = ask("Part ID (blank to finish)");
                    if (part.isBlank()) break;
                    MenuItem item = r.menu.get(part);
                    if (!(item != null)) throw new IllegalArgumentException("Part not found");
                    parts.add(item);
                }
                discount = amount("Discount percent");
            } else price = amount("Price (per kg if weighted)");
            r.addItem(ItemFactory.create(type, id, name, category, price, number("Preparation minutes"), number("Stock"), parts, discount));
        } else if (choice == 7) r.removeItem(ask("Item ID"));
        else if (choice == 8) {
            MenuItem item = r.menu.get(ask("Item ID"));
            if (!(item != null)) throw new IllegalArgumentException("Item not found");
            int stock = number("New stock");
            if (!(stock >= 0)) throw new IllegalArgumentException("Negative stock");
            item.stock = stock;
        } else if (choice == 9) {
            app.orders.values().stream().filter(o -> o.restaurant.equals(r) && o.placedAt.toLocalDate().equals(LocalDate.now())).forEach(this::show);
            System.out.println("Today's revenue: " + app.delivered().stream().filter(o -> o.restaurant.equals(r) && o.deliveredAt.toLocalDate().equals(LocalDate.now())).map(o -> o.total).reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add));
        } else System.out.println("Invalid option");
    }

    private void riderArea() {
        System.out.println("1 Duty 2 Current order 3 Pick up 4 Deliver 5 Statistics 6 Assign next ready order 0 Back");
        int choice = number("Option");
        if (choice == 0) return;
        Rider r = get(app.riders, "Rider");
        if (choice == 1) {
            if (r.activeOrder != null && r.onDuty) throw new BusyRiderException();
            r.onDuty = !r.onDuty;
            System.out.println("On duty: " + r.onDuty);
        } else if (choice == 2) {
            if (r.activeOrder == null) System.out.println("No assigned order");
            else show(r.activeOrder);
        } else if (choice == 3) {
            if (!(r.activeOrder != null)) throw new IllegalArgumentException("No assigned order");
            app.change(r.activeOrder, Status.OUT_FOR_DELIVERY);
        } else if (choice == 4) r.delivered();
        else if (choice == 5)
            System.out.println("Deliveries: " + r.completedDeliveries + " | Average minutes: " + r.averageMinutes());
        else if (choice == 6) app.dispatch(r);
        else System.out.println("Invalid option");
    }

    private void reports() {
        System.out.println("1 Revenue range 2 Top five 3 Average by district 4 High rated 5 Status counts 6 Riders 7 Most ordered item 8 Customer history 9 Peak hour 10 Inactive customers 11 Platform stats 12 Add restaurant 13 Remove restaurant 14 Create promotion 0 Back");
        int choice = number("Option");
        if (choice == 0) return;
        switch (choice) {
            case 1 ->
                    System.out.println(app.revenue(LocalDate.parse(ask("From YYYY-MM-DD")), LocalDate.parse(ask("To YYYY-MM-DD"))));
            case 2 ->
                    app.topFive(YearMonth.parse(ask("Month YYYY-MM"))).forEach((r, value) -> System.out.println(r.name + " " + value));
            case 3 -> System.out.println(app.averageByDistrict());
            case 4 -> app.popularRestaurants().forEach(r -> System.out.println(r.name));
            case 5 -> System.out.println(app.countByStatus());
            case 6 ->
                    app.riderReport().forEach(r -> System.out.println(r.name + " " + r.completedDeliveries + " " + r.averageMinutes() + " minutes"));
            case 7 -> System.out.println(app.mostOrdered().map(i -> i.name).orElse("No ordered item"));
            case 8 -> {
                Customer c = get(app.customers, "Customer");
                app.history(c).forEach(this::show);
                System.out.println("Spent: " + app.spent(c));
            }
            case 9 -> System.out.println(app.peakHour().map(h -> h + ":00").orElse("No orders"));
            case 10 -> app.inactiveCustomers().forEach(c -> System.out.println(c.name));
            case 11 ->
                    System.out.println("Restaurants: " + app.restaurants.size() + " Customers: " + app.customers.size() + " Riders: " + app.riders.size() + " Orders: " + app.orders.size());
            case 12 -> {
                String id = ask("ID"), name = ask("Name"), district = ask("District");
                Set<String> cuisines = Arrays.stream(ask("Cuisines separated by comma").split(",")).map(String::trim).collect(Collectors.toSet());
                app.addRestaurant(new Restaurant(id, name, district, cuisines, Double.parseDouble(ask("Rating"))));
            }
            case 13 -> {
                Restaurant r = get(app.restaurants, "Restaurant");
                if (!(app.orders.values().stream().noneMatch(o -> o.restaurant.equals(r) && o.status != Status.DELIVERED && o.status != Status.CANCELLED)))
                    throw new IllegalArgumentException("Restaurant has active orders");
                app.restaurants.remove(r.id);
            }
            case 14 -> {
                String type = ask("Type (PERCENT / FIXED / FREE)"), code = ask("Code");
                LocalDate expiry = LocalDate.parse(ask("Expiry YYYY-MM-DD"));
                BigDecimal min = amount("Minimum subtotal");
                String district = ask("District (blank for all)");
                boolean first = ask("First order only? y/n").equalsIgnoreCase("y");
                String restriction = district.isBlank() ? null : district;
                Promotion p = switch (type.toUpperCase()) {
                    case "PERCENT" ->
                            new PercentagePromotion(code, expiry, min, restriction, first, amount("Percent"), amount("Maximum discount"));
                    case "FIXED" ->
                            new FixedPromotion(code, expiry, min, restriction, first, amount("Discount amount"));
                    case "FREE" -> new FreeDeliveryPromotion(code, expiry, min, restriction, first);
                    default -> throw new IllegalArgumentException("Unknown promotion type");
                };
                app.addPromotion(p);
            }
            default -> System.out.println("Invalid option");
        }
    }

    public void start() {
        boolean running = true;

        while (running) {
            try {
                printMainMenu();
                int option = number("Choose an option");

                switch (option) {
                    case 1 -> customerArea();
                    case 2 -> restaurantArea();
                    case 3 -> riderArea();
                    case 4 -> reports();
                    case 0 -> running = false;
                    default -> System.out.println("Invalid option");
                }
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n============================================");
        System.out.println(" MASR DELIVERY - Main Menu");
        System.out.println("============================================");
        System.out.println(" 1. Customer");
        System.out.println(" 2. Restaurant");
        System.out.println(" 3. Rider");
        System.out.println(" 4. Admin & Reports");
        System.out.println(" 0. Exit");
        System.out.println("============================================");
    }
}
