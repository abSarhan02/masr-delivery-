public class Order extends BaseEntity {

    public final Customer customer;
    public final Restaurant restaurant;
    public final Address address;
    public final List<Line> lines;
    public final Promotion promotion;
    public final String notes;
    public final double distanceKm;

    // Save when the order was created
    public final LocalDateTime placedAt = LocalDateTime.now();

    // Listeners are notified when the order status changes
    public final List<OrderListener> listeners =
            new ArrayList<>();

    public Status status = Status.PLACED;

    public Rider rider;

    public LocalDateTime deliveredAt, readyAt;

    public boolean paid;

    public BigDecimal subtotal;
    public BigDecimal deliveryFee;
    public BigDecimal serviceFee;
    public BigDecimal discount;
    public BigDecimal total;


    public Order(
            String id,
            Customer c,
            Restaurant r,
            Address a,
            List<Line> lines,
            Promotion p,
            String notes,
            double km) {

        super(id);


        // An order must contain at least one item
        if (lines.isEmpty())
            throw new IllegalArgumentException(
                    "Order needs at least one item"
            );


        // The delivery address must belong to the customer
        if (!c.addresses.contains(a))
            throw new IllegalArgumentException(
                    "Address does not belong to customer"
            );


        if (km < 0)
            throw new IllegalArgumentException(
                    "Invalid distance"
            );


        // Don't allow orders from closed restaurants
        if (!r.open)
            throw new ClosedRestaurantException();


        // Validate every item before creating the order
        for (Line line : lines) {

            if (r.menu.get(line.item().id) != line.item())
                throw new IllegalArgumentException(
                        "Item is not in this restaurant"
                );

            line.item().check(line.quantity());
        }


        customer = c;
        restaurant = r;
        address = a;

        // Keep an immutable copy of the order lines
        this.lines = List.copyOf(lines);

        promotion = p;
        this.notes = notes;
        distanceKm = km;


        // Calculate the subtotal of all items
        subtotal = lines.stream()
                .map(Line::total)
                .reduce(
                        BigDecimal.ZERO.setScale(2),
                        BigDecimal::add
                );


        // Get the shared platform configuration
        Config cfg = Config.get();


        // Calculate delivery cost based on distance
        deliveryFee = cfg.baseFee
                .add(
                        cfg.extraKmFee.multiply(
                                BigDecimal.valueOf(
                                        Math.max(0, km - 3)
                                )
                        )
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );


        // Silver customers get 10% off delivery
        if (c.tier() == Tier.SILVER)
            deliveryFee = deliveryFee
                    .multiply(new BigDecimal("0.90"))
                    .setScale(
                            2,
                            RoundingMode.HALF_UP
                    );


        // Gold customers get free delivery
        if (c.tier() == Tier.GOLD)
            deliveryFee = BigDecimal.ZERO.setScale(2);


        // Calculate the platform service fee
        serviceFee = subtotal
                .multiply(cfg.serviceRate)
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );


        // Apply the promotion if one was selected
        discount = p == null
                ? BigDecimal.ZERO.setScale(2)
                : p.discount(this);


        // Final price can't go below zero
        total = subtotal
                .add(deliveryFee)
                .add(serviceFee)
                .subtract(discount)
                .max(BigDecimal.ZERO.setScale(2));
    }


    public void listen(OrderListener l) {
        listeners.add(l);
    }


    public void change(Status next) {

        // Only allow valid status transitions
        boolean valid = switch (status) {

            case PLACED ->
                    next == Status.ACCEPTED
                            || next == Status.CANCELLED;

            case ACCEPTED ->
                    next == Status.PREPARING
                            || next == Status.CANCELLED;

            case PREPARING ->
                    next == Status.READY
                            || next == Status.CANCELLED;

            case READY ->
                    next == Status.ASSIGNED
                            || next == Status.CANCELLED;

            case ASSIGNED ->
                    next == Status.OUT_FOR_DELIVERY
                            || next == Status.CANCELLED;

            case OUT_FOR_DELIVERY ->
                    next == Status.DELIVERED;

            default -> false;
        };


        if (!valid)
            throw new InvalidStatusException();


        status = next;


        // Save when the order becomes ready
        if (next == Status.READY)
            readyAt = LocalDateTime.now();


        if (next == Status.DELIVERED) {

            deliveredAt = LocalDateTime.now();

            // Completed orders are used for customer tiers
            customer.completedOrders++;
        }


        // Notify everyone listening to this order
        listeners.forEach(
                listener -> listener.changed(this)
        );
    }
}
