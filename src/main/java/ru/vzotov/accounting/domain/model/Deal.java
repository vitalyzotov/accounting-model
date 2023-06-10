package ru.vzotov.accounting.domain.model;

import org.apache.commons.lang3.Validate;
import ru.vzotov.banking.domain.model.BudgetCategoryId;
import ru.vzotov.banking.domain.model.OperationId;
import ru.vzotov.cashreceipt.domain.model.ReceiptId;
import ru.vzotov.ddd.shared.AggregateRoot;
import ru.vzotov.ddd.shared.Entity;
import ru.vzotov.domain.model.Money;
import ru.vzotov.person.domain.model.Owned;
import ru.vzotov.person.domain.model.PersonId;
import ru.vzotov.purchase.domain.model.PurchaseId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

/**
 * <p>Deal model.</p>
 * <p>
 *     A deal is a financially significant action that is accompanied by one or more financial operations
 *     and can be confirmed by one or more cash receipts.
 * </p>
 */
@AggregateRoot
public class Deal implements Entity<Deal>, Owned {

    private DealId dealId;

    /**
     * Date of the deal. Usually this is the earliest date of operations that belong to the deal.
     */
    private LocalDate date;

    /**
     * Financial result of the deal
     */
    private Money amount;

    /**
     * Budget category
     */
    private BudgetCategoryId category;

    /**
     * Description of the deal
     */
    private String description;

    /**
     * Comment to the deal
     */
    private String comment;

    /**
     * Cash receipts
     */
    private Set<ReceiptId> receipts;

    /**
     * Operations that belong to the deal
     */
    private Set<OperationId> operations;

    /**
     * Card operations
     */
    private Set<OperationId> cardOperations;

    /**
     * Purchases that belong to the deal
     */
    private List<PurchaseId> purchases;

    /**
     * Owner of the deal
     */
    private PersonId owner;

    public Deal(DealId dealId, PersonId owner, LocalDate date, Money amount) {
        this(dealId, owner, date, amount, null, null, null, emptySet(), emptySet(), emptySet(), emptyList());
    }

    public Deal(DealId dealId, PersonId owner, LocalDate date, Money amount, String description, String comment, BudgetCategoryId category) {
        this(dealId, owner, date, amount, description, comment, category, emptySet(), emptySet(), emptySet(), emptyList());
    }

    public Deal(DealId dealId, PersonId owner, LocalDate date, Money amount,
                String description, String comment, BudgetCategoryId category,
                Set<ReceiptId> receipts, Set<OperationId> operations, Set<OperationId> cardOperations, List<PurchaseId> purchases) {
        Validate.notNull(dealId);
        Validate.notNull(owner);
        Validate.notNull(date);
        Validate.notNull(amount);
        Validate.notNull(receipts);
        Validate.notNull(operations);
        Validate.notNull(cardOperations);
        Validate.notNull(purchases);

        this.dealId = dealId;
        this.owner = owner;
        this.date = date;
        this.amount = amount;
        this.description = description;
        this.comment = comment;
        this.category = category;

        this.receipts = new HashSet<>(receipts);
        this.operations = new HashSet<>(operations);
        this.cardOperations = new HashSet<>(cardOperations);
        this.purchases = new ArrayList<>(purchases);
    }

    public PersonId owner() {
        return owner;
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

    public BudgetCategoryId category() {
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

    public Set<OperationId> cardOperations() {
        return Collections.unmodifiableSet(cardOperations);
    }

    public List<PurchaseId> purchases() {
        return Collections.unmodifiableList(purchases);
    }

    public Set<ReceiptId> receipts() {
        return Collections.unmodifiableSet(receipts);
    }

    public void moveReceipt(ReceiptId receipt, Deal target) {
        this.receipts.remove(receipt);
        target.receipts.add(receipt);
    }

    public void moveOperation(OperationId operation, Deal target) {
        this.operations.remove(operation);
        target.operations.add(operation);
    }

    public void movePurchase(PurchaseId purchase, Deal target) {
        this.purchases.remove(purchase);
        target.purchases.add(purchase);
    }

    public void setOperations(Set<OperationId> operations) {
        this.operations.retainAll(operations);
        this.operations.addAll(operations);
    }

    public void addOperation(OperationId operation) {
        this.operations.add(operation);
    }

    public void setReceipts(Set<ReceiptId> receipts) {
        this.receipts.retainAll(receipts);
        this.receipts.addAll(receipts);
    }

    public void addReceipt(ReceiptId receipt) {
        this.receipts.add(receipt);
    }

    public void setPurchases(List<PurchaseId> purchases) {
        this.purchases.clear();
        this.purchases.addAll(purchases);
    }

    public void addPurchase(PurchaseId purchase) {
        this.purchases.add(purchase);
    }

    public void removePurchase(PurchaseId purchase) {
        this.purchases.remove(purchase);
    }

    public void assignCategory(BudgetCategoryId category) {
        this.category = category;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void join(Deal other) {
        Validate.notNull(other);

        // This condition makes it impossible creation of transactions between family accounts.
        // Validate.isTrue(owner.equals(other.owner));

        this.receipts.addAll(other.receipts);
        other.receipts.clear();

        this.operations.addAll(other.operations);
        other.operations.clear();

        this.purchases.addAll(other.purchases);
        other.purchases.clear();
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

    @Override
    public String toString() {
        return "Deal{" +
                "id=" + id +
                ", dealId=" + dealId +
                ", date=" + date +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                '}';
    }

    protected Deal() {
        // for Hibernate
    }

    private Long id; // surrogate key
}
