package ru.vzotov.accounting.domain.model;

import org.apache.commons.lang.Validate;
import ru.vzotov.ddd.shared.ValueObject;

import java.util.Objects;
import java.util.UUID;

public class BudgetRuleId implements ValueObject<BudgetRuleId> {

    private String value;

    public BudgetRuleId(String value) {
        Validate.notEmpty(value);
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static BudgetRuleId nextId() {
        return new BudgetRuleId(UUID.randomUUID().toString());
    }

    @Override
    public boolean sameValueAs(BudgetRuleId that) {
        return that != null && Objects.equals(value, that.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BudgetRuleId that = (BudgetRuleId) o;
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

    protected BudgetRuleId() {
        // for Hibernate
    }
}
