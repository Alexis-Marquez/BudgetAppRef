package budgetapprefactored.Budgets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/{userId}")
public class BudgetController {
    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping("/latestBudget")
    public ResponseEntity<Optional<Budget>> getLatestBudget(@PathVariable String userId){

        Optional<Budget> currentBudget = budgetService.getBudgetByUserId(userId);
        return currentBudget.map(budget -> new ResponseEntity<>(currentBudget, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(currentBudget, HttpStatus.NO_CONTENT));
    }
}
