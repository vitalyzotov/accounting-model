package ru.vzotov.accounting.domain.model;

import ru.vzotov.ddd.shared.ValueObject;

public enum BudgetDirection implements ValueObject<BudgetDirection> {
    INCOME('+'), EXPENSE('-'), MOVE('M');

    private final char symbol;

    BudgetDirection(char symbol) {
        this.symbol = symbol;
    }

    public char symbol() {
        return symbol;
    }

    @Override
    public boolean sameValueAs(BudgetDirection other) {
        return this.equals(other);
    }

    public static BudgetDirection of(char symbol) {
        for (BudgetDirection v : BudgetDirection.values()) {
            if (v.symbol == symbol) return v;
        }
        throw new IllegalArgumentException("Unknown direction");
    }

    public static BudgetDirection of(String symbol) {
        return of(symbol, null);
    }

    public static BudgetDirection of(String symbol, BudgetDirection defaultValue) {
        return symbol == null || symbol.isEmpty() ? defaultValue : of(symbol.charAt(0));
    }

}
