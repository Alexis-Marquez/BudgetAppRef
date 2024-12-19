package budgetapprefactored.Transactions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
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
    private String accountId;
    private String userId;
    private LocalDate time;
    private BigDecimal amount;
    private String name;
    private String description;
    private String accountName;
    private String category;
    private String type;
    public Transaction(String accountId, String userId, LocalDate time, BigDecimal amount, String name, String accountName, String description, String category, String type) {
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
