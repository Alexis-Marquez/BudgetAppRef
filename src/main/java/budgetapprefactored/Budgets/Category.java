package budgetapprefactored.Budgets;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;
@Data
public class Category {
    private String name;
    private String id;
    private String userId;
    private BigDecimal balance;
    private BigDecimal total;
    public Category(String name, BigDecimal total, String userId){
        this.id = String.valueOf(UUID.randomUUID());
        this.userId = userId;
        this.balance=BigDecimal.ZERO;
        this.name=name;
        this.total=total;
    }
}

