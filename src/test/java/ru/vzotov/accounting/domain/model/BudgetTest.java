package ru.vzotov.accounting.domain.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.vzotov.banking.domain.model.AccountNumber;
import ru.vzotov.banking.domain.model.BankRecord;
import ru.vzotov.banking.domain.model.Operation;
import ru.vzotov.banking.domain.model.OperationId;
import ru.vzotov.banking.domain.model.OperationType;
import ru.vzotov.calendar.domain.model.Recurrence;
import ru.vzotov.calendar.domain.model.RecurrenceUnit;
import ru.vzotov.calendar.domain.model.WorkCalendars;
import ru.vzotov.domain.model.Money;
import ru.vzotov.person.domain.model.PersonId;

import javax.script.ScriptException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnit4.class)
public class BudgetTest {

    @Test
    public void testConstruct() throws ScriptException {
        Recurrence recurrence;
        final Set<BudgetRule> rules = new HashSet<>();

        LocalDate start = LocalDate.of(2020, Month.JANUARY, 1);
        LocalDate finish = LocalDate.of(2020, Month.MARCH, 31);
        AccountNumber number = new AccountNumber("40817810108290012345");

        recurrence = new Recurrence(start, finish, RecurrenceUnit.MONTHLY, 1, 7);
        assertThat(recurrence.toString()).isEqualTo("M2020-01-01(1)[7]2020-03-31");
        assertThat(Recurrence.fromString(recurrence.toString())).isEqualTo(recurrence);
        BudgetRule rule = new BudgetRule(
                BudgetRuleId.nextId(),
                BudgetRuleType.INCOME,
                null,
                null,
                number,
                recurrence,
                "Заработная плата",
                Money.rubles(18000.0d),
                new Calculation("value.amount().doubleValue() * (calendar.workdaysBetween(prevMonth.atDay(16), prevMonth.atEndOfMonth()) / calendar.workdaysBetween(prevMonth.atDay(1), prevMonth.atEndOfMonth()));")
        );
        //assertThat(rule.ruleId()).isEqualTo(BudgetRule.ruleIdOf(rule.name()));
        rules.add(rule);

        recurrence = new Recurrence(start, null, RecurrenceUnit.MONTHLY, 1, 22);
        assertThat(recurrence.toString()).isEqualTo("M2020-01-01(1)[22]");
        assertThat(Recurrence.fromString(recurrence.toString())).isEqualTo(recurrence);
        rules.add(new BudgetRule(
                BudgetRuleId.nextId(),
                BudgetRuleType.INCOME,
                null,
                null,
                number,
                recurrence,
                "Аванс",
                Money.rubles(18000.0d),
                new Calculation("value.amount().doubleValue() * (calendar.workdaysBetween(month.atDay(1), month.atDay(16)) / calendar.workdaysBetween(month.atDay(1), month.atEndOfMonth()));")
        ));

        recurrence = new Recurrence(start, null, RecurrenceUnit.WEEKLY);
        assertThat(recurrence.toString()).isEqualTo("W2020-01-01(1)[1]");
        assertThat(Recurrence.fromString(recurrence.toString())).isEqualTo(recurrence);
        rules.add(new BudgetRule(
                BudgetRuleId.nextId(),
                BudgetRuleType.EXPENSE,
                null,
                number,
                null,
                recurrence,
                "Гипермаркет",
                Money.rubles(1500.0d)
        ));

        recurrence = new Recurrence(LocalDate.of(2020, Month.MARCH, 5));
        assertThat(recurrence.toString()).isEqualTo("T(1)2020-03-05[1]");
        assertThat(Recurrence.fromString(recurrence.toString())).isEqualTo(recurrence);
        rules.add(new BudgetRule(
                BudgetRuleId.nextId(),
                BudgetRuleType.EXPENSE,
                null,
                null,
                null,
                recurrence,
                "Покупка чего-то важного",
                Money.rubles(2000.0d)
        ));

        recurrence = new Recurrence(start, null, RecurrenceUnit.MONTHLY, 1, 29);
        assertThat(recurrence.toString()).isEqualTo("M2020-01-01(1)[29]");
        assertThat(Recurrence.fromString(recurrence.toString())).isEqualTo(recurrence);
        rules.add(new BudgetRule(
                BudgetRuleId.nextId(),
                BudgetRuleType.EXPENSE,
                null,
                null,
                null,
                recurrence,
                "Платеж по кредиту",
                Money.rubles(3000.0d)
        ));

        List<BankRecord<?>> operations = new ArrayList<>();
        operations.add(new Operation(
                new OperationId("op-1"),
                LocalDate.of(2020, Month.MARCH, 2),
                Money.rubles(10d),
                OperationType.WITHDRAW,
                number,
                "test operation 1"
        ));

        Budget budget = new Budget(BudgetId.nextId(), PersonId.nextId(),"default", rules);
        List<BudgetBalance> result = budget.calculate(
                WorkCalendars.CALENDAR_2020,
                Collections.emptyList(),
                number,
                LocalDate.of(2020, Month.MARCH, 1),
                LocalDate.of(2020, Month.MARCH, 31),
                operations
        );
        assertThat(result).hasSize(5);
    }

    @Test
    public void testRecurrenceNth() {
        Recurrence recurrence = new Recurrence(LocalDate.of(2020, Month.JANUARY, 2), LocalDate.of(2020, Month.MARCH, 31), RecurrenceUnit.WEEKLY, 3, 3);
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate date = recurrence.start(); recurrence.finish().isAfter(date); date = date.plusDays(1)) {
            if (recurrence.matches(date)) dates.add(date);
        }
        assertThat(dates).isNotEmpty().containsExactly(
                LocalDate.of(2020, 1, 8),
                LocalDate.of(2020, 1, 29),
                LocalDate.of(2020, 2, 19),
                LocalDate.of(2020, 3, 11)
        );
    }
}
