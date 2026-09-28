package model;

import java.math.BigDecimal;

public class Config {
    private static final Config INSTANCE = new Config();

    public final BigDecimal baseFee = new BigDecimal("15.00");
    public final BigDecimal extraKmFee = new BigDecimal("3.00");
    public final BigDecimal serviceRate = new BigDecimal("0.10");
    final int poolSize = 4;
    final String dataPath = "data";

    private Config() {
    }

    public static Config get() {
        return INSTANCE;
    }
}
