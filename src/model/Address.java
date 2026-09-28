package model;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

public record Address(String district, String detail) {
    public Address {
        if (district == null || district.isBlank() || detail == null || detail.isBlank())
            throw new IllegalArgumentException("Invalid address");
    }
}
