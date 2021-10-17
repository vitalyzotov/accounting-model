package ru.vzotov.accounting.domain.model;

import org.apache.commons.lang.Validate;
import ru.vzotov.banking.domain.model.AccountNumber;
import ru.vzotov.banking.domain.model.BudgetCategoryId;
import ru.vzotov.ddd.shared.AggregateRoot;
import ru.vzotov.ddd.shared.Entity;
import ru.vzotov.domain.model.Money;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Статья баланса бюджета.
 * Формируется по правилу бюджета <code>BudgetRule</code>.
 */
@AggregateRoot
public class BudgetPlan implements Entity<BudgetPlan> {

    private BudgetPlanId itemId;

    private BudgetDirection direction;

    private AccountNumber source;

    private AccountNumber target;

    private BudgetCategoryId category;

    private Money value;

    private BudgetRule rule;

    private LocalDate date;

    public BudgetPlan(BudgetPlanId itemId, BudgetRule rule, LocalDate date, BudgetDirection direction, Money value) {
        this(itemId, rule, date, direction, value, null, null);
    }

    public BudgetPlan(BudgetPlanId itemId, BudgetRule rule, LocalDate date, BudgetDirection direction, Money value, BudgetCategoryId category) {
        this(itemId, rule, date, direction, value, null, null, category);
    }

    public BudgetPlan(BudgetPlanId itemId, BudgetRule rule, LocalDate date, BudgetDirection direction, Money value, AccountNumber source, AccountNumber target) {
        this(itemId, rule, date, direction, value, source, target, null);
    }

    public BudgetPlan(BudgetPlanId itemId, BudgetRule rule, LocalDate date, BudgetDirection direction, Money value, AccountNumber source, AccountNumber target, BudgetCategoryId category) {
        Validate.notNull(itemId);
        Validate.notNull(rule);
        Validate.notNull(direction);
        Validate.notNull(value);

        this.itemId = itemId;
        this.rule = rule;
        this.date = date;
        this.source = source;
        this.target = target;
        this.category = category;
        this.direction = direction;
        this.value = value;
    }

    public BudgetPlan withData(BudgetRule rule, LocalDate date, BudgetDirection direction, Money value,
                               AccountNumber source, AccountNumber target, BudgetCategoryId category) {
        BudgetPlan result = new BudgetPlan(itemId, rule, date, direction, value, source, target, category);
        result.id = id;
        return result;
    }

    public BudgetPlanId itemId() {
        return itemId;
    }

    public BudgetRule rule() {
        return rule;
    }

    public LocalDate date() {
        return date;
    }

    public AccountNumber source() {
        return source;
    }

    public AccountNumber target() {
        return target;
    }

    public BudgetCategoryId category() {
        return category;
    }

    public BudgetDirection direction() {
        return direction;
    }

    public Money value() {
        return value;
    }

    @Override
    public boolean sameIdentityAs(BudgetPlan that) {
        return that != null &&
                Objects.equals(itemId, that.itemId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BudgetPlan that = (BudgetPlan) o;
        return sameIdentityAs(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }

    protected BudgetPlan() {
        //for Hibernate
    }

    private Long id; // surrogate key

}
