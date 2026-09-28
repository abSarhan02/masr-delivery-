package promotion;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

import java.math.*;
import java.time.*;

public class FixedPromotion extends Promotion {
    public final BigDecimal amount;

    public FixedPromotion(String c, LocalDate e, BigDecimal m, String d, boolean f, BigDecimal a) {
        super(c, e, m, d, f);
        amount = a;
    }

    public BigDecimal calculate(Order o) {
        return amount.min(o.subtotal);
    }
}
