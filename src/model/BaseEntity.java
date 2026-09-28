package model;

import model.*;
import promotion.*;
import exception.*;
import factory.*;
import service.*;

import java.util.*;

public abstract class BaseEntity {
    public final String id;

    public BaseEntity(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("ID is required");
        this.id = id;
    }

    @Override
    public final boolean equals(Object o) {
        return o != null && getClass() == o.getClass() && id.equals(((BaseEntity) o).id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), id);
    }
}
