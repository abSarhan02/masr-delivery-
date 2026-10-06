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

    // A customer can have multiple delivery addresses
    public final List<Address> addresses = new ArrayList<>();

    // Keep the most recent searches
    public final Deque<String> searches = new ArrayDeque<>();

    public BigDecimal wallet;
    public int completedOrders;


    public Customer(
            String id,
            String name,
            String mobile,
            BigDecimal wallet) {

        super(id);

        // Validate Egyptian mobile numbers
        if (!mobile.matches("(010|011|012|015)[0-9]{8}"))
            throw new IllegalArgumentException(
                    "Invalid Egyptian mobile number"
            );

        if (wallet.signum() < 0)
            throw new IllegalArgumentException("Negative wallet");

        this.name = name;
        this.mobile = mobile;
        this.wallet = wallet;
    }


    public Tier tier() {

        // Customer tier depends on completed orders
        return completedOrders >= 30
                ? Tier.GOLD
                : completedOrders >= 10
                ? Tier.SILVER
                : Tier.BRONZE;
    }


    public void addAddress(Address a) {

        // Avoid adding the same address twice
        if (!addresses.contains(a))
            addresses.add(a);
    }


    public List<Address> addresses() {

        // Return a copy to protect the original list
        return List.copyOf(addresses);
    }


    public void search(String q) {

        // Add the new search at the beginning
        searches.addFirst(q);

        // Store only the last 5 searches
        if (searches.size() > 5)
            searches.removeLast();
    }


    public List<String> lastSearches() {
        return List.copyOf(searches);
    }


    public void debit(BigDecimal a) {

        // Check the wallet before completing the payment
        if (wallet.compareTo(a) < 0)
            throw new InsufficientWalletException();

        wallet = wallet.subtract(a);
    }


    public void refund(BigDecimal a) {
        wallet = wallet.add(a);
    }
}
