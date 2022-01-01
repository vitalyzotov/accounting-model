package ru.vzotov.accounting.domain.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import ru.vzotov.banking.domain.model.BudgetCategoryId;
import ru.vzotov.banking.domain.model.OperationId;
import ru.vzotov.cashreceipt.domain.model.CheckId;
import ru.vzotov.domain.model.Money;
import ru.vzotov.purchase.domain.model.PurchaseId;

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@RunWith(JUnit4.class)
public class DealTest {

    @Test
    public void testConstruct() {
        Deal deal = new Deal(
                new DealId("test deal"),
                LocalDate.of(2021, 12, 14),
                Money.kopecks(100),
                "Deal description",
                "Deal comment",
                BudgetCategoryId.of("my category"),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptySet(),
                Collections.emptyList()
        );

        assertThat(deal.dealId()).isEqualTo(new DealId("test deal"));
        assertThat(deal.date()).isEqualTo(LocalDate.of(2021, 12, 14));
        assertThat(deal.amount()).isEqualTo(Money.rubles(1));

        assertThatThrownBy(() -> new Deal(
                null,
                LocalDate.now(), Money.kopecks(100),
                "Deal description", "Deal comment", BudgetCategoryId.of("my category"),
                Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), Collections.emptyList()
        )).isInstanceOf(Throwable.class);
    }

    @Test
    public void testJoin() {
        BudgetCategoryId category1 = BudgetCategoryId.of("cat1");
        BudgetCategoryId category2 = BudgetCategoryId.of("cat2");

        CheckId receipt1 = new CheckId("receipt1");
        CheckId receipt2 = new CheckId("receipt2");

        OperationId operation1 = new OperationId("op1");
        OperationId operation2 = new OperationId("op2");

        PurchaseId purchase1 = new PurchaseId("purchase1");
        PurchaseId purchase2 = new PurchaseId("purchase2");

        Deal target = new Deal(
                new DealId("target-deal"),
                LocalDate.of(2021, 12, 10),
                Money.kopecks(100),
                "Target deal description",
                "Target deal comment",
                category1,
                Collections.singleton(receipt1),
                Collections.singleton(operation1),
                Collections.emptySet(),
                Collections.singletonList(purchase1)
        );

        Deal source = new Deal(
                DealId.nextId(),
                LocalDate.of(2021, 12, 14),
                Money.kopecks(300),
                "Source deal description",
                "Source deal comment",
                category2,
                Collections.singleton(receipt2),
                Collections.singleton(operation2),
                Collections.emptySet(),
                Collections.singletonList(purchase2)
        );

        assertThat(target).isNotEqualTo(source);

        target.join(source);
        assertThat(source.receipts()).isEmpty();
        assertThat(source.operations()).isEmpty();
        assertThat(source.purchases()).isEmpty();

        assertThat(target.receipts()).containsExactlyInAnyOrder(receipt1, receipt2);
        assertThat(target.operations()).containsExactlyInAnyOrder(operation1, operation2);
        assertThat(target.purchases()).containsExactlyInAnyOrder(purchase1, purchase2);

        assertThat(target.amount()).isEqualTo(Money.rubles(1));
        assertThat(target.date()).isEqualTo(LocalDate.of(2021, 12, 10));
        assertThat(target.category()).isEqualTo(category1);
        assertThat(target.description()).isEqualTo("Target deal description");
        assertThat(target.comment()).isEqualTo("Target deal comment");
    }
}
