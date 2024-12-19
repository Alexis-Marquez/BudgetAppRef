package budgetapprefactored.Users;

import budgetapprefactored.Exceptions.DuplicateCategoryException;
import budgetapprefactored.Exceptions.UserNotFoundException;
import budgetapprefactored.Budgets.Budget;
import budgetapprefactored.Budgets.BudgetRepository;
import budgetapprefactored.Budgets.BudgetService;
import budgetapprefactored.Budgets.Category;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;
    private final BudgetService budgetService;
    private final BudgetRepository budgetRepository;

    public UserService(UserRepository userRepository, MongoTemplate mongoTemplate, BudgetService budgetService, BudgetRepository budgetRepository) {
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
        this.budgetService = budgetService;
        this.budgetRepository = budgetRepository;
    }
    private static final String MONTHLY_CRON = "0 0 0 1 * *";
    @Scheduled(cron = MONTHLY_CRON) // Run on the 1st day of each month
    public void scheduleMonthlyAddBudget() throws UserNotFoundException {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            this.createBudget(user.getUserId(), user.getBudgetMonthTotal(), YearMonth.now());
        }
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findUserByEmail(username);
    }

    public Optional<User> getUserByUserId(String userId){
        return userRepository.findUserByUserId(userId);
    }

    public User createUser(String name, String email, String password) {
        return userRepository.insert(new User(name,email,password));
    }
    @Transactional
    protected void updateBudgetMonthTotal(String userId, BigDecimal newTotal) {
        if (newTotal == null) {
            throw new IllegalArgumentException("newTotal cannot be null");
        }

        Query queryFindUser = Query.query(Criteria.where("userId").is(userId));
        Update updateOpBudget = new Update().set("budgetMonthTotal", newTotal);

        try {
            UpdateResult result = mongoTemplate.updateFirst(queryFindUser, updateOpBudget, User.class);
            if (result.getMatchedCount() == 0) {
                throw new IllegalArgumentException("No user found with userId: " + userId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error updating budgetMonthTotal for userId: " + userId, e);
        }
    }


    @Transactional
    public boolean createBudget(String userId, BigDecimal newTotal, YearMonth yearMonth) throws UserNotFoundException {
        Optional<User> userOpt = userRepository.findUserByUserId(userId);
        if (userOpt.isEmpty()) throw new UserNotFoundException("User with ID " + userId + " does not exist.");

        updateBudgetMonthTotal(userId, newTotal);

        Optional<Budget> currBudgetOpt = budgetService.getBudgetByUserIdAndMonthYear(userId, yearMonth);
        Optional<Budget> prevBudgetOpt = budgetService.getBudgetByUserIdAndMonthYear(userId, yearMonth.minusMonths(1));

        Budget newBudget;
        if (currBudgetOpt.isPresent()) {
            BigDecimal currBalance = currBudgetOpt.get().getCurrentBalance();
            newBudget = new Budget(yearMonth.toString(), newTotal, userId, currBalance);
        } else if (prevBudgetOpt.isPresent()) {
            newBudget = new Budget(yearMonth.toString(), newTotal, userId, BigDecimal.ZERO);
            newBudget.getCategories().addAll(prevBudgetOpt.get().getCategories());
        } else {
            newBudget = new Budget(yearMonth.toString(), newTotal, userId);
            newBudget.getCategories().addAll(userOpt.get().getAvailableCategories());
        }

        budgetRepository.insert(newBudget);
        mongoTemplate.update(User.class)
                .matching(Criteria.where("userId").is(userId))
                .apply(new Update().push("budgetList").value(newBudget))
                .first();

        return true;
    }

    public List<Category> getAvailableCategories(String userId) {
        Optional<Budget> budget =  budgetRepository.findBudgetByUserIdAndMonthYear(userId, YearMonth.now().toString());
        if(budget.isPresent()){
            return budget.orElseThrow().getCategories();
        }
        else{
            return new ArrayList<>();
        }
    }

    @Transactional
    public Optional<Category> addCategory(String userId, BigDecimal total, String name) throws DuplicateCategoryException {
        Optional<Budget> budget = budgetRepository.findBudgetByUserIdAndMonthYear(userId, YearMonth.now().toString());
        if (budget.isPresent()) {
            Budget currentBudget = budget.get();
            if (currentBudget.getCategories().stream().anyMatch(c -> c.getName().equals(name))) {
                throw new DuplicateCategoryException("Category "+ name+" already exists.");
            }
            Category value = new Category(name, total, userId);
            mongoTemplate.update(Budget.class)
                    .matching(Criteria.where("userId").is(userId))
                    .apply(new Update().push("categories").value(value))
                    .first();
            mongoTemplate.update(User.class)
                    .matching(Criteria.where("userId").is(userId))
                    .apply(new Update().push("availableCategories").value(value))
                    .first();
            return Optional.of(value);
        }
        return Optional.empty();
    }

    public void deleteUserByUserId(String userId) {
        userRepository.deleteUserByUserId(userId);
    }
}

