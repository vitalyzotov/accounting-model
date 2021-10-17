package ru.vzotov.accounting.domain.model;

import org.apache.commons.lang.Validate;
import ru.vzotov.banking.domain.model.AccountNumber;
import ru.vzotov.banking.domain.model.BudgetCategoryId;
import ru.vzotov.calendar.domain.model.Recurrence;
import ru.vzotov.calendar.domain.model.WorkCalendar;
import ru.vzotov.ddd.shared.ValueObject;
import ru.vzotov.domain.model.Money;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Правило бюджета
 */
public class BudgetRule implements ValueObject<BudgetRule> {

    /**
     * Идентификатор правила
     */
    private BudgetRuleId ruleId;

    /**
     * Тип правила (доход/расход)
     */
    private BudgetRuleType type;

    /**
     * Название правила
     */
    private String name;

    /**
     * Номер счета, с которого должно произойти списание
     */
    private AccountNumber sourceAccount;

    /**
     * Номер счета, на который должно произойти начисление
     */
    private AccountNumber targetAccount;

    /**
     * Категория бюджета
     */
    private BudgetCategoryId categoryId;

    /**
     * Повторение
     */
    private Recurrence recurrence;

    /**
     * Базовая сумма
     */
    private Money value;

    /**
     * Расчет суммы
     */
    private Calculation calculation;

    /**
     * Флаг актуальности. Можно временно включать/выключать правила.
     */
    private boolean enabled;

    public BudgetRule(BudgetRuleId ruleId, BudgetRuleType type, BudgetCategoryId categoryId, AccountNumber sourceAccount, AccountNumber targetAccount, Recurrence recurrence, String name, Money value) {
        this(ruleId, type, categoryId, sourceAccount, targetAccount, recurrence, name, value, null);
    }

    public BudgetRule(BudgetRuleId ruleId, BudgetRuleType type, BudgetCategoryId categoryId, AccountNumber sourceAccount, AccountNumber targetAccount, Recurrence recurrence, String name, Money value, Calculation calculation) {
        this(ruleId, type, categoryId, sourceAccount, targetAccount, recurrence, name, value, calculation, true);
    }

    public BudgetRule(BudgetRuleId ruleId, BudgetRuleType type, BudgetCategoryId categoryId, AccountNumber sourceAccount, AccountNumber targetAccount, Recurrence recurrence, String name, Money value, Calculation calculation, boolean enabled) {
        Validate.notNull(ruleId);
        Validate.notNull(type);
        Validate.notNull(name);
        Validate.notNull(value);
        Validate.notNull(recurrence);
        this.ruleId = ruleId;
        this.type = type;
        this.categoryId = categoryId;
        this.sourceAccount = sourceAccount;
        this.targetAccount = targetAccount;
        this.name = name;
        this.value = value;
        this.recurrence = recurrence;
        this.calculation = calculation;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public BudgetRuleType type() {
        return type;
    }

    public String name() {
        return name;
    }

    public BudgetCategoryId categoryId() {
        return categoryId;
    }

    public AccountNumber sourceAccount() {
        return sourceAccount;
    }

    public AccountNumber targetAccount() {
        return targetAccount;
    }

    public Money value() {
        return value;
    }

    public Calculation calculation() {
        return calculation;
    }

    public Recurrence recurrence() {
        return recurrence;
    }

    public boolean matches(LocalDate day, WorkCalendar calendar) {
        return enabled && recurrence.matches(day, calendar);
    }

    public BudgetRuleId ruleId() {
        return ruleId;
    }

    @Override
    public boolean sameValueAs(BudgetRule rule) {
        return rule != null &&
                Objects.equals(ruleId, rule.ruleId) &&
                Objects.equals(type, rule.type) &&
                Objects.equals(name, rule.name) &&
                Objects.equals(categoryId, rule.categoryId) &&
                Objects.equals(sourceAccount, rule.sourceAccount) &&
                Objects.equals(targetAccount, rule.targetAccount) &&
                Objects.equals(recurrence, rule.recurrence) &&
                Objects.equals(value, rule.value) &&
                Objects.equals(calculation, rule.calculation);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BudgetRule rule = (BudgetRule) o;
        return sameValueAs(rule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ruleId, type, name, categoryId, sourceAccount, targetAccount, recurrence, value, calculation);
    }

    @Override
    public String toString() {
        return "rule{" +
                "" + type.symbol() +
                ", " + name +
                ", source=" + sourceAccount +
                ", target=" + targetAccount +
                ", categoryId=" + categoryId +
                ", recurrence=" + recurrence +
                ", value=" + value +
                ", calculation=" + calculation +
                ", ruleId=" + ruleId +
                ", id=" + id +
                '}';
    }

    protected BudgetRule() {
        // for Hibernate
    }

    private Long id; // surrogate key
}
