package budgetapprefactored.Budgets;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Optional;
@Service
public class BudgetService {
    private final BudgetRepository budgetRepository;

    public BudgetService(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public Optional<Budget> singleBudget(ObjectId id) {
        return budgetRepository.findBudgetById(id);
    }

    public Optional<Budget> getBudgetByUserId(String userId) {
        return budgetRepository.findBudgetByUserIdAndMonthYear(userId, YearMonth.now().toString());
    }
    public Optional<Budget> getBudgetByUserIdAndMonthYear(String userId, YearMonth monthYear) {
        return budgetRepository.findBudgetByUserIdAndMonthYear(userId, monthYear.toString());
    }

}
