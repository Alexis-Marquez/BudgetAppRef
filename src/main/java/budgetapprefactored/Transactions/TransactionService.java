package budgetapprefactored.Transactions;

import budgetapprefactored.Accounts.*;
import budgetapprefactored.Budgets.Budget;
import budgetapprefactored.Budgets.BudgetService;
import budgetapprefactored.Budgets.Category;
import budgetapprefactored.Exceptions.CategoryNotFoundException;
import budgetapprefactored.Exceptions.EmptyBudgetException;
import budgetapprefactored.Exceptions.UserNotFoundException;
import budgetapprefactored.Users.User;
import budgetapprefactored.Users.UserService;
import budgetapprefactored.utils.EncryptionUtil;
import com.mongodb.BasicDBObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.security.auth.login.AccountNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {
    private final int numberOfItemsPerPage = 5;

    private final TransactionRepository transactionRepository;

    private final AccountService accountService;

    private final BudgetService budgetService;

    private final MongoTemplate mongoTemplate;//for more complex operations

    private final UserService userService;

    private final EncryptionUtil encryptionUtil;

    public TransactionService(TransactionRepository transactionRepository, AccountService accountService, BudgetService budgetService, MongoTemplate mongoTemplate, UserService userService, EncryptionUtil encryptionUtil) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.budgetService = budgetService;
        this.mongoTemplate = mongoTemplate;
        this.userService = userService;
        this.encryptionUtil = encryptionUtil;
    }

    public Optional<Transaction> createTransaction(BigDecimal amount, String accountId, String userId, LocalDate time, String name, String description, String category, Transaction.TransactionType type) throws Exception {
        Optional<Account> curr = accountService.singleAccount(accountId);
        if(curr.isEmpty()){
            throw new AccountNotFoundException();
        }

        Optional<User> currUser = userService.getUserByUserId(userId);
        if(currUser.isEmpty()){
            throw new UserNotFoundException("User with ID " + userId + " does not exist.");
        }

        Transaction transaction = transactionRepository.insert(new Transaction(accountId, userId, time, amount, name, curr.get().getName(), description, category, type));

        mongoTemplate.update(User.class)
                .matching(Criteria.where("userId").is(userId))
                .apply(new Update().push("transactionList").value(transaction))
                .first();

        accountUpdater(accountId, amount, type);

        if (type == Transaction.TransactionType.EXPENSE) {
            budgetUpdater(userId, amount, YearMonth.from(time), currUser.get());
            categoryUpdater(userId, amount, category, YearMonth.from(time), currUser.get().getBudgetMonthTotal());
        }

        return Optional.of(transaction);
    }


    private void categoryUpdater(String userId, BigDecimal amount, String category, YearMonth monthYear, BigDecimal total) throws EmptyBudgetException, CategoryNotFoundException {
        Optional<Budget> budgetOptional = budgetService.getBudgetByUserIdAndMonthYear(userId, monthYear);

        if (budgetOptional.isEmpty()) {
            throw new EmptyBudgetException("User has no budgets");
        }
        List<Category> categories = budgetOptional.get().getCategories();
        Category categoryToUpdate = null;
        for (Category c : categories) {
            if (c.getName().equals(category)) {
                categoryToUpdate = c;
                break;
            }
        }
        if (categoryToUpdate != null) {
            categoryToUpdate.setBalance(categoryToUpdate.getBalance().add(amount.abs()));
            mongoTemplate.updateFirst(
                    new Query().addCriteria(Criteria.where("userId").is(userId)),
                    new Update().pull("categories", new BasicDBObject("name", category)),
                    Budget.class
            );

            mongoTemplate.updateFirst(
                    new Query().addCriteria(Criteria.where("userId").is(userId)),
                    new Update().push("categories", categoryToUpdate),
                    Budget.class
            );
        } else {
            throw new CategoryNotFoundException("Category "+ category+" not found");
        }
    }


    private void accountUpdater(String accountId, BigDecimal amount, Transaction.TransactionType type) throws Exception {
        Query queryAccountUpdate = new Query(new Criteria("accountId").is(accountId));
        BigDecimal currTotal = accountService.singleAccount(accountId).orElseThrow().getBalance();
        Update updateOpBalanceAccount = new Update().set("balance", currTotal.add(amount));
        mongoTemplate.updateFirst(queryAccountUpdate, updateOpBalanceAccount, Account.class);
    }
    private Optional<Budget> budgetUpdater(String userId, BigDecimal amount, YearMonth monthYear, User currUser) throws UserNotFoundException {
        Optional<Budget> currBudget = budgetService.getBudgetByUserIdAndMonthYear(currUser.getUserId(), monthYear);
       if(currBudget.isEmpty()){
           userService.createBudget(userId, currUser.getBudgetMonthTotal(), monthYear);
       }
       else {
           Query queryBudgetUpdate = new Query(new Criteria("id").is(currBudget.get().getId()));
           BigDecimal currTotalBudget = budgetService.singleBudget(currBudget.get().getId()).get().getCurrentBalance();
           Update updateOpBalanceBudget = new Update().set("currentBalance", currTotalBudget.add(amount.abs()));
           mongoTemplate.updateFirst(queryBudgetUpdate, updateOpBalanceBudget, Budget.class);
       }
        return (currBudget);
    }
    public List<Transaction> getNext5RecentTransactions(String userId, int page) {
        Pageable pageable = PageRequest.of(page-1, 5); // Skip 10, take 5
        return transactionRepository.findNext5ByUserIdOrderByTimeDesc(userId, pageable);
    }

    public Integer getTransactionSize(String userId) {

        return transactionRepository.countTransactionsByUserId(userId);
    }
}
