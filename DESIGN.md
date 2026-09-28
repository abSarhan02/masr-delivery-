# Masr Delivery - Design Notes

I kept the project simple and used standard Java only.

## Collections

- `HashMap` is used for restaurants, customers, riders and orders because they are searched by ID.
- `LinkedHashMap` is used for a restaurant menu because it keeps the insertion order.
- `Set` is used for cuisines because the same cuisine should not appear twice.
- `PriorityQueue` is used for READY orders. Gold customers have priority, then the oldest order is used.
- `ArrayDeque` is used for the last five customer searches. When a sixth search is added, the oldest one is removed.
- Methods that return internal lists use copies so the caller cannot modify the original collection.

## Money and dates

I used `BigDecimal` for EGP values because using `double` for money can give rounding problems.
I used `LocalDate`, `LocalDateTime` and `YearMonth` instead of the old date classes.

## Design patterns

### Strategy / polymorphism

Each promotion has its own calculation. I did not put all promotion types inside one large `if` or `switch` in `Order`.

The menu items also use polymorphism. `StandardItem`, `ComboItem` and `WeightedItem` all extend `MenuItem` and calculate their own price.

### Builder

`OrderBuilder` creates an order step by step. This avoids a constructor with too many parameters.

### Factory

`ItemFactory` creates the correct menu item from a type value. This keeps the type decision in one place.

### Observer

`OrderListener` is called when an order status changes. This allows other actions to react to the change without putting them directly inside the order.

### Singleton

`Config` has one instance and contains the delivery and service fee values. I used it because the assignment asks for one shared configuration.

### Rider strategy

`Motorcycle`, `Bicycle` and `Car` implement `Vehicle`. Each one has its own delivery limits. A new vehicle can be added by creating another class.

## Exceptions

The platform exceptions extend `RuntimeException`.
I used unchecked exceptions because these are validation/business errors handled by the console application. The program catches them and prints a clear message.

## Test data

`DemoData` loads customers, restaurants, all three item types, riders and promotions when the program starts.
Order `O1` is the worked example from the assignment:

- subtotal: 240.00 EGP
- distance: 12 km
- Bronze customer
- promotion: NILE20
- expected total: 258.00 EGP
