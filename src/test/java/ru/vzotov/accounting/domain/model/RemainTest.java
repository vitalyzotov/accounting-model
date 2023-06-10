package ru.vzotov.accounting.domain.model;

import org.junit.jupiter.api.Test;
import ru.vzotov.banking.domain.model.AccountNumber;
import ru.vzotov.domain.model.Money;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

public class RemainTest {

    @Test
    public void testConstruct() {
        final Remain remain1 = new Remain(
                new AccountNumber("40817810108290012345"),
                LocalDate.of(2018, Month.JANUARY, 1),
                Money.rubles(10000d)
        );
        final Remain remain2 = new Remain(
                new AccountNumber("40817810108290012345"),
                LocalDate.of(2018, Month.JANUARY, 1),
                Money.rubles(20000d)
        );
        assertThat(remain1.remainId().value()).isEqualTo("40817810108290012345_20180101");
        assertThat(remain1).isEqualTo(remain2);
    }
}
