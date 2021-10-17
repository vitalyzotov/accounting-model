package ru.vzotov.accounting.domain.model;

import ru.vzotov.banking.domain.model.AccountNumber;
import ru.vzotov.ddd.shared.ValueObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Objects;

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.YEAR;

/**
 * Идентификатор остатка.
 * Остатки идентифицируем по номеру счета и дате.
 * Длина идентификатора: номер_счета(20)_дата(8)=29
 */
public class RemainId implements ValueObject<RemainId> {

    private static DateTimeFormatter ID_DATE_FORMAT = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendValue(YEAR, 4)
            .appendValue(MONTH_OF_YEAR, 2)
            .appendValue(DAY_OF_MONTH, 2)
            .toFormatter();

    private String value;

    public RemainId(String value) {
        this.value = value;
    }

    public RemainId(AccountNumber accountNumber, LocalDate dateTime) {
        this(accountNumber.number() + "_" + ID_DATE_FORMAT.format(dateTime));
    }

    public String value() {
        return value;
    }

    @Override
    public boolean sameValueAs(RemainId that) {
        return that != null && Objects.equals(value, that.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RemainId that = (RemainId) o;
        return sameValueAs(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    protected RemainId() {
    }
}
