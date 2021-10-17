package ru.vzotov.accounting.domain.model;

import org.apache.commons.lang.Validate;
import ru.vzotov.banking.domain.model.AccountNumber;
import ru.vzotov.banking.domain.model.BankRecord;
import ru.vzotov.calendar.domain.model.WorkCalendar;
import ru.vzotov.ddd.shared.AggregateRoot;
import ru.vzotov.ddd.shared.Entity;
import ru.vzotov.domain.model.Money;

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
 * Бюджет
 */
@AggregateRoot
public class Budget implements Entity<Budget> {

    private static final String LOCALE_DEFAULT = Locale.US.getLanguage();

    private static final Currency CURRENCY_DEFAULT = Currency.getInstance("RUR");

    /**
     * Уникальный идентификатор
     */
    private BudgetId budgetId;

    /**
     * Название
     */
    private String name;

    /**
     * Правила формирования бюджета
     */
    private Set<BudgetRule> rules = new HashSet<>();

    /**
     * Локаль для определения первого дня недели
     */
    private String locale;

    private Currency currency;

    public Budget(BudgetId budgetId, String name, Set<BudgetRule> rules) {
        this(budgetId, name, rules, CURRENCY_DEFAULT, LOCALE_DEFAULT);
    }

    public Budget(BudgetId budgetId, String name, Set<BudgetRule> rules, Currency currency, String locale) {
        Validate.notNull(budgetId);
        Validate.notEmpty(name);
        Validate.notNull(rules);
        Validate.notNull(currency);
        Validate.notNull(locale);

        this.budgetId = budgetId;
        this.name = name;
        this.rules = new HashSet<>(rules);
        this.currency = currency;
        this.locale = locale;
    }

    public BudgetId budgetId() {
        return budgetId;
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
     * Производит расчет в заданном периоде времени согласно бюджетным правилам.
     *
     * @param calendar       производственный календарь, охватывающий расчетный период
     * @param actualRemains  известные остатки по счетам. Если остаток по счету неизвестен, то принимаем его равным нулю.
     * @param defaultAccount номер счета, используемый по умолчанию когда правило не уточняет с какого счета поизводить списание или начисление
     * @param start          первый день расчетного периода
     * @param finish         последний день расчетного периода
     * @return список итогов расчета с разбивкой по неделям
     * @throws ScriptException при ошибках расчета
     */
    public List<BudgetBalance> calculate(
            WorkCalendar calendar,
            List<Remain> actualRemains,
            AccountNumber defaultAccount,
            LocalDate start,
            LocalDate finish,
            List<BankRecord<?>> operations) throws ScriptException {
        Validate.isTrue(!calendar.from().isAfter(start) && !calendar.to().isBefore(finish), "Календарь должен охватывать расчетный период");

        final WeekFields weekFields = WeekFields.of(new Locale(this.locale));
        final List<BudgetBalance> result = new ArrayList<>();
        final List<Remain> knownRemains = actualRemains.stream()
                .filter(remain -> !remain.date().isAfter(finish))
                .sorted(Comparator.comparing(Remain::date).reversed())
                .collect(Collectors.toList());

        final List<BankRecord<?>> knownOperations = operations.stream()
                .filter(op -> !(op.recorded().isAfter(finish) || op.recorded().isBefore(start)))
                .collect(Collectors.toList());

        final Map<AccountNumber, Remain> currentRemains = new HashMap<>();

        for (LocalDate date = start; !date.isAfter(finish); date = date.plusWeeks(1)) {

            // вычисляем границы текущего периода
            final LocalDate weekStart = date.isEqual(start) ? start : date.with(weekFields.dayOfWeek(), 1L);
            final LocalDate weekLastDay = date.with(weekFields.dayOfWeek(), 7L);
            final LocalDate weekEnd = weekLastDay.isAfter(finish) ? finish : weekLastDay;

            // получаем остатки по счетам на текущий период
            knownRemains
                    .stream()
                    .filter(remain -> !remain.date().isAfter(weekStart)) // берем остатки на дату, не позднее начала недели
                    .forEach(remain -> currentRemains.merge(remain.account(), remain, (a, b) -> a.date().isAfter(b.date()) ? a : b)); // в currentRemains остается остаток с самой поздней датой

            // в currentRemains находятся остатки по счетам на начало периода
            // сохраняем их в startRemains
            final Map<AccountNumber, Remain> startRemains = new HashMap<>(currentRemains);

            // считаем правила
            final Map<BudgetRule, Money> calculation = calculateRules(calendar, weekStart, weekEnd);

            final LocalDate itemDate = date;
            // формируем итоги
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
                                rule.categoryId()
                        );
                    })
                    .peek(item -> {
                        // вычисляем остатки на конец периода
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
                            case INCOME:
                                targetRemain = new Remain(target, weekEnd, targetRemain.value().add(item.value()));
                                currentRemains.put(target, targetRemain);
                                break;
                            case EXPENSE:
                                sourceRemain = new Remain(source, weekEnd, sourceRemain.value().subtract(item.value()));
                                currentRemains.put(source, sourceRemain);
                                break;
                            case MOVE:
                                sourceRemain = new Remain(source, weekEnd, sourceRemain.value().subtract(item.value()));
                                currentRemains.put(source, sourceRemain);

                                targetRemain = new Remain(target, weekEnd, targetRemain.value().add(item.value()));
                                currentRemains.put(target, targetRemain);
                                break;
                            default:
                                throw new IllegalArgumentException();
                        }
                    })
                    .collect(Collectors.toList());

            // считаем движение средств по счетам
            final Map<AccountNumber, AccountMovement> movements = new HashMap<>();
            final Map<AccountNumber, List<BankRecord<?>>> weekOperations = knownOperations.stream()
                    .filter(op -> !(op.recorded().isAfter(weekEnd) || op.recorded().isBefore(weekStart)))
                    .collect(Collectors.groupingBy(
                            op -> op.account().accountNumber(),
                            Collectors.toList()
                    ));
            weekOperations.forEach((k, v) -> {
                final Remain startRemain = startRemains.computeIfAbsent(k, n -> new Remain(n, weekStart, Money.kopecks(0L)));
                Money finishRemainValue = startRemain.value();
                for (BankRecord<?> op : v) {
                    switch (op.type()) {
                        case DEPOSIT:
                            finishRemainValue = finishRemainValue.add(op.amount());
                            break;
                        case WITHDRAW:
                            finishRemainValue = finishRemainValue.subtract(op.amount());
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }
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
