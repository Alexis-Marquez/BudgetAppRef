package budgetapprefactored.Accounts;

import budgetapprefactored.utils.EncryptionUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.util.UUID;

@Document(collection = "accounts")
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    private ObjectId id;
    private String accountId;
    private String userId;
    private String type;
    private String  balance;
    private String name;

    public Account(String userId, String type, String name, BigDecimal balance)throws Exception {
        this.accountId = UUID.randomUUID().toString();
        this.userId = userId;
        this.type = type;
        try {
            this.balance = EncryptionUtil.encrypt(String.valueOf(balance));
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting balance: " + e.getMessage(), e);
        }
        this.name = name;
    }

    public BigDecimal getBalance() throws Exception {
        try {
            return new BigDecimal(EncryptionUtil.decrypt(balance));
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting balance: " + e.getMessage(), e);
        }
    }
    public String getName() {
        return this.name;
    }
}
