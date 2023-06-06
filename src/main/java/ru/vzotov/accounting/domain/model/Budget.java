package ru.vzotov.accounting.domain.model;

import org.apache.commons.lang3.Validate;
import ru.vzotov.banking.domain.model.AccountNumber;
import ru.vzotov.banking.domain.model.BankRecord;
import ru.vzotov.calendar.domain.model.WorkCalendar;
import ru.vzotov.ddd.shared.AggregateRoot;
import ru.vzotov.ddd.shared.Entity;
import ru.vzotov.domain.model.Money;
import ru.vzotov.person.domain.model.PersonId;

import javax.script.ScriptException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Budget
 */
@AggregateRoot
public class Budget implements Entity<Budget> {

    private static final String LOCALE_DEFAULT = Locale.US.getLanguage();

    private static final Currency CURRENCY_DEFAULT = Currency.getInstance("RUR");

    /**
     * Unique identifier
     */
    private BudgetId budgetId;

    /**
     * Name of the budget
     */
    private String name;

    /**
     * Budget rules
     */
    private Set<BudgetRule> rules = new HashSet<>();

    /**
     * Locale to detect first day of week and other location-specific things
     */
    private String locale;

    /**
     * Default currency
     */
    private Currency currency;

    /**
     * Owner of the budget
     */
    private PersonId owner;

    public Budget(BudgetId budgetId, PersonId owner, String name, Set<BudgetRule> rules) {
        this(budgetId, owner, name, rules, CURRENCY_DEFAULT, LOCALE_DEFAULT);
    }

    public Budget(BudgetId budgetId, PersonId owner, String name, Set<BudgetRule> rules, Currency currency, String locale) {
        Validate.notNull(budgetId);
        Validate.notNull(owner);
        Validate.notEmpty(name);
        Validate.notNull(rules);
        Validate.notNull(currency);
        Validate.notNull(locale);

        this.budgetId = budgetId;
        this.owner = owner;
        this.name = name;
        this.rules = new HashSet<>(rules);
        this.currency = currency;
        this.locale = locale;
    }

    public BudgetId budgetId() {
        return budgetId;
    }

    public PersonId owner() {
        return owner;
    }

    public String name() {
        return name;
    }

    public Currency currency() {
        return currency;
    }

    public String locale() {
        return locale;
    }

    public Set<BudgetRule> rules() {
        return Collections.unmodifiableSet(rules);
    }

    public void addRule(BudgetRule rule) {
        this.rules.add(rule);
    }

