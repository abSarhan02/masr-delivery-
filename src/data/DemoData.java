package data;

import model.*;
import promotion.*;
import service.Platform;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class DemoData {

    private DemoData() {
    }

    public static void load(Platform platform) {
        addCustomers(platform);
        addRestaurants(platform);
        addRiders(platform);
        addPromotions(platform);
        addExampleOrder(platform);
    }

    private static void addCustomers(Platform platform) {
        Customer ahmed = new Customer("C1", "Ahmed Hassan", "01012345678", new BigDecimal("1500.00"));
        ahmed.addAddress(new Address("Faisal", "Al Haram Street"));
        platform.addCustomer(ahmed);

        Customer sara = new Customer("C2", "Sara Ali", "01123456789", new BigDecimal("900.00"));
        sara.addAddress(new Address("Dokki", "Tahrir Street"));
        sara.completedOrders = 12; // Silver customer for testing
        platform.addCustomer(sara);

        Customer omar = new Customer("C3", "Omar Adel", "01234567890", new BigDecimal("2200.00"));
        omar.addAddress(new Address("Heliopolis", "El Hegaz Street"));
        omar.completedOrders = 31; // Gold customer for testing
        platform.addCustomer(omar);
    }

    private static void addRestaurants(Platform platform) {
        Restaurant nileKitchen = new Restaurant(
                "R1", "Nile Kitchen", "Maadi", Set.of("Egyptian", "Grill"), 4.7);

        StandardItem meal = new StandardItem(
                "I1", "Egyptian Meal", "Main", new BigDecimal("240.00"), 20, 30);
        StandardItem rice = new StandardItem(
                "I2", "Rice", "Sides", new BigDecimal("35.00"), 10, 40);
        WeightedItem kofta = new WeightedItem(
                "I3", "Grilled Kofta", "Grill", new BigDecimal("320.00"), 25, 20);
        ComboItem familyCombo = new ComboItem(
                "I4", "Family Combo", "Combo", List.of(meal, rice), new BigDecimal("10"), 30, 10);

        nileKitchen.addItem(meal);
        nileKitchen.addItem(rice);
        nileKitchen.addItem(kofta);
        nileKitchen.addItem(familyCombo);
        platform.addRestaurant(nileKitchen);

        Restaurant cairoBites = new Restaurant(
                "R2", "Cairo Bites", "Dokki", Set.of("Fast Food"), 4.3);
        cairoBites.addItem(new StandardItem(
                "I1", "Chicken Burger", "Burgers", new BigDecimal("95.00"), 15, 25));
        cairoBites.addItem(new StandardItem(
                "I2", "Fries", "Sides", new BigDecimal("40.00"), 8, 35));
        platform.addRestaurant(cairoBites);

        Restaurant levant = new Restaurant(
                "R3", "Levant House", "Nasr City", Set.of("Syrian", "Grill"), 4.8);
        levant.addItem(new StandardItem(
                "I1", "Chicken Shawarma", "Sandwiches", new BigDecimal("85.00"), 12, 30));
        platform.addRestaurant(levant);
    }

    private static void addRiders(Platform platform) {
        Rider rider1 = new Rider("D1", "Mahmoud", new Motorcycle(), "Maadi");
        rider1.onDuty = true;
        platform.addRider(rider1);

        Rider rider2 = new Rider("D2", "Youssef", new Bicycle(), "Dokki");
        rider2.onDuty = true;
        platform.addRider(rider2);

        Rider rider3 = new Rider("D3", "Khaled", new Car(), "Nasr City");
        platform.addRider(rider3);
    }

    private static void addPromotions(Platform platform) {
        platform.addPromotion(new PercentagePromotion(
                "NILE20", LocalDate.now().plusMonths(6), BigDecimal.ZERO,
                null, false, new BigDecimal("20"), new BigDecimal("50")));

        platform.addPromotion(new FixedPromotion(
                "SAVE30", LocalDate.now().plusMonths(6), new BigDecimal("150"),
                null, false, new BigDecimal("30")));

        platform.addPromotion(new FreeDeliveryPromotion(
                "FREEDEL", LocalDate.now().plusMonths(6), new BigDecimal("100"),
                null, false));
    }

    private static void addExampleOrder(Platform platform) {
        Customer customer = platform.customers.get("C1");
        Restaurant restaurant = platform.restaurants.get("R1");
        MenuItem item = restaurant.menu.get("I1");
        Promotion promo = platform.promotions.get("NILE20");

        // This is the worked example from the assignment: total must be 258 EGP
        Order order = new OrderBuilder(
                "O1", customer, restaurant, customer.addresses().get(0), 12)
                .add(item, BigDecimal.ONE)
                .promotion(promo)
                .notes("Demo order")
                .build();

        platform.place(order);
    }
}
