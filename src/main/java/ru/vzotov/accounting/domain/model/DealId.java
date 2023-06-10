package ru.vzotov.accounting.domain.model;

import org.apache.commons.lang3.Validate;
import ru.vzotov.ddd.shared.ValueObject;

import java.util.Objects;
import java.util.UUID;

public class DealId implements ValueObject<DealId> {
    private String value;

    public DealId(String value) {
        Validate.notEmpty(value);
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static DealId nextId() {
        return new DealId(UUID.randomUUID().toString());
    }

    @Override
    public boolean sameValueAs(DealId that) {
        return that != null && Objects.equals(value, that.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DealId that = (DealId) o;
        return sameValueAs(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }

    protected DealId() {
        // for Hibernate
    }
}
