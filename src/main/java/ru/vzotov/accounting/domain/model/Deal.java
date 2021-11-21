package ru.vzotov.accounting.domain.model;

import org.apache.commons.lang.Validate;
import ru.vzotov.banking.domain.model.BudgetCategory;
import ru.vzotov.banking.domain.model.OperationId;
import ru.vzotov.cashreceipt.domain.model.CheckId;
import ru.vzotov.ddd.shared.AggregateRoot;
import ru.vzotov.ddd.shared.Entity;
import ru.vzotov.domain.model.Money;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * <p>Модель сделки.</p>
 * <p>
 * Сделка - финансово значимое действие, которое сопровождается одной или несколькими финансовыми операциями
 * и может подтверждаться одним или несколькими чеками.
 * </p>
 */
@AggregateRoot
public class Deal implements Entity<Deal> {

    private DealId dealId;

    /**
     * Дата сделки. В простейшем случае определяется минимальной датой финансовой операции, относящейся к сделке.
     */
    private LocalDate date;

    /**
     * Финансовый результат сделки
     */
    private Money amount;

    /**
     * Категория бюджета
     */
    private BudgetCategory category;

    private String description;

    private String comment;

    /**
     * Все чеки сделки
     */
    private Set<CheckId> receipts;

    /**
     * Все операции сделки
     */
    private Set<OperationId> operations;

    public Deal(DealId dealId, LocalDate date, Money amount) {
        this(dealId, date, amount, null, null, null, Collections.emptySet(), Collections.emptySet());
    }

    public Deal(DealId dealId, LocalDate date, Money amount, String description, String comment, BudgetCategory category) {
        this(dealId, date, amount, description, comment, category, Collections.emptySet(), Collections.emptySet());
    }

    public Deal(DealId dealId, LocalDate date, Money amount,
                String description, String comment, BudgetCategory category,
                Set<CheckId> receipts, Set<OperationId> operations) {
        Validate.notNull(dealId);
        Validate.notNull(date);
        Validate.notNull(amount);
        Validate.notNull(receipts);
        Validate.notNull(operations);

        this.dealId = dealId;
        this.date = date;
        this.amount = amount;
        this.description = description;
        this.comment = comment;
        this.category = category;

        this.receipts = new HashSet<>(receipts);
        this.operations = new HashSet<>(operations);
    }

    public LocalDate date() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Money amount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public DealId dealId() {
        return dealId;
    }

    public BudgetCategory category() {
        return category;
    }

    public String description() {
        return description;
    }

    public String comment() {
        return comment;
    }

    public Set<OperationId> operations() {
        return Collections.unmodifiableSet(operations);
    }

    public Set<CheckId> receipts() {
        return Collections.unmodifiableSet(receipts);
    }

    public void moveReceipt(CheckId receipt, Deal target) {
        this.receipts.remove(receipt);
        target.receipts.add(receipt);
    }

    public void moveOperation(OperationId operation, Deal target) {
        this.operations.remove(operation);
        target.operations.add(operation);
    }

    public void setOperations(Set<OperationId> operations) {
        this.operations.retainAll(operations);
        this.operations.addAll(operations);
    }

    public void setReceipts(Set<CheckId> receipts) {
        this.receipts.retainAll(receipts);
        this.receipts.addAll(receipts);
    }

    public void assignCategory(BudgetCategory category) {
        this.category = category;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean sameIdentityAs(Deal that) {
        return that != null && Objects.equals(dealId, that.dealId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Deal that = (Deal) o;
        return sameIdentityAs(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dealId);
    }

    protected Deal() {
        // for Hibernate
    }

    private Long id; // surrogate key
}
