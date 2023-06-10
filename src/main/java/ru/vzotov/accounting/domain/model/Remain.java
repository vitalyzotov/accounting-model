package ru.vzotov.accounting.domain.model;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import ru.vzotov.banking.domain.model.AccountNumber;
import ru.vzotov.ddd.shared.AggregateRoot;
import ru.vzotov.ddd.shared.Entity;
import ru.vzotov.domain.model.Money;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Остаток по счету на дату
 */
@AggregateRoot
public class Remain implements Entity<Remain> {

    private RemainId remainId;
    private LocalDate date;
    private AccountNumber account;
    private Money value;

    public Remain(AccountNumber account, LocalDate date, Money value) {
        Validate.notNull(account);
        Validate.notNull(date);
        Validate.notNull(value);
        this.remainId = new RemainId(account, date);
        this.date = date;
        this.account = account;
        this.value = value;
    }

    public LocalDate date() {
        return date;
    }

    public RemainId remainId() {
        return remainId;
    }

    public AccountNumber account() {
        return account;
    }

    public Money value() {
        return value;
    }

    @Override
    public boolean sameIdentityAs(Remain other) {
        return other != null && new EqualsBuilder()
                .append(remainId, other.remainId)
                .isEquals();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Remain other = (Remain) o;
        return sameIdentityAs(other);
    }

    @Override
    public int hashCode() {
        return Objects.hash(remainId);
    }

    protected Remain() {
        //for Hibernate
    }

    /**
     * Surrogate key
     */
    private Long id;
}
