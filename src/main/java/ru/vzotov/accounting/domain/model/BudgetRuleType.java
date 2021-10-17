package ru.vzotov.accounting.domain.model;

import ru.vzotov.ddd.shared.ValueObject;

public enum BudgetRuleType implements ValueObject<BudgetRuleType> {
    INCOME('+'), EXPENSE('-'), MOVE('M');

    private char symbol;

    BudgetRuleType(char symbol) {
        this.symbol = symbol;
    }

    public char symbol() {
        return symbol;
    }

    @Override
    public boolean sameValueAs(BudgetRuleType other) {
        return this.equals(other);
    }

    public static BudgetRuleType of(char symbol) {
        for (BudgetRuleType v : BudgetRuleType.values()) {
            if (v.symbol == symbol) return v;
        }
        throw new IllegalArgumentException("Unknown rule type");
    }

}
