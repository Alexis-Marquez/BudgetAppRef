package budgetapprefactored.Accounts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.UUID;

@Document(collection = "accounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    private ObjectId id;
    private String accountId;
    private String userId;
    private String type;
    private BigDecimal balance;
    private String name;

    public Account(String userId, String type, String name, BigDecimal balance) {
        this.accountId = UUID.randomUUID().toString();
        this.userId = userId;
        this.type = type;
        this.balance = balance;
        this.name = name;
    }
}
