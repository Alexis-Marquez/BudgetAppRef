package budgetapprefactored.Budgets;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetRepository extends MongoRepository<Budget, ObjectId> {
    Optional<Budget> findBudgetById(ObjectId id);

    Optional<Budget> findBudgetByUserIdAndMonthYear(String userId, String yearMonth);
}