    public void deleteRule(BudgetRule rule) {
        this.rules.remove(rule);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public List<BudgetBalance> calculate(
            WorkCalendar calendar,
            List<Remain> actualRemains,
            AccountNumber defaultAccount,
            LocalDate start,
            LocalDate finish) throws ScriptException {
        return calculate(
                calendar,
                actualRemains,
                defaultAccount,
                start,
                finish,
                Collections.emptyList()
        );
    }

    /**
     * Calculate budget within specified time range according to budget rules.
     *
     * @param calendar       work calendar for the time range
     * @param actualRemains  known remains for accounts. If remain was not listed here, then it will be assumed that account remain is zero.
     * @param defaultAccount default account number that will be used if budget rule doesn't contain any account number for deposit or withdraw
     * @param start          first day of time range (inclusive)
     * @param finish         last day of time range (inclusive)
     * @return list of budget balance entries for each week of time range
     * @throws ScriptException in case of calculation error
     */
    public List<BudgetBalance> calculate(
            WorkCalendar calendar,
            List<Remain> actualRemains,
            AccountNumber defaultAccount,
            LocalDate start,
            LocalDate finish,
            List<BankRecord<?>> operations) throws ScriptException {
        Validate.isTrue(!calendar.from().isAfter(start) && !calendar.to().isBefore(finish), "Calendar must include the calculated time range");

        final WeekFields weekFields = WeekFields.of(new Locale(this.locale));
        final List<BudgetBalance> result = new ArrayList<>();
        final List<Remain> knownRemains = actualRemains.stream()
                .filter(remain -> !remain.date().isAfter(finish))
                .sorted(Comparator.comparing(Remain::date).reversed())
                .toList();

        final List<BankRecord<?>> knownOperations = operations.stream()
                .filter(op -> !(op.recorded().isAfter(finish) || op.recorded().isBefore(start)))
                .toList();

        final Map<AccountNumber, Remain> currentRemains = new HashMap<>();

        for (LocalDate date = start; !date.isAfter(finish); date = date.plusWeeks(1)) {

            // calculate time range of current period
            final LocalDate weekStart = date.isEqual(start) ? start : date.with(weekFields.dayOfWeek(), 1L);
            final LocalDate weekLastDay = date.with(weekFields.dayOfWeek(), 7L);
            final LocalDate weekEnd = weekLastDay.isAfter(finish) ? finish : weekLastDay;

            // calculate remains for current period
            knownRemains
                    .stream()
                    .filter(remain -> !remain.date().isAfter(weekStart)) // use remains with date before end of period
                    .forEach(remain -> currentRemains.merge(remain.account(), remain, (a, b) -> a.date().isAfter(b.date()) ? a : b)); // use most actual remains

            // currentRemains contain remains for start of period
            // put them into startRemains
            final Map<AccountNumber, Remain> startRemains = new HashMap<>(currentRemains);

            // calculate rules
            final Map<BudgetRule, Money> calculation = calculateRules(calendar, weekStart, weekEnd);

            final LocalDate itemDate = date;
            final List<BudgetPlan> items = calculation.entrySet().stream()
                    .map(entry -> {
                        final BudgetRule rule = entry.getKey();
                        return new BudgetPlan(
                                BudgetPlanId.nextId(),
                                rule,
                                itemDate,
                                BudgetDirection.of(rule.type().symbol()),
                                entry.getValue(),
                                rule.sourceAccount(),
                                rule.targetAccount(),
                                rule.categoryId(),
                                rule.purchaseCategoryId()
                        );
                    })
                    .peek(item -> {
                        // calculate remains for end of period
                        final AccountNumber source = item.source() == null ? defaultAccount : item.source();
                        final AccountNumber target = item.target() == null ? defaultAccount : item.target();
                        Remain sourceRemain = currentRemains.get(source);
                        if (sourceRemain == null) {
                            sourceRemain = new Remain(source, weekEnd, Money.ofRaw(0, item.value().currency()));
                        }
                        Remain targetRemain = currentRemains.get(target);
                        if (targetRemain == null) {
                            targetRemain = new Remain(target, weekEnd, Money.ofRaw(0, item.value().currency()));
                        }
                        switch (item.direction()) {
                            case INCOME -> {
                                targetRemain = new Remain(target, weekEnd, targetRemain.value().add(item.value()));
                                currentRemains.put(target, targetRemain);
                            }
                            case EXPENSE -> {
                                sourceRemain = new Remain(source, weekEnd, sourceRemain.value().subtract(item.value()));
                                currentRemains.put(source, sourceRemain);
                            }
                            case MOVE -> {
                                sourceRemain = new Remain(source, weekEnd, sourceRemain.value().subtract(item.value()));
                                currentRemains.put(source, sourceRemain);
                                targetRemain = new Remain(target, weekEnd, targetRemain.value().add(item.value()));
                                currentRemains.put(target, targetRemain);
                            }
                            default -> throw new IllegalArgumentException();
                        }
                    })
                    .collect(Collectors.toList());

            // calculate flow of funds for all accounts
            final Map<AccountNumber, AccountMovement> movements = new HashMap<>();
            final Map<AccountNumber, List<BankRecord<?>>> weekOperations = knownOperations.stream()
                    .filter(op -> !(op.recorded().isAfter(weekEnd) || op.recorded().isBefore(weekStart)))
                    .collect(Collectors.groupingBy(
                            BankRecord::account,
                            Collectors.toList()
                    ));
            weekOperations.forEach((k, v) -> {
                final Remain startRemain = startRemains.computeIfAbsent(k, n -> new Remain(n, weekStart, Money.kopecks(0L)));
                Money finishRemainValue = startRemain.value();
                for (BankRecord<?> op : v) {
                    finishRemainValue = switch (op.type()) {
                        case DEPOSIT -> finishRemainValue.add(op.amount());
                        case WITHDRAW -> finishRemainValue.subtract(op.amount());
                        default -> throw new IllegalArgumentException();
                    };
                }
                final Remain finishRemain = new Remain(k, weekEnd, finishRemainValue);
                AccountMovement movement = new AccountMovement(startRemain, finishRemain, v);
                movements.put(k, movement);
            });

            final BudgetBalance budgetBalance = new BudgetBalance(
                    weekStart, weekEnd, items,
                    new ArrayList<>(currentRemains.values()),
                    new ArrayList<>(movements.values())
            );
            result.add(budgetBalance);
        }

        return result;
    }

    private Map<BudgetRule, Money> calculateRules(WorkCalendar calendar, LocalDate start, LocalDate finish) throws ScriptException {
        Map<BudgetRule, Money> calculation = new LinkedHashMap<>();
        for (LocalDate date = start; !date.isAfter(finish); date = date.plusDays(1)) {
            for (BudgetRule rule : rules) {
                if (rule.matches(date, calendar)) {
                    Money value;
                    if (rule.calculation() == null) {
                        value = rule.value();
                    } else {
                        Map<String, Object> arguments = new HashMap<>();
                        arguments.put("date", date);
                        arguments.put("month", YearMonth.from(date));
                        arguments.put("prevMonth", YearMonth.from(date).minusMonths(1));
                        arguments.put("nextMonth", YearMonth.from(date).plusMonths(1));
                        arguments.put("value", rule.value());
                        arguments.put("currency", rule.value().currency().getCurrencyCode());
                        arguments.put("calendar", calendar);
                        value = rule.calculation().calculate(arguments);
                    }
                    calculation.put(rule, value);
                }
            }
        }
        return calculation;
    }

    @Override
    public boolean sameIdentityAs(Budget that) {
        return that != null && Objects.equals(budgetId, that.budgetId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Budget that = (Budget) o;
        return sameIdentityAs(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(budgetId);
    }

    protected Budget() {
        // for Hibernate
    }

    private Long id; // surrogate key
}
