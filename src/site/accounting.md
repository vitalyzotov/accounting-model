```mermaid
classDiagram
    direction LR
    class Balance {
        - remain: Remain
        - value: Money
        - operations: List<Operation>
    }
    class AccountMovement {
        - start: Remain
        - finish: Remain
        - operations: List<BankRecord>
    }
    class Budget {
        - budgetId: BudgetId
        - name: String
        - rules: Set<BudgetRule>
        - locale: String
        - currency: Currency
    }
    <<AggregateRoot>> Budget
    class BudgetId
    class BudgetRule {
        + ruleId: BudgetRuleId
        + type: BudgetRuleType
        + name: String
        + sourceAccount: AccountNumber
        + targetAccount: AccountNumber
        + categoryId: BudgetCategoryId
        + recurrence: Recurrence
        + value: Money
        + calculation: Calculation
        + enabled: boolean
    }
    class BudgetBalance {
        - from: LocalDate
        - to: LocalDate
        - items: List<BudgetPlan>
        - remains: List<Remain>
        - movements: List<AccountMovement>
    }
    class BudgetDirection
    <<enumeration>> BudgetDirection
    class BudgetPlan {
        - itemId: BudgetPlanId
        - direction: BudgetDirection
        - source: AccountNumber
        - target: AccountNumber
        - category: BudgetCategoryId
        - value: Money
        - rule: BudgetRule
        - date: LocalDate
    }
    <<AggregateRoot>> BudgetPlan
    class BudgetPlanId
    class BudgetRuleId
    class BudgetRuleType
    <<enumeration>> BudgetRuleType
    class Calculation {
        - expression: String
        +calculate(Map<String, Object> arguments) Money
    }
    class Remain {
        + remainId: RemainId
        + date: LocalDate
        + account: AccountNumber
        + value: Money
    }
    <<AggregateRoot>> Remain
    class RemainId
    
    BudgetRule -- Calculation
    Budget -- BudgetId
    Budget *-- BudgetRule
    BudgetPlan *-- BudgetDirection
    BudgetPlan *-- BudgetPlanId
    BudgetRule -- BudgetPlan
    BudgetRuleId --* BudgetRule
    BudgetRuleType -- BudgetRule
    BudgetBalance *-- BudgetPlan
    BudgetBalance *-- Remain
    BudgetBalance *-- AccountMovement
    Remain *-- RemainId
    Remain -- Balance
```
