package ru.vzotov.accounting.domain.model;

import org.apache.commons.lang3.Validate;
import ru.vzotov.banking.domain.model.BankRecord;
import ru.vzotov.ddd.shared.ValueObject;

import java.util.List;
import java.util.Objects;

/**
 * Класс содержит данные для моделирования движения по счету
 */
public class AccountMovement implements ValueObject<AccountMovement> {

    /**
     * Остаток на счете на начало периода (фактический)
     */
    private Remain start;

    /**
     * Остаток на счете в конце периода (расчетный)
     */
    private Remain finish;

    /**
     * Операции по счету
     */
    private List<BankRecord<?>> operations;

    public AccountMovement(Remain start, Remain finish, List<BankRecord<?>> operations) {
        Validate.notNull(start);
        Validate.notNull(finish);
        Validate.notNull(operations);
        this.start = start;
        this.finish = finish;
        this.operations = operations;
    }

    public Remain start() {
        return start;
    }

    public Remain finish() {
        return finish;
    }

    public List<BankRecord<?>> operations() {
        return operations;
    }

    @Override
    public boolean sameValueAs(AccountMovement that) {
        return that != null && Objects.equals(start, that.start) &&
                Objects.equals(finish, that.finish) &&
                Objects.equals(operations, that.operations);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountMovement that = (AccountMovement) o;
        return sameValueAs(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, finish, operations);
    }

    @Override
    public String toString() {
        return "AccountMovement{" +
                "start=" + start +
                ", finish=" + finish +
                ", operations=" + operations +
                '}';
    }
}
