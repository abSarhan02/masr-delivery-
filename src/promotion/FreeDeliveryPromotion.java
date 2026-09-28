package promotion;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

import java.math.*;
import java.time.*;

public class FreeDeliveryPromotion extends Promotion {
    public FreeDeliveryPromotion(String c, LocalDate e, BigDecimal m, String d, boolean f) {
        super(c, e, m, d, f);
    }

    public BigDecimal calculate(Order o) {
        o.deliveryFee = BigDecimal.ZERO.setScale(2);
        return BigDecimal.ZERO.setScale(2);
    }
}
