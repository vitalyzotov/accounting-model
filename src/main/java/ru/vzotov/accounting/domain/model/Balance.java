package ru.vzotov.accounting.domain.model;

import org.apache.commons.lang.Validate;
import ru.vzotov.banking.domain.model.Operation;
import ru.vzotov.ddd.shared.ValueObject;
import ru.vzotov.domain.model.Money;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Финансовый баланс.
 */
public class Balance implements ValueObject<Balance> {

    /**
     * Остаток по счету, от которого проводим расчет баланса
     */
    private Remain remain;

    /**
     * Значение баланса
     */
    private Money value;

    /**
     * Операции, учитываемые при расчете баланса
     */
    private Collection<Operation> operations;

    public Balance(Remain remain, Collection<Operation> operations) {
        Validate.notNull(remain);
        Validate.notNull(operations);

        this.remain = remain;
        this.operations = Collections.unmodifiableCollection(operations);
        this.value = doCalculate();
    }

    public Remain remain() {
        return remain;
    }

    public Money value() {
        return value;
    }

    public Collection<Operation> operations() {
        return operations;
    }

    private Money doCalculate() {
        Money result = remain.value();
        for (Operation op : operations) {
            if (op.date().isBefore(remain.date())) {
                // пропускаем операции, выполненные до подсчета остатка
                continue;
            }
            Money v = op.amount();
            Validate.isTrue(v.currency().equals(result.currency()));
            switch (op.type()) {
                case DEPOSIT:
                    result = result.add(v);
                    break;

                case WITHDRAW:
                    result = result.subtract(v);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown operation type");
            }
        }
        return result;
    }

    @Override
    public boolean sameValueAs(Balance balance) {
        return balance != null && Objects.equals(remain, balance.remain) && Objects.equals(operations, balance.operations);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Balance that = (Balance) o;
        return sameValueAs(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(remain, operations);
    }

}
