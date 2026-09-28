package model;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

import java.math.*;
import java.util.*;

public class Customer extends BaseEntity {
    public final String name, mobile;
    public final List<Address> addresses = new ArrayList<>();
    public final Deque<String> searches = new ArrayDeque<>();
    public BigDecimal wallet;
    public int completedOrders;

    public Customer(String id, String name, String mobile, BigDecimal wallet) {
        super(id);
        if (!mobile.matches("(010|011|012|015)[0-9]{8}"))
            throw new IllegalArgumentException("Invalid Egyptian mobile number");
        if (wallet.signum() < 0) throw new IllegalArgumentException("Negative wallet");
        this.name = name;
        this.mobile = mobile;
        this.wallet = wallet;
    }

    public Tier tier() {
        return completedOrders >= 30 ? Tier.GOLD : completedOrders >= 10 ? Tier.SILVER : Tier.BRONZE;
    }

    public void addAddress(Address a) {
        if (!addresses.contains(a)) addresses.add(a);
    }

    public List<Address> addresses() {
        return List.copyOf(addresses);
    }

    public void search(String q) {
        searches.addFirst(q);
        if (searches.size() > 5) searches.removeLast();
    }

    public List<String> lastSearches() {
        return List.copyOf(searches);
    }

    public void debit(BigDecimal a) {
        if (wallet.compareTo(a) < 0) throw new InsufficientWalletException();
        wallet = wallet.subtract(a);
    }

    public void refund(BigDecimal a) {
        wallet = wallet.add(a);
    }
}
