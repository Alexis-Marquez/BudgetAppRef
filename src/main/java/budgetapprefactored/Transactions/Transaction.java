package budgetapprefactored.Transactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document(collection = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @Id
    private ObjectId id;
    @Indexed
    private String userId;
    @Indexed
    private String accountId;

    private LocalDate time;
    @NotNull
    private BigDecimal amount;
    private String name;
    private String description;
    private String accountName;
    private String category;
    public enum TransactionType {
        INCOME, EXPENSE
    }
    private TransactionType type;

    public Transaction(String accountId, String userId, LocalDate time, @NotNull BigDecimal amount, String name, String accountName, String description, String category, TransactionType type) {
        this.accountId = accountId;
        this.userId = userId;
        this.time = time;
        this.amount = amount;
        this.name = name;
        this.accountName = accountName;
        this.description = description;
        this.category = category;
        this.type = type;
    }
}
