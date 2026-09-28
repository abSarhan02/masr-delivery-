package promotion;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

import java.math.*;
import java.time.*;

public class PercentagePromotion extends Promotion {
    public final BigDecimal percent, cap;

    public PercentagePromotion(String c, LocalDate e, BigDecimal m, String d, boolean f, BigDecimal p, BigDecimal cap) {
        super(c, e, m, d, f);
        this.percent = p;
        this.cap = cap;
    }

    public BigDecimal calculate(Order o) {
        return o.subtotal.multiply(percent).divide(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP).min(cap);
    }
}
