package budgetapprefactored.Users;

import budgetapprefactored.Budgets.Budget;
import budgetapprefactored.Budgets.Category;
import budgetapprefactored.Transactions.Transaction;
import lombok.*;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import budgetapprefactored.Accounts.Account;
import java.math.BigDecimal;
import java.util.*;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String userId;
    @Id
    private ObjectId id;
    @NotNull
    private String name;

    @NotNull
    private String email;

    @NotNull
    private String password;

    private List<Account> accountList;
    private List<Transaction> transactionList;
    private List<Budget> budgetList;
    private BigDecimal budgetMonthTotal;
    @Getter
    private List<Category> availableCategories = new ArrayList<>();

    public User(@NotNull String name, @NotNull String email, @NotNull String password) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.userId = UUID.randomUUID().toString();
        availableCategories.add(new Category("expenses", budgetMonthTotal, this.userId));
        availableCategories.add(new Category("income", budgetMonthTotal, this.userId));
    }

}
