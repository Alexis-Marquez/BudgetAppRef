package budgetapprefactored.Transactions;

import budgetapprefactored.Accounts.AccountService;
import budgetapprefactored.Budgets.Budget;
import budgetapprefactored.Budgets.BudgetService;
import budgetapprefactored.Budgets.Category;
import budgetapprefactored.Users.User;
import budgetapprefactored.Users.UserService;
import budgetapprefactored.Accounts.Account;
import com.mongodb.BasicDBObject;
import budgetapprefactored.Transactions.Transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

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

    public TransactionService(TransactionRepository transactionRepository, AccountService accountService, BudgetService budgetService, MongoTemplate mongoTemplate, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.budgetService = budgetService;
        this.mongoTemplate = mongoTemplate;
        this.userService = userService;
    }

    public Optional<Transaction> createTransaction(BigDecimal amount, String accountId, String userId, LocalDate time, String name, String description, String category, String type) throws AccountNotFoundException {
        Optional<Account> curr = accountService.singleAccount(accountId);
        if(curr.isEmpty()){
            return Optional.empty();
        }
        Optional<User> currUser = userService.getUserByUserId(userId);
        if(currUser.isEmpty()){
            return Optional.empty();
        }
        Transaction transaction = transactionRepository.insert(new Transaction(accountId, userId, time, amount,name,curr.get().getName(),description, category, type));
            mongoTemplate.update(User.class)
                    .matching(Criteria.where("userId").is(userId))
                    .apply(new Update().push("transactionList").value(transaction))
                    .first();
            accountUpdater(accountId,amount,type);
            if(type.equals("expense")){
                budgetUpdater(userId,amount, YearMonth.from(time), currUser.get());
                categoryUpdater(userId, amount, category , YearMonth.from(time), currUser.get().getBudgetMonthTotal()); //Needs fixing
            }
            return Optional.of(transaction);
    }

    private void categoryUpdater(String userId, BigDecimal amount, String category, YearMonth monthYear, BigDecimal total) {
        List<Category> categories = budgetService.getBudgetByUserIdAndMonthYear(userId, monthYear).get().getCategories();
        for(Category c : categories ){
            if(c.getName().equals(category)){
                c.setBalance(c.getBalance().add(amount.abs()));
                mongoTemplate.updateFirst(new Query().addCriteria(Criteria.where("userId").is(userId)), new Update().pull("categories",new BasicDBObject("name", category)), Budget.class);
                mongoTemplate.updateFirst(new Query().addCriteria(Criteria.where("userId").is(userId)), new Update().push("categories", c), Budget.class);
                return;
            }
        }
        mongoTemplate.updateFirst(new Query().addCriteria(Criteria.where("userId").is(userId)), new Update().push("categories", new Category(category, total, userId)), Budget.class);
    }

    private void accountUpdater(String accountId, BigDecimal amount, String type){
        Query queryAccountUpdate = new Query(new Criteria("accountId").is(accountId));
        BigDecimal currTotal = accountService.singleAccount(accountId).orElseThrow().getBalance();
        Update updateOpBalanceAccount = new Update().set("balance", currTotal.add(amount));
        mongoTemplate.updateFirst(queryAccountUpdate, updateOpBalanceAccount, Account.class);
    }
    private Optional<Budget> budgetUpdater(String userId, BigDecimal amount, YearMonth monthYear, User currUser){
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
