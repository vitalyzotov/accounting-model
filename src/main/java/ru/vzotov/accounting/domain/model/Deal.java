package ru.vzotov.accounting.domain.model;

import ru.vzotov.banking.domain.model.OperationId;
import ru.vzotov.cashreceipt.domain.model.CheckQRCode;
import ru.vzotov.ddd.shared.AggregateRoot;
import ru.vzotov.ddd.shared.Entity;

import java.util.HashSet;
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
     * Все чеки сделки
     */
    private final Set<CheckQRCode> checks = new HashSet<>();

    /**
     * Все операции сделки
     */
    private final Set<OperationId> operations = new HashSet<>();

    public DealId dealId() {
        return dealId;
    }

    @Override
    public boolean sameIdentityAs(Deal deal) {
        return false;
    }

    public void moveCheck(CheckQRCode check, Deal target) {
        //todo: implement
    }

    public void moveOperation(OperationId operation, Deal target) {
        //todo: implement
    }

    private Long id; // surrogate key
}
