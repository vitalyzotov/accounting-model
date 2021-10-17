package ru.vzotov.accounting.domain.model;

import org.apache.commons.lang.Validate;
import ru.vzotov.ddd.shared.ValueObject;
import ru.vzotov.domain.model.Money;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Расчетный баланс бюджета за период (обычно за неделю).
 * В БД не хранится.
 */
public class BudgetBalance implements ValueObject<BudgetBalance> {

    /**
     * Первый день периода
     */
    private final LocalDate from;

    /**
     * Последний день периода
     */
    private final LocalDate to;

    /**
     * Статьи баланса
     */
    private final List<BudgetPlan> items;

    /**
     * Расчетные остатки по счетам на конец периода
     */
    private final List<Remain> remains;

    private final List<AccountMovement> movements;

    public BudgetBalance(LocalDate from, LocalDate to, List<BudgetPlan> items, List<Remain> remains, List<AccountMovement> movements) {
        Validate.notNull(from);
        Validate.notNull(to);
        Validate.isTrue(!from.isAfter(to));
        this.from = from;
        this.to = to;
        this.items = new ArrayList<>(items);
        this.remains = new ArrayList<>(remains);
        this.movements = new ArrayList<>(movements);
    }

    public LocalDate from() {
        return from;
    }

    public LocalDate to() {
        return to;
    }

    public List<Remain> remains() {
        return remains;
    }

    public boolean isFuture() {
        return from.isAfter(LocalDate.now());
    }

    public boolean isCurrent() {
        LocalDate now = LocalDate.now();
        return !(now.isBefore(from) || now.isAfter(to));
    }

    public boolean isPast() {
        return to.isBefore(LocalDate.now());
    }

    public Money incomes() {
        return items.stream()
                .filter(i -> BudgetDirection.INCOME.equals(i.direction()))
                .map(BudgetPlan::value)
                .reduce(Money::add).orElse(null);
    }

    public Money expenses() {
        return items.stream()
                .filter(i -> BudgetDirection.EXPENSE.equals(i.direction()))
                .map(BudgetPlan::value)
                .reduce(Money::add).orElse(null);
    }

    public Money balance() {
        Money incomes = incomes();
        Money expenses = expenses();
        if (incomes == null && expenses != null) incomes = Money.ofRaw(0, expenses.currency());
        if (incomes != null && expenses == null) expenses = Money.ofRaw(0, incomes.currency());
        return incomes != null ? incomes.subtract(expenses) : null;
    }

    public List<BudgetPlan> items() {
        return items;
    }

    public List<AccountMovement> movements() {
        return movements;
    }

    @Override
    public boolean sameValueAs(BudgetBalance that) {
        return that != null && Objects.equals(from, that.from) &&
                Objects.equals(to, that.to) &&
                Objects.equals(items, that.items);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BudgetBalance that = (BudgetBalance) o;
        return sameValueAs(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, items);
    }

    @Override
    public String toString() {
        return "BudgetBalance{" +
                "from=" + from +
                ", to=" + to +
                '}';
    }
}
