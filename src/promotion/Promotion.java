package promotion;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

import java.math.*;
import java.time.*;

public abstract class Promotion extends BaseEntity {
    public final LocalDate expiry;
    final BigDecimal minimum;
    final String district;
    final boolean firstOrderOnly;

    public Promotion(String code, LocalDate expiry, BigDecimal minimum, String district, boolean first) {
        super(code.toUpperCase());
        this.expiry = expiry;
        this.minimum = minimum;
        this.district = district;
        this.firstOrderOnly = first;
    }

    public BigDecimal discount(Order o) {
        if (expiry.isBefore(LocalDate.now())) throw new InvalidPromotionException("Promotion has expired");
        if (o.subtotal.compareTo(minimum) < 0) throw new InvalidPromotionException("Minimum subtotal is " + minimum);
        if (district != null && !district.equalsIgnoreCase(o.address.district()))
            throw new InvalidPromotionException("Wrong district");
        if (firstOrderOnly && o.customer.completedOrders > 0) throw new InvalidPromotionException("First order only");
        return calculate(o);
    }

    public abstract BigDecimal calculate(Order o);
}
